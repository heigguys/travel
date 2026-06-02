# 测试指南 - 公司旅行管理系统

## 环境要求

| 依赖 | 版本要求 | 说明 |
|------|---------|------|
| Java | 17+ | 注意不能用 Java 8 |
| MySQL | 5.7+ | 需要知道 root 密码 |
| Tomcat | 9.0+ | 用于运行前端 |

---

## 第一步：配置数据库密码

用记事本打开配置文件：

```
notepad "back-end\src\main\resources\application.properties"
```

找到这行，改成你的 MySQL 密码：

```
spring.datasource.password=你的密码
```

保存关闭。

---

## 第二步：启动后端

打开 CMD，进入后端目录：

```cmd
cd back-end
mvnw.cmd spring-boot:run
```

看到以下内容说明启动成功，**不要关这个窗口**：

```
Started BackEndApplication in x.xxx seconds
```

后端运行在 `http://localhost:8080`

---

## 第三步：打包前端

**重新打开一个新的 CMD 窗口**，进入前端目录：

```cmd
cd front-end
mvnw.cmd package -DskipTests
```

---

## 第四步：部署前端到 Tomcat

**4.1 修改 Tomcat 端口（避免和后端 8080 冲突）**

用记事本打开：

```
notepad "你的Tomcat路径\conf\server.xml"
```

找到：
```xml
<Connector port="8080"
```
改成：
```xml
<Connector port="8081"
```
保存关闭。

**4.2 删除 Tomcat 默认首页**

```cmd
rmdir /s /q "你的Tomcat路径\webapps\ROOT"
```

**4.3 复制 WAR 包**

```cmd
copy "front-end\target\front-end-1.0-SNAPSHOT.war" "你的Tomcat路径\webapps\ROOT.war"
```

**4.4 启动 Tomcat**

```cmd
你的Tomcat路径\bin\startup.bat
```

---

## 第五步：打开浏览器测试

访问：`http://localhost:8081`

### 测试账号

| 角色 | 员工编号 | 密码 |
|------|---------|------|
| 管理员 | A001 | 123456 |
| 普通用户 | U001 | 123456 |
| 普通用户 | U002 | 123456 |

---

## 功能测试清单

### 管理员（A001）

- [ ] 登录成功，进入管理界面
- [ ] 查看全部 8 条旅行计划（含未发布的）
- [ ] 新建旅行计划
- [ ] 发布 / 撤销计划
- [ ] 回复用户咨询

### 普通用户（U001）

- [ ] 登录成功，只能看到已发布的计划
- [ ] 申请旅行计划
- [ ] 添加随行人员
- [ ] 查看"我的申请"
- [ ] 导出申请 PDF
- [ ] 取消申请
- [ ] 发起咨询

---

## 常见问题

**后端启动报 Access Denied**
→ `application.properties` 里的数据库密码不对，改成你的 MySQL 密码

**后端启动报 Port 8080 already in use**
→ 有程序占用了 8080 端口，管理员身份运行 CMD 执行：
```cmd
netstat -ano | findstr :8080
taskkill /PID 对应的PID /F
```

**浏览器打开是 Tomcat 默认页面**
→ webapps\ROOT 目录没删干净，关闭 Tomcat，删掉 ROOT 目录再重启

**页面打开但接口报错**
→ 检查后端是否还在运行（后端 CMD 窗口不能关）
