variable "region" {
  type        = string
  default     = "ap-northeast-2"
  description = "AWS 리전. 서울 기준."
}

variable "environment" {
  type    = string
  default = "prod"
}

variable "project" {
  type    = string
  default = "lms"
}

# -------- EC2 --------
variable "instance_type" {
  type        = string
  default     = "t4g.small"
  description = "ARM Graviton. 크레딧 더 아끼려면 t4g.micro."
}

variable "ssh_allowed_cidr" {
  type        = string
  description = "SSH 접근을 허용할 내 집/사무실 IP. 반드시 /32 로 지정."
  # 예: "203.0.113.45/32"
}

variable "ssh_public_key" {
  type        = string
  description = "EC2 접속용 공개키 (ssh-keygen -t ed25519 로 생성 후 .pub 내용)"
}

variable "root_volume_size" {
  type    = number
  default = 30
}

# -------- RDS --------
variable "db_instance_class" {
  type    = string
  default = "db.t4g.micro"
}

variable "db_allocated_storage" {
  type    = number
  default = 20
}

variable "db_name" {
  type    = string
  default = "lms"
}

variable "db_username" {
  type      = string
  default   = "lmsadmin"
  sensitive = true
}

variable "db_password" {
  type      = string
  description = "RDS 마스터 비밀번호. openssl rand -base64 24 권장."
  sensitive = true
}

# -------- 비용 가드레일 --------
variable "monthly_budget_usd" {
  type        = number
  default     = 35
  description = "AWS Budgets 월 한도(USD). 50/80/95% 임계치에서 이메일 알람."
}

variable "budget_notification_email" {
  type        = string
  description = "예산 알람 수신 이메일"
}
