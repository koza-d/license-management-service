locals {
  name = "${var.project}-${var.environment}"
}

data "aws_availability_zones" "available" {
  state = "available"
}

# =============================================
# VPC (Public Subnet 2개, NAT Gateway 없음)
# =============================================
resource "aws_vpc" "this" {
  cidr_block           = "10.20.0.0/16"
  enable_dns_hostnames = true
  enable_dns_support   = true
  tags                 = { Name = "${local.name}-vpc" }
}

resource "aws_internet_gateway" "this" {
  vpc_id = aws_vpc.this.id
  tags   = { Name = "${local.name}-igw" }
}

resource "aws_subnet" "public" {
  count                   = 2
  vpc_id                  = aws_vpc.this.id
  cidr_block              = "10.20.${count.index + 1}.0/24"
  availability_zone       = data.aws_availability_zones.available.names[count.index]
  map_public_ip_on_launch = false
  tags = {
    Name = "${local.name}-public-${count.index + 1}"
  }
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.this.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.this.id
  }
  tags = { Name = "${local.name}-public-rt" }
}

resource "aws_route_table_association" "public" {
  count          = 2
  subnet_id      = aws_subnet.public[count.index].id
  route_table_id = aws_route_table.public.id
}

# =============================================
# Security Groups
# =============================================
resource "aws_security_group" "ec2" {
  name        = "${local.name}-ec2"
  description = "App EC2: SSH from my IP, 80/443 open (Cloudflare-fronted)"
  vpc_id      = aws_vpc.this.id

  ingress {
    description = "SSH from my IP"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = [var.ssh_allowed_cidr]
  }
  ingress {
    description = "HTTP"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
    description = "HTTPS"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  tags = { Name = "${local.name}-ec2-sg" }
}

resource "aws_security_group" "rds" {
  name        = "${local.name}-rds"
  description = "RDS MySQL: allow 3306 from EC2 SG only"
  vpc_id      = aws_vpc.this.id

  ingress {
    description     = "MySQL from EC2 only"
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [aws_security_group.ec2.id]
  }
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  tags = { Name = "${local.name}-rds-sg" }
}

# =============================================
# S3 백업 버킷 (암호화 + 버전관리 off + 수명주기)
# =============================================
resource "random_id" "bucket_suffix" {
  byte_length = 4
}

resource "aws_s3_bucket" "backup" {
  bucket        = "${local.name}-backup-${random_id.bucket_suffix.hex}"
  force_destroy = false
  tags          = { Name = "${local.name}-backup" }
}

resource "aws_s3_bucket_public_access_block" "backup" {
  bucket                  = aws_s3_bucket.backup.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_server_side_encryption_configuration" "backup" {
  bucket = aws_s3_bucket.backup.id
  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_s3_bucket_lifecycle_configuration" "backup" {
  bucket = aws_s3_bucket.backup.id
  rule {
    id     = "retention"
    status = "Enabled"
    filter {}
    transition {
      days          = 30
      storage_class = "GLACIER_IR"
    }
    expiration {
      days = 90
    }
  }
}

# =============================================
# IAM: EC2 → S3 백업 업로드용
# =============================================
resource "aws_iam_role" "ec2" {
  name = "${local.name}-ec2-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Principal = { Service = "ec2.amazonaws.com" }
      Action = "sts:AssumeRole"
    }]
  })
}

resource "aws_iam_role_policy" "ec2_backup" {
  name = "${local.name}-ec2-backup-policy"
  role = aws_iam_role.ec2.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Action = [
        "s3:PutObject",
        "s3:GetObject",
        "s3:ListBucket"
      ]
      Resource = [
        aws_s3_bucket.backup.arn,
        "${aws_s3_bucket.backup.arn}/*"
      ]
    }]
  })
}

resource "aws_iam_role_policy_attachment" "ssm" {
  role       = aws_iam_role.ec2.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_instance_profile" "ec2" {
  name = "${local.name}-ec2-profile"
  role = aws_iam_role.ec2.name
}

# =============================================
# EC2 (t4g.small, ARM Ubuntu 22.04)
# =============================================
resource "aws_key_pair" "deployer" {
  key_name   = "${local.name}-deployer"
  public_key = var.ssh_public_key
}

data "aws_ami" "ubuntu_arm" {
  most_recent = true
  owners      = ["099720109477"] # Canonical
  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd*/ubuntu-jammy-22.04-arm64-server-*"]
  }
  filter {
    name   = "architecture"
    values = ["arm64"]
  }
}

resource "aws_eip" "app" {
  domain = "vpc"
  tags   = { Name = "${local.name}-eip" }
}

resource "aws_instance" "app" {
  ami                         = data.aws_ami.ubuntu_arm.id
  instance_type               = var.instance_type
  subnet_id                   = aws_subnet.public[0].id
  vpc_security_group_ids      = [aws_security_group.ec2.id]
  iam_instance_profile        = aws_iam_instance_profile.ec2.name
  key_name                    = aws_key_pair.deployer.key_name
  associate_public_ip_address = true
  user_data                   = file("${path.module}/user_data.sh")
  user_data_replace_on_change = false

  root_block_device {
    volume_size           = var.root_volume_size
    volume_type           = "gp3"
    delete_on_termination = true
    encrypted             = true
  }

  metadata_options {
    http_tokens   = "required"
    http_endpoint = "enabled"
  }

  tags = { Name = "${local.name}-app" }

  lifecycle {
    ignore_changes = [ami, user_data]
  }
}

resource "aws_eip_association" "app" {
  instance_id   = aws_instance.app.id
  allocation_id = aws_eip.app.id
}

# =============================================
# RDS MySQL 8.0 (Single-AZ, Free Tier 호환 스펙)
# =============================================
resource "aws_db_subnet_group" "this" {
  name       = "${local.name}-db-subnet"
  subnet_ids = aws_subnet.public[*].id
  tags       = { Name = "${local.name}-db-subnet" }
}

resource "aws_db_parameter_group" "this" {
  name   = "${local.name}-mysql80"
  family = "mysql8.0"

  parameter {
    name  = "character_set_server"
    value = "utf8mb4"
  }
  parameter {
    name  = "collation_server"
    value = "utf8mb4_unicode_ci"
  }
  parameter {
    name  = "slow_query_log"
    value = "1"
  }
  parameter {
    name  = "long_query_time"
    value = "1"
  }
  parameter {
    name  = "time_zone"
    value = "Asia/Seoul"
  }
}

resource "aws_db_instance" "this" {
  identifier              = "${local.name}-mysql"
  engine                  = "mysql"
  engine_version          = "8.0"
  instance_class          = var.db_instance_class
  allocated_storage       = var.db_allocated_storage
  max_allocated_storage   = 0 # Storage autoscaling OFF (비용 방어)
  storage_type            = "gp3"
  storage_encrypted       = true
  db_name                 = var.db_name
  username                = var.db_username
  password                = var.db_password
  port                    = 3306
  db_subnet_group_name    = aws_db_subnet_group.this.name
  vpc_security_group_ids  = [aws_security_group.rds.id]
  parameter_group_name    = aws_db_parameter_group.this.name
  publicly_accessible     = false
  multi_az                = false
  backup_retention_period = 7
  backup_window           = "18:00-19:00" # UTC → KST 03:00
  maintenance_window      = "sun:19:00-sun:20:00"
  skip_final_snapshot     = false
  final_snapshot_identifier = "${local.name}-mysql-final"
  deletion_protection     = true
  apply_immediately       = false
  auto_minor_version_upgrade = true

  # Performance Insights: db.t4g.micro 는 미지원이라 비활성화.
  # 더 큰 인스턴스(예: db.t4g.small)로 옮길 때 true + retention 7 활성화.
  performance_insights_enabled = false

  tags = { Name = "${local.name}-mysql" }
}

# =============================================
# CloudWatch Log Group (App 로그용, 3일 보관)
# =============================================
resource "aws_cloudwatch_log_group" "app" {
  name              = "/lms/app"
  retention_in_days = 3
}

# =============================================
# Budgets (Free Tier 초과 감지 + 자동 EC2 Stop)
# =============================================
resource "aws_budgets_budget" "monthly" {
  name         = "${local.name}-monthly"
  budget_type  = "COST"
  limit_amount = tostring(var.monthly_budget_usd)
  limit_unit   = "USD"
  time_unit    = "MONTHLY"

  # ACTUAL: 과금 지연(24~48h) 있지만 현실 확정치
  notification {
    comparison_operator        = "GREATER_THAN"
    threshold                  = 30
    threshold_type             = "PERCENTAGE"
    notification_type          = "ACTUAL"
    subscriber_email_addresses = [var.budget_notification_email]
  }
  notification {
    comparison_operator        = "GREATER_THAN"
    threshold                  = 50
    threshold_type             = "PERCENTAGE"
    notification_type          = "ACTUAL"
    subscriber_email_addresses = [var.budget_notification_email]
  }
  notification {
    comparison_operator        = "GREATER_THAN"
    threshold                  = 70
    threshold_type             = "PERCENTAGE"
    notification_type          = "ACTUAL"
    subscriber_email_addresses = [var.budget_notification_email]
  }

  # FORECASTED: 지연 없이 "이 추세면 도달 예상"을 실시간 추정
  notification {
    comparison_operator        = "GREATER_THAN"
    threshold                  = 50
    threshold_type             = "PERCENTAGE"
    notification_type          = "FORECASTED"
    subscriber_email_addresses = [var.budget_notification_email]
  }
  notification {
    comparison_operator        = "GREATER_THAN"
    threshold                  = 90
    threshold_type             = "PERCENTAGE"
    notification_type          = "FORECASTED"
    subscriber_email_addresses = [var.budget_notification_email]
  }
}

# Budgets 서비스가 EC2 Stop을 대신 실행하기 위한 IAM 역할
resource "aws_iam_role" "budget_action" {
  name = "${local.name}-budget-action-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "budgets.amazonaws.com" }
      Action    = "sts:AssumeRole"
    }]
  })
}

resource "aws_iam_role_policy" "budget_action" {
  name = "${local.name}-budget-action-policy"
  role = aws_iam_role.budget_action.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Action = [
        "ec2:StopInstances",
        "ec2:DescribeInstances",
        "ec2:DescribeInstanceStatus",
        "ssm:StartAutomationExecution",
        "ssm:GetAutomationExecution",
        "ssm:DescribeAutomationExecutions"
      ]
      Resource = "*"
    }]
  })
}

# 70% ACTUAL 도달 시 EC2 자동 정지 (RDS는 계속 실행되지만 db.t4g.micro는 Free Tier)
resource "aws_budgets_budget_action" "stop_ec2" {
  budget_name        = aws_budgets_budget.monthly.name
  action_type        = "RUN_SSM_DOCUMENTS"
  approval_model     = "AUTOMATIC"
  notification_type  = "ACTUAL"
  execution_role_arn = aws_iam_role.budget_action.arn

  action_threshold {
    action_threshold_type  = "PERCENTAGE"
    action_threshold_value = 70
  }

  definition {
    ssm_action_definition {
      action_sub_type = "STOP_EC2_INSTANCES"
      instance_ids    = [aws_instance.app.id]
      region          = var.region
    }
  }

  subscriber {
    address           = var.budget_notification_email
    subscription_type = "EMAIL"
  }
}
