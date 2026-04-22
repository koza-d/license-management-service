output "ec2_public_ip" {
  description = "Cloudflare DNS 의 A 레코드에 입력할 고정 IP"
  value       = aws_eip.app.public_ip
}

output "ec2_instance_id" {
  value = aws_instance.app.id
}

output "rds_endpoint" {
  description = ".env 의 DB_HOST 에 입력"
  value       = aws_db_instance.this.address
}

output "rds_port" {
  value = aws_db_instance.this.port
}

output "backup_bucket" {
  description = ".env 의 BACKUP_BUCKET 에 입력"
  value       = aws_s3_bucket.backup.id
}

output "ssh_command_hint" {
  description = "접속 예시. ssh_public_key 에 대응하는 프라이빗키 사용."
  value       = "ssh -i ~/.ssh/id_ed25519 ubuntu@${aws_eip.app.public_ip}"
}
