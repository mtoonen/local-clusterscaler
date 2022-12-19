#!/usr/bin/env bash
./mvnw package -Pnative -DskipTests -Dnative-image.docker-build=true
docker build -f src/main/docker/Dockerfile.native -t meinetoonen/local-clusterscaler .
docker push meinetoonen/local-clusterscaler
