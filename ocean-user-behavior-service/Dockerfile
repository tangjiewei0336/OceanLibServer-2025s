FROM openjdk:8-jdk-alpine

# 将jar包复制到镜像中
COPY target/ocean-user-behavior-service-1.0-SNAPSHOT.jar /app/ocean-user-behavior-service-1.0-SNAPSHOT.jar

# 设置工作目录
WORKDIR /app

# 暴露端口
EXPOSE 8088

# 运行jar包，使用java命令启动
ENTRYPOINT ["java","-Dspring.profiles.active=dev","-jar","ocean-user-behavior-service-1.0-SNAPSHOT.jar"]