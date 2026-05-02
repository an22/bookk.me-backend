#!/usr/bin/env sh
docker compose -f environment-compose.yml up -d
cd service || exit
export $(grep -v '^#' auth.env | xargs) || exit
cd ../../.. || exit
./gradlew --stop || exit
./gradlew :service:authorization:microservice:publishImageToLocalRegistry
cd deployment/local-testing-environment/service || exit
export $(grep -v '^#' user.env | xargs) || exit
cd ../../.. || exit
./gradlew :service:user:microservice:publishImageToLocalRegistry
cd deployment/local-testing-environment/service || exit
export $(grep -v '^#' business.env | xargs) || exit
cd ../../.. || exit
./gradlew :service:business:microservice:publishImageToLocalRegistry
docker context use default || exit
cd deployment/local-testing-environment || exit
docker compose -f microservices-compose.yml up -d
