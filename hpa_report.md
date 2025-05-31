# HPA实现操作

**启动指令**：  

```cmd
minikube start --driver=docker
kubectl apply -f ./k8s/middle-ware/
kubectl apply -f ./k8s/microservices/
kubectl apply -f ./k8s/hpa.yaml
```  

在一个终端：  

```cmd
kubectl port-forward service/ocean-nacos 8848:8848
```

在另一个终端：

``` cmd  
cd ./k8s/port_forward/
./start-port-forward.bat
```

**其他可能会用到的指令**：
1.撤销部署HPA： 

```cmd
kubectl delete hpa ocean-collection-hpa
kubectl delete hpa ocean-comment-hpa
kubectl delete hpa ocean-docs-hpa
kubectl delete hpa ocean-gateway-hpa
kubectl delete hpa ocean-note-hpa
kubectl delete hpa ocean-notify-hpa
kubectl delete hpa ocean-user-behavior-hpa
kubectl delete hpa ocean-user-hpa
```  

2.安装部署Metrics Server
```cmd

kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
```

## HPA具体实现
首先，给每个容器设限

```yaml
        resources:
            requests:
              cpu: "150m"   
              memory: "256Mi" 
            limits:
              cpu: "600m"  
              memory: "512Mi" 

```

