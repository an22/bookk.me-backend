#!/usr/bin/env sh

manager_ip=$2
manager_user=$3
wireguard_admin_password=$4
ssh_name=${5:-id_rsa}

#This environment variables should be defined for the deployment system
#Kafka
export KAFKA_CLUSTER_ID=
export KAFKA_BROKER_PORT=
export KAFKA_CONTROLLER_PORT=
export KAFKA_UI_USERNAME=
export KAFKA_UI_PASSWORD=
#WireguardVPN
export VPN_SUBNET=
#Runner
export RUNNER_ACCESS_TOKEN=
export MOBILE_RUNNER_ACCESS_TOKEN=
#Nginx
export DOMAIN_NAME=
export HOSTINGER_API_KEY=
#Monitoring
export GRAFANA_USER=
export GRAFANA_PASSWORD=

echo "Manager IP: $manager_ip"
echo "Manager User: $manager_user"
echo "Using ssh key: $ssh_name"
echo "Wireguard password: $wireguard_admin_password"

docker network create --driver overlay --opt encrypted operational
docker network create --driver overlay --opt encrypted agent_network

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