def call(Map config = [:]) {
    container('kubeclt') {
        withCredentials([
            string(credentialsId: 'aws-secret-key-id', variable: 'AWS_ACCESS_KEY_ID'),
            string(credentialsId: 'aws-secret-access-key', variable: 'AWS_SECRET_ACCESS_KEY'),
            string(credentialsId: 'aws-region', variable: 'AWS_REGION')
        ]) {
            sh "aws eks update-kubeconfig --name '${config.cluster_name}'"
            
            sh "echo namespace '${config.namespace}' and deployment '${config.deployment}' image '${config.image_registry}' tag '${config.short_sha}'"

            sh "kubectl set image -n '${config.namespace}' deployment '${config.deployment}' '${config.deployment}'='${config.image_registry}':'${config.short_sha}'"

            sh "kubectl -n '${config.namespace}' rollout status deployment '${config.deployment}'"
        }
    }
}