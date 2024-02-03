void call(app_env) {
    stage("Deploy to: ${app_env.long_name}") {
        println 'Performing a deployment through Ansible..'
        app_env.ip_addresses.each { ip ->
            println "Deploying to ${ip}"
        }
    }
}
