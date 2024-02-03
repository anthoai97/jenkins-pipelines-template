# Jenkin Pipeline

## Overview
Some Jenkins pipelines useful for Jenkin Kubernetes

### Install local Jenkins

```bash
docker build -t jenkins:lts-docker .

# Start Jenkins
docker run --name jenkins --restart=on-failure\
  -p 8080:8080 \
  -p 50000:50000 \
  -v "${PWD}/data/jenkins-data:/var/jenkins_home" \
  -v "${PWD}/data/jenkins-docker-certs:/certs/client:ro" \
  -v /var/run/docker.sock:/var/run/docker.sock \
  --privileged \
  --user root \
  -d \
  jenkins:lts-docker
``` 