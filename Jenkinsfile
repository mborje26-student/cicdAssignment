pipeline {
    agent any

    tools {
        maven 'Maven Project'
    }

    stages {
        stage('Checkout') {
            steps {
             git branch: 'main', url: 'https://github.com/mborje26-student/cicdAssignment'
            }
        }

        stage('Build') {
            steps {
              bat 'mvn clean install'
             }
        }

        stage('Test') {
            steps {
                bat 'mvn test'
            }
        }

        stage('SonarQube Analysis') {
            environment {
                SONAR_HOST_URL = 'http://192.168.0.4:9000'
                SONAR_CREDS = credentials('usernamepass')
            }
            steps {
                bat 'mvn sonar:sonar -Dsonar.projectKey=sample_project -Dsonar.host.url=$SONAR_HOST_URL'
            }
        }

        withSonarQubeEnv('YourSonarQubeInstance') {
            sh 'mvn clean verify sonar:sonar'
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 1, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
    }
}
