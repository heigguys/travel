<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>公司旅行管理系统 - 旅行计划编辑</title>
    <link rel="stylesheet" href="assets/css/app.css">
</head>
<body>
<main class="shell">
    <!-- 旅行计划编辑页：管理员新增或编辑旅行计划，URL 参数 id 存在时为编辑模式。 -->
    <section id="planEditView" class="form-page">
        <header class="topbar">
            <h1 id="planEditTitle">旅行计划</h1>
            <button id="planEditBackBtn" type="button">← 返回列表</button>
        </header>
        <div class="form-card">
            <form id="planEditForm" class="stack">
                <input type="hidden" name="id">
                <label>目的地 *
                    <input name="destination" required placeholder="如：敦煌莫高窟" maxlength="10">
                </label>
                <div class="grid2">
                    <label>启程日 *
                        <div class="date-field" id="startDateField">
                            <input name="startYear" type="text" inputmode="numeric" placeholder="YYYY" maxlength="4">
                            <span class="date-sep">/</span>
                            <input name="startMonth" type="text" inputmode="numeric" placeholder="M" maxlength="2">
                            <span class="date-sep">/</span>
                            <input name="startDay" type="text" inputmode="numeric" placeholder="D" maxlength="2">
                        </div>
                    </label>
                    <label>返回日 *
                        <div class="date-field" id="endDateField">
                            <input name="endYear" type="text" inputmode="numeric" placeholder="YYYY" maxlength="4">
                            <span class="date-sep">/</span>
                            <input name="endMonth" type="text" inputmode="numeric" placeholder="M" maxlength="2">
                            <span class="date-sep">/</span>
                            <input name="endDay" type="text" inputmode="numeric" placeholder="D" maxlength="2">
                        </div>
                    </label>
                </div>
                <div class="grid2">
                    <label>价格（元）*
                        <input name="price" type="number" min="0" step="0.01" required>
                    </label>
                    <label>定员数（人）*
                        <input name="capacity" type="number" min="1" required>
                    </label>
                </div>
                <label>PDF 附件
                    <input name="file" type="file" accept="application/pdf">
                </label>
                <label class="inline">
                    <input name="published" type="checkbox"> 公开
                </label>
                <p id="planEditError" class="form-error hidden"></p>
                <div class="dialog-actions">
                    <button type="button" id="planEditCancelBtn">取消</button>
                    <button class="primary" type="submit">保存</button>
                </div>
            </form>
        </div>
    </section>
</main>
<div id="toast" class="toast hidden"></div>
<script src="assets/js/app.js"></script>
</body>
</html>
