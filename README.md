# 公司旅行管理系统修改说明

## 本次主要改动

### 1. 旅行计划编辑校验

涉及文件：

- `front-end/src/main/webapp/assets/js/app.js`
- `front-end/src/main/webapp/plan-edit.jsp`
- `front-end/src/main/webapp/assets/css/app.css`

改动内容：

- 管理员编辑旅行计划时，目的地、启程日、返回日、价格、定员数、PDF 附件支持失焦后即时校验。
- 启程日和返回日会分别显示具体错误，不再统一显示笼统日期错误。
- 返回日早于启程日时，会根据当前修改的日期字段显示对应提示。
- 去掉浏览器原生校验气泡，避免气泡抢焦点导致日期月份无法点击。
- PDF 上传改为只允许点击“选择PDF”按钮触发，文件名显示区域不再触发上传。
- 编辑已有计划时，如果已上传 PDF，会显示原 PDF 文件名。

### 2. 公开计划编辑限制

涉及文件：

- `front-end/src/main/webapp/assets/js/app.js`
- `front-end/src/main/webapp/assets/css/app.css`
- `back-end/src/main/java/com/two/backend/service/TravelPlanService.java`

改动内容：

- 管理员一览页中，未公开计划可以编辑。
- 已公开计划的编辑按钮显示为灰色禁用状态，不可点击。
- 直接访问公开计划编辑 URL 时，前端会提示并返回列表。
- 后端更新接口增加校验，已公开计划不可编辑。

### 3. 删除计划前邮件通知

涉及文件：

- `front-end/src/main/webapp/plans.jsp`
- `front-end/src/main/webapp/assets/js/app.js`
- `back-end/src/main/java/com/two/backend/controller/TravelPlanController.java`
- `back-end/src/main/java/com/two/backend/service/TravelPlanService.java`
- `back-end/src/main/java/com/two/backend/service/MailNotificationService.java`
- `back-end/pom.xml`
- `back-end/src/main/resources/application.properties`

改动内容：

- 删除旅行计划前会先查询是否已有员工申请。
- 如果无人申请，管理员可直接确认删除。
- 如果已有员工申请，弹窗显示：
  `已经有员工申请本计划。如需删除本计划，请先发送邮件通知员工。`
- 弹窗会展示已申请员工姓名和邮箱。
- 点击“邮件通知”后，系统会给所有已申请员工发送旅游计划取消通知邮件。
- 邮件发送成功后，后端再执行旅行计划删除。
- 点击“取消”则关闭弹窗，不执行删除。
- 后端普通删除接口增加保护：已有申请时不能直接删除，必须先走邮件通知后删除流程。

### 4. 页面 UI 视觉升级

涉及文件：

- `front-end/src/main/webapp/assets/css/app.css`
- `front-end/src/main/webapp/pdf-viewer.jsp`

改动内容：

- 在 `app.css` 末尾新增企业级视觉覆盖层，统一系统主色、字体、背景、边框、圆角、阴影和控件状态。
- 页面背景由单一浅灰改为更细腻的蓝白灰背景，并加入低透明网格纹理，整体更接近企业后台系统。
- 登录页卡片增加品牌色顶部条、阴影、入场动画和更清晰的层级。
- 旅游计划一览页的顶部栏、筛选栏、表格容器、分页区域改为卡片化样式。
- 表格表头、行 hover、链接、排序表头和操作图标统一调整，提升可读性和操作反馈。
- 普通按钮、主按钮、危险按钮、禁用按钮、输入框、下拉框、文本域统一交互状态，包括 hover、focus、active 效果。
- 弹窗增加更大的阴影、圆角、遮罩模糊和弹出动画。
- Toast 提示增加阴影、圆角和滑入动画。
- 表单页、旅行计划编辑页、申请页摘要卡片、上传区域、随行人员行等区域统一边框、背景和阴影风格。
- 增加 `prefers-reduced-motion` 适配，用户系统设置减少动画时会自动弱化动画。
- 优化移动端按钮、顶部栏、工具栏和弹窗底部操作区的排布。
- `pdf-viewer.jsp` 内联样式同步升级，PDF 预览页的工具栏、按钮、背景和 iframe 容器与主系统视觉保持一致。

## 默认用户信息

初始化用户位于：

- `back-end/src/main/resources/data.sql`

当前默认用户：

| 用户ID | 员工编号 | 姓名 | 邮箱 | 角色 | 默认密码 |
| --- | --- | --- | --- | --- | --- |
| 1 | 聂宁波 | 聂宁波 | 2190058893@qq.com | 管理员 | 123456 |
| 2 | 唐笑松 | 唐笑松 | 2837069797@qq.com | 普通用户 | 123456 |
| 3 | 史简 | 史简 | 1466648412@qq.com | 普通用户 | 123456 |

说明：

- `role=0` 表示管理员。
- `role=1` 表示普通用户。
- 默认密码 `123456` 的 MD5 值是 `e10adc3949ba59abbe56e057f20f883e`。
- `data.sql` 使用 `insert ignore`，如果数据库中已有同 ID 或同员工编号的数据，重启服务不会覆盖原有数据。

## 默认邮件参数

配置文件位于：

- `back-end/src/main/resources/application.properties`

当前默认发件人：

```properties
app.mail.from=2837069797@qq.com
```

如果需要真正发送邮件，还需要配置 QQ 邮箱 SMTP。示例：

```properties
spring.mail.host=smtp.qq.com
spring.mail.port=465
spring.mail.username=2837069797@qq.com
spring.mail.password=QQ邮箱SMTP授权码
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.ssl.enable=true
app.mail.from=2837069797@qq.com
```

注意：

- `spring.mail.password` 不是 QQ 登录密码，而是 QQ 邮箱开启 SMTP 服务后生成的授权码。
- 如果不配置 `spring.mail.host`，系统可以正常启动，但点击“邮件通知”会提示邮件服务未配置。
- 修改邮件配置后，需要重启后端服务。

## 默认运行参数

后端默认端口：

```properties
server.port=8080
```

前端允许跨域来源：

```properties
app.frontend.allowed-origin-patterns=http://*:8081,http://*:8082
```

MySQL 默认连接：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/db_travel?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
spring.datasource.username=root
spring.datasource.password=MySql80
```

PDF 附件默认存储目录：

```properties
app.storage.root=storage
```

## 验证情况

已执行：

```bash
C:\Users\zm\.cache\codex-runtimes\codex-primary-runtime\dependencies\node\bin\node.exe --check front-end\src\main\webapp\assets\js\app.js
cd back-end
.\mvnw.cmd -q -DskipTests compile
```

结果：

- 前端 JS 语法检查通过。
- 后端 Maven 编译通过。
