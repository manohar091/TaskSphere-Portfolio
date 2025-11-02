# 🚀 TaskSphere Deployment Guide

## 🎯 **Overview**

This guide provides step-by-step instructions for deploying TaskSphere to various environments including local development, staging, and production AWS infrastructure.

---

## 🏠 **Local Development Setup**

### **Prerequisites**
- ☕ Java 21+
- 🐳 Docker & Docker Compose
- 🗄️ MySQL 8.0+ (or use Docker)
- 🔄 Redis 7.0+ (or use Docker)
- 📦 Maven 3.6+

### **Quick Start with Docker**

```bash
# 1. Clone the repository
git clone https://github.com/manohar091/TaskSphere.git
cd TaskSphere

# 2. Start infrastructure services
cd infra
docker-compose up -d mysql redis prometheus grafana

# 3. Wait for services to be ready (check with docker-compose ps)
# 4. Run the application
cd ../backend
./mvnw spring-boot:run -Dspring.profiles.active=dev
```

### **Manual Setup**

```bash
# 1. Start MySQL
sudo systemctl start mysql
mysql -u root -p
CREATE DATABASE tasksphere_dev;
CREATE USER 'tasksphere_user'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON tasksphere_dev.* TO 'tasksphere_user'@'localhost';

# 2. Start Redis
sudo systemctl start redis

# 3. Configure environment variables
export DB_URL=jdbc:mysql://localhost:3306/tasksphere_dev
export DB_USERNAME=tasksphere_user
export DB_PASSWORD=password
export REDIS_HOST=localhost
export REDIS_PORT=6379

# 4. Run application
cd backend
./mvnw spring-boot:run
```

### **Verify Local Setup**
```bash
# Health check
curl http://localhost:8081/actuator/health

# API documentation
open http://localhost:8081/swagger-ui.html

# Metrics
curl http://localhost:8081/actuator/prometheus
```

---

## 🐳 **Docker Deployment**

### **Build Docker Image**

```bash
# Navigate to backend directory
cd backend

# Build the application
./mvnw clean package -DskipTests

# Build Docker image
docker build -t tasksphere-backend:latest .

# Run with Docker
docker run -p 8081:8081 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e DB_URL=jdbc:mysql://host.docker.internal:3306/tasksphere \
  -e DB_USERNAME=tasksphere_user \
  -e DB_PASSWORD=password \
  -e REDIS_HOST=host.docker.internal \
  tasksphere-backend:latest
```

### **Docker Compose Full Stack**

```bash
# Start complete stack
cd infra
docker-compose up --build

# Scale application instances
docker-compose up --scale tasksphere-app=3

# View logs
docker-compose logs -f tasksphere-app

# Stop services
docker-compose down
```

---

## ☁️ **AWS Production Deployment**

### **Prerequisites**
- 🔑 AWS CLI configured with appropriate permissions
- 🐳 Docker installed for building images
- 🏷️ AWS Account with ECR, ECS, RDS, and ElastiCache permissions

### **Step 1: Infrastructure Setup**

#### **Create VPC and Networking**
```bash
# Create VPC
aws ec2 create-vpc --cidr-block 10.0.0.0/16 --tag-specifications 'ResourceType=vpc,Tags=[{Key=Name,Value=TaskSphere-VPC}]'

# Create subnets
aws ec2 create-subnet --vpc-id vpc-12345678 --cidr-block 10.0.1.0/24 --availability-zone us-east-1a
aws ec2 create-subnet --vpc-id vpc-12345678 --cidr-block 10.0.2.0/24 --availability-zone us-east-1b

# Create Internet Gateway
aws ec2 create-internet-gateway --tag-specifications 'ResourceType=internet-gateway,Tags=[{Key=Name,Value=TaskSphere-IGW}]'
```

#### **Create RDS MySQL Instance**
```bash
# Create DB subnet group
aws rds create-db-subnet-group \
  --db-subnet-group-name tasksphere-db-subnet-group \
  --db-subnet-group-description "TaskSphere Database Subnet Group" \
  --subnet-ids subnet-12345678 subnet-87654321

# Create RDS instance
aws rds create-db-instance \
  --db-instance-identifier tasksphere-db \
  --db-instance-class db.t3.micro \
  --engine mysql \
  --engine-version 8.0.35 \
  --master-username admin \
  --master-user-password 'YourSecurePassword123!' \
  --allocated-storage 20 \
  --storage-type gp2 \
  --vpc-security-group-ids sg-12345678 \
  --db-subnet-group-name tasksphere-db-subnet-group \
  --backup-retention-period 7 \
  --multi-az \
  --storage-encrypted
```

#### **Create ElastiCache Redis Cluster**
```bash
# Create cache subnet group
aws elasticache create-cache-subnet-group \
  --cache-subnet-group-name tasksphere-cache-subnet-group \
  --cache-subnet-group-description "TaskSphere Cache Subnet Group" \
  --subnet-ids subnet-12345678 subnet-87654321

# Create Redis cluster
aws elasticache create-replication-group \
  --replication-group-id tasksphere-redis \
  --description "TaskSphere Redis Cluster" \
  --primary-cluster-id tasksphere-redis-001 \
  --num-cache-clusters 2 \
  --cache-node-type cache.t3.micro \
  --engine redis \
  --engine-version 7.0 \
  --cache-subnet-group-name tasksphere-cache-subnet-group \
  --security-group-ids sg-12345678
```

### **Step 2: Container Registry Setup**

```bash
# Create ECR repository
aws ecr create-repository --repository-name tasksphere

# Get login token
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 123456789.dkr.ecr.us-east-1.amazonaws.com

# Build and tag image
docker build -t tasksphere backend/
docker tag tasksphere:latest 123456789.dkr.ecr.us-east-1.amazonaws.com/tasksphere:latest

# Push to ECR
docker push 123456789.dkr.ecr.us-east-1.amazonaws.com/tasksphere:latest
```

### **Step 3: ECS Cluster Setup**

#### **Create ECS Cluster**
```bash
# Create cluster
aws ecs create-cluster --cluster-name tasksphere-cluster --capacity-providers FARGATE
```

#### **Create Task Definition**
```json
{
  "family": "tasksphere-backend",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "1024",
  "memory": "2048",
  "executionRoleArn": "arn:aws:iam::123456789:role/ecsTaskExecutionRole",
  "taskRoleArn": "arn:aws:iam::123456789:role/ecsTaskRole",
  "containerDefinitions": [
    {
      "name": "tasksphere-backend",
      "image": "123456789.dkr.ecr.us-east-1.amazonaws.com/tasksphere:latest",
      "portMappings": [
        {
          "containerPort": 8081,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "prod"
        },
        {
          "name": "DB_URL",
          "value": "jdbc:mysql://tasksphere-db.cluster-xyz.us-east-1.rds.amazonaws.com:3306/tasksphere"
        }
      ],
      "secrets": [
        {
          "name": "DB_USERNAME",
          "valueFrom": "arn:aws:secretsmanager:us-east-1:123456789:secret:tasksphere/db-username"
        },
        {
          "name": "DB_PASSWORD",
          "valueFrom": "arn:aws:secretsmanager:us-east-1:123456789:secret:tasksphere/db-password"
        }
      ],
      "healthCheck": {
        "command": [
          "CMD-SHELL",
          "curl -f http://localhost:8081/actuator/health || exit 1"
        ],
        "interval": 30,
        "timeout": 5,
        "retries": 3,
        "startPeriod": 60
      },
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/tasksphere",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      }
    }
  ]
}
```

#### **Create ECS Service**
```bash
# Register task definition
aws ecs register-task-definition --cli-input-json file://task-definition.json

# Create service
aws ecs create-service \
  --cluster tasksphere-cluster \
  --service-name tasksphere-service \
  --task-definition tasksphere-backend:1 \
  --desired-count 2 \
  --launch-type FARGATE \
  --network-configuration 'awsvpcConfiguration={subnets=[subnet-12345678,subnet-87654321],securityGroups=[sg-12345678],assignPublicIp=ENABLED}' \
  --load-balancers targetGroupArn=arn:aws:elasticloadbalancing:us-east-1:123456789:targetgroup/tasksphere-tg/1234567890,containerName=tasksphere-backend,containerPort=8081
```

### **Step 4: Load Balancer Setup**

```bash
# Create Application Load Balancer
aws elbv2 create-load-balancer \
  --name tasksphere-alb \
  --subnets subnet-12345678 subnet-87654321 \
  --security-groups sg-12345678

# Create target group
aws elbv2 create-target-group \
  --name tasksphere-tg \
  --protocol HTTP \
  --port 8081 \
  --vpc-id vpc-12345678 \
  --target-type ip \
  --health-check-path /actuator/health \
  --health-check-interval-seconds 30 \
  --healthy-threshold-count 2 \
  --unhealthy-threshold-count 3

# Create listener
aws elbv2 create-listener \
  --load-balancer-arn arn:aws:elasticloadbalancing:us-east-1:123456789:loadbalancer/app/tasksphere-alb/1234567890 \
  --protocol HTTP \
  --port 80 \
  --default-actions Type=forward,TargetGroupArn=arn:aws:elasticloadbalancing:us-east-1:123456789:targetgroup/tasksphere-tg/1234567890
```

---

## 🔄 **CI/CD Pipeline Setup**

### **GitHub Secrets Configuration**

```bash
# Add these secrets to your GitHub repository:
AWS_ACCESS_KEY_ID: AKIA...
AWS_SECRET_ACCESS_KEY: ...
AWS_REGION: us-east-1
ECR_REPOSITORY: tasksphere
ECS_SERVICE: tasksphere-service
ECS_CLUSTER: tasksphere-cluster
SLACK_WEBHOOK_URL: https://hooks.slack.com/...
```

### **Automated Deployment**

```bash
# Trigger deployment
git add .
git commit -m "Deploy to production"
git push origin main

# Monitor deployment
aws ecs describe-services --cluster tasksphere-cluster --services tasksphere-service
```

---

## 🩺 **Health Checks & Monitoring**

### **Application Health Checks**

```bash
# Basic health check
curl https://api.tasksphere.app/actuator/health

# Detailed health check
curl https://api.tasksphere.app/actuator/health/db
curl https://api.tasksphere.app/actuator/health/redis

# Metrics endpoint
curl https://api.tasksphere.app/actuator/metrics
```

### **Infrastructure Monitoring**

```bash
# ECS service status
aws ecs describe-services --cluster tasksphere-cluster --services tasksphere-service

# RDS instance status
aws rds describe-db-instances --db-instance-identifier tasksphere-db

# ElastiCache status
aws elasticache describe-replication-groups --replication-group-id tasksphere-redis
```

---

## 🔧 **Configuration Management**

### **Environment Variables**

| Variable | Development | Production | Description |
|----------|-------------|------------|-------------|
| `SPRING_PROFILES_ACTIVE` | `dev` | `prod` | Active Spring profile |
| `DB_URL` | `localhost:3306` | RDS endpoint | Database connection URL |
| `DB_USERNAME` | `dev_user` | AWS Secrets | Database username |
| `DB_PASSWORD` | `password` | AWS Secrets | Database password |
| `REDIS_HOST` | `localhost` | ElastiCache endpoint | Redis host |
| `JWT_SECRET` | `dev-secret` | AWS Secrets | JWT signing secret |

### **AWS Secrets Manager**

```bash
# Create database credentials secret
aws secretsmanager create-secret \
  --name tasksphere/database \
  --description "TaskSphere database credentials" \
  --secret-string '{"username":"admin","password":"YourSecurePassword123!"}'

# Create JWT secret
aws secretsmanager create-secret \
  --name tasksphere/jwt \
  --description "TaskSphere JWT signing secret" \
  --generate-random-password \
  --password-length 64
```

---

## 🔄 **Blue-Green Deployment**

### **Setup Blue-Green Pipeline**

```bash
# Create second target group (Green)
aws elbv2 create-target-group \
  --name tasksphere-tg-green \
  --protocol HTTP \
  --port 8081 \
  --vpc-id vpc-12345678 \
  --target-type ip \
  --health-check-path /actuator/health

# Deploy to green environment
aws ecs update-service \
  --cluster tasksphere-cluster \
  --service tasksphere-service-green \
  --task-definition tasksphere-backend:2 \
  --desired-count 2

# Switch traffic to green
aws elbv2 modify-listener \
  --listener-arn arn:aws:elasticloadbalancing:us-east-1:123456789:listener/app/tasksphere-alb/1234567890/1234567890 \
  --default-actions Type=forward,TargetGroupArn=arn:aws:elasticloadbalancing:us-east-1:123456789:targetgroup/tasksphere-tg-green/1234567890
```

---

## 🛠️ **Troubleshooting**

### **Common Deployment Issues**

#### **ECS Task Fails to Start**
```bash
# Check task definition
aws ecs describe-task-definition --task-definition tasksphere-backend

# Check service events
aws ecs describe-services --cluster tasksphere-cluster --services tasksphere-service

# Check CloudWatch logs
aws logs get-log-events --log-group-name /ecs/tasksphere --log-stream-name ecs/tasksphere-backend/task-id
```

#### **Database Connection Issues**
```bash
# Test database connectivity
mysql -h tasksphere-db.cluster-xyz.us-east-1.rds.amazonaws.com -u admin -p

# Check security groups
aws ec2 describe-security-groups --group-ids sg-12345678
```

#### **Load Balancer Health Check Failures**
```bash
# Check target health
aws elbv2 describe-target-health --target-group-arn arn:aws:elasticloadbalancing:us-east-1:123456789:targetgroup/tasksphere-tg/1234567890

# Test health endpoint directly
curl http://10.0.1.100:8081/actuator/health
```

---

## 📊 **Performance Optimization**

### **ECS Task Sizing**

| Environment | CPU | Memory | Instances |
|-------------|-----|--------|-----------|
| Development | 256 | 512MB | 1 |
| Staging | 512 | 1GB | 2 |
| Production | 1024 | 2GB | 3+ |

### **Auto Scaling Configuration**

```bash
# Create auto scaling target
aws application-autoscaling register-scalable-target \
  --service-namespace ecs \
  --resource-id service/tasksphere-cluster/tasksphere-service \
  --scalable-dimension ecs:service:DesiredCount \
  --min-capacity 2 \
  --max-capacity 10

# Create scaling policy
aws application-autoscaling put-scaling-policy \
  --service-namespace ecs \
  --resource-id service/tasksphere-cluster/tasksphere-service \
  --scalable-dimension ecs:service:DesiredCount \
  --policy-name tasksphere-scaling-policy \
  --policy-type TargetTrackingScaling \
  --target-tracking-scaling-policy-configuration file://scaling-policy.json
```

---

## 🔐 **Security Checklist**

### **Pre-Deployment Security**
- [ ] All secrets stored in AWS Secrets Manager
- [ ] Security groups properly configured
- [ ] Database encryption enabled
- [ ] SSL/TLS certificates configured
- [ ] IAM roles follow least privilege principle
- [ ] Container images scanned for vulnerabilities

### **Post-Deployment Verification**
- [ ] Health checks passing
- [ ] Monitoring and alerting configured
- [ ] Backup strategy verified
- [ ] Disaster recovery tested
- [ ] Security headers configured
- [ ] API rate limiting enabled

---

## 📞 **Support & Maintenance**

### **Deployment Support**
- 📧 **Email**: manohar.pagadala@example.com
- 🐙 **GitHub Issues**: [TaskSphere Issues](https://github.com/manohar091/TaskSphere/issues)
- 📖 **Documentation**: [Technical Docs](TECHNICAL.md)

### **Emergency Contacts**
- **Production Issues**: +1-xxx-xxx-xxxx
- **Security Issues**: security@tasksphere.app
- **Infrastructure Issues**: devops@tasksphere.app

---

**🚀 Ready to deploy? Follow the appropriate section for your target environment!**