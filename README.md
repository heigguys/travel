# 公司旅行管理系统

面向公司内部员工的旅行计划管理与申请系统。管理员维护计划、上传附件、确认申请、回复咨询；普通员工浏览公开计划、提交申请、维护随行人员、导出 PDF。

---

## 技术栈

| 分层 | 技术 |
|------|------|
| 前端 | JSP、HTML、CSS、原生 JavaScript |
| 前端容器 | Tomcat 9.0+ |
| 后端 | Java 17、Spring Boot 3.x |
| 持久层 | MyBatis（注解 SQL） |
| 数据库 | MySQL 5.7+ |
| PDF 导出 | OpenPDF |
| 文件存储 | 本地 `back-end/storage/` 目录 |

---

## 项目结构

```
travel/
├── back-end/                   # Spring Boot 后端
│   └── src/main/
│       ├── java/com/two/backend/
│       │   ├── config/         # CORS 等配置
│       │   ├── controller/     # REST 接口
│       │   ├── dto/            # 请求/响应 DTO
│       │   ├── mapper/         # MyBatis Mapper
│       │   ├── model/          # 数据实体
│       │   └── service/        # 业务逻辑
│       └── resources/
│           ├── schema.sql      # 建表语句
│           ├── data.sql        # 初始测试数据
│           └── application.properties
├── front-end/                  # JSP 前端
│   └── src/main/webapp/
│       ├── assets/
│       │   ├── css/app.css
│       │   └── js/app.js
│       ├── index.jsp           # 登录页
│       └── plans.jsp           # 主功能页
├── 项目式样书.md               # 功能规格文档
├── 接口与表结构详细信息.md     # 接口与数据库文档
└── TESTING.md                  # 测试指南
```

---

## 快速启动

### 前置要求

- Java 17+
- MySQL 5.7+（确保服务已启动）
- Tomcat 9.0+（用于部署前端）

### 第一步：初始化数据库

登录 MySQL，创建数据库：

```sql
CREATE DATABASE db_travel DEFAULT CHARACTER SET utf8mb4;
```

项目启动时会自动执行 `schema.sql` 和 `data.sql`，无需手动导入。

### 第二步：配置数据库密码

编辑 `back-end/src/main/resources/application.properties`：

```properties
spring.datasource.password=你的MySQL密码
```

### 第三步：启动后端

```cmd
cd back-end
mvnw.cmd spring-boot:run
```

看到 `Started BackEndApplication` 说明启动成功，后端运行在 `http://localhost:8080`。

### 第四步：打包前端

新开一个 CMD 窗口：

```cmd
cd front-end
mvnw.cmd package -DskipTests
```

### 第五步：部署前端到 Tomcat

```cmd
# 1. 修改 Tomcat 端口为 8081（避免与后端冲突）
#    编辑 Tomcat/conf/server.xml，将 port="8080" 改为 port="8081"

# 2. 删除 Tomcat 默认首页
rmdir /s /q "Tomcat路径\webapps\ROOT"

# 3. 复制 WAR 包
copy "front-end\target\front-end-1.0-SNAPSHOT.war" "Tomcat路径\webapps\ROOT.war"

# 4. 启动 Tomcat
Tomcat路径\bin\startup.bat
```

### 第六步：访问系统

浏览器打开 `http://localhost:8081`

---

## 默认账号

密码原文均为 `123456`。

| 角色 | 员工编号 | 说明 |
|------|---------|------|
| 管理员 | A001 | 维护计划、回复咨询、导出 PDF |
| 普通员工 | U001 | 申请计划、维护随行人员 |
| 普通员工 | U002 | 申请计划、维护随行人员 |

---

## 主要功能

### 管理员

| 功能 | 说明 |
|------|------|
| 旅行计划 CRUD | 新建、编辑、删除旅行计划 |
| 计划发布/撤销 | 控制员工可见性 |
| PDF 附件上传 | 为计划上传说明 PDF |
| 申请人预览 | 删除计划前查看已申请员工 |
| 咨询回复 | 查看并回复员工咨询 |

### 普通员工

| 功能 | 说明 |
|------|------|
| 浏览公开计划 | 按关键字/状态筛选和排序 |
| 申请计划 | 填写人数和备注 |
| 随行人员管理 | 维护随行人员姓名、性别、身份证、床位 |
| 我的申请 | 查看申请记录、修改、取消 |
| 导出 PDF | 导出个人申请信息为 PDF |
| 发起咨询 | 就某计划向管理员咨询 |

### 数据校验

| 字段 | 规则 |
|------|------|
| 随行人员姓名 | 2~20 字符，仅中文/英文/空格/顿号，离焦提示 |
| 随行人员身份证 | 标准 18 位格式，末位可为 X，离焦提示 |
| 旅行目的地 | 不超过 10 个字符，离焦提示 |
| 日期 | 返回日不早于启程日 |

---

## 旅行计划状态

状态以整数存储在数据库（`tinyint`），系统在以下时机自动刷新：用户登录时、管理员新增或编辑计划时、员工申请或取消时、每天凌晨 0 点定时任务。

| 值 | 状态 | 触发条件 |
|----|------|---------|
| 0 | 可申请 | 出发日未到 且 申请人数 < 定员 |
| 1 | 已成团 | 出发日未到 且 申请人数 ≥ 定员 |
| 2 | 进行中 | 出发日已到 且 返回日未到 |
| 3 | 已结束 | 返回日已过 且 申请人数 ≥ 定员 |
| 4 | 未成团 | 返回日已过 且 申请人数 < 定员 |

---

## 接口概览

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 登录 |
| POST | `/api/auth/logout` | 登出 |
| GET | `/api/plans` | 查询旅行计划列表 |
| POST | `/api/plans` | 新建旅行计划（管理员）|
| PUT | `/api/plans/{id}` | 编辑旅行计划（管理员）|
| POST | `/api/plans/{id}/delete` | 删除旅行计划（管理员）|
| GET | `/api/plans/{id}/delete-preview` | 删除前申请人预览（管理员）|
| GET | `/api/plans/{id}/file` | 下载计划附件 |
| POST | `/api/plans/{id}/apply` | 提交申请 |
| GET | `/api/my-applications` | 我的申请列表 |
| GET | `/api/my-applications/export.pdf` | 导出申请 PDF |
| POST | `/api/applications/{id}/cancel` | 取消申请 |
| GET/POST | `/api/plans/{id}/consultations` | 咨询消息 |
| PUT | `/api/auth/password` | 修改密码 |

详细接口规范见 `接口与表结构详细信息.md`。

---

## 常见问题

**后端启动报 Access Denied**
→ `application.properties` 数据库密码不对

**后端启动报 Port 8080 already in use**
```cmd
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

**浏览器显示 Tomcat 默认页面**
→ `webapps/ROOT` 目录未删干净，关闭 Tomcat 后重新删除再部署

**前端接口跨域报错**
→ 确认后端已启动，且 `application.properties` 中 `app.frontend.allowed-origins` 包含前端地址

---

## 更新日志

### feature/nie-jane-tang

本分支在原有功能基础上合并了组长 Nie 的改进，并补充了 Jane 的设计，共 7 次提交。

**合并自 Nie**

| 改动 | 说明 |
|------|------|
| 数据库字段规范化 | `users.role`、`applications.status`、`consultations.sender_role` 改为 `tinyint` 整数存储，替代原有字符串值 |
| 满员状态自动更新 | 员工申请或取消后，后端即时刷新对应旅行计划的状态 |
| 删除前申请人预览 | 管理员点击删除时，弹窗展示已申请员工名单，防止误删 |
| PDF 状态可读化 | 导出 PDF 中申请状态由原始整数改为"申请成功"/"取消"文字 |

**Nie-Jane 共同**

| 改动 | 说明 |
|------|------|
| 随行人员校验 | 姓名不能含数字，身份证须符合 18 位格式，均为离焦触发气泡提示 |
| 日期分段输入 | 启程日和返回日拆分为年 / 月 / 日三格，输入满位自动跳格，支持左右箭头键跨格移动 |

**Jane**

| 改动 | 说明 |
|------|------|
| 五状态重设计 | 旅行计划状态由文字改为整数（0–4），覆盖可申请 / 已成团 / 进行中 / 已结束 / 未成团全生命周期 |
| 状态自动刷新机制 | 登录时、新增/编辑计划时、申请/取消时即时刷新；另有每天凌晨 0 点定时任务兜底 |

---

## 相关文档

- `项目式样书.md` — 功能规格与业务逻辑
- `接口与表结构详细信息.md` — 接口与数据库表结构
- `TESTING.md` — 详细测试步骤与测试清单
