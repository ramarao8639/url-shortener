pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                echo 'Source code checkout successful'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }

    }

    post {
        success {
            echo 'Pipeline completed successfully!'
        }

        failure {
            echo 'Pipeline failed!'
        }
    }
}