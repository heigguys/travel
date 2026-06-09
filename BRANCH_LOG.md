# feature/nie-jane-tang 分支说明

本分支由 Jane 基于 `main` 分支创建，合并了组长 Nie 的重构成果，并加入了 Jane 的功能设计。

---

## 背景

`main` 分支已有基础的旅行计划管理功能（登录、计划列表、申请、咨询、修改密码）。

Nie 在独立分支中完成了数据库字段规范化和部分交互优化。Jane 原本在本地实现了日期分段输入、随行人员校验、多状态设计等功能，两人的工作存在交叉，本分支对两份代码进行了手动合并，保留各自设计意图，解决冲突点。

---

## 合并策略说明

合并过程中遇到以下冲突，按下述规则取舍：

| 冲突点 | Nie 的方案 | Jane 的方案 | 最终取舍 |
|--------|-----------|------------|---------|
| 日期输入 | `<input type="date">` 原生日期选择器 | 年/月/日三格分段输入 | 保留 Jane（视觉一致性） |
| 目的地字符限制 | 120 字符，无离焦提示 | 10 字符，有离焦气泡提示 | 保留 Jane（业务需求） |
| 离焦校验风格 | HTML `pattern` + `title` | `setCustomValidity` + `reportValidity` | 保留 Jane（体验更统一） |
| 旅行计划状态 | `tinyint`（2 值：满员/未满） | `varchar`（5 值：多生命周期） | 保留 Jane 的状态数量，采用 Nie 的整数存储 |
| PDF 申请状态 | 整数转可读文字 | 原始字符串直接输出 | 采用 Nie（可读性更好） |

---

## 各模块变更明细

### 数据库（Nie）

- `users.role`：`varchar('ADMIN'/'USER')` → `tinyint`（0=管理员，1=普通用户）
- `applications.status`：`varchar('ACTIVE'/'CANCELED')` → `tinyint`（0=有效，1=已取消）
- `consultations.sender_role`：`varchar` → `tinyint`
- `travel_plans.status`：`varchar` → `tinyint`（0–4，见下节）
- `travel_plans.destination`：保持 `varchar(10)`（Jane 的限制）

### 旅行计划五状态（Jane）

原有状态为粗粒度字符串，本分支重新设计为整数枚举，覆盖计划全生命周期：

```
0  可申请   出发日未到 且 申请人数 < 定员
1  已成团   出发日未到 且 申请人数 ≥ 定员
2  进行中   出发日已到 且 返回日未到
3  已结束   返回日已过 且 申请人数 ≥ 定员（成功出行）
4  未成团   返回日已过 且 申请人数 < 定员（未成行）
```

状态**不依赖手动维护**，由系统在以下时机自动计算并写入数据库：

| 触发时机 | 范围 |
|---------|------|
| 用户登录 | 全量刷新所有计划 |
| 管理员新建计划 | 仅新计划 |
| 管理员编辑计划 | 全量刷新（日期可能改变） |
| 员工申请 / 取消 | 仅该计划 |
| 每天凌晨 0 点（定时任务） | 全量刷新所有计划 |

### 后端（Nie）

- `User.role`、`Application.status`、`Consultation.senderRole` 改为 `Integer`，添加常量（`ROLE_ADMIN=0` 等）
- `ApplicationMapper`：所有 SQL 字符串比较改为整数比较
- `ApplicationService`：申请/取消后即时刷新计划状态
- `PdfExportService`：申请状态由整数转为"申请成功"/"取消"文字输出
- `AuthService`、`ConsultationService`、`TravelPlanService`：移除 `Role` 枚举依赖，改用整数比较

### 前端（Nie-Jane）

- **随行人员校验**：姓名不含数字（离焦气泡提示）、身份证 18 位格式（离焦气泡提示）
- **日期分段输入**：启程日/返回日拆为年/月/日三格，输入满位自动跳格，左右箭头键跨格移动光标
- **删除确认弹窗**：删除计划前展示已申请员工列表，需二次确认
- **状态标签**：`planStatusLabel()` 将整数状态映射为中文展示

---

## 数据库迁移说明

若原数据库已存在，需手动执行以下语句（`schema.sql` 仅在建表时生效，不会自动 ALTER）：

```sql
-- 修改字段类型（如尚未执行）
ALTER TABLE users MODIFY COLUMN role tinyint NOT NULL COMMENT '0 管理员，1 普通用户';
ALTER TABLE applications MODIFY COLUMN status tinyint NOT NULL DEFAULT 0 COMMENT '0 有效，1 已取消';
ALTER TABLE consultations MODIFY COLUMN sender_role tinyint NOT NULL COMMENT '0 管理员，1 普通用户';

-- 旅行计划状态字段（如已是 varchar，先删后加）
ALTER TABLE travel_plans DROP COLUMN status;
ALTER TABLE travel_plans ADD COLUMN status tinyint NOT NULL DEFAULT 0
  COMMENT '0=可申请，1=已成团，2=进行中，3=已结束，4=未成团';
```

执行后重启后端，登录时会自动刷新所有计划的正确状态。

---

## 提交记录

| # | 提交信息 | 作者 |
|---|---------|------|
| 1 | 合并 nie：数据库角色和申请状态字段改为整数存储，规范字段设计 | Nie |
| 2 | 合并 nie：员工申请或取消后，旅行计划满员状态自动更新 | Nie |
| 3 | 合并 nie：管理员删除计划前弹窗展示已申请员工名单，防止误删 | Nie |
| 4 | 合并 nie：PDF 导出申请状态显示为申请成功或取消 | Nie |
| 5 | 随行人员姓名和身份证格式校验，删除弹窗交互（Nie-Jane） | Nie-Jane |
| 6 | 日期输入年月日分段，支持键盘左右键切换（Nie-Jane） | Nie-Jane |
| 7 | 旅行计划新增未开始、招募中、进行中、已结束四种状态（Jane，待议） | Jane |
