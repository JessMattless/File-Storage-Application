#!/bin/sh
ssh-keyscan -t rsa 172.18.0.2 >> ~/.ssh/known_hosts
ssh-keyscan -t rsa 172.18.0.3 >> ~/.ssh/known_hosts
ssh-keyscan -t rsa 172.18.0.4 >> ~/.ssh/known_hosts
ssh-keyscan -t rsa 172.18.0.5 >> ~/.ssh/known_hosts
