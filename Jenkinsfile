pipeline{
    agent any

    tools{
        jdk "JDK21"
        maven "Maven3"
    }

    options {
        skipStagesAfterUnstable()
    }

    environment{
        JAR_NAME = "RevShop-0.0.1-SNAPSHOT.jar"
        REMOTE_DIR = "/home/ec2-user"
        APP_PORT = "8080"
    }

    stages{

        stage("Checkout"){
            steps{
                checkout scm
            }
        }

        stage("Build"){
            steps{
                bat 'mvn -B clean compile'
            }
            post{
                success{
                    echo "build succesful"
                }
                failure{
                    echo "build failed"
                }
            }
        }

        stage("test"){
            steps{
                bat 'mvn -B test'
            }
            post{
                success{
                    echo "test succesful"
                }
                failure{
                    echo "test failed"
                }
            }

        }

        stage("Package"){
            steps{
                bat 'mvn -B package -DskipTest'
            }
            post{
                success{
                    echo "jar created succesfully"
                }
                failure{
                    echo "jar creation failed"
                }
            }
        }

        stage("Deploy to EC2"){
            steps{
                sshPublisher(
                    publishers:[
                        sshPublisherDesc(
                            configName:"ec2-server",
                            verbose: true,
                            transfers:[
                                sshTransfer(
                                    sourceFiles:"target/${JAR_NAME}",
                                    removePrefix: "target/",
                                    remoteDirectory:"${REMOTE_DIR}",
                                    flatten: true,
                                    execCommand: """
                                        pkill -f "java -jar" || true

                                        nohup java -jar ${REMOTE_DIR}/${JAR_NAME} --spring.profiles.active=aws > application.log 2>&1 &
                                    """
                                )
                            ]

                        )
                    ]
                )

            }
            post {
                success{
                    echo "deployed succesfully"
                }
            }
        }

    }
}