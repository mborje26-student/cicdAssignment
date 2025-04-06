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
                SONAR_HOST_URL = 'https://192.168.0.4:9000'
                SONAR_AUTH_TOKEN = credentials('sonarqube')
            }
            steps {
                sh 'mvn sonar:sonar -Dsonar.projectKey=sample_project -Dsonar.host.url=$SONAR_HOST_URL'
            }
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
