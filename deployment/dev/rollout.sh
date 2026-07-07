#!/usr/bin/env sh
docker compose -f environment-compose.yml up -d
#Sometimes gradle daemon cannot resolve docker from ENV, daemon restart helps
./gradlew --stop

#Auth service
cd service || exit
export $(grep -v '^#' auth.env | xargs) || exit
cd ../../.. || exit
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

#Notification service
cd deployment/dev/service || exit
export $(grep -v '^#' notifications.env | xargs) || exit
cd ../../.. || exit
./gradlew :service:notifications:microservice:publishImageToLocalRegistry --no-configuration-cache || exit

docker context use default || exit
cd deployment/dev || exit
docker compose -f microservices-compose.yml up -d
