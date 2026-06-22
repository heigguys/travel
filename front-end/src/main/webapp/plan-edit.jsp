<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>公司旅行管理系统 - 旅行计划编辑</title>
    <link rel="stylesheet" href="assets/css/app.css?v=<%= System.currentTimeMillis() %>">
</head>
<body>
<main class="shell form-shell">
    <!-- 旅行计划编辑页：管理员新增或编辑旅行计划，URL 参数 id 存在时为编辑模式。 -->
    <section id="planEditView" class="form-page">
        <div class="form-card plan-edit-card">
            <form id="planEditForm" class="plan-edit-form">
                <input type="hidden" name="id">
                <h1 id="planEditTitle" class="plan-edit-title">旅游计划添加</h1>

                <div class="plan-form-row">
                    <div class="plan-row-label">
                        <strong>目的地 <span class="required">*</span></strong>
                        <span>请输入旅游目的地</span>
                    </div>
                    <label class="field-only">
                        <span class="visually-hidden">目的地</span>
                        <input name="destination" required placeholder="如：敦煌莫高窟" maxlength="10">
                    </label>
                </div>

                <div class="plan-form-row">
                    <div class="plan-row-label">
                        <strong>日期 <span class="required">*</span></strong>
                        <span>请选择行程日期</span>
                    </div>
                    <div class="plan-control-grid">
                        <label>启程日
                            <div class="date-field" id="startDateField">
                                <span class="date-placeholder">YYYY/MM/DD</span>
                                <input name="startYear" data-date-part="year" type="text" inputmode="numeric" maxlength="4">
                                <span class="date-sep">/</span>
                                <input name="startMonth" data-date-part="month" type="text" inputmode="numeric" maxlength="2">
                                <span class="date-sep">/</span>
                                <input name="startDay" data-date-part="day" type="text" inputmode="numeric" maxlength="2">
                                <button class="date-picker-btn" type="button" aria-label="选择启程日">
                                    <svg aria-hidden="true" viewBox="0 0 24 24"><rect x="3" y="4" width="18" height="18" rx="2"/><path d="M16 2v4"/><path d="M8 2v4"/><path d="M3 10h18"/></svg>
                                </button>
                                <input class="date-native" type="date" tabindex="-1">
                            </div>
                        </label>
                        <label>返回日
                            <div class="date-field" id="endDateField">
                                <span class="date-placeholder">YYYY/MM/DD</span>
                                <input name="endYear" data-date-part="year" type="text" inputmode="numeric" maxlength="4">
                                <span class="date-sep">/</span>
                                <input name="endMonth" data-date-part="month" type="text" inputmode="numeric" maxlength="2">
                                <span class="date-sep">/</span>
                                <input name="endDay" data-date-part="day" type="text" inputmode="numeric" maxlength="2">
                                <button class="date-picker-btn" type="button" aria-label="选择返回日">
                                    <svg aria-hidden="true" viewBox="0 0 24 24"><rect x="3" y="4" width="18" height="18" rx="2"/><path d="M16 2v4"/><path d="M8 2v4"/><path d="M3 10h18"/></svg>
                                </button>
                                <input class="date-native" type="date" tabindex="-1">
                            </div>
                        </label>
                    </div>
                </div>

                <div class="plan-form-row">
                    <div class="plan-row-label">
                        <strong>费用与定员数 <span class="required">*</span></strong>
                        <span>设置费用和旅游团规模</span>
                    </div>
                    <div class="plan-control-grid">
                        <label>价格（元）
                            <input name="price" type="text" inputmode="decimal" required>
                        </label>
                        <label>定员数（人）
                            <input name="capacity" type="number" required placeholder="人数需大于等于10">
                        </label>
                    </div>
                </div>

                <div class="plan-form-row">
                    <div class="plan-row-label">
                        <strong>附件</strong>
                        <span>上传计划相关文档</span>
                    </div>
                    <label id="planFileDropzone" class="upload-dropzone" for="planFileInput">
                        <span class="upload-file-copy">
                            <strong id="planFileText" class="upload-file-name">点击上传PDF文件</strong>
                            <span id="planFileHint" class="upload-file-hint">仅支持 PDF</span>
                        </span>
                        <input id="planFileInput" name="file" type="file" accept="application/pdf">
                    </label>
                </div>

                <div class="plan-form-row">
                    <div class="plan-row-label">
                        <strong>是否公开</strong>
                        <span>设置计划的可见性</span>
                    </div>
                    <div class="published-box">
                        <label class="radio-option">
                            <input name="published" type="radio" value="true"> 是
                        </label>
                        <label class="radio-option">
                            <input name="published" type="radio" value="false" checked> 否
                        </label>
                    </div>
                    <p class="plan-note">默认“否”，公开后计划对一般用户可见</p>
                </div>

                <p id="planEditError" class="form-error hidden"></p>
                <div class="plan-form-footer">
                    <span>所有带 <span class="required">*</span> 为必填项</span>
                    <div class="dialog-actions">
                        <button class="primary" type="submit">保存</button>
                        <button type="button" id="planEditCancelBtn">取消</button>
                    </div>
                </div>
                <button id="planEditBackBtn" class="hidden" type="button">返回列表</button>
            </form>
        </div>
    </section>
</main>
<div id="toast" class="toast hidden"></div>
<script src="assets/js/app.js?v=<%= System.currentTimeMillis() %>"></script>
</body>
</html>
