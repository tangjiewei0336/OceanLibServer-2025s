# OceanLib 压力测试文档

## 运行环境要求

- Python 3.8+
- Locust 2.15.1+
- Windows 10/11 操作系统
- Docker & Kubernetes 环境（用于运行微服务）

## 安装步骤

1. 安装 Python 依赖：

```bash
pip install locust
```


## 测试脚本说明

项目包含三个主要测试脚本：

1. user_test.py - 用户服务测试
   - 登录认证
   - 用户名查重
   - 获取用户基本信息
   - 获取用户完整信息

2. collection_test.py - 收藏夹服务测试
   - 获取收藏夹信息
   - 创建收藏夹
   - 删除收藏夹

3. doc_test.py - 文档服务测试
   - 获取首页分组数据
   - 获取文档类型分类
   - 获取文件列表
   - 获取文件详细信息

## 运行测试
1. 启动微服务：  

参考report.md启动微服务：
```cmd
minikube start --driver=docker
kubectl apply -f ./k8s/middle-ware/
kubectl apply -f ./k8s/microservices/
kubectl port-forward service/ocean-nacos 8848:8848
kubectl port-forward service/ocean-gateway 8080:8080
```

2. 启动单个测试：

```bash
cd locust
# 用户服务测试
locust -f user_test.py

# 收藏夹服务测试(推荐)
locust -f collection_test.py

# 文档服务测试
locust -f doc_test.py
```

3. 访问 Web UI：
   - 打开浏览器访问 `http://localhost:8089`
   - 设置并发用户数和生成速率
   - 点击 "Start swarming" 开始测试

## 测试参数配置

所有测试脚本共享以下基本配置：

```python
wait_time = between(1, 3)  # 用户操作间隔1-3秒
host = "http://localhost:8080"  # 网关地址
```

## 注意事项

1. 运行测试前确保：
   - Kubernetes 集群正常运行
   - 所有微服务已部署并可访问
   - Gateway 服务运行在 8080 端口

2. 认证信息：
   - 测试用户名：neosunjz
   - 测试密码：1999sun
   - 或使用预配置的 JWT Token

3. 资源限制：
   - 建议在测试环境中运行
   - 注意监控服务器资源使用情况

## 测试结果分析

Locust Web UI 提供以下关键指标：

- 响应时间统计
- 请求成功/失败率
- RPS (每秒请求数)
- 并发用户数

## 故障排除

1. 连接失败：
   - 检查网关地址是否正确
   - 确认服务是否正常运行

2. 认证错误：
   - 验证 Token 是否过期
   - 检查认证信息是否正确

3. 性能问题：
   - 检查服务器资源使用情况
   - 调整并发用户数和生成速率