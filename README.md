# Ocean Library System [Ocean 开源文库]
**Ocean 开源文库（微服务版）**

![演示视频](https://github.com/NeoSunJZ/OceanLibServer/raw/refs/heads/master/Ocean%E4%BB%8B%E7%BB%8D%E8%A7%86%E9%A2%91.mp4
)

## 简介

Ocean文库为一个全开源的高校文库系统，实现百度文库的基本功能并基于高校场景进行创新，以期打破垄断，为高校建立专属文库提供支持。是首个开源且能够提供“商业化级”服务的高校文库平台软件。Ocean可使用K8S部署，基于SpringCloud与Vue技术栈，旨在建立一个在校大学生可自由分享、交换学霸笔记、习题试卷、课件资料的开放文库平台。

该系统以文档在线阅览，论坛发帖交流为核心，主要分为文档、论坛、个人中心、管理中心四个子系统，其中文档系统承担用户文档上传下载、在线预览、评论交流、赞踩评分、收藏、文档检索等功能；论坛系统承担用户发帖看帖、跟帖回复、赞踩、收藏、帖子检索等功能；个人中心承担用户基本信息维护、积分钱包、徽章、收藏夹、阅读记录和已发布文档和帖子管理等功能；管理中心承担管理员审核文档帖子、处理举报信息、修改用户积分等功能，以及其他系统维护功能。

考虑到实际需求，Ocean被设计为B/S（RIA）架构，由服务器端和H5手机客户端、电脑Web端构成。服务器端基于Spring Cloud技术采取微服务架构实现，前端则基于VUE技术实现。具体来说，Ocean的前端采用VUE3构建，使用开源UI库为VantUI(手机版)、Antd VUE(网页版)；Ocean的服务器端采用SpringCloud+Spring Boot构建，鉴权框架采用Spring Security+JWT，数据框架为MyBatisPlus、SpringData（主要是MongoDB使用）、分页采用PageHelper，业务包采用PDFBox、iText、Jsoup。文档存储采用阿里云OSS、业务数据存储采用MySQL、搜索数据库采用Solr、结构化数据存储采用MongoDB。


