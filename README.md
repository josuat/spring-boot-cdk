# AWS CDK example deploying a simple REST API to ECS


### Build

1. Build docker image **user-api**

        ./gradlew build
        ./gradlew dockerTag



### Run locally

    docker-compose up -d