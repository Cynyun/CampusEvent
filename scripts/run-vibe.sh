#!/usr/bin/env bash
set -euo pipefail
cd /root/myapp
export CAMPUSEVENT_DB_PATH=/root/myapp/data/campusevent.db
exec java \
  -Xms128m -Xmx512m -Xss512k \
  -XX:+UseG1GC -XX:MaxGCPauseMillis=200 \
  -XX:MaxMetaspaceSize=192m -XX:ReservedCodeCacheSize=48m \
  -XX:MaxDirectMemorySize=64m \
  -Duser.timezone=Asia/Shanghai \
  -Djava.security.egd=file:/dev/./urandom \
  -jar campusevent-server.jar \
  --server.address=0.0.0.0 \
  --server.port=80 \
  --server.tomcat.threads.max=50 \
  --server.tomcat.threads.min-spare=5
