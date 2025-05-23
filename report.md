# k8s 构建部分内容

这个项目的目的见项目的仓库的项目介绍。这里不多解释。


项目的启动逻辑 nacos作为中间件先启动 -> 其它微服务在它之后启动。共一个中间件nacos，8个微服务。ocean-common是公共模块。微服务和中间件镜像我已经上传到了仓库。

项目的通信逻辑，ocean-gateway是流量入口，分发流量到微服务，打开8080端口。  
nacos实现服务注册和发现，需要打开8848端口，进行可视化

启动指令：
```cmd
minikube start --driver=docker
kubectl apply -f ./k8s/middle-ware/
kubectl apply -f ./k8s/microservices/
kubectl port-forward service/ocean-nacos 8848:8848
kubectl port-forward service/ocean-gateway 8080:8080
```

---


可能会用到的指令，如果需要重新构建镜像 or 通过简单通过docker进行测试

构建镜像：

nacos目录下
```cmd
docker build -t boyuzhu4216/ocean-nacos:v1.0 .
```
OceanLib根目录下
```cmd
cd ocean-collection-service
docker build -t boyuzhu4216/ocean-collection-service:v1.0 .
cd ../ocean-comment-service
docker build -t boyuzhu4216/ocean-comment-service:v1.0 .
cd ../ocean-docs-service
docker build -t boyuzhu4216/ocean-docs-service:v1.0 .
cd ../ocean-gateway
docker build -t boyuzhu4216/ocean-gateway:v1.0 .
cd ../ocean-note-service
docker build -t boyuzhu4216/ocean-note-service:v1.0 .
cd ../ocean-notify-service
docker build -t boyuzhu4216/ocean-notify-service:v1.0 .
cd ../ocean-user-behavior-service
docker build -t boyuzhu4216/ocean-user-behavior-service:v1.0 .
cd ../ocean-user-service
docker build -t boyuzhu4216/ocean-user-service:v1.0 .
```

使用docker的运行


```cmd
docker run -d -p 8848:8848 --network ocean-network --name ocean-nacos boyuzhu4216/ocean-nacos:v1.0
docker run -d -p 8080:8080 --network ocean-network --name ocean-gateway boyuzhu4216/ocean-gateway:v1.0


docker run -d --network ocean-network --name ocean-collection-service boyuzhu4216/ocean-collection-service:v1.0
docker run -d --network ocean-network --name ocean-comment-service boyuzhu4216/ocean-comment-service:v1.0
docker run -d --network ocean-network --name ocean-docs-service boyuzhu4216/ocean-docs-service:v1.0
docker run -d --network ocean-network --name ocean-note-service boyuzhu4216/ocean-note-service:v1.0
docker run -d --network ocean-network --name ocean-notify-service boyuzhu4216/ocean-notify-service:v1.0
docker run -d --network ocean-network --name ocean-user-behavior-service boyuzhu4216/ocean-user-behavior-service:v1.0
docker run -d --network ocean-network --name ocean-user-service boyuzhu4216/ocean-user-service:v1.0

```





