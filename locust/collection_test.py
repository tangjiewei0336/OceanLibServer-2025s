from locust import HttpUser, task, between
import random
import json
from enum import Enum

# ------------------------- 枚举定义 -------------------------
class MainType(Enum):
    NOTE = "NOTE"
    DOCUMENT  = "DOCUMENT"
    

# ------------------------- 用户行为类 -------------------------
class CollectionServiceUser(HttpUser):
    wait_time = between(1, 3)  # 用户操作间隔 1~3 秒
    host = "http://localhost:8080"  # 网关地址

    def on_start(self):
        """用户初始化：登录并获取 Token"""
        # 测试用户凭证（可扩展为从文件读取）
        self.username = f"neosunjz"
        self.password = "1999sun"

        self.collections = {k: [] for k in MainType.__members__.keys()}  # 初始化收藏夹字典
        self.token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJuZW9zdW5qeiIsInJvbGUiOiJBRE1JTiIsImV4cCI6MTc0OTQ3MDczNCwiaWF0IjoxNzQ3Mzg0MzM0LCJqdGkiOiI2OGY0NTQ4Yy0xOGQzLTRlOWItYmMxMi1kOTI1MWM4ZWEyOWMiLCJ1c2VybmFtZSI6Im5lb3N1bmp6In0.aKtevaCsJO9SRq7-puegNdUFIQknjskCkNQ0965UNbgLDcWjP28KGifHjnuFxHDZg5ceIzoOogGkJzGOGA5t0A"
        # # 登录获取 Token
        # response = self.client.post(
        #     f"/userAuth/login?username={self.username}&password={self.password}"
        #     # json={"username": self.username, "password": self.password}
        # )
        
        # if response.status_code == 200:
        #     self.token = response.json()["msg"]
        #     self.collection_id = None  # 用于存储创建的收藏夹ID
        # else:
        #     self.environment.runner.quit()
        #     raise Exception("Login failed")

    # ------------------------- /getCollection 测试 -------------------------
    # 需要测试时将注释取消
    @task(5)
    def get_collection(self):
        """测试获取收藏夹信息"""
        payload = ''
        params = {
            "username": self.username,
            "mainType": random.choice(list(MainType)).value,  # 随机选择一个主类型 
        }
        
        with self.client.get(
            "/collectionService/getCollection",
            headers={"Authorization": f"{self.token}"},
            params={k: v for k, v in params.items() if v is not None},
            catch_response=True,
            name="/getCollection"
        ) as response:
            # 验证响应结构
            if response.status_code != 200:
                response.failure(f"Status code: {response.status_code}")
            elif "collection" not in response.json()["msg"]:
                response.failure("Invalid response structure")
            else:
                collections = response.json()["msg"]["collection"]
                self.collections[params["mainType"]] = [col["collectionID"] for col in collections]
                self.main_type = params["mainType"]


    # ------------------------- /addCollection 测试 -------------------------
    # 需要测试时将注释取消
    # @task(3)
    # def add_collection(self):
    #     """测试创建新收藏夹"""
    #     params = {
    #         "newName": f"collection_{random.randint(1000,9999)}",
    #         "isPublic": "true",  # 注意使用字符串格式
    #         "mainType": random.choice(list(MainType)).value,
    #         "desc": "a%20collection%20for%20test"  # URL编码后的参数
    #     }

    #     # 请求头配置（复制Apifox请求头）
    #     headers = {
    #         "Authorization": self.token,
    #     }
        
    #     with self.client.post(
    #         "/collectionService/addCollection",
    #         headers=headers,
    #         params=params,
    #         catch_response=True,
    #         name="/addCollection"
    #     ) as response:
    #         if response.status_code == 200:
    #             collections = response.json()["msg"]["collection"]
    #             self.collection_id = collections[-1]["collectionID"]
    #             self.main_type = params["mainType"]
    #         else:
    #             response.failure(f"Create failed: {response.text} Status code: {response.status_code}")


    # ------------------------- /deleteCollection 测试 -------------------------
    # 需要测试时将注释取消
    # @task(2)
    # def delete_collection(self):
    #     """测试删除收藏夹"""
    #     if not self.collections[self.main_type]:
    #         return
    #     # main_type = random.choice(list(MainType)).value
    #     params = {
    #         "username": self.username,
    #         "collectionID": self.collections[self.main_type][-1] if self.collections[self.main_type] else self.collection_id,
    #         "mainType": self.main_type
    #     }
            
    #     with self.client.get(
    #         f"/collectionService/deleteCollection",
    #         headers={"Authorization": f"{self.token}"},
    #         params=params,
    #         catch_response=True,
    #         name="/deleteCollection"
    #     ) as response:
    #         if response.status_code == 200:
    #             self.collection_id = None  # 重置收藏夹ID
    #         else:
    #             response.failure(f"Delete failed: {response.text}")

    

# ------------------------- 测试配置 -------------------------
if __name__ == "__main__":
    # 用于本地调试
    import os
    os.system("locust -f this_script.py")