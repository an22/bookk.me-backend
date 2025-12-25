#!/usr/bin/env sh

subnet_ip=$1
manager_ip=$2
manager_user=$3
wireguard_admin_password=$4
ssh_name=${5:-id_rsa}

echo "Subnet IP: $subnet_ip"
echo "Manager IP: $manager_ip"
echo "Manager User: $manager_user"
echo "Using ssh key: $ssh_name"
echo "Wireguard password: $wireguard_admin_password"

docker network create -d overlay operational
docker network create -d overlay agent_network

ssh-add --apple-use-keychain "$HOME/.ssh/$ssh_name"

docker context create swarm-manager --docker "host=ssh://$manager_user@$manager_ip"
docker context use swarm-manager

OS_NAME=$(uname -s)

if [ "$OS_NAME" = "Darwin" ]; then
    brew install wireguard
else
    sudo apt install wireguard
fi

password_hash="$(docker run ghcr.io/wg-easy/wg-easy:nightly wgpw "$wireguard_admin_password")"

export VPN_PASSWORD_HASH="$password_hash"

docker stack deploy vpn --compose-file vpn/vpn-compose.yml
docker stack deploy nginx --compose-file nginx/nginx-compose.yml
docker secret create dozzle-auth orchestrator/dozzle/users.actual.yaml
docker stack deploy orchestrator --compose-file orchestrator/orchestrator-compose.yml
docker stack deploy runner --compose-file runner/runner-compose.yml