#!/bin/bash

docker stop postgres_bank
docker rm postgres_bank
docker volume rm bank_rest_postgres_data
docker compose -f docker-compose.yml up -d
