#!/usr/bin/env sh
docker compose -f environment-compose.yml up -d
#Auth service
cd service || exit
export $(grep -v '^#' auth.env | xargs) || exit
cd ../../.. || exit
#Sometimes gradle daemon cannot resolve docker from END, daemon restart helps
./gradlew --stop
./gradlew :service:authorization:microservice:publishImageToLocalRegistry --no-configuration-cache || exit

#User service
cd deployment/dev/service || exit
export $(grep -v '^#' user.env | xargs) || exit
cd ../../.. || exit
./gradlew :service:user:microservice:publishImageToLocalRegistry --no-configuration-cache || exit

#Business service
cd deployment/dev/service || exit
export $(grep -v '^#' business.env | xargs) || exit
cd ../../.. || exit
./gradlew :service:business:microservice:publishImageToLocalRegistry --no-configuration-cache || exit

#Appointments service
cd deployment/dev/service || exit
export $(grep -v '^#' appointments.env | xargs) || exit
cd ../../.. || exit
./gradlew :service:appointments:microservice:publishImageToLocalRegistry --no-configuration-cache || exit

docker context use default || exit
cd deployment/dev || exit
docker compose -f microservices-compose.yml up -d
