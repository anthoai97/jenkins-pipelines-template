def call(Map config = [:]) {
    office365ConnectorSend webhookUrl: '<webhook-url>',
        message: 'Jenkins run CI/CD failure !!!',
        status: 'Failure !!! 🤧 🤕 😢 🤧 🤕 😢 🤧 🤕 😢',
        color: '#E53145',
        factDefinitions: [
            [name: "Commit", template: "${GIT_COMMIT.take(7)}"],
            [name: "Job", template: "$JOB_NAME"],
            [name: "History", template: "$JOB_DISPLAY_URL"]]
}