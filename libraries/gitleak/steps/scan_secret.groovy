void call() {
    stage('Setup Gitleaks') {
        script {
            def gitleaksInstalled = sh(returnStatus: true, script: 'which gitleaks > /dev/null') == 0
                
            if (!gitleaksInstalled) {
                    sh 'mkdir -p bin'
                    sh 'curl -sfL https://install.goreleaser.com/github.com/zricethezav/gitleaks.sh | sh -s -- -b bin'                }
            env.PATH = "${env.WORKSPACE}/bin:${env.PATH}"
        }
    }
}