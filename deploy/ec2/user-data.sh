#!/bin/bash
# EC2 (Amazon Linux 2023) 初次開機設定：Docker + swap + Caddy(HTTPS) + 部署腳本
# 用法：aws ec2 run-instances ... --user-data file://deploy/ec2/user-data.sh
set -euxo pipefail

DOMAIN=3k-api.scolley31.com
REGION=ap-northeast-1
REGISTRY=779846811027.dkr.ecr.ap-northeast-1.amazonaws.com
REPO=legends_of_the_three_kingdoms

# --- swap（t3.micro 只有 1GB RAM）---
if [ ! -f /swapfile ]; then
  dd if=/dev/zero of=/swapfile bs=1M count=2048
  chmod 600 /swapfile
  mkswap /swapfile
  echo '/swapfile none swap sw 0 0' >> /etc/fstab
fi
swapon -a

# --- Docker ---
dnf install -y docker
systemctl enable --now docker
docker network inspect 3k >/dev/null 2>&1 || docker network create 3k

mkdir -p /opt/3k

# --- Caddy：自動申請 Let's Encrypt 憑證，反向代理到後端（含 WebSocket）---
cat > /opt/3k/Caddyfile <<EOF
${DOMAIN} {
  encode gzip
  reverse_proxy 3k-backend:8080
}
EOF

docker rm -f caddy 2>/dev/null || true
docker run -d --name caddy --network 3k --restart unless-stopped \
  -p 80:80 -p 443:443 \
  -v /opt/3k/Caddyfile:/etc/caddy/Caddyfile:ro \
  -v caddy_data:/data -v caddy_config:/config \
  caddy:2

# --- 部署腳本：CI 透過 SSM 執行 /opt/3k/deploy.sh <tag> ---
cat > /opt/3k/deploy.sh <<EOF
#!/bin/bash
set -euo pipefail
TAG=\${1:-latest}
IMAGE=${REGISTRY}/${REPO}:\${TAG}

aws ecr get-login-password --region ${REGION} | docker login --username AWS --password-stdin ${REGISTRY}
docker pull "\${IMAGE}"

docker rm -f 3k-backend 2>/dev/null || true
docker run -d --name 3k-backend --network 3k --restart unless-stopped \\
  -p 127.0.0.1:8080:8080 \\
  -e JAVA_TOOL_OPTIONS="-Xms256m -Xmx512m" \\
  "\${IMAGE}"

# 等後端起來（最多 180 秒）
for i in \$(seq 1 60); do
  CODE=\$(curl -s -o /dev/null -w '%{http_code}' http://127.0.0.1:8080/api-docs || true)
  if [ "\${CODE}" != "000" ]; then
    echo "backend is up: \${IMAGE}"
    docker image prune -af --filter "until=72h" >/dev/null || true
    exit 0
  fi
  sleep 3
done
echo "backend failed to start" >&2
docker logs --tail 100 3k-backend >&2 || true
exit 1
EOF
chmod +x /opt/3k/deploy.sh
