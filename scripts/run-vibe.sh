#!/usr/bin/env bash
set -euo pipefail
cd /root/myapp
export CAMPUSEVENT_DB_PATH=/root/myapp/data/campusevent.db
exec java \
  -Xms16m -Xmx48m -Xss256k \
  -XX:+UseSerialGC -XX:TieredStopAtLevel=1 \
  -XX:MaxMetaspaceSize=48m -XX:ReservedCodeCacheSize=12m \
  -XX:MaxDirectMemorySize=4m \
  -Duser.timezone=Asia/Shanghai \
  -Djava.security.egd=file:/dev/./urandom \
  -jar campusevent-server.jar \
  --server.address=0.0.0.0 \
  --server.port=80 \
  --server.tomcat.threads.max=10 \
  --server.tomcat.threads.min-spare=2
