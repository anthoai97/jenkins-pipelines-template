libraries {
    maven
    sonarqube
    ansible
    splunk {
      	afterSteps = [ "static_code_analysis", "unit_test"  ]
    }
  	git
}

stages {
    continuous_integration {
      	unit_test
        build
        static_code_analysis
    }
}

application_environments {
    dev {
        ip_addresses = [ "0.0.0.1", "0.0.0.2" ]
    }
    prod {
        long_name = "Production" 
        ip_addresses = [ "0.0.1.1", "0.0.1.2", "0.0.1.3", "0.0.1.4" ]
    }
}

keywords {
    requiresApproval = false 
}

steps {
    unit_test {
        stage = "Unit Test"
        image = "maven"
        command = "mvn -v"
    }
}