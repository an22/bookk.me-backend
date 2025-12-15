
# Server

Core project usage hints

# Table of Contents
1. [Deployment](#deployment)

## Deployment (MacOS only) <a name="deployment"></a>

### 1. Core infrastructure

#### Connect to remote host
Connect to remote node which will be starting point for Swarm deployment

#### Init swarm node
```
docker swarm init --advertise-addr {subnet ip}
docker network create -d overlay operational
docker network create -d overlay agent_network
```

#### Load SSH into terminal
```
ssh-add --apple-use-keychain ~/.ssh/id_rsa //or ~/.ssh/id_ed25519
```

#### Create docker context on your host machine
```
docker context create swarm-manager --docker host=ssh://{user}@{ip}
docker context use swarm-manager
```

####  Deploy VPN

1. Install wireguard on your host machine, see [installation guide](https://www.wireguard.com/install/)
2. Generate password hash for admin panel access

```
docker run ghcr.io/wg-easy/wg-easy:nightly wgpw {your password}
```

3. Create docker stack

```
export VPN_PASSWORD_HASH={your hash from step 2, do not forget to escape dollar sign char}
docker stack deploy vpn --compose-file deployment/vpn/vpn-compose.yml
```

#### Deploy NGINX
1. Place well known files to
```
/srv/static/android_well_known.json
/srv/static/ios_well_known.json
```
2. Make sure certificates available
```
/etc/letsencrypt/live/{domain_name}/fullchain.pem
/etc/letsencrypt/live/{domain_name}privkey.pem
```
3. Run
```
docker stack deploy nginx --compose-file deployment/nginx/nginx-compose.yml
```

#### Deploy container manager

```
docker stack deploy orchestrator --compose-file deployment/orchestrator/orchestrator-compose.yml
```

#### Connect to VPN
TODO

#### Deploy Github runner
```
export RUNNER_ACCESS_TOKEN={get access token from github}
docker stack deploy runner --compose-file deployment/runner/runner-compose.yml
```
#### Get worker token

TODO

#### Add worker nodes

IP address can be incorrect since subnet IP is used, so make sure you use correct manager IP

```
docker swarm join --token {token from manager} {subnet ip}:2377
```

