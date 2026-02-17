def call(Map config = [:]) {

    pipeline {
        agent any

        environment {
            IMAGE_NAME = config.imageName ?: "node-app"
            CONTAINER_NAME = config.containerName ?: "node-container"
            PORT = config.port ?: "3000"
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
