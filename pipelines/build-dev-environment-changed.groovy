pipeline {
    agent any 
    options {
        disableConcurrentBuilds()
        disableResume()
    }

    parameters {
        string(name: 'ENVIRONMENT_NAME', trim: true, description: 'Name for the Docker environment')     
        password(name: 'MYSQL_PASSWORD', defaultValue: '', description: 'Password to use for MySQL container - root user')
        string(name: 'MYSQL_PORT', trim: true, description: 'Port to expose MySQL')

        booleanParam(name: 'SKIP_STEP_1', defaultValue: false, description: 'STEP 1 - RE-CREATE DOCKER IMAGE')
    }
  
    stages {
        stage('Checkout GIT repository') {
            steps {     
              script {
                git branch: 'master',
                credentialsId: '21f01d09-06da9cc35103',
                url: 'git@mysecret-nonexistent-repo/jenkins.git'
              }
            }
        }
        stage('Create latest Docker image') {
            steps {     
              script {
                if (!params.SKIP_STEP_1){    
                    echo "Creating docker image with name ${params.ENVIRONMENT_NAME} using port: ${params.MYSQL_PORT}"
                    sh """
                    sed "s/<PASSWORD>/${params.MYSQL_PASSWORD}/g" pipelines/include/create_developer-changed.template > pipelines/include/create_developer.sql
                    """

                    sh """
                    docker build pipelines/ -t ${params.ENVIRONMENT_NAME}:latest
                    """

                }else{
                    echo "Skipping STEP 1"
                }
              }
            }
        }
        
        stage('Validate MySQL Port') {
            steps {
                script {
                    // Convert parameter to integer safely
                    def port = params.MYSQL_PORT.isInteger() ? params.MYSQL_PORT.toInteger() : -1

                    // Check if port is in valid range (3306-33060)
                    if (port < 3306 || port > 33060) {
                        error "❌ Invalid MySQL Port: '${params.MYSQL_PORT}'. Port must be a number between 3306 and 33060."
                    } else {
                        echo "✅ MySQL Port '${port}' is valid."
                    }
                }
            }
        }


        stage('Start new container using latest image and create user') {
            steps {     
              script {
                def dateTime = (sh(script: "date +%Y%m%d%H%M%S", returnStdout: true).trim())
                def containerName = "${params.ENVIRONMENT_NAME}_${dateTime}"
                sh """
                docker run -itd --name ${containerName} --rm -e MYSQL_ROOT_PASSWORD=${params.MYSQL_PASSWORD} -p ${params.MYSQL_PORT}:3306 ${params.ENVIRONMENT_NAME}:latest
                """
                sleep 20

               sh """
                   docker exec ${containerName} /bin/bash -c 'mysql --user="root" --password="$params.MYSQL_PASSWORD" < /scripts/create_developer.sql'
               """
                echo "✅ Docker container created: ${containerName}"

                echo "✅ Developer user created with password: ${params.MYSQL_PASSWORD}"

              }
            }
        }
    }
}