package com.myorg;

import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Duration;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps;
import software.amazon.awscdk.services.autoscaling.ElbHealthCheckOptions;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.rds.*;
import software.amazon.awscdk.services.secretsmanager.ISecret;
import software.amazon.awscdk.services.secretsmanager.Secret;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DeployStack extends Stack {
  public DeployStack(final Construct scope, final String id) {
    this(scope, id, null);
  }

  public DeployStack(final Construct scope, final String id, final StackProps props) {
    super(scope, id, props);

    Vpc vpc = Vpc.Builder.create(this, "user-api-vpc")
        .maxAzs(2)
        .natGateways(0)
        .subnetConfiguration(Arrays.asList(SubnetConfiguration.builder()
                .cidrMask(26)
                .name("tasks")
                .subnetType(SubnetType.PUBLIC)
                .build()))
        .build();

    Cluster cluster = Cluster.Builder.create(this, "user-api-cluster")
        .clusterName("user-api-cluster")
        .vpc(vpc)
        .capacity(AddCapacityOptions.builder()
            .autoScalingGroupName("user-api-autoscaling-group")
            .instanceType(InstanceType.of(InstanceClass.BURSTABLE3, InstanceSize.MICRO))
            .machineImage(EcsOptimizedImage.amazonLinux2())
            .maxCapacity(2)
            .healthCheck(software.amazon.awscdk.services.autoscaling.HealthCheck.elb(
                ElbHealthCheckOptions.builder()
                    .grace(Duration.minutes(2))
                    .build()))
            .build())
        .build();

    SecurityGroup ecsTaskSecGroup = SecurityGroup.Builder.create(this, "user-api-ecTask-sg")
        .vpc(vpc)
        .allowAllOutbound(true)
        .build();

    SecurityGroup dbSecGroup = SecurityGroup.Builder.create(this, "user-api-db-sg")
        .vpc(vpc)
        .build();
    dbSecGroup.addIngressRule(ecsTaskSecGroup, Port.tcp(3306));

    String dbSecretName = (String)this.getNode().tryGetContext("mysql-credentials-secret");
    String imageRepo = (String)this.getNode().tryGetContext("ecr-repo-name");
    ISecret rdsDbAdminSecret = Secret.fromSecretName(this, "mysql-rds-credentials", dbSecretName);

    DatabaseInstance mySqlRdsInstance = DatabaseInstance.Builder.create(this, "user-api-mysql-rds")
        .engine(DatabaseInstanceEngine.mysql(
            MySqlInstanceEngineProps.builder().version(MysqlEngineVersion.VER_5_7).build()))
        .instanceType(InstanceType.of(InstanceClass.BURSTABLE3, InstanceSize.MICRO))
        .vpc(vpc)
        .vpcSubnets(SubnetSelection.builder().subnetType(SubnetType.PUBLIC).build())
        .securityGroups(List.of(dbSecGroup))
        .multiAz(false)
        .autoMinorVersionUpgrade(true)
        .allocatedStorage(5)
        .storageType(StorageType.GP2)
        .backupRetention(Duration.days(3))
        .deletionProtection(false)
        .databaseName("userapi")
        .credentials(Credentials.fromSecret(rdsDbAdminSecret))
        .port(3306)
        .build();

    String rdsHost = mySqlRdsInstance.getDbInstanceEndpointAddress();

    Role role = Role.Builder.create(this, "user-api-task-role")
        .roleName("user-api-task-role")
        .assumedBy(new ServicePrincipal("ecs-tasks.amazonaws.com"))
        .managedPolicies(List.of(
            ManagedPolicy.fromAwsManagedPolicyName("service-role/AmazonECSTaskExecutionRolePolicy"),
            ManagedPolicy.fromAwsManagedPolicyName("SecretsManagerReadWrite")
        ))
        .build();

    ApplicationLoadBalancedFargateService fargateService = ApplicationLoadBalancedFargateService.Builder.create(this, "user-api-fargate-service")
        .cluster(cluster)
        .cpu(256)
        .memoryLimitMiB(512)
        .desiredCount(1)
        .taskImageOptions(ApplicationLoadBalancedTaskImageOptions.builder()
            .image(ContainerImage.fromEcrRepository(
                Repository.fromRepositoryName(this, "user-api", imageRepo)))
            .containerPort(8080)
            .taskRole(role)
            .environment(Map.of(
                "SPRING_DATASOURCE_URL", String.format("jdbc-secretsmanager:mysql://%s/userapi", rdsHost),
                "SPRING_DATASOURCE_DRIVER_CLASS_NAME", "com.amazonaws.secretsmanager.sql.AWSSecretsManagerMySQLDriver",
                "SPRING_DATASOURCE_USERNAME", dbSecretName))
            .build())
        .securityGroups(List.of(ecsTaskSecGroup))
        .publicLoadBalancer(true)
        .assignPublicIp(true)
        .build();

    fargateService.getTargetGroup().configureHealthCheck(
        software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck.builder()
            .healthyHttpCodes("200")
            .path("/actuator/health")
            .build());

    ScalableTaskCount scalableTask = fargateService.getService().autoScaleTaskCount(EnableScalingProps.builder()
        .minCapacity(1)
        .maxCapacity(2)
        .build());

    scalableTask.scaleOnCpuUtilization("user-api", CpuUtilizationScalingProps.builder()
        .targetUtilizationPercent(60) // Scale when the CPU utilization is at 60%
        .build());
  }
}
