# Jenkin Pipeline

## Overview
Some Jenkins pipelines useful for Jenkin Kubernetes

### Install local Jenkins

```bash
docker build -t jenkins:lts-docker .
```

# Start Jenkins
```bash
docker run --name jenkins \
    -v /var/run/docker.sock:/var/run/docker.sock \
    -v "${PWD}/data/jenkins-data:/var/jenkins_home" \
    -v "${PWD}/data/jenkins-docker-certs:/certs/client:ro" \
    --rm
    --privileged \
    --user root \
    -p 50000:50000 \
    -p 8080:8080 \
    jenkins-sec:lts-docker
``` 