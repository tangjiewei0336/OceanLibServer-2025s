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
        
        # 登录获取 Token
        response = self.client.post(
            f"/userAuth/login?username={self.username}&password={self.password}"
            # json={"username": self.username, "password": self.password}
        )
        
        if response.status_code == 200:
            self.token = response.json()["msg"]
            self.collection_id = None  # 用于存储创建的收藏夹ID
        else:
            self.environment.runner.quit()
            raise Exception("Login failed")

    # ------------------------- /getCollection 测试 -------------------------
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

    # ------------------------- 依赖收藏夹的操作 -------------------------
    # @task(2)
    # def collection_operations(self):
    #     """需要已存在收藏夹的链式操作"""
    #     if not self.collection_id:
    #         return
        
    #     # ------------------------- /changeCollectionInfo -------------------------
    #     # 修改收藏夹信息
    #     update_data = {
    #         "collectionId": self.collection_id,
    #         "newName": f"修改后的收藏夹_{random.randint(1,100)}",
    #         "newDescription": "更新后的描述"
    #     }
    #     self.client.post(
    #         "/collectionService/changeCollectionInfo",
    #         headers={"Authorization": f"{self.token}"},
    #         json=update_data,
    #         name="/changeCollectionInfo"
    #     )
        
    #     # ------------------------- /changeCollectionItem -------------------------
    #     # 修改收藏项（模拟添加项目）
    #     item_data = {
    #         "collectionId": self.collection_id,
    #         "operation": "ADD",
    #         "targetType": "DOC",
    #         "targetId": f"doc_{random.randint(1000,9999)}"
    #     }
    #     self.client.post(
    #         "/collectionService/changeCollectionItem",
    #         headers={"Authorization": f"{self.token}"},
    #         json=item_data,
    #         name="/changeCollectionItem"
    #     )
        
    #     # ------------------------- /getCollectionItemList -------------------------
    #     # 获取收藏项列表
    #     self.client.get(
    #         f"/collectionService/getCollectionItemList?collectionId={self.collection_id}",
    #         headers={"Authorization": f"{self.token}"},
    #         name="/getCollectionItemList"
    #     )

    # ------------------------- /deleteCollection 测试 -------------------------
    @task(2)
    def delete_collection(self):
        """测试删除收藏夹"""
        if not self.collections[self.main_type]:
            return
        # main_type = random.choice(list(MainType)).value
        params = {
            "username": self.username,
            "collectionID": self.collections[self.main_type][-1] if self.collections[self.main_type] else self.collection_id,
            "mainType": self.main_type
        }
            
        with self.client.get(
            f"/collectionService/deleteCollection",
            headers={"Authorization": f"{self.token}"},
            params=params,
            catch_response=True,
            name="/deleteCollection"
        ) as response:
            if response.status_code == 200:
                self.collection_id = None  # 重置收藏夹ID
            else:
                response.failure(f"Delete failed: {response.text}")

    # ------------------------- /deleteCollectionItem 测试 -------------------------
    # @task(2)
    # def delete_collection_item(self):
    #     """测试删除收藏项（需要先存在收藏项）"""
    #     if not self.collection_id:
    #         return
            
    #     # 假设存在一个收藏项
    #     target_id = f"doc_{random.randint(1000,9999)}"
    #     with self.client.get(
    #         f"/collectionService/deleteCollectionItem?collectionId={self.collection_id}&targetId={target_id}",
    #         headers={"Authorization": f"{self.token}"},
    #         catch_response=True,
    #         name="/deleteCollectionItem"
    #     ) as response:
    #         if response.status_code != 200:
    #             response.failure(f"Delete item failed: {response.text}")

# ------------------------- 测试配置 -------------------------
if __name__ == "__main__":
    # 用于本地调试
    import os
    os.system("locust -f this_script.py")