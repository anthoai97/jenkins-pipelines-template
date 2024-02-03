def call(Map config = [:]) {
   def printenv = {
    sh 'env'
  }

  pipeline {
    agent {
      kubernetes {
        defaultContainer 'terraform'
        yaml '''
          apiVersion: v1
          kind: Pod
          spec:
            containers:
            - name: terraform
              image: registry.gitlab.com/hcg-openhub/hcg-hub/hcg-tf:1.0.0
              command:
              - sleep
              args:
              - 99d
      '''
      }
    }

    environment {
      // System env script auto fill DONT TOUCH
        BUILD_ENV = ''
        AWS_ACCESS_KEY_ID     = credentials('aws-secret-key-id')
        AWS_SECRET_ACCESS_KEY = credentials('aws-secret-access-key')
        AWS_REGION = credentials('aws-region')
    }

    stages {
        stage('Environment Variables Dev') {
            when {
                branch 'develop'
            }
            steps {
                script {
                    BUILD_ENV = 'environments/develop' // overrides pipeline level NAME env variable
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
                    BUILD_ENV = 'environments/qa' // overrides pipeline level NAME env variable
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
                    BUILD_ENV = 'environments/production' // overrides pipeline level NAME env variable
                    printenv ()
                }
            }
        }

        stage('Terraform init') {
                steps {

                    dir("$BUILD_ENV") {
                        sh 'ls'

                        sh "terraform init"
                    }
                    
                }
            }

        stage('Terraform plan') {
            steps {
                sh "terraform plan"
            }
        }

        stage('Terraform apply') {
            input {
                message "Do you want to proceed for production deployment?"
            }
            steps {
                sh "terraform apply -auto-approve"
            }
        }

    }

    post {
      failure {
        sentFailureNotification()
      }
    }
  }
}