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
            - name: node16
              image: registry.gitlab.com/hcg-openhub/hcg-hub/hcg-node:16-alpine
              command:
              - sleep
              args:
              - 99d
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
            BUILD_ENV = 'prod' // overrides pipeline level NAME env variable
            CLUSTER_NAME = "'${env.CLUSTER_NAME_PROD}'"
            printenv ()
          }
        }
      }

      stage('Build a Node project') {
        steps {
          
          container('node16') {
            sh "npm install"

            sh "npm run build-'${BUILD_ENV}'"
          }

          // sh "mkdir node_modules"

          // cache( maxCacheSize: 1500, caches: [
          //     arbitraryFileCache(path: 'node_modules', cacheValidityDecidingFile: 'package-lock.json')
          // ]) {
          //   container('node16') {
          //       sh "npm install"

          //       sh "npm run build-'${BUILD_ENV}'"
          //   }
          // }
        }
      }

      stage('Build Front-End Image') {
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