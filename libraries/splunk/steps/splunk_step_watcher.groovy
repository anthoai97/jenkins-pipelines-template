@BeforeStep
void before() {
    println "Splunk: running before the ${hookContext.library} library's ${hookContext.step} step" 
}

@AfterStep
void after({ hookContext.step.equals("static_code_analysis") }) {
    println "Splunk: running after the ${hookContext.library} library's ${hookContext.step} step" 
}
