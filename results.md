# Welcome

## Thank you for the opportunity to do this challenge, I decided to do a copy of send files, just to preserve the original files, check bellow:

```
|-- Dockerfile
|-- build-dev-environment.groovy
|-- build-dev-environment-changed.groovy(new file)
`-- include
    `-- create_developer.template
    `-- create_developer-changed.template(new file)
```

## Expected behaviour(README file):

- Developers can trigger the pipeline with parameters (Environment name, MySQL password and MySQL port) Done ✅ Done!  
- A docker image built from latest MySQL image ✅ Done!  
- Spin up a container from the image built above, exposing the requested port on the Docker host ✅ Done!  
- Prepare the environment by creating an account for the developer (username: developer, password: based on input parameter) ✅ Done!  

## Changed on file build-dev-environment-changed.groovy(line 2) the agent to run the code on my own machine.

## Added some echo description in some parameters to be created.

## Changed string ```<PASSOWRD>``` to ```<PASSWORD>``` on the line 2, file create_developer-changed.template.

## Added on file build-dev-environment-changed.groovy(requested README file, line 34) this block do validate MySQL range ports(line 46 to 60):

```
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

```

## Added some sql commands(requested README file, line 35), on the file create_developer-changed.template(line 5 to 8):

```
USE DEVAPP;
CREATE TABLE departments (DEPT INT(4) NOT NULL PRIMARY KEY, DEPT_NAME VARCHAR(250) NOT NULL);
INSERT INTO departments (DEPT, DEPT_NAME) VALUES (1001, 'Human Resources'), (1002, 'Finance'), (1003, 'IT Services'), (1004, 'Marketing'),
(1005, 'Sales');

```

## Added a time on the file build-dev-environment-changed.groovy(line 71) to wait for the container be available and, after that import the commands present on .sql file:

```
sleep 20
```

## Added info to show to developer the password inputed on parameter job:


```
echo "✅ Developer user created with password: ${params.MYSQL_PASSWORD}"

```


