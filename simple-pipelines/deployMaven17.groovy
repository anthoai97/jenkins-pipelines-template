#!/usr/bin/env groovy
def call(Map config = [:]) {
  def printenv = {
    sh 'env'
  }

  pipeline {
    agent {
      kubernetes {
        yaml '''
          apiVersion: v1
          kind: Pod
          spec:
            containers:
            - name: maven
              image: maven:3.8.5-openjdk-17
              command:
              - sleep
              args:
              - 99d
              volumeMounts:
              - name: maven-cache
                mountPath: /root/.m2/repository
            - name: kaniko
              image: gcr.io/kaniko-project/executor:v1.9.1-debug
              command:
              - sleep
              args:
              - 9999999
              volumeMounts:
              - name: kaniko-secret
                mountPath: /kaniko/.docker
            - name: kubeclt
              image: registry.gitlab.com/hcg-openhub/hcg-hub/aws-kubectl:22-12-2022
              command:
              - sleep
              args:
              - 99d
            restartPolicy: Never
            volumes:
            - name: kaniko-secret
              secret:
                  secretName: dockercred
                  items:
                  - key: .dockerconfigjson
                    path: config.json
            - name: maven-cache
              persistentVolumeClaim:
              claimName: maven-cache-claim
      '''
      }
    }

    environment {
      // System env script auto fill DONT TOUCH
      BUILD_ENV = ''
      CLUSTER_NAME = ''
      CI_COMMIT_SHORT_SHA = GIT_COMMIT.take(7)
      IMAGE_REGISTRY = "${config.image_registry}/${BRANCH_NAME}"
    }

    stages {
      stage('Environment Variables Develop') {
        when {
          branch 'develop'
        }
        steps {
          script {
            BUILD_ENV = 'dev' // overrides pipeline level NAME env variable
            CLUSTER_NAME = "'${env.CLUSTER_NAME_DEV}'"
            printenv ()
          }
        }
      }

      stage('Environment Variables Qa') {
        when {
          branch 'qa'
        }
        steps {
          script {
            BUILD_ENV = 'qa' // overrides pipeline level NAME env variable
            CLUSTER_NAME = "'${env.CLUSTER_NAME_QA}'"
            printenv ()
          }
        }
      }

      stage('Environment Variables Production') {
        when {
          branch 'master'
        }
        steps {
          script {
            BUILD_ENV = 'production' // overrides pipeline level NAME env variable
            CLUSTER_NAME = "'${env.CLUSTER_NAME_PROD}'"
            printenv ()
          }
        }
      }

      stage('Build a Maven project') {
        steps {
          container('maven') {
            sh "mvn -B -ntp clean package -DskipTests -Denv='${BUILD_ENV}' -P'${BUILD_ENV}'"
          }
        }
      }

      stage('Build Java Image') {
        steps {
          container('kaniko') {
          sh '''
              /kaniko/executor --context `pwd` --destination ${IMAGE_REGISTRY}:${CI_COMMIT_SHORT_SHA} --destination ${IMAGE_REGISTRY}:latest
            '''
          }
        }
      }

      stage('Deploy') {
        steps {
          deployEKS(
            cluster_name: "$CLUSTER_NAME",
            namespace: "${config.namespace}",
            deployment: "${config.deployment}",
            image_registry: "$IMAGE_REGISTRY",
            short_sha: "$CI_COMMIT_SHORT_SHA"
          )
        }
      }
    }

    post {
      failure {
        sentFailureNotification()
      }
      aborted {
        sentAbortNotification()
      }
    }
  }
}