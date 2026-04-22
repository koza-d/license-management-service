#!/usr/bin/env bash
set -euxo pipefail

# ---------- 기본 패키지 ----------
export DEBIAN_FRONTEND=noninteractive
apt-get update -y
apt-get install -y ca-certificates curl gnupg unzip cron

# ---------- Docker 설치 ----------
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
  | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
chmod a+r /etc/apt/keyrings/docker.gpg

ARCH="$(dpkg --print-architecture)"
CODENAME="$(. /etc/os-release && echo "$VERSION_CODENAME")"
echo "deb [arch=${ARCH} signed-by=/etc/apt/keyrings/docker.gpg] \
  https://download.docker.com/linux/ubuntu ${CODENAME} stable" \
  > /etc/apt/sources.list.d/docker.list

apt-get update -y
apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

systemctl enable --now docker
usermod -aG docker ubuntu

# ---------- AWS CLI v2 (백업 업로드용) ----------
curl -sSL "https://awscli.amazonaws.com/awscli-exe-linux-aarch64.zip" -o /tmp/awscliv2.zip
unzip -q /tmp/awscliv2.zip -d /tmp
/tmp/aws/install
rm -rf /tmp/aws /tmp/awscliv2.zip

# ---------- 앱 디렉터리 ----------
mkdir -p /opt/lms
chown ubuntu:ubuntu /opt/lms

# 최초 배포는 GitHub Actions 가 SSH 로 수행.
# /opt/lms/docker-compose.yml, Caddyfile, .env 는 배포 파이프라인이 채움.

# ---------- 타임존 / 시간 동기화 ----------
timedatectl set-timezone Asia/Seoul

# ---------- 스왑 (t4g.small 2GB 대응) ----------
if [ ! -f /swapfile ]; then
  fallocate -l 2G /swapfile
  chmod 600 /swapfile
  mkswap /swapfile
  swapon /swapfile
  echo "/swapfile none swap sw 0 0" >> /etc/fstab
fi

# ---------- unattended-upgrades (보안 패치 자동) ----------
apt-get install -y unattended-upgrades
systemctl enable --now unattended-upgrades

echo "user_data complete" > /var/log/user_data.done
