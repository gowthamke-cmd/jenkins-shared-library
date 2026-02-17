def call(Map config = [:]) {

    def imageName = config.imageName ?: "node-app"
    def containerName = config.containerName ?: "node-container"
    def port = config.port ?: "3006"

    pipeline {
        agent any

        environment {
            IMAGE_NAME = "${imageName}"
            CONTAINER_NAME = "${containerName}"
            PORT = "${port}"
        }

        stages {

            stage('Checkout') {
                steps {
                    checkout scm
                }
            }

            stage('Install Dependencies') {
                agent {
                    docker {
                        image 'node:20-alpine'
                        reuseNode true
                    }
                }
                steps {
                    sh 'npm install'
                }
            }

            stage('Build Docker Image') {
                steps {
                    sh """
                    docker build -t ${IMAGE_NAME} .
                    """
                }
            }

            stage('Deploy Container') {
                steps {
                    sh """
                    docker stop ${CONTAINER_NAME} || true
                    docker rm ${CONTAINER_NAME} || true

                    docker run -d \
                      -p ${PORT}:3000 \
                      --name ${CONTAINER_NAME} \
                      ${IMAGE_NAME}
                    """
                }
            }
        }
    }
}
