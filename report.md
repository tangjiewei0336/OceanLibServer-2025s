所有微服务容器都挂载到一个名为ocean-network的网络中
docker network create ocean-network
运行逻辑，gateway是流量的入口，需要打开8080端口
gateway注册到了nacos上，nacos负责服务注册和分发，需要打开8848端口
其它微服务容器不需要打开 端口自动在自网络中发现和注册即可

构建镜像






docker build -t boyuzhu4216/ocean-collection-service:v1.0 .
docker build -t boyuzhu4216/ocean-comment-service:v1.0 .
docker build -t boyuzhu4216/ocean-docs-service:v1.0 .
docker build -t boyuzhu4216/ocean-gateway:v1.0 .
docker build -t boyuzhu4216/ocean-note-service:v1.0 .
docker build -t boyuzhu4216/ocean-notify-service:v1.0 .
docker build -t boyuzhu4216/ocean-user-behavior-service:v1.0 .
docker build -t boyuzhu4216/ocean-user-service:v1.0 .


nacos
docker run -d -p 8848:8848 --network ocean-network --name ocean-nacos boyuzhu4216/ocean-nacos:v1.0
docker run -d -p 8080:8080 --network ocean-network --name ocean-gateway boyuzhu4216/ocean-gateway:v1.0
docker run -d --network ocean-network --name ocean-note-service boyuzhu4216/ocean-note-service:v1.0


docker run -d--network ocean-network --name ocean-user-service boyuzhu4216/ocean-user-service:latest
docker run -d--network ocean-network --name ocean-file-service boyuzhu4216/ocean-file-service:latest
docker run -d--network ocean-network --name ocean-notify-service boyuzhu4216/ocean-notify-service:latest
docker run -d--network ocean-network --name ocean-activity-service boyuzhu4216/ocean-activity-service:latest
docker run -d--network ocean-network --name ocean-order-service boyuzhu4216/ocean-order-service:latest
docker run -d--network ocean-network --name ocean-search-service boyuzhu4216/ocean-search-service:latest
docker run -d--network ocean-network --name ocean-activity-service boyuzhu4216/ocean-activity-service:latest
docker run -d--network ocean-network --name ocean-file-service boyuzhu4216/ocean-file-service:latest
docker run -d--network ocean-network --name ocean-notify-service boyuzhu4216/ocean-notify-service:latest
docker run -d--network ocean-network --name ocean-order-service boyuzhu4216/ocean-order-service:latest