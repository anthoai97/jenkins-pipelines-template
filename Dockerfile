FROM jenkins/jenkins:lts
USER root
RUN apt-get update
RUN apt-get install ca-certificates curl
RUN install -m 0755 -d /etc/apt/keyrings
RUN curl -fsSL https://download.docker.com/linux/debian/gpg -o /etc/apt/keyrings/docker.asc
RUN chmod a+r /etc/apt/keyrings/docker.asc

RUN apt-get install -y wget apt-transport-https gnupg
RUN wget -qO - https://aquasecurity.github.io/trivy-repo/deb/public.key | gpg --dearmor | tee /usr/share/keyrings/trivy.gpg > /dev/null
RUN echo "deb [signed-by=/usr/share/keyrings/trivy.gpg] https://aquasecurity.github.io/trivy-repo/deb generic main" | tee -a /etc/apt/sources.list.d/trivy.list
RUN apt-get update
RUN apt-get install trivy

RUN wget https://github.com/gitleaks/gitleaks/releases/download/v8.20.1/gitleaks_8.20.1_linux_x64.tar.gz
RUN tar -zxvf gitleaks_8.20.1_linux_x64.tar.gz
RUN mv gitleaks /usr/local/bin/
RUN rm gitleaks_8.20.1_linux_x64.tar.gz

# Add the repository to Apt sources:
RUN echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/debian \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  tee /etc/apt/sources.list.d/docker.list > /dev/null
RUN apt-get update && apt-get install -y docker-ce docker-ce-cli containerd.io
EXPOSE 8080

