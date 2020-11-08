# AWS CDK example deploying a simple REST API to ECS


### Build

Build docker image **user-api**

    ./gradlew build
    ./gradlew dockerTag

### Run locally

    docker-compose up -d

### Deploy to AWS

**Prerequisites:** 
1. AWS CLI and CDK installed
1. AWS CLI default region & credentials set (`aws configure`)

**Deployment**

1. Create secret in AWS Secrets Manager with credentials for database containing the following plaintext. 
    AWS will use this to configure user/password for the created RDS MySQL database

        {
          "username": "<username>",
          "password": "<password>"
        }

1. Create repository in AWS ECR and note URI

1. Deploy docker image to AWS ECR repository

        ## example of ecr-uri/repo-name: 00001111222.dkr.ecr.ap-southeast-2.amazonaws.com/user-api
        aws ecr get-login-password | docker login --username AWS --password-stdin <ecr-uri>
        docker tag user-api:latest <ecr-uri>/<repo-name>:latest
        docker push <ecr-uri>/<repo-name>:latest

1. CDK deployment to AWS

        ## cd into deploy directory
        cdk deploy --context mysql-credentials-secret=<secret-name> --context ecr-repo-name=<repo-name>

## Caveats

- _Since this REST API uses HTTP basic auth, it should only be made accessible behind HTTPS 
    to protect the login credentials._ This has been skipped here to simplify deployment and testing.
    
- At present the API authentication credentials are in a configuration which is packaged in a docker image. 
    This is a security risk and also requires to rebuild and redeploy the image to change the password. 
    For a production deployment credentials should be provided via the secrets manager.
    
- The API application is configured to generate/update the SQL tables automatically. 
    This may not be ideal for a production deployment.

