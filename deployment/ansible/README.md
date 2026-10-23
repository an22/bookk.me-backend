# Ansible deployment

Runs from your machine (or CI) over SSH against the prod swarm. Nothing is installed on the servers besides Docker and the Python libraries the Docker modules need.

Prod runs the **monolith** (`MonolithServer`, events in-process, no Kafka) as one stack: the app, one MariaDB holding all five schemas, and one Redis. The app answers on the network aliases `authorization.service`, `user.service`, `business.service`, `appointments.service` and `notifications.service`, so nginx routes `/api/<svc>` to it unchanged.

The compose files in `deployment/prod` and `deployment/adminpanel` stay the source of truth: a stack deploy copies the stack's directory to `/opt/bookk/stacks/<stack>/` on the manager, writes its secret files from the vault next to it, and runs `docker stack deploy` there with the stack's variables in the environment. Kafka (`deployment/prod/kafka`) and the per-service stacks (`deployment/prod/microservice`) are not deployed by Ansible.

## One-time setup

```sh
brew install ansible ansible-lint
security add-generic-password -a "$USER" -s bookk-ansible-vault -w "$(openssl rand -base64 32)"
security find-generic-password -s bookk-ansible-vault -w     # save this in your password manager
cd deployment/ansible
make vault-import    # builds the encrypted group_vars/all/vault.yml and generates every key and password
make vault-edit      # fill in what the import reported as CHANGE_ME
```

`make vault-import` reads `deployment/prod/keys/global/.env`, the dozzle users and alertmanager config, and generates the MariaDB encryption key, the signing-key encryption key and the database/Redis passwords. `make vault-update` brings an existing vault to the current structure: it generates whatever is missing and never replaces a value that is set.

The vault password is read by `scripts/vault-pass.sh`: `$ANSIBLE_VAULT_PASSWORD` first (CI), then the `bookk-ansible-vault` keychain item, then `~/.bookk-vault-pass`. `vault.example.yml` shows the expected structure. Add every worker to `inventory/prod.yml` and its address to `vault_hosts`; the monolith, its database and Redis are placed on workers. A stack that keeps data on a local volume is pinned to one node by a swarm label, which `make infra tags=swarm` applies from the host's `swarm_labels` (values must be quoted strings):

```yaml
    workers:
      hosts:
        worker-1:
          ansible_host: "{{ vault_hosts['worker-1'] }}"
          swarm_labels:
            database: "true"
            kafka: "true"
```

The monolith's MariaDB and Valkey run only on the node labelled `database: "true"`, Kafka (when deployed) only on the one labelled `kafka: "true"`; the app itself runs on any worker. Without the label the task stays pending instead of starting on an empty volume elsewhere, so label exactly one node for each before the first deploy (`make bootstrap` applies the labels before `make deploy`).

Edit the vault with an editor that keeps no history of the decrypted temp file: `EDITOR=nano make vault-edit`, or VS Code with `"workbench.localHistory.exclude": { "**/.ansible/tmp/**": true }`.

**Back up the vault password and `vault_swarm_secrets.mariadb_encryption_key`.** Every table is encrypted with that key; without it the database files and their backups are unreadable.

## First deployment

Publish the image first: bump `version` in the root `build.gradle.kts`, then push a matching tag (`git tag monolith/0.15.2 && git push origin monolith/0.15.2`). `.github/workflows/monolith-publish.yml` pushes `ghcr.io/an22/com.bookk.server:<version>` and refuses a tag that differs from the project version.

```sh
make check                     # what would change, connects but changes nothing
make bootstrap                 # hosts, swarm, networks, secrets, infra stacks (creates stacks that don't exist yet)
make deploy v=0.15.2 first=1   # the monolith, creating its stack
```

## Commands

| What | Command |
|---|---|
| Preview everything (no changes) | `make check` |
| Preview one stack | `make check tags=nginx` |
| Hosts, swarm, networks, secrets and all infra stacks | `make infra` |
| Only some stacks | `make infra tags=nginx,monitoring` |
| Deploy / roll back the monolith | `make deploy v=0.15.2` |
| Edit secrets | `make vault-edit` |
| Syntax check / lint | `make syntax` / `make lint` |

Tags: `hosts`, `swarm`, `vpn`, `nginx`, `orchestrator`, `monitoring`, `admin`.

## Safety rails

- **No accidental duplicate stacks.** A stack deploy fails if no stack of that name is deployed yet, so a wrong name in `group_vars/all/main.yml` cannot start a second copy of a stack next to the real one. `make bootstrap` and `make deploy … first=1` pass `-e allow_new_stacks=true`.
- **Secrets are only created, never replaced.** The swarm role creates a missing secret from `vault_swarm_secrets` and refuses one still set to `CHANGE_ME`; rotating an existing secret stays a manual `docker secret` operation, because swarm secrets in use cannot be updated in place.
- **`--check` skips stack deploys** (`community.docker.docker_stack` has no check mode); it still shows file changes, network and secret differences.
- **Secret files are never diffed or logged** (`diff: false`, `no_log`).

## Swarm configs and secrets from files are immutable

Stacks that mount a config or secret from a file (nginx, vpn, monitoring, orchestrator, monolith) version its name by hand (`nginx-v45`, `monolith-mariadb-init-schemas-v1`). Changing such a file without bumping the name fails the deploy with "only updates to Labels are allowed" — same as with `docker stack deploy` directly.

## From CI

GitHub Actions only publishes images and calls this playbook; it never runs `docker` against the swarm itself.

- `.github/workflows/monolith-publish.yml` runs on a `monolith/<version>` tag: it checks the tag equals `version` in the root `build.gradle.kts`, pushes `ghcr.io/an22/com.bookk.server:<version>`, then calls the deploy workflow.
- `.github/workflows/microservice-publish.yml` runs on an `auth/`, `user/`, `business/`, `appointment/` or `notification/<version>` tag and only publishes `ghcr.io/an22/com.bookk.server.microservice.<service>:<version>` (same version check); nothing deploys microservices while prod runs the monolith.
- `.github/workflows/deploy.yml` runs `ansible-playbook playbooks/deploy.yml -e version=<version>` on a GitHub-hosted runner. Run it by hand (**Actions → Deploy monolith → Run workflow**) to redeploy or roll back to any published version.

Every workflow runs on GitHub-hosted `ubuntu-latest` runners; nothing runs inside the swarm. The deploy job therefore reaches the manager over SSH from GitHub's address ranges, which change, so the manager's SSH port must accept connections from the internet (key-only root login) at the `vault_hosts['manager-1']` address. The job installs Ansible with `uv`, pinned to the same versions as a local Homebrew install: ansible-core 2.21.5, community.docker 5.4.0, community.general 13.5.0. It pulls with the registry token from the vault, not `GITHUB_TOKEN`: the deploy passes `--with-registry-auth`, which stores the credentials in the swarm service for later pulls, and `GITHUB_TOKEN` expires when the job ends.

GitHub setup the workflows need:
- Secrets `ANSIBLE_VAULT_PASSWORD` (new), `SSH_PRIVATE_KEY`, `SSH_KNOWN_HOSTS` and `GRADLE_ENCRYPTION_KEY` (already used by the old workflows).
- An environment `production` (**Settings → Environments**) with yourself as required reviewer, so every deploy waits for an approval. Without protection rules GitHub creates it on first use and deploys without asking.

`make infra` / `make bootstrap` are not wired into CI: infrastructure changes are rare and reviewed by hand, so run them from your machine.
