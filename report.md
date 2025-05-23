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



## 微服务构建与 Kubernetes 调度部分

本项目为一个基于 Spring Boot 和 Nacos 的微服务应用系统。初始形态为本地开发和部署环境下的微服务集群，缺乏标准的云原生特性。为实现云原生化部署，本部分依次完成了微服务模块化、容器化、服务注册与发现配置优化，以及 Kubernetes 环境中的调度与服务编排工作。
（服务实现的功能这里略，在仓库中有介绍）
---

### 一、项目模块划分与打包构建

项目原生为多个微服务模块组成，但未考虑在容器化或 Kubernetes 环境中的部署可行性。因此，首先对项目进行如下改造与构建：

#### 1. Maven 打包配置

通过编写各微服务模块的 `pom.xml` 文件，采用 Maven 工具对每个模块分别进行独立打包，产出对应的 `.jar` 可执行包。构建目标为每个微服务独立构建、独立部署。

#### 2. 云原生特性适配

* **Nacos 服务注册调整**：将原先硬编码为 `localhost` 的 Nacos 地址统一替换为 Kubernetes 内部的服务 DNS 名称（ `nacos-service`），从而支持基于集群 DNS 的服务发现机制。
* **端口固定化**：每个服务的端口在配置中进行明确绑定，确保在容器化环境中能准确映射并进行 `Service` 暴露。

---

### 二、Dockerfile 编写与镜像构建

#### 1. Dockerfile 编写原则

为每个微服务编写独立的 Dockerfile，基础镜像统一采用 `openjdk:8-jre-alpine`，以保证镜像体积小、启动速度快、运行稳定。每个 Dockerfile 的结构如下：

```dockerfile
FROM openjdk:8-jdk-alpine
COPY target/ocean-comment-service-1.0-SNAPSHOT.jar /app/ocean-comment-service-1.0-SNAPSHOT.jar
WORKDIR /app
EXPOSE 8082
ENTRYPOINT ["java","-Dspring.profiles.active=dev","-jar","ocean-comment-service-1.0-SNAPSHOT.jar"]
```

#### 2. Docker 镜像构建命令

在每个模块根目录下执行以下命令进行镜像构建，共计 9 个镜像：

```bash
boyuzhu4216/ocean-collection-service:v1.0
boyuzhu4216/ocean-comment-service:v1.0
boyuzhu4216/ocean-docs-service:v1.0
boyuzhu4216/ocean-gateway:v1.0
boyuzhu4216/ocean-note-service:v1.0
boyuzhu4216/ocean-notify-service:v1.0
boyuzhu4216/ocean-user-behavior-service:v1.0
boyuzhu4216/ocean-user-service:v1.0
boyuzhu4216/ocean-nacos:v1.0

```

#### 3. 镜像上传

使用 `docker push` 将所有构建好的镜像推送至 Docker Hub 远程仓库（`boyuzhu4216/`），以供 Kubernetes 后续拉取：



---

### 三、容器化验证测试

在 Kubernetes 上部署前，使用本地 `Docker` 网络下`ocean-network`子网络进行模拟微服务环境，验证容器间联通性与服务可用性：

1. 创建一个自定义的 Docker 网络，以模拟 Kubernetes 中服务 Pod 之间的统一网络层，确保各服务容器处于相同的逻辑子网内，实现基于容器名称的互相访问：
2. Nacos 运行成功后，将暴露 8848 端口供服务注册与调用，同时由于网络统一，其他容器可以通过`ocean-nacos:8848` 直接访问
3. 使用 `apifox` 对外部接口进行调试，确保服务间依赖链条未因容器化中断。
4. 验证服务注册至本地部署的 Nacos 实例，完成服务注册与发现。

---

### 四、Kubernetes 构建与部署

#### 1. Deployment 与 Service 编排

为每个服务编写一套 Kubernetes `Deployment` 与 `Service` 文件：

* **Deployment 文件**：定义副本数、镜像地址、环境变量（如 Nacos 地址）、端口暴露等。
* **Service 文件**：类型设为 `ClusterIP`，实现微服务在 Kubernetes 集群内的可发现性。

存在在 /k8s/文件中
分为两个部分 /mircroservices/ 和 /middle-ware/两大部分

以`ocean-gateway`作为示例：

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ocean-gateway
  labels:
    app: ocean-gateway
spec:
  replicas: 1
  selector:
    matchLabels:
      app: ocean-gateway
  template:
    metadata:
      labels:
        app: ocean-gateway
    spec:
      containers:
        - name: ocean-gateway
          image: boyuzhu4216/ocean-gateway:v1.0
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: 8080
---
apiVersion: v1
kind: Service
metadata:
  name: ocean-gateway
spec:
  selector:
    app: ocean-gateway
  ports:
    - protocol: TCP
      port: 8080
      targetPort: 8080
  type: ClusterIP

```

#### 2. Nacos 服务构建与适配

Nacos 的服务注册端口不止 8848，额外需要开放内部通信端口（如 9848/9849）。因此，Nacos 的 Service 需开放如下端口：

```yaml
ports:
  - port: 8848
  - port: 9848
  - port: 9849
  - port: 7848
```

#### 3. 集群部署与调试

* 使用 `minikube` 模拟本地 Kubernetes 集群。
* 使用 `kubectl apply -f` 将各服务的资源清单部署到集群。
* 利用 `kubectl exec` 和 `kubectl logs` 查看服务运行状态。
* 验证 Nacos 服务是否正确注册所有微服务节点。

---

### 五、实施过程中遇到的问题与解决方案

#### 主要问题：微服务与 Nacos之间的发现与通信问题

**原因**：

1. 在容器环境中，nacos运行的地址不再可知，因此无法通过硬编码的方式进行服务注册与发现。
2. Nacos 在 Kubernetes 中若未开放全部需要的通信端口，注册与发现会失败。但在docker同一子网下，容器之间端口自动可连通，所以在docker下不是问题，但是在k8s环境中会出现问题


**解决**：
1. 使用服务名称`ocean-nacos`来进行nacos服务的发现。同时每个 `Deployment` 设置正确的注册地址。
2. 简单docker环境中，我们将容器部署在同一子网下。在k8s环境中，我们显式开放nacos所需要的全部端口，便于其它服务进行发现和连通。
