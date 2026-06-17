<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>公司旅行管理系统 - 申请旅行计划</title>
    <link rel="stylesheet" href="assets/css/app.css">
</head>
<body>
<main class="shell">
    <!-- 旅行计划申请页：员工提交或修改申请，URL 参数 planId 指定目标计划。 -->
    <section id="planApplyView" class="form-page">
        <header class="topbar">
            <div>
                <h1>申请旅行计划</h1>
                <p id="applyPlanInfo" class="muted"></p>
            </div>
            <button id="planApplyBackBtn" type="button">← 返回列表</button>
        </header>
        <div class="form-card">
            <form id="planApplyForm" class="stack">
                <input type="hidden" name="planId">
                <input type="hidden" name="applicationId">
                <!-- 申请人行：JS 初始化时自动填入登录用户姓名 -->
                <div id="planApplyApplicantRow"></div>
                <!-- 随行人员分隔标题：有随行人员时显示 -->
                <div id="planApplyCompanionsDivider" class="section-divider hidden"><span>随行人员</span></div>
                <!-- 随行人员列表 -->
                <div id="planApplyCompanionsRows" class="stack"></div>
                <button id="addPlanApplyCompanionBtn" type="button">新增随行人员</button>
                <label>选项 / 备注
                    <textarea name="optionText" rows="3" placeholder="如座位、房型、餐食等需求"></textarea>
                </label>
                <p id="planApplyError" class="form-error hidden"></p>
                <div class="dialog-actions">
                    <button type="button" id="planApplyCancelBtn">取消</button>
                    <button class="primary" type="submit">保存申请</button>
                </div>
            </form>
        </div>
    </section>
</main>
<div id="toast" class="toast hidden"></div>
<script src="assets/js/app.js"></script>
</body>
</html>
