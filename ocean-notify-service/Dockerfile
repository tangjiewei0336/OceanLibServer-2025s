FROM openjdk:8-jdk-alpine

# 将jar包复制到镜像中
COPY target/ocean-notify-service-1.0-SNAPSHOT.jar /app/ocean-notify-service-1.0-SNAPSHOT.jar

# 设置工作目录
WORKDIR /app

# 暴露端口
EXPOSE 8087

# 运行jar包，使用java命令启动
ENTRYPOINT ["java","-Dspring.profiles.active=dev","-jar","ocean-notify-service-1.0-SNAPSHOT.jar"]