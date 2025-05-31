from locust import HttpUser, task, between
import json

class OceanUser(HttpUser):
    wait_time = between(1, 3)  # 用户请求间隔时间

    def on_start(self):
        # 模拟用户登录（获取 JWT Token）
        response = self.client.post("/api/user/login", json={
            "username": "test_user",
            "password": "test123"
        })
        self.token = response.json().get("token")
    
    @task(3)  # 权重为3，更高优先级
    def view_document(self):
        # 测试文档预览（假设文档ID为1）
        self.client.get(
            "/api/docs/1/preview",
            headers={"Authorization": f"Bearer {self.token}"}
        )
    
    @task(1)  # 权重为1，较低优先级
    def upload_document(self):
        # 模拟上传文档（需替换为真实文件路径）
        files = {"file": open("test.pdf", "rb")}
        self.client.post(
            "/api/docs/upload",
            headers={"Authorization": f"Bearer {self.token}"},
            files=files
        )