# ============================================================
# Dockerfile — 短链接管理系统后端
# 多阶段构建：Maven 编译 → JRE 运行
#
# 构建镜像：
#   docker build -t zsc-server:latest .
#
# 运行容器：
#   docker run -p 8080:8080 --name zsc-server zsc-server:latest
# ============================================================

# ---------- Stage 1: Maven 构建 ----------
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# 1. 先拷贝依赖声明文件，利用 Docker 层缓存加速
COPY pom.xml ./
COPY zsc-admin/pom.xml ./zsc-admin/
COPY zsc-common/pom.xml ./zsc-common/
COPY zsc-framework/pom.xml ./zsc-framework/
COPY zsc-system/pom.xml ./zsc-system/
COPY zsc-quartz/pom.xml ./zsc-quartz/
COPY zsc-generator/pom.xml ./zsc-generator/
COPY zsc-module/pom.xml ./zsc-module/

# 2. 下载所有依赖（源码未变时缓存此层）
RUN mvn dependency:go-offline -B -DskipTests || true

# 3. 拷贝全部源码
COPY . .

# 4. 编译打包（跳过测试）
RUN mvn clean package -DskipTests -B

# ---------- Stage 2: 运行时 ----------
# 复用 maven 镜像中的 JDK，避免额外拉取 eclipse-temurin 镜像
FROM maven:3.9-eclipse-temurin-17 AS runtime

# 维护标签
LABEL maintainer="zsc" \
      description="短链接管理系统后端" \
      version="3.9.1"

# 从 maven 镜像中提取 JDK 环境
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH=$JAVA_HOME/bin:$PATH

# 安装运行时必备工具（curl 用于健康检查，tzdata 用于时区）
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl tzdata && \
    ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    rm -rf /var/lib/apt/lists/*

# 应用工作目录
WORKDIR /app

# 从构建阶段拷贝 JAR
COPY --from=builder /build/zsc-admin/target/zsc-admin.jar ./app.jar

# 日志挂载点
VOLUME ["/app/logs"]

# 健康检查
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD curl -sf http://localhost:8080/actuator/health || exit 1

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.profiles.active=druid,docker", \
    "-jar", "/app/app.jar"]