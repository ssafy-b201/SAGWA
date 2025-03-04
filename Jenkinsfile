pipeline {
    agent any
    environment {
        DOCKER_COMPOSE_VERSION = '1.25.0' // 사용할 Docker Compose의 버전
        GITLAB_TOKEN = credentials('wns1915_sagwa') // Jenkins에 저장된 GitLab Token의 ID
    }
    stages {                                            
        stage('Checkout') {
            steps {
                git branch: 'release', credentialsId: 'wns1915_sagwa', url: 'https://lab.ssafy.com/ztjdwnz/apple.git' // GitLab 리포지토리
            }
        }
		stage('Update Local Repository') {
            steps {
                script {
						withCredentials([usernamePassword(credentialsId: 'wns1915_sagwa', usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD')]) {
							sh '''
							ENCODED_USERNAME=$(echo $GIT_USERNAME | sed 's/@/%40/g')
							cd /home/ubuntu/apple
							git pull https://$ENCODED_USERNAME:$GIT_PASSWORD@lab.ssafy.com/ztjdwnz/apple.git release
							'''
						}
                }
            }
        }
        stage('Build Docker Images') {
            steps {
                script {
                    sh 'docker-compose -f /home/ubuntu/apple/devway/docker-compose.yml build --no-cache app_sagwa'
                    sh 'docker-compose -f /home/ubuntu/apple/devway/docker-compose.yml up -d app_sagwa'
                }
            }
        }
        stage('Deploy') {
            steps {
                script {
					sh 'docker-compose -f /home/ubuntu/oringe/devway/docker-compose.yml build --no-cache nginx '
                    sh 'docker-compose -f /home/ubuntu/oringe/devway/docker-compose.yml up -d nginx '
                    sh 'docker-compose -f /home/ubuntu/oringe/devway/docker-compose.yml up -d certbot'
                }
            }
        }
    }
}
