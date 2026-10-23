#!/usr/bin/env bash
set -euo pipefail

if [ $# -lt 2 ]; then
  echo "usage: $0 <app-id> <private-key.pem> [owner] [repo...]" >&2
  exit 1
fi

app_id=$1
private_key=$2
owner=${3:-an22}
shift $(( $# < 3 ? $# : 3 ))
repos=("$@")
[ ${#repos[@]} -eq 0 ] && repos=(bookk.me-backend bookk.me-mobile)

base64url() { openssl base64 -A | tr '+/' '-_' | tr -d '='; }
api() { curl -sS -H "Accept: application/vnd.github+json" "$@"; }

now=$(date +%s)
header=$(printf '{"alg":"RS256","typ":"JWT"}' | base64url)
payload=$(printf '{"iat":%d,"exp":%d,"iss":"%s"}' $((now - 60)) $((now + 540)) "$app_id" | base64url)
signature=$(printf '%s.%s' "$header" "$payload" | openssl dgst -sha256 -sign "$private_key" -binary | base64url)
jwt="$header.$payload.$signature"

app_name=$(api -H "Authorization: Bearer $jwt" https://api.github.com/app | jq -r '.name // empty')
[ -n "$app_name" ] || { echo "FAIL app id or private key rejected"; exit 1; }
echo "ok   app '$app_name' accepts the private key"

installation_id=$(api -H "Authorization: Bearer $jwt" "https://api.github.com/users/$owner/installation" | jq -r '.id // empty')
[ -n "$installation_id" ] || { echo "FAIL app is not installed on '$owner'"; exit 1; }
echo "ok   installed on '$owner'"

installation_token=$(api -X POST -H "Authorization: Bearer $jwt" "https://api.github.com/app/installations/$installation_id/access_tokens" | jq -r '.token // empty')
[ -n "$installation_token" ] || { echo "FAIL could not create an installation token"; exit 1; }

status=0
for repo in "${repos[@]}"; do
  expires_at=$(api -X POST -H "Authorization: Bearer $installation_token" \
    "https://api.github.com/repos/$owner/$repo/actions/runners/registration-token" | jq -r '.expires_at // empty')
  if [ -n "$expires_at" ]; then
    echo "ok   can register runners for $owner/$repo"
  else
    echo "FAIL cannot register runners for $owner/$repo (not selected at install, or Administration permission missing)"
    status=1
  fi
done
exit $status
