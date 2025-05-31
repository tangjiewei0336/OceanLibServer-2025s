from locust import HttpUser, task, between
import random
import json

class UserServiceUser(HttpUser):
    wait_time = between(1, 3)
    host = "http://localhost:8080"  # 网关地址
    
    def on_start(self):
        """初始化：执行登录并获取令牌"""
        self.username = "neosunjz"
        self.password = "1999sun"
        self.token = None
        
        # ------------------------- /login 接口 -------------------------
        with self.client.post(
            url="/userAuth/login",
            params={"username": self.username, "password": self.password},
            headers=self.headers(include_auth=False),  # 登录时不带token
            name="/login",
            catch_response=True
        ) as response:
            if response.status_code == 200:
                self.token = response.json().get("msg")
            else:
                self.environment.runner.quit()
                response.failure("登录失败")

    # ------------------------- 公共头配置 -------------------------
    def headers(self, include_auth=True):
        """生成与Apifox完全一致的请求头"""
        headers = {
            
        }
        if include_auth and self.token:
            headers["Authorization"] = self.token
        return headers

    # ------------------------- /checkSameUsername -------------------------
    @task(4)
    def check_username(self):
        """检查用户名是否重复"""
        # 80%概率检查已存在用户，20%随机生成新用户名
        username = self.username if random.random() < 0.8 else f"user{random.randint(100000, 999999)}"
        
        with self.client.get(
            url="/userInfoService/checkSameUsername",
            params={"username": username},
            headers=self.headers(),
            name="/checkSameUsername",
            catch_response=True
        ) as response:
            # 验证标准响应格式
            if response.status_code != 200:
                response.failure(f"状态码异常: {response.status_code}")
            

    # ------------------------- /getUserLimitedInfo -------------------------
    @task(3)
    def get_limited_info(self):
        """获取用户有限信息"""
        with self.client.get(
            url="/userInfoService/getUserLimitedInfo",
            params={"username": self.username},
            headers=self.headers(),
            name="/getUserLimitedInfo",
            catch_response=True
        ) as response:
            required_fields = ["nickname", "avatar", "realname"]
            if response.status_code == 200:
                if not all(field in response.json()["msg"] for field in required_fields):
                    response.failure("响应字段不完整")
            else:
                response.failure(f"请求失败: {response.text}")

    # ------------------------- /getUserAllInfo -------------------------
    @task(2)
    def get_full_info(self):
        """获取用户完整信息"""
        with self.client.get(
            url="/userInfoService/getUserAllInfo",
            headers={**self.headers(), "username": self.username},  # 附加username头
            name="/getUserAllInfo",
            catch_response=True
        ) as response:
            sensitive_fields = ["email", "phoneNum", "wallet"]
            if response.status_code == 200:
                if not all(field in response.json()["msg"] for field in sensitive_fields):
                    response.failure("敏感信息字段缺失")
            else:
                response.failure(f"认证失败: {response.status_code}")

# ------------------------- 测试执行 -------------------------
if __name__ == "__main__":
    import os
    os.system("locust -f user_test.py")