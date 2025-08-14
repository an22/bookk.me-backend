#!/bin/sh
openssl req -x509 -newkey rsa:4096 -keyout ca.key -out ca.crt -sha256 -days 365 -nodes -subj "/C=JP/ST=Hokkaido/L=Hokkaido/O=BlankCompany/OU=BlankSectionName/CN=local.bookkk.me"
openssl genrsa -out "server.key" 2048
openssl req -new -key server.key -out server.csr -config openssl.conf
openssl x509 -req -days 365 -in server.csr -CA ca.crt -CAkey ca.key -CAcreateserial -extensions v3_req -extfile openssl.conf -out server.crt
rm ca.srl
rm server.csr