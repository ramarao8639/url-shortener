pipeline {
    agent any

    environment {
        AWS_REGION = 'ap-south-1'
        AWS_ACCOUNT_ID = '809554585891'
        ECR_REPOSITORY = 'url-shortener'

        ECR_REGISTRY = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

        IMAGE_TAG = "${BUILD_NUMBER}"

        IMAGE_NAME = "${ECR_REGISTRY}/${ECR_REPOSITORY}:${BUILD_NUMBER}"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm

                sh '''
                    echo "Source code checkout successful"
                    git branch
                    git log -1 --oneline
                '''
            }
        }


        stage('Build Application') {
            steps {
                sh '''
                    mvn clean package -DskipTests=true
                '''
            }
        }


        stage('Docker Build') {
            steps {
                sh '''
                    docker build \
                    -t $IMAGE_NAME \
                    .
                '''
            }
        }


        stage('Login to AWS ECR') {
            steps {
                sh '''
                    aws ecr get-login-password \
                    --region $AWS_REGION \
                    | docker login \
                    --username AWS \
                    --password-stdin $ECR_REGISTRY
                '''
            }
        }


        stage('Push Docker Image') {
            steps {
                sh '''
                    docker push $IMAGE_NAME
                '''
            }
        }


        stage('Update Kubernetes Deployment') {
            steps {
                sh '''
                    sed -i \
                    "s|image: .*url-shortener:.*|image: $IMAGE_NAME|g" \
                    k8s/app/deployment.yaml

                    echo "Updated deployment image:"
                    grep "image:" k8s/app/deployment.yaml
                '''
            }
        }


        stage('Push Deployment Update to GitHub') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'github-url-shortener',
                        usernameVariable: 'GITHUB_USER',
                        passwordVariable: 'GITHUB_TOKEN'
                    )
                ]) {

                    sh '''
                        git config user.email "jenkins@url-shortener.local"
                        git config user.name "Jenkins"

                        git add k8s/app/deployment.yaml

                        git commit -m "Deploy image $IMAGE_NAME" || echo "No changes to commit"

                        git push https://$GITHUB_USER:$GITHUB_TOKEN@github.com/ramarao8639/url-shortener.git main
                    '''
                }
            }
        }


        stage('Wait for ArgoCD Sync') {
            steps {
                sh '''
                    echo "Waiting for ArgoCD to detect Git changes..."

                    sleep 30
                '''
            }
        }


        stage('Check Deployment') {
            steps {
                sh '''
                    echo "Pipeline completed."

                    echo "Docker Image:"
                    echo $IMAGE_NAME
                '''
            }
        }
    }


    post {

        success {
            echo '''
=========================================
BUILD AND DEPLOYMENT SUCCESSFUL
=========================================

Flow:

1. Maven Build Completed
2. Docker Image Created
3. Docker Image Pushed to ECR
4. Kubernetes Manifest Updated
5. Changes Pushed to GitHub
6. ArgoCD Will Detect Changes
7. Kubernetes Pod Will Deploy
=========================================
'''
        }


        failure {
            echo 'Pipeline failed!'
        }
    }
}