from locust import HttpUser, task, between
import random
import json

class DocServiceUser(HttpUser):
    wait_time = between(1, 3)
    host = "http://localhost:8080"  # 网关地址
    file_ids = []  # 用于存储获取到的文件ID

    def on_start(self):
        """初始化：使用固定令牌（或动态登录获取）"""
        # 方式1：直接使用Apifox示例中的静态令牌
        self.token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJuZW9zdW5qeiIsInJvbGUiOiJBRE1JTiIsImV4cCI6MTc0OTQ3MDczNCwiaWF0IjoxNzQ3Mzg0MzM0LCJqdGkiOiI2OGY0NTQ4Yy0xOGQzLTRlOWItYmMxMi1kOTI1MWM4ZWEyOWMiLCJ1c2VybmFtZSI6Im5lb3N1bmp6In0.aKtevaCsJO9SRq7-puegNdUFIQknjskCkNQ0965UNbgLDcWjP28KGifHjnuFxHDZg5ceIzoOogGkJzGOGA5t0A"
        
        # 方式2：动态登录获取令牌（如果需要）
        # response = self.client.post("/userAuth/login", params={"username": "neosunjz", "password": "1999sun"})
        # self.token = response.json()["msg"]

    # ------------------------- 请求头配置 -------------------------
    @property
    def common_headers(self):
        """返回与Apifox完全一致的请求头"""
        return {
            "Authorization": self.token
        }

    # ------------------------- /getIndexPageGroups -------------------------
    @task(3)
    def get_index_page_groups(self):
        """测试首页分组数据"""
        with self.client.get(
            "/docClassificationService/getIndexPageGroups",
            headers=self.common_headers,
            name="/getIndexPageGroups",
            catch_response=True
        ) as response:
            # 验证基础响应结构
            if response.status_code == 200:
                try:
                    data = response.json()
                    if "msg" not in data:
                        response.failure("Missing groups field")
                except json.JSONDecodeError:
                    response.failure("Invalid JSON response")
            else:
                response.failure(f"Status code: {response.status_code}")

    # ------------------------- /getTypesByTypeString -------------------------
    @task(4)
    def get_types_by_type_string(self):
        """测试文档类型分类"""
        type_strings = ["doc", "pdf", "ppt"]  # 可扩展更多类型
        params = {
            "typeString": random.choice(type_strings)
        }

        with self.client.get(
            "/docClassificationService/getTypesByTypeString",
            params=params,
            headers=self.common_headers,
            name="/getTypesByTypeStrings",
            catch_response=True
        ) as response:
            if response.status_code != 200:
                response.failure(f"Invalid status code: {response.status_code}")
            
    # ------------------------- /getFileList -------------------------
    @task(5)
    def get_file_list(self):
        """测试文件列表分页查询"""
        params = {
            "username": "neosunjz",
            "pageNum": random.randint(1, 5),  # 模拟随机翻页
            "pageSize": 10,
            "isFolder": random.choice([0, 1])  # 随机查询文件或文件夹
        }

        with self.client.get(
            "/docInfoService/getFileList",
            params=params,
            headers=self.common_headers,
            name="/getFileList",
            catch_response=True
        ) as response:
            if response.status_code == 200:
                try:
                    file_list = response.json().get("files", [])
                    # 记录文件ID用于后续查询
                    if file_list:
                        self.file_ids.extend([f["fileID"] for f in file_list])
                except json.JSONDecodeError:
                    response.failure("JSON parse error")
            else:
                response.failure(f"Request failed: {response.text}")

    # ------------------------- /getFileInfoByFileID -------------------------
    @task(2)
    def get_file_info_by_id(self):
        """测试文件详细信息查询"""
        # if not self.file_ids:
        #     return  # 无可用文件ID时跳过

        params = {
            "fileID": random.randint(1, 10)  # 从已获取的ID中随机选择
        }

        with self.client.get(
            "/docInfoService/getFileInfoByFileID",
            params=params,
            headers=self.common_headers,
            name="/getFileInfoByFileID",
            catch_response=True
        ) as response:
            if response.status_code == 200:
                
                if not "msg" in response.json():
                    response.failure("Missing required fields")
            else:
                response.failure(f"Failed with code: {response.status_code}")