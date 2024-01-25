def call(Map config = [:]) {
    office365ConnectorSend webhookUrl: '<>',
        message: 'Jenkins run CI/CD aborted !!!',
        status: 'Aborted !!! 🤧 🤕 😢 🤧 🤕 😢 🤧 🤕 😢',
        color: '#999897',
        factDefinitions: [
            [name: "Commit", template: "${GIT_COMMIT.take(7)}"],
            [name: "Job", template: "$JOB_NAME"],
            [name: "History", template: "$JOB_DISPLAY_URL"]]
}