# oauth2 mock server

在测试 oauth2 client 应用的时候的，oauth2 mock server 方便完成 oauth2 client 的开发。

使用 [sa-token oauth2-server](https://sa-token.cc/doc.html#/oauth2/readme) 的[示例](https://github.com/dromara/Sa-Token/tree/dev/sa-token-demo/sa-token-demo-oauth2/sa-token-demo-oauth2-server)修改而来。感谢 sa-token 项目。

## 快速开始

### 生成密钥/公钥

* 私钥用于 jwt 签名，输出 id_token
* 公钥用于 jwks.json。

```bash
# 演示目录
mkdir ~/oauth2-server
cd ~/oauth2-server

# 保存私钥/公钥使用 
mkdir -p pem

# 参考：https://tongqijie.com/post/rs256-rsa-with-sha-256-si-yao-he-gong-yao-sheng-cheng-fang-fa-zjc22re2
# 生成私钥
openssl genrsa -out pem/rs256_private_key.pem 2048

# 生成公钥
# 用于 http://sa-oauth-server.com:9080/.well-known/openid-configuration 返回 jwks_uri 连接返回 jwks.json 内容。
openssl rsa -pubout -in pem/rs256_private_key.pem -out pem/rs256_public_key.pem
```

### 启动 oauth2 server

#### 配置 mock 用户

```bash
cd ~/oauth2-server
mkdir -p mock
cd mock

# 在 users.json 保存登录用户
vi users.json
```

users.json 文件内容格式如下，按自己的需求修改：

```json
[
  {
    "username": "user_xxx",
    "password": "pw_yyy_change_me",
    "nickname": "这里写昵称",
    "avatar": "http://xxx.com/avatar.jpg",
    "email": "admin@example.com"
  }
]
```

#### 启动 docker

```bash
# 也可以自定义 client 配置
# -v ~/oauth2-server/mock/clients.json:/app/mock/clients.json \

docker run --name oauth2-mock-server -d \
    -p 9080:9080 -p 8000:8000 \
    -v ~/oauth2-server/mock/users.json:/app/mock/users.json \
    -v ~/oauth2-server/pem:/app/pem \
    chenlb/oauth2-server:0.0.3
```

默认 clients.json 文件内容如下：
```json
[
  {
    "client_id": "my_client_id_1001",
    "client_secret": "aaaa-bbbb-cccc-dddd-eeee",
    "allow_redirect_uris": [
      "*"
    ],
    "contract_scopes": ["openid","email","profile","oidc","unionid"],
    "allow_grant_types": [
      "authorization_code",
      "refresh_token"
    ],
    "subject_id": "my_org_id_001"
  }
]
```

### 打开 H5 oauth2 client 测试界面

设置 hosts 文件，将 sa-oauth-server.com 映射到本机 ip （推荐内网 ip，不是 127.0.0.1），如：
```bash
# vi /etc/hosts
# 假设内网 ip 为 192.168.0.10 
192.168.0.10       sa-oauth-server.com
```

H5 oauth2 client 测试界面地址：http://sa-oauth-server.com:9080/client/oauth2-client.html

## 开发说明

### 本地构建 docker 镜像

设置 maven 镜像仓库地址，打开 docker/setting.sh 文件，aliyun 镜像仓库地址去掉注释：

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.2.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.2.0 https://maven.apache.org/xsd/settings-1.2.0.xsd">
    <!-- ... -->
    <!-- 去掉 159 行 和 166 行的注释内容 -->
    <!-- 159 行
    <mirror>
      <id>aliyunmaven</id>
      <mirrorOf>*</mirrorOf>
      <name>阿里云公共仓库</name>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
    -->
    <!-- ... -->
</settings>
```
