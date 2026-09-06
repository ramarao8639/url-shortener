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
                    git log -1 --oneline
                '''
            }
        }


        stage('Build Application') {
            steps {
                sh '''
                    echo "Building Spring Boot application..."
                    mvn clean package -DskipTests
                '''
            }
        }


        stage('Test') {
            steps {
                withEnv([
                    'SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/url_shortener',
                    'SPRING_DATASOURCE_USERNAME=postgres',
                    'SPRING_DATASOURCE_PASSWORD=root'
                ]) {

                    sh '''
                        echo "========================================="
                        echo "Running tests using local PostgreSQL"
                        echo "========================================="

                        echo "Database URL: $SPRING_DATASOURCE_URL"

                        mvn test
                    '''
                }
            }
        }


        stage('Docker Build') {
            steps {
                sh '''
                    echo "========================================="
                    echo "Building Docker Image"
                    echo "========================================="

                    docker build -t $IMAGE_NAME .

                    echo "Docker image created:"
                    docker images | grep url-shortener
                '''
            }
        }


        stage('Login to AWS ECR') {
            steps {

                withCredentials([
                    usernamePassword(
                        credentialsId: 'aws-ecr-credentials',
                        usernameVariable: 'AWS_ACCESS_KEY_ID',
                        passwordVariable: 'AWS_SECRET_ACCESS_KEY'
                    )
                ]) {

                    sh '''
                        echo "========================================="
                        echo "Authenticating with AWS"
                        echo "========================================="

                        aws sts get-caller-identity

                        echo "Logging into AWS ECR..."

                        aws ecr get-login-password \
                            --region $AWS_REGION \
                        | docker login \
                            --username AWS \
                            --password-stdin $ECR_REGISTRY
                    '''
                }
            }
        }


        stage('Push Docker Image') {
            steps {
                sh '''
                    echo "========================================="
                    echo "Pushing Docker Image to AWS ECR"
                    echo "========================================="

                    docker push $IMAGE_NAME

                    echo "Image pushed successfully:"
                    echo $IMAGE_NAME
                '''
            }
        }


        stage('Update Kubernetes Deployment') {
            steps {
                sh '''
                    echo "========================================="
                    echo "Updating Kubernetes Deployment"
                    echo "========================================="

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
                        echo "========================================="
                        echo "Pushing Kubernetes Manifest to GitHub"
                        echo "========================================="

                        git config user.email "jenkins@url-shortener.local"
                        git config user.name "Jenkins"

                        git add k8s/app/deployment.yaml

                        git commit -m "Deploy image $IMAGE_NAME" || echo "No changes to commit"

                        git push \
                        https://$GITHUB_USER:$GITHUB_TOKEN@github.com/ramarao8639/url-shortener.git \
                        HEAD:main
                    '''
                }
            }
        }


        stage('Wait for ArgoCD Sync') {
            steps {
                sh '''
                    echo "========================================="
                    echo "Waiting for ArgoCD to detect Git changes"
                    echo "========================================="

                    sleep 30
                '''
            }
        }


        stage('Deployment Complete') {
            steps {
                sh '''
                    echo "========================================="
                    echo "PIPELINE COMPLETED SUCCESSFULLY"
                    echo "========================================="

                    echo "Docker Image:"
                    echo $IMAGE_NAME

                    echo ""
                    echo "Deployment Flow:"
                    echo "GitHub -> Jenkins"
                    echo "Jenkins -> Docker Build"
                    echo "Docker -> AWS ECR"
                    echo "Jenkins -> Update Kubernetes YAML"
                    echo "GitHub -> ArgoCD"
                    echo "ArgoCD -> Kubernetes"
                    echo "========================================="
                '''
            }
        }
    }


    post {

        success {
            echo '''
=========================================
BUILD AND DEPLOYMENT SUCCESSFUL!
=========================================
'''
        }


        failure {
            echo '''
=========================================
PIPELINE FAILED!
=========================================
Check the Jenkins Console Output.
'''
        }
    }
}