const API_BASE = window.API_BASE || `${window.location.protocol}//${window.location.hostname || "localhost"}:8080/api`;
// 当前登录用户和计划列表缓存，供计划页渲染与事件处理复用。
let currentUser = null;
let plans = [];
// 当前排序状态：col 为后端字段名，dir 为 asc/desc，null 表示未排序。
let sortState = { col: null, dir: null };
let currentPlanPage = 1;
const PLAN_PAGE_SIZE = 10;
const PLAN_LIST_REFRESH_MS = 10000;
let planListRefreshTimer = null;
let planListRefreshInFlight = false;
let globalNavEventsBound = false;
let activeConsultPlanId = null;
let activeConsultPlanNo = "";
let activeConsultParticipantId = null;
let activeConsultMode = "plan";
let consultOverview = null;
let consultRefreshTimer = null;
let consultRefreshInFlight = false;

// 简化 DOM 查询写法，所有调用都按元素 id 获取节点。
const $ = (id) => document.getElementById(id);

// 将用户角色整数转换为显示文字（0=管理员，1=普通员工）。
const roleLabel = (role) => Number(role) === 0 ? "管理员" : "普通员工";

// 将申请状态整数转换为显示文字（0=申请成功，1=取消）。
const applicationStatusLabel = (status) => Number(status) === 0 ? "申请成功" : "取消";

const applicationStatusBadge = (status) => {
    const value = Number(status);
    return `<span class="status-badge application-status status-application-${value}">${applicationStatusLabel(value)}</span>`;
};

// 将价格统一显示为人民币格式，例如 ¥2,280。
const formatPrice = (price) => {
    const value = Number(price);
    if (!Number.isFinite(value)) return "";
    return `¥${value.toLocaleString("en-US", {maximumFractionDigits: 0})}`;
};

const parsePriceInput = (price) => {
    const normalized = String(price || "").replace(/[¥￥,\s]/g, "");
    return normalized === "" ? NaN : Number(normalized);
};

const MAX_PLAN_PRICE = 10000;

// 将后端日期 YYYY-MM-DD 统一显示为 YYYY/MM/DD。
const formatDate = (date) => String(date || "").replaceAll("-", "/");

const formatDateRange = (startDate, endDate) => `${formatDate(startDate)} - ${formatDate(endDate)}`;

const formatDateInputValue = (date) => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
};

const formatDateTime = (value) => {
    if (!value) return "";
    const normalized = String(value).replace("T", " ");
    const [datePart = "", timePart = ""] = normalized.split(" ");
    return `${formatDate(datePart)} ${timePart.slice(0, 5)}`.trim();
};

const formatMessageDate = (value) => {
    if (!value) return "";
    const normalized = String(value).replace("T", " ");
    const [datePart = ""] = normalized.split(" ");
    return formatDate(datePart);
};

const formatMessageTime = (value) => {
    if (!value) return "";
    const normalized = String(value).replace("T", " ");
    const [, timePart = ""] = normalized.split(" ");
    return timePart.slice(0, 5);
};

// 将旅行计划状态整数转换为显示文字。
const planStatusLabel = (status) => {
    const map = {0: "可申请", 1: "已成团", 2: "进行中", 3: "已结束", 4: "未成团"};
    return map[Number(status)] ?? "";
};

const planStatusBadge = (status) => {
    const value = Number(status);
    return `<span class="status-badge plan-status status-plan-${value}">${planStatusLabel(value)}</span>`;
};

const publishStatusBadge = (published) => {
    const isPublished = published === true || Number(published) === 1 || String(published).toLowerCase() === "true";
    return `<span class="status-badge publish-status ${isPublished ? "status-published" : "status-draft"}">${isPublished ? "已公开" : "未公开"}</span>`;
};

const actionIcons = {
    edit: `<svg aria-hidden="true" viewBox="0 0 24 24"><path d="M12 20h9"/><path d="M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4Z"/></svg>`,
    delete: `<svg aria-hidden="true" viewBox="0 0 24 24"><path d="M3 6h18"/><path d="M8 6V4h8v2"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/></svg>`,
    apply: `<svg aria-hidden="true" viewBox="0 0 24 24"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M19 8v6"/><path d="M22 11h-6"/></svg>`,
    consult: `<svg aria-hidden="true" viewBox="0 0 24 24"><path d="M21 15a4 4 0 0 1-4 4H8l-5 3V7a4 4 0 0 1 4-4h10a4 4 0 0 1 4 4Z"/></svg>`
};

function actionButton(action, id, label, extraClass = "") {
    return `<button class="icon-btn ${extraClass}" data-action="${action}" data-id="${id}" title="${label}" aria-label="${label}" type="button">${actionIcons[action]}</button>`;
}

function disabledActionButton(action, label) {
    return `<button class="icon-btn disabled" title="${label}" aria-label="${label}" type="button" disabled>${actionIcons[action]}</button>`;
}

// 统一 API 请求封装：自动携带 Cookie，并兼容 JSON 响应和文件流响应。
async function api(path, options = {}) {
    const response = await fetch(API_BASE + path, {
        credentials: "include",
        headers: options.body instanceof FormData ? {} : {"Content-Type": "application/json"},
        ...options
    });
    const contentType = response.headers.get("content-type") || "";
    if (contentType.includes("application/json")) {
        const payload = await response.json();
        if (!payload.success) throw new Error(payload.message);
        return payload.data;
    }
    if (!response.ok) throw new Error("请求失败");
    return response;
}

// 显示短暂的全局提示消息。
function toast(message) {
    const toastEl = $("toast");
    if (!toastEl) return;
    toastEl.textContent = message;
    toastEl.classList.remove("hidden");
    setTimeout(() => toastEl.classList.add("hidden"), 2600);
}

// 跳转到指定 JSP 页面。
function go(page) {
    window.location.href = page;
}

// 平滑关闭弹窗，避免原生 dialog 直接消失造成生硬跳变。
function closeDialog(dialog) {
    if (!dialog || !dialog.open || dialog.classList.contains("dialog-closing")) return;
    dialog.classList.add("dialog-closing");
    window.setTimeout(() => {
        dialog.classList.remove("dialog-closing");
        dialog.close();
    }, 180);
}

// 根据当前用户信息刷新计划页顶部状态，并按角色控制管理员入口。
function showPlansPage() {
    updateGlobalNav();
    const admin = Number(currentUser.role) === 0;
    $("newPlanBtn").classList.toggle("hidden", !admin);
    $("publishedFilter").classList.toggle("hidden", !admin);
}

function updateGlobalNav() {
    const userInfo = $("userInfo");
    if (userInfo && currentUser) {
        userInfo.textContent = `${currentUser.name}（${roleLabel(currentUser.role)}）`;
    }
}

// 根据筛选条件加载旅行计划列表。
async function loadPlans({resetPage = true} = {}) {
    const params = new URLSearchParams();
    if ($("keywordInput").value) params.set("keyword", $("keywordInput").value);
    if ($("statusFilter").value) params.set("status", $("statusFilter").value);
    if (!$("publishedFilter").classList.contains("hidden") && $("publishedFilter").value) {
        params.set("published", $("publishedFilter").value);
    }
    if (sortState.col) {
        params.set("sort", sortState.col);
        params.set("sortDir", sortState.dir);
    }
    plans = await api("/plans?" + params.toString());
    if (resetPage) currentPlanPage = 1;
    renderPlans();
}

function startPlanListAutoRefresh() {
    stopPlanListAutoRefresh();
    planListRefreshTimer = setInterval(refreshPlanListInBackground, PLAN_LIST_REFRESH_MS);
}

function stopPlanListAutoRefresh() {
    if (planListRefreshTimer) {
        clearInterval(planListRefreshTimer);
        planListRefreshTimer = null;
    }
    planListRefreshInFlight = false;
}

async function refreshPlanListInBackground() {
    if (!$("appView") || planListRefreshInFlight || document.hidden) return;
    planListRefreshInFlight = true;
    try {
        await loadPlans({resetPage: false});
    } catch (error) {
        console.warn("刷新旅行计划一览失败", error);
    } finally {
        planListRefreshInFlight = false;
    }
}

// 渲染旅行计划表格，管理员会额外看到公开状态和编辑/删除操作。
function renderPlans() {
    const admin = Number(currentUser.role) === 0;
    const columns = [
        {label: "状态", className: "col-status"},
        {label: "旅游计划编号", sort: "planNo", className: "col-plan-no"},
        {label: "目的地", sort: "destination", className: "col-destination"},
        {label: "往返日期", sort: "startDate", className: "col-date-range"},
        {label: "价格", sort: "price", className: "col-price"},
        {label: "定员数", sort: "capacity", className: "col-capacity"},
        {label: "申请总人数", sort: "applicantTotal", className: "col-applicant-total"},
        {label: "我的申请人数", sort: "myApplicantCount", className: "col-my-applicant-count"}
    ];
    if (admin) columns.push({label: "公开状态", className: "col-published"});
    columns.push({label: "操作", className: "col-actions"});
    const arrows = `<div class="sort-arrows"><span class="arrow-up">▲</span><span class="arrow-down">▼</span></div>`;
    $("planHeader").innerHTML = columns.map((column) => {
        if (!column.sort) return `<th class="${column.className}">${column.label}</th>`;
        const active = sortState.col === column.sort ? (sortState.dir === "asc" ? " asc" : " desc") : "";
        return `<th class="${column.className} sortable${active}" data-sort="${column.sort}"><div class="th-inner">${column.label}${arrows}</div></th>`;
    }).join("");
    const totalPages = Math.max(Math.ceil(plans.length / PLAN_PAGE_SIZE), 1);
    currentPlanPage = Math.min(Math.max(currentPlanPage, 1), totalPages);
    const pagePlans = plans.slice((currentPlanPage - 1) * PLAN_PAGE_SIZE, currentPlanPage * PLAN_PAGE_SIZE);
    $("planRows").innerHTML = pagePlans.map((plan) => {
        const pdfViewerUrl = `pdf-viewer.jsp?id=${encodeURIComponent(plan.id)}&planNo=${encodeURIComponent(plan.planNo)}`;
        const planNo = escapeHtml(plan.planNo || "");
        const destination = escapeHtml(plan.destination || "");
        const fileLink = plan.filePath
            ? `<a href="${pdfViewerUrl}" target="_blank" rel="noopener" title="${planNo}">${planNo}</a>`
            : planNo;
        const adminCells = admin ? `<td class="col-published">${publishStatusBadge(plan.published)}</td>` : "";
        const editAction = plan.published
            ? disabledActionButton("edit", "已公开的计划不可编辑")
            : actionButton("edit", plan.id, "编辑");
        const adminActions = admin
            ? `${editAction}${actionButton("delete", plan.id, "删除", "danger")}`
            : "";
        const consultClass = plan.hasUnreadConsultation ? "has-unread" : "";
        return `<tr>
            <td class="col-status">${planStatusBadge(plan.status)}</td>
            <td class="col-plan-no" title="${planNo}">${fileLink}</td>
            <td class="col-destination" title="${destination}">${destination}</td>
            <td class="col-date-range">${formatDateRange(plan.startDate, plan.endDate)}</td>
            <td class="col-price">${formatPrice(plan.price)}</td>
            <td class="col-capacity">${plan.capacity}</td>
            <td class="col-applicant-total">${plan.applicantTotal || 0}</td>
            <td class="col-my-applicant-count">${plan.myApplicantCount || 0}</td>
            ${adminCells}
            <td class="col-actions">
                <div class="plan-actions">
                    ${adminActions}
                    ${actionButton("apply", plan.id, "申请")}
                    ${actionButton("consult", plan.id, "咨询", consultClass)}
                </div>
            </td>
        </tr>`;
    }).join("");
    renderPlanPagination();
}

// 渲染旅行计划分页控件，每页固定显示 10 条。
function renderPlanPagination() {
    const pagination = $("planPagination");
    if (!pagination) return;
    const total = plans.length;
    if (!total) {
        pagination.innerHTML = `<span class="pagination-info">共 0 条</span>`;
        return;
    }
    const totalPages = Math.ceil(total / PLAN_PAGE_SIZE);
    const buttons = Array.from({length: totalPages}, (_, index) => {
        const page = index + 1;
        const active = page === currentPlanPage ? " active" : "";
        return `<button class="page-btn${active}" data-page="${page}" type="button">${page}</button>`;
    }).join("");
    pagination.innerHTML = `
        <span class="pagination-info">共 ${total} 条，第 ${currentPlanPage} / ${totalPages} 页</span>
        <button class="page-btn" data-page="${currentPlanPage - 1}" type="button" ${currentPlanPage === 1 ? "disabled" : ""}>上一页</button>
        ${buttons}
        <button class="page-btn" data-page="${currentPlanPage + 1}" type="button" ${currentPlanPage === totalPages ? "disabled" : ""}>下一页</button>
    `;
}

// 打开随行人员弹窗；没有历史数据时按申请人数生成空行。
async function openCompanionsDialog(applicationId, count = 1) {
    $("companionsForm").applicationId.value = applicationId;
    const rows = await api(`/applications/${applicationId}/companions`);
    $("companionsRows").innerHTML = "";
    const source = rows.length ? rows : Array.from({length: count}, () => ({}));
    source.forEach((row) => addCompanionRow(row, "companionsRows"));
    $("companionsDialog").showModal();
}

// 动态新增一行随行人员输入项，并绑定姓名和身份证号的离焦校验。
function addCompanionRow(row = {}, containerId = "companionsRows") {
    const div = document.createElement("div");
    div.className = "companion-row";
    div.innerHTML = `
        <input placeholder="姓名" value="${escapeHtml(row.name || "")}" required maxlength="20">
        <select><option value="女" ${row.gender === "女" ? "selected" : ""}>女</option><option value="男" ${row.gender === "男" ? "selected" : ""}>男</option></select>
        <input placeholder="身份证号" value="${escapeHtml(row.idCard || "")}" required>
        <select><option value="true" ${row.bedNeeded !== false ? "selected" : ""}>占床</option><option value="false" ${row.bedNeeded === false ? "selected" : ""}>不占床</option></select>
        <button type="button">删除</button>`;
    div.querySelector("button").onclick = () => div.remove();

    const inputs = div.querySelectorAll("input");
    const nameInput = inputs[0];
    const idCardInput = inputs[1];

    // 姓名：不能包含数字，离焦时校验，重新输入时清除提示。
    nameInput.addEventListener("blur", () => {
        if (/\d/.test(nameInput.value)) {
            nameInput.setCustomValidity("姓名不能包含数字");
        } else {
            nameInput.setCustomValidity("");
        }
        nameInput.reportValidity();
    });
    nameInput.addEventListener("input", () => nameInput.setCustomValidity(""));

    // 身份证号：18位，前17位数字，末位数字或X，离焦时校验。
    idCardInput.addEventListener("blur", () => {
        if (idCardInput.value && !/^\d{17}[\dX]$/i.test(idCardInput.value.trim())) {
            idCardInput.setCustomValidity("身份证号须为18位（末位可为数字或X）");
        } else {
            idCardInput.setCustomValidity("");
        }
        idCardInput.reportValidity();
    });
    idCardInput.addEventListener("input", () => idCardInput.setCustomValidity(""));

    $(containerId).appendChild(div);
}

function collectCompanionRows(containerId = "companionsRows") {
    return Array.from($(containerId).querySelectorAll(".companion-row")).map((row) => {
        const inputs = row.querySelectorAll("input,select");
        return {name: inputs[0].value, gender: inputs[1].value, idCard: inputs[2].value, bedNeeded: inputs[3].value === "true"};
    });
}

// 收集随行人员表单行并覆盖保存到后端。
async function saveCompanions(event) {
    event.preventDefault();
    const rows = collectCompanionRows();
    await api(`/applications/${$("companionsForm").applicationId.value}/companions`, {method: "POST", body: JSON.stringify(rows)});
    closeDialog($("companionsDialog"));
    toast("随行人员已保存");
}

// 打开咨询弹窗并加载当前计划的历史消息。
async function openConsultDialog(planId) {
    stopConsultAutoRefresh();
    const plan = plans.find((item) => Number(item.id) === Number(planId)) || {};
    activeConsultMode = "plan";
    activeConsultPlanId = planId;
    activeConsultPlanNo = plan.planNo || String(planId);
    activeConsultParticipantId = null;
    $("consultForm").reset();
    $("consultForm").planId.value = planId;
    $("consultPlanNo").textContent = activeConsultPlanNo;
    await renderConsultSessions(planId, {forceScroll: true});
    $("consultDialog").showModal();
    startConsultAutoRefresh();
}

async function openGlobalConsultDialog() {
    if (!$("consultDialog")) {
        go("plans.jsp?consult=1");
        return;
    }
    stopConsultAutoRefresh();
    activeConsultMode = "global";
    $("consultForm").reset();
    await renderGlobalConsultOverview({forceScroll: true});
    $("consultDialog").showModal();
    startConsultAutoRefresh();
}

async function loadConsultOverview() {
    consultOverview = await api("/consultations/overview");
    renderConsultNavBadge(consultOverview.unreadTotal || 0);
    return consultOverview;
}

function renderConsultNavBadge(count) {
    const badge = $("consultUnreadBadge");
    if (!badge) return;
    const value = Number(count || 0);
    badge.textContent = value > 99 ? "99+" : String(value);
    badge.classList.toggle("hidden", value <= 0);
}

async function refreshConsultNavBadge() {
    if (!$("consultMessagesBtn") || !currentUser) return;
    try {
        await loadConsultOverview();
    } catch (error) {
        console.warn("刷新咨询未读数失败", error);
    }
}

async function renderGlobalConsultOverview({forceScroll = false} = {}) {
    const overview = await loadConsultOverview();
    const employees = overview.employees || [];
    const sessionsEl = $("consultSessions");
    const plansEl = $("consultPlanNo");
    if (!employees.length) {
        activeConsultParticipantId = null;
        activeConsultPlanId = null;
        activeConsultPlanNo = "";
        $("consultForm").planId.value = "";
        $("consultForm").participantUserId.value = "";
        sessionsEl.innerHTML = "<p class='muted'>暂无咨询员工</p>";
        plansEl.innerHTML = "<p class='muted'>暂无旅游计划</p>";
        renderEmptyConsultMessages("暂无咨询内容");
        return;
    }

    if (!activeConsultParticipantId || !employees.some((employee) => Number(employee.participantUserId) === Number(activeConsultParticipantId))) {
        activeConsultParticipantId = employees[0].participantUserId;
    }
    const activeEmployee = employees.find((employee) => Number(employee.participantUserId) === Number(activeConsultParticipantId)) || employees[0];
    const employeePlans = activeEmployee.plans || [];
    if (!activeConsultPlanId || !employeePlans.some((plan) => Number(plan.planId) === Number(activeConsultPlanId))) {
        activeConsultPlanId = employeePlans[0]?.planId || null;
    }
    const activePlan = employeePlans.find((plan) => Number(plan.planId) === Number(activeConsultPlanId)) || employeePlans[0] || {};
    activeConsultPlanNo = activePlan.planNo || "";
    $("consultForm").planId.value = activeConsultPlanId || "";
    $("consultForm").participantUserId.value = activeConsultParticipantId || "";

    sessionsEl.innerHTML = employees.map((employee) => {
        const active = Number(employee.participantUserId) === Number(activeConsultParticipantId) ? " active" : "";
        const label = consultSessionLabel(employee);
        const unreadCount = Number(employee.unreadCount || 0);
        const unreadBadge = unreadCount > 0
            ? `<span class="consult-session-badge" aria-label="${unreadCount} 条未读消息">${unreadCount > 99 ? "99+" : unreadCount}</span>`
            : "";
        return `<button class="consult-session${active}" type="button" data-consult-employee="${employee.participantUserId}" title="${label}">
            <span class="consult-session-main">
                <span>${label}</span>
                <small>${employeePlansSummary(employee)}</small>
            </span>
            ${unreadBadge}
        </button>`;
    }).join("");

    plansEl.innerHTML = employeePlans.length ? employeePlans.map((plan) => {
        const active = Number(plan.planId) === Number(activeConsultPlanId) ? " active" : "";
        const unreadCount = Number(plan.unreadCount || 0);
        const unreadBadge = unreadCount > 0
            ? `<span class="consult-session-badge" aria-label="${unreadCount} 条未读消息">${unreadCount > 99 ? "99+" : unreadCount}</span>`
            : "";
        const planNo = escapeHtml(plan.planNo || String(plan.planId || ""));
        return `<button class="consult-session consult-plan-option${active}" type="button" data-consult-plan="${plan.planId}" title="${planNo}">
            <span class="consult-session-main">
                <span>${planNo}</span>
                <small>${formatDateTime(plan.latestCreatedAt)}</small>
            </span>
            ${unreadBadge}
        </button>`;
    }).join("") : "<p class='muted'>暂无旅游计划</p>";

    if (activeConsultPlanId) {
        await renderMessages(activeConsultPlanId, {forceScroll});
    } else {
        renderEmptyConsultMessages("请选择旅游计划");
    }
}

async function renderConsultSessions(planId, {forceScroll = false} = {}) {
    const sessions = await api(`/plans/${planId}/consultations/sessions`);
    const sessionsEl = $("consultSessions");
    if (!sessions.length) {
        activeConsultParticipantId = null;
        $("consultForm").participantUserId.value = "";
        sessionsEl.innerHTML = "<p class='muted'>暂无员工咨询</p>";
        renderEmptyConsultMessages("请选择左侧员工会话");
        return;
    }
    if (!activeConsultParticipantId || !sessions.some((session) => Number(session.participantUserId) === Number(activeConsultParticipantId))) {
        activeConsultParticipantId = sessions[0].participantUserId;
    }
    $("consultForm").participantUserId.value = activeConsultParticipantId;
    sessionsEl.innerHTML = sessions.map((session) => {
        const active = Number(session.participantUserId) === Number(activeConsultParticipantId) ? " active" : "";
        const label = consultSessionLabel(session);
        const unreadCount = Number(session.unreadCount || 0);
        const unreadBadge = unreadCount > 0
            ? `<span class="consult-session-badge" aria-label="${unreadCount} 条未读消息">${unreadCount > 99 ? "99+" : unreadCount}</span>`
            : "";
        return `<button class="consult-session${active}" type="button" data-consult-user="${session.participantUserId}" title="${label}">
            <span class="consult-session-main">
                <span>${label}</span>
                <small>${formatDateTime(session.latestCreatedAt)}</small>
            </span>
            ${unreadBadge}
        </button>`;
    }).join("");
    await renderMessages(planId, {forceScroll});
    const hasRemainingUnread = Number(currentUser.role) === 0
        ? sessions.some((session) => Number(session.participantUserId) !== Number(activeConsultParticipantId)
            && Number(session.unreadCount || 0) > 0)
        : false;
    plans = plans.map((plan) => Number(plan.id) === Number(planId)
        ? {...plan, hasUnreadConsultation: hasRemainingUnread}
        : plan);
    renderPlans();
}

function employeePlansSummary(employee) {
    const planCount = (employee.plans || []).length;
    return planCount ? `${planCount} 个旅游计划` : "暂无旅游计划";
}

// 渲染咨询消息列表，并对消息正文做 HTML 转义。
async function renderMessages(planId, {forceScroll = false} = {}) {
    if (!activeConsultParticipantId) {
        renderEmptyConsultMessages("暂无员工咨询");
        return;
    }
    const messages = await api(`/plans/${planId}/consultations?participantUserId=${encodeURIComponent(activeConsultParticipantId)}`);
    const messagesEl = $("messages");
    const shouldKeepAtBottom = forceScroll || isNearBottom(messagesEl);
    let currentDate = "";
    messagesEl.innerHTML = messages.map((msg) => {
        const messageDate = formatMessageDate(msg.createdAt);
        const divider = messageDate && messageDate !== currentDate
            ? `<div class="message-date-divider">${messageDate}</div>`
            : "";
        currentDate = messageDate || currentDate;
        return `
            ${divider}
            <div class="message ${Number(msg.userId) === Number(currentUser.id) ? "mine" : ""}">
                <p>${escapeHtml(msg.content)}</p>
                <small>${formatMessageTime(msg.createdAt)}</small>
            </div>
        `;
    }).join("") || "<p class='muted'>暂无咨询内容</p>";
    if (shouldKeepAtBottom) {
        requestAnimationFrame(() => {
            messagesEl.scrollTop = messagesEl.scrollHeight;
        });
    }
}

function renderEmptyConsultMessages(message) {
    $("messages").innerHTML = `<p class='muted'>${escapeHtml(message)}</p>`;
}

function consultSessionLabel(session) {
    const employeeNo = escapeHtml(session.employeeNo || "");
    const userName = escapeHtml(session.userName || "员工");
    return employeeNo ? `（${employeeNo}）${userName}` : userName;
}

function isNearBottom(element) {
    return element.scrollHeight - element.scrollTop - element.clientHeight <= 48;
}

function startConsultAutoRefresh() {
    consultRefreshTimer = setInterval(refreshActiveConsultMessages, 3000);
}

function stopConsultAutoRefresh() {
    if (consultRefreshTimer) {
        clearInterval(consultRefreshTimer);
        consultRefreshTimer = null;
    }
    activeConsultPlanId = null;
    activeConsultPlanNo = "";
    activeConsultParticipantId = null;
    activeConsultMode = "plan";
    consultRefreshInFlight = false;
}

async function refreshActiveConsultMessages() {
    if (consultRefreshInFlight || !$("consultDialog").open) return;
    consultRefreshInFlight = true;
    try {
        if (activeConsultMode === "global") {
            await renderGlobalConsultOverview();
        } else if (activeConsultPlanId) {
            await renderConsultSessions(activeConsultPlanId);
        }
    } catch (error) {
        console.warn("刷新咨询消息失败", error);
    } finally {
        consultRefreshInFlight = false;
    }
}

function messageSenderName(msg) {
    if (Number(msg.userId) === Number(currentUser.id)) return "我";
    if (Number(msg.senderRole) === 0) return "管理员";
    return escapeHtml(msg.userName || "员工");
}

// 发送咨询消息，成功后清空输入框并刷新消息列表。
async function sendConsult(event) {
    event.preventDefault();
    const form = $("consultForm");
    if (!form.content.value.trim()) return;
    if (!form.planId.value) {
        toast("请先选择旅游计划");
        return;
    }
    if (Number(currentUser.role) === 0 && !form.participantUserId.value) {
        toast("请先选择咨询员工");
        return;
    }
    await api(`/plans/${form.planId.value}/consultations`, {
        method: "POST",
        body: JSON.stringify({
            content: form.content.value,
            participantUserId: form.participantUserId.value ? Number(form.participantUserId.value) : null
        })
    });
    form.content.value = "";
    if (activeConsultMode === "global") {
        await renderGlobalConsultOverview({forceScroll: true});
    } else {
        await renderConsultSessions(form.planId.value, {forceScroll: true});
    }
}

function handleConsultInputKeydown(event) {
    if (event.key !== "Enter" || event.shiftKey || event.isComposing) return;
    event.preventDefault();
    event.currentTarget.form.requestSubmit();
}

// 删除旅行计划前先加载申请人预览，打开确认弹窗。
async function deletePlan(planId) {
    const applicants = await api(`/plans/${planId}/delete-preview`);
    $("deleteForm").planId.value = planId;
    const hasApplicants = applicants.length > 0;
    $("deleteMessage").textContent = hasApplicants
        ? "已经有员工申请本计划。如需删除本计划，请先发送邮件通知员工。"
        : "确认删除该旅行计划？删除后将无法恢复。";
    $("deleteApplicantSummary").textContent = hasApplicants
        ? `${applicants.length} 位员工已申请`
        : "暂无员工申请";
    $("deleteApplicants").innerHTML = applicants.length
        ? applicants.map((a) => `
            <div class="delete-applicant-card">
                <div class="delete-applicant-avatar" aria-hidden="true">${escapeHtml((a.userName || "?").slice(0, 1))}</div>
                <div class="delete-applicant-info">
                    <strong>${escapeHtml(a.userName || "未命名员工")}</strong>
                    <p>${escapeHtml(a.email || "暂无邮箱")}</p>
                </div>
            </div>
        `).join("")
        : `
            <div class="delete-empty-state">
                <strong>暂无员工申请</strong>
                <p>可以直接确认删除该旅行计划。</p>
            </div>
        `;
    $("mailNotifyBtn").classList.toggle("hidden", !hasApplicants);
    $("confirmDeleteBtn").classList.toggle("hidden", hasApplicants);
    $("deleteDialog").showModal();
}

// 确认删除旅行计划，并刷新列表。
async function confirmDeletePlan(event) {
    event.preventDefault();
    const planId = $("deleteForm").planId.value;
    await api(`/plans/${planId}/delete`, {method: "POST"});
    closeDialog($("deleteDialog"));
    toast("旅行计划已删除");
    await loadPlans();
}

// 向已申请员工发送取消通知邮件后删除旅行计划。
async function notifyApplicantsAndDelete() {
    const planId = $("deleteForm").planId.value;
    const button = $("mailNotifyBtn");
    button.disabled = true;
    button.textContent = "发送中...";
    try {
        await api(`/plans/${planId}/notify-cancel-and-delete`, {method: "POST"});
        closeDialog($("deleteDialog"));
        toast("邮件通知已发送，旅行计划已删除");
        await loadPlans();
    } catch (error) {
        toast(error.message || "邮件通知发送失败");
    } finally {
        button.disabled = false;
        button.textContent = "邮件通知";
    }
}

// 打开"我的申请"弹窗，展示当前用户申请及可执行操作。
async function openMyApps() {
    const apps = await api("/my-applications");
    $("myAppsRows").innerHTML = apps.length ? `
        <div class="my-app-dialog-header">
            <div>
                <h3>我的申请</h3>
                <p>查看申请状态并维护同行人员信息</p>
            </div>
        </div>
        <div class="my-apps-summary">
            <span>共 ${apps.length} 条申请</span>
            <span>${apps.filter((app) => Number(app.status) === 0).length} 条生效中</span>
        </div>
        <div class="my-apps-list">
            ${apps.map((app) => `
                <article class="my-app-card">
                    <div class="my-app-main">
                        <div class="my-app-title">
                            <strong>${escapeHtml(app.destination || "")}</strong>
                            <span>${escapeHtml(app.planNo || "")}</span>
                        </div>
                        <div class="my-app-meta">
                            <span>申请人数 ${app.applicantCount}</span>
                            ${applicationStatusBadge(app.status)}
                        </div>
                        ${app.optionText ? `<p class="my-app-note">${escapeHtml(app.optionText)}</p>` : ""}
                    </div>
                    <div class="my-app-actions">
                        <button data-app="${app.id}" data-count="${app.applicantCount}" type="button">修改人员</button>
                        ${Number(app.status) === 0 ? `<button class="danger" data-cancel="${app.id}" type="button">取消申请</button>` : ""}
                    </div>
                </article>`).join("")}
        </div>
    ` : `<div class="my-app-dialog-header">
        <div>
            <h3>我的申请</h3>
            <p>查看申请状态并维护同行人员信息</p>
        </div>
    </div>
    <div class="my-apps-empty">
        <strong>暂无申请</strong>
        <span>提交旅行计划申请后，会在这里显示申请状态和人员信息。</span>
    </div>`;
    if (!$("myAppsDialog").open) $("myAppsDialog").showModal();
}

// 取消指定申请，并刷新我的申请和计划列表。
async function cancelApplication(id) {
    await api(`/applications/${id}/cancel`, {method: "POST"});
    toast("申请已取消");
    await openMyApps();
    if ($("planRows")) await loadPlans();
}

// 转义用户输入内容，避免咨询消息中的 HTML 被浏览器执行。
function escapeHtml(text) {
    return String(text).replace(/[&<>"']/g, (char) => ({'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'}[char]));
}

// 绑定密码输入框的显示/隐藏按钮，并在输入为空时自动恢复隐藏状态。
function setupPasswordToggle(input, toggle, onInput = () => {}) {
    if (!input || !toggle) return () => {};
    const eyeIcon = `<svg aria-hidden="true" viewBox="0 0 24 24"><path d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6S2 12 2 12Z"/><circle cx="12" cy="12" r="3"/></svg>`;
    const eyeOffIcon = `<svg aria-hidden="true" viewBox="0 0 24 24"><path d="M3 3l18 18"/><path d="M10.6 10.6A2 2 0 0 0 13.4 13.4"/><path d="M9.9 5.2A10.5 10.5 0 0 1 12 5c6.5 0 10 7 10 7a18.3 18.3 0 0 1-2.8 3.7"/><path d="M6.6 6.6C3.7 8.5 2 12 2 12s3.5 7 10 7a9.7 9.7 0 0 0 4.3-1"/></svg>`;
    const renderIcon = () => {
        const visible = input.type === "text";
        toggle.innerHTML = visible ? eyeIcon : eyeOffIcon;
        toggle.setAttribute("aria-label", visible ? "隐藏密码" : "显示密码");
        toggle.setAttribute("title", visible ? "隐藏密码" : "显示密码");
    };
    const sync = () => {
        const hasPassword = input.value.length > 0;
        toggle.classList.toggle("hidden", !hasPassword);
        if (!hasPassword) {
            input.type = "password";
        }
        renderIcon();
    };

    input.addEventListener("input", () => {
        onInput();
        sync();
    });

    toggle.addEventListener("click", () => {
        const showingPassword = input.type === "text";
        input.type = showingPassword ? "password" : "text";
        renderIcon();
        input.focus();
    });

    sync();
    return sync;
}

// 显示修改密码弹窗内的成功或失败提示。
function showPasswordMessage(message, success = false) {
    const messageEl = $("passwordMessage");
    messageEl.textContent = message;
    messageEl.classList.toggle("form-error", !success);
    messageEl.classList.toggle("form-success", success);
    messageEl.classList.remove("hidden");
}

// 隐藏修改密码提示信息。
function hidePasswordMessage() {
    $("passwordMessage").classList.add("hidden");
}

function resetPasswordForm(passwordForm) {
    passwordForm.reset();
    [passwordForm.oldPassword, passwordForm.newPassword, passwordForm.confirmPassword].forEach((input) => {
        input.type = "password";
    });
}

// 初始化登录页：已登录自动进入计划页，登录成功后跳转 plans.jsp。
async function initLoginPage() {
    const loginForm = $("loginForm");
    if (!loginForm) return false;
    const forceShowLogin = new URLSearchParams(window.location.search).get("showLogin") === "1";

    const loginError = $("loginError");
    const employeeNoInput = loginForm.employeeNo;
    const passwordInput = loginForm.password;
    const clearLoginError = () => loginError.classList.add("hidden");
    const clearLoginFieldError = (input) => input.setCustomValidity("");
    const setLoginRequiredMessage = (input, message) => {
        if (!input.value.trim()) {
            input.setCustomValidity(message);
        }
    };

    employeeNoInput.addEventListener("input", () => {
        clearLoginFieldError(employeeNoInput);
        clearLoginError();
    });
    employeeNoInput.addEventListener("invalid", () => setLoginRequiredMessage(employeeNoInput, "员工编号不能为空"));
    passwordInput.addEventListener("input", () => {
        clearLoginFieldError(passwordInput);
        clearLoginError();
    });
    passwordInput.addEventListener("invalid", () => setLoginRequiredMessage(passwordInput, "密码不能为空"));

    setupPasswordToggle(passwordInput, $("loginPasswordToggle"), clearLoginError);
    loginForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        clearLoginError();
        clearLoginFieldError(employeeNoInput);
        clearLoginFieldError(passwordInput);

        if (!employeeNoInput.value.trim()) {
            employeeNoInput.setCustomValidity("员工编号不能为空");
            employeeNoInput.reportValidity();
            return;
        }
        if (!passwordInput.value.trim()) {
            passwordInput.setCustomValidity("密码不能为空");
            passwordInput.reportValidity();
            return;
        }

        try {
            currentUser = await api("/auth/login", {
                method: "POST",
                body: JSON.stringify({employeeNo: employeeNoInput.value.trim(), password: passwordInput.value})
            });
            go("plans.jsp");
        } catch (error) {
            const message = error.message || "";
            loginError.textContent = error instanceof TypeError
                ? "无法连接服务器，请检查后端地址或网络"
                : (message === "该用户不存在"
                    ? "账号不存在"
                    : (message === "密码错误" ? "密码不正确，请重新输入" : (message || "无法连接服务器，请检查后端地址或网络")));
            loginError.classList.remove("hidden");
        }
    });

    if (!forceShowLogin) {
        try {
            currentUser = await api("/auth/me");
            go("plans.jsp");
        } catch {
            return true;
        }
    }
    return true;
}

// 年填满4位自动跳月，月填满2位自动跳日；左右箭头键在格边界时跨格移动光标。
function setupDateAutoJump(fieldId) {
    const field = $(fieldId);
    if (!field) return;
    const [y, m, d] = field.querySelectorAll("input[data-date-part]");
    const parts = [y, m, d];
    const nativeDate = field.querySelector(".date-native");
    const pickerBtn = field.querySelector(".date-picker-btn");

    const syncHasValue = () => {
        field.classList.toggle("has-value", parts.some((input) => input.value));
    };
    const clamp = (value, min, max) => Math.min(Math.max(value, min), max);
    const daysInMonth = (year, month) => new Date(year, month, 0).getDate();
    const normalizeNumber = (input) => {
        input.value = input.value.replace(/\D/g, "");
        syncHasValue();
    };
    const normalizeDateParts = () => {
        const year = Number(y.value);
        let month = Number(m.value);
        let day = Number(d.value);

        if (m.value) {
            month = clamp(Number.isFinite(month) ? month : 1, 1, 12);
            m.value = String(month).padStart(2, "0");
        }

        if (d.value) {
            const effectiveYear = y.value.length === 4 && Number.isFinite(year) ? year : 2000;
            const effectiveMonth = m.value ? month : 1;
            const maxDay = daysInMonth(effectiveYear, effectiveMonth);
            day = clamp(Number.isFinite(day) ? day : 1, 1, maxDay);
            d.value = String(day).padStart(2, "0");
        }

        syncHasValue();
    };
    const toNativeDateValue = () => {
        if (!y.value || !m.value || !d.value) return "";
        return `${y.value.padStart(4, "0")}-${m.value.padStart(2, "0")}-${d.value.padStart(2, "0")}`;
    };
    const fillFromNativeDate = (value) => {
        const [year = "", month = "", day = ""] = value.split("-");
        y.value = year;
        m.value = month;
        d.value = day;
        syncHasValue();
    };

    // 输入满位自动跳下一格
    y.addEventListener("input", () => {
        normalizeNumber(y);
        if (y.value.length === 4) m.focus();
    });
    m.addEventListener("input", () => {
        normalizeNumber(m);
        if (m.value.length >= 2) d.focus();
    });
    d.addEventListener("input", () => normalizeNumber(d));
    m.addEventListener("blur", normalizeDateParts);
    d.addEventListener("blur", normalizeDateParts);
    y.addEventListener("blur", normalizeDateParts);

    // 左右箭头键：光标在边界时跳相邻格，并把光标定位到对应端
    parts.forEach((input, i) => {
        input.addEventListener("keydown", (e) => {
            if (e.key === "ArrowRight" && input.selectionStart === input.value.length && i < parts.length - 1) {
                e.preventDefault();
                parts[i + 1].focus();
                parts[i + 1].setSelectionRange(0, 0);
            }
            if (e.key === "ArrowLeft" && input.selectionStart === 0 && i > 0) {
                e.preventDefault();
                const prev = parts[i - 1];
                prev.focus();
                prev.setSelectionRange(prev.value.length, prev.value.length);
            }
        });
    });

    if (pickerBtn && nativeDate) {
        pickerBtn.addEventListener("click", () => {
            normalizeDateParts();
            nativeDate.value = toNativeDateValue();
            if (typeof nativeDate.showPicker === "function") {
                nativeDate.showPicker();
            } else {
                nativeDate.click();
            }
        });
        nativeDate.addEventListener("change", () => fillFromNativeDate(nativeDate.value));
    }

    syncHasValue();
}

function bindGlobalNavEvents() {
    if (globalNavEventsBound) return;
    globalNavEventsBound = true;

    document.addEventListener("click", async (event) => {
        const button = event.target.closest("button");
        if (!button) return;
        if (button.dataset.close !== undefined) closeDialog(button.closest("dialog"));
        if (button.dataset.app) await openCompanionsDialog(Number(button.dataset.app), Number(button.dataset.count));
        if (button.dataset.cancel) await cancelApplication(Number(button.dataset.cancel));
    });

    const logoutBtn = $("logoutBtn");
    if (logoutBtn) {
        logoutBtn.onclick = async () => {
            await api("/auth/logout", {method: "POST"});
            go("index.jsp");
        };
    }

    const myAppsBtn = $("myAppsBtn");
    if (myAppsBtn) myAppsBtn.onclick = openMyApps;

    const consultMessagesBtn = $("consultMessagesBtn");
    if (consultMessagesBtn) consultMessagesBtn.onclick = openGlobalConsultDialog;

    const exportPdfBtn = $("exportPdfBtn");
    if (exportPdfBtn) exportPdfBtn.onclick = () => window.open(API_BASE + "/my-applications/export.pdf", "_blank");

    const addCompanionBtn = $("addCompanionBtn");
    if (addCompanionBtn) addCompanionBtn.onclick = () => addCompanionRow();

    const companionsForm = $("companionsForm");
    if (companionsForm) companionsForm.addEventListener("submit", saveCompanions);

    const passwordForm = $("passwordForm");
    const passwordBtn = $("passwordBtn");
    if (passwordForm && passwordBtn) {
        const syncOldPasswordToggle = setupPasswordToggle(passwordForm.oldPassword, $("oldPasswordToggle"), hidePasswordMessage);
        const syncNewPasswordToggle = setupPasswordToggle(passwordForm.newPassword, $("newPasswordToggle"), hidePasswordMessage);
        const syncConfirmPasswordToggle = setupPasswordToggle(passwordForm.confirmPassword, $("confirmPasswordToggle"), hidePasswordMessage);

        passwordBtn.onclick = () => {
            resetPasswordForm(passwordForm);
            hidePasswordMessage();
            syncOldPasswordToggle();
            syncNewPasswordToggle();
            syncConfirmPasswordToggle();
            $("passwordDialog").showModal();
        };
        passwordForm.addEventListener("submit", async (event) => {
            event.preventDefault();
            hidePasswordMessage();
            if (passwordForm.newPassword.value !== passwordForm.confirmPassword.value) {
                showPasswordMessage("两次输入的新密码不一致");
                return;
            }
            try {
                await api("/auth/password", {
                    method: "POST",
                    body: JSON.stringify({
                        oldPassword: passwordForm.oldPassword.value,
                        newPassword: passwordForm.newPassword.value,
                        confirmPassword: passwordForm.confirmPassword.value
                    })
                });
                showPasswordMessage("密码修改成功", true);
                closeDialog($("passwordDialog"));
                resetPasswordForm(passwordForm);
                toast("密码修改成功");
            } catch (error) {
                showPasswordMessage(error.message || "密码修改失败");
            }
        });
    }
}

// 绑定计划列表页按钮、弹窗和表单事件。
function bindPlansPageEvents() {
    document.addEventListener("click", async (event) => {
        const button = event.target.closest("button");
        if (!button) return;
        if (button.dataset.page) {
            currentPlanPage = Number(button.dataset.page);
            renderPlans();
            return;
        }
        if (button.dataset.consultEmployee) {
            activeConsultParticipantId = Number(button.dataset.consultEmployee);
            activeConsultPlanId = null;
            await renderGlobalConsultOverview({forceScroll: true});
            return;
        }
        if (button.dataset.consultPlan) {
            activeConsultPlanId = Number(button.dataset.consultPlan);
            await renderGlobalConsultOverview({forceScroll: true});
            return;
        }
        if (button.dataset.consultUser) {
            activeConsultParticipantId = Number(button.dataset.consultUser);
            $("consultForm").participantUserId.value = activeConsultParticipantId;
            await renderConsultSessions(activeConsultPlanId, {forceScroll: true});
            return;
        }
        const action = button.dataset.action;
        const id = Number(button.dataset.id);
        // 编辑和申请跳转到独立页面，通过 URL 参数传递计划 ID。
        if (action === "edit") go("plan-edit.jsp?id=" + id);
        if (action === "delete") await deletePlan(id);
        if (action === "apply") go("plan-apply.jsp?planId=" + id);
        if (action === "consult") await openConsultDialog(id);
    });

    // 列头点击排序：升序 → 降序 → 取消，切换后重新请求后端。
    $("planHeader").addEventListener("click", async (e) => {
        const th = e.target.closest("th[data-sort]");
        if (!th) return;
        const col = th.dataset.sort;
        if (sortState.col === col) {
            if (sortState.dir === "asc") sortState.dir = "desc";
            else { sortState.col = null; sortState.dir = null; }
        } else {
            sortState.col = col;
            sortState.dir = "asc";
        }
        await loadPlans();
    });

    $("logoutBtn").onclick = async () => {
        await api("/auth/logout", {method: "POST"});
        go("index.jsp");
    };
    $("searchBtn").onclick = loadPlans;
    $("keywordInput").addEventListener("keydown", (event) => {
        if (event.key === "Enter") {
            event.preventDefault();
            loadPlans();
        }
    });
    $("statusFilter").addEventListener("change", () => loadPlans());
    $("publishedFilter").addEventListener("change", () => loadPlans());
    // 新增计划跳转到编辑页（无 id 参数 = 新增模式）。
    $("newPlanBtn").onclick = () => go("plan-edit.jsp");
    $("deleteForm").addEventListener("submit", confirmDeletePlan);
    $("mailNotifyBtn").onclick = notifyApplicantsAndDelete;
    $("consultForm").addEventListener("submit", sendConsult);
    $("consultForm").content.addEventListener("keydown", handleConsultInputKeydown);
    $("consultDialog").addEventListener("close", stopConsultAutoRefresh);
    window.addEventListener("beforeunload", stopPlanListAutoRefresh);
}

// 初始化计划列表页：未登录跳回登录页，认证错误和加载错误分开处理防止循环跳转。
async function initPlansPage() {
    if (!$("appView")) return false;
    bindPlansPageEvents();
    try {
        currentUser = await api("/auth/me");
    } catch {
        go("index.jsp");
        return false;
    }
    bindGlobalNavEvents();
    showPlansPage();
    try {
        await refreshConsultNavBadge();
        await loadPlans();
        startPlanListAutoRefresh();
        if (new URLSearchParams(window.location.search).get("consult") === "1") {
            await openGlobalConsultDialog();
        }
    } catch (error) {
        toast(error.message || "加载计划失败");
    }
    return true;
}

// 初始化旅行计划编辑页：管理员新增或编辑计划，id 参数存在时预填已有数据。
async function initPlanEditPage() {
    if (!$("planEditView")) return false;
    try {
        currentUser = await api("/auth/me");
    } catch {
        go("index.jsp");
        return false;
    }
    bindGlobalNavEvents();
    updateGlobalNav();
    await refreshConsultNavBadge();

    const params = new URLSearchParams(window.location.search);
    const id = params.get("id");
    const creating = !id;
    const form = $("planEditForm");
    const fileInput = $("planFileInput");
    const fileText = $("planFileText");
    const fileHint = $("planFileHint");
    const fileDropzone = $("planFileDropzone");
    const backBtn = $("planEditBackBtn");
    const cancelBtn = $("planEditCancelBtn");
    if (backBtn) backBtn.onclick = () => go("plans.jsp");
    if (cancelBtn) cancelBtn.onclick = () => go("plans.jsp");
    const updateFileDisplay = (fileName) => {
        const hasFile = Boolean(fileName);
        if (fileText) fileText.textContent = hasFile ? fileName : "点击上传PDF文件";
        if (fileHint) fileHint.textContent = hasFile ? "已选择 PDF，点击可重新选择" : "仅支持 PDF";
        if (fileDropzone) fileDropzone.classList.toggle("has-file", hasFile);
    };
    $("planEditTitle").textContent = id ? "旅游计划修改" : "旅游计划添加";

    if (id) {
        try {
            const plan = await api(`/plans/${id}`);
            if (plan.published) {
                toast("已公开的旅行计划不可编辑");
                setTimeout(() => go("plans.jsp"), 900);
                return true;
            }
            form.id.value = plan.id;
            form.destination.value = plan.destination || "";
            // 将 YYYY-MM-DD 拆分回三个输入格。
            const [sy = "", sm = "", sd = ""] = (plan.startDate || "").split("-");
            form.startYear.value = sy;
            form.startMonth.value = sm || "";
            form.startDay.value = sd || "";
            const [ey = "", em = "", ed = ""] = (plan.endDate || "").split("-");
            form.endYear.value = ey;
            form.endMonth.value = em || "";
            form.endDay.value = ed || "";
            form.price.value = formatPrice(plan.price);
            form.capacity.value = plan.capacity || "";
            form.published.value = Boolean(plan.published) ? "true" : "false";
            updateFileDisplay(plan.fileName);
        } catch (error) {
            toast(error.message || "加载计划失败");
        }
    }

    if (fileInput) {
        fileInput.addEventListener("change", () => {
            updateFileDisplay(fileInput.files.length ? fileInput.files[0].name : "");
        });
    }

    // 复用分段日期自动跳格逻辑。
    setupDateAutoJump("startDateField");
    setupDateAutoJump("endDateField");
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const minStartDate = formatDateInputValue(tomorrow);
    const startNativeDate = $("startDateField")?.querySelector(".date-native");
    if (creating && startNativeDate) {
        startNativeDate.min = minStartDate;
    }

    // 目的地离焦校验。
    const destInput = form.destination;
    destInput.addEventListener("blur", () => {
        destInput.setCustomValidity(destInput.value.length > 10 ? "目的地不能超过10个字符" : "");
        if (destInput.value.length > 10) destInput.reportValidity();
    });
    destInput.addEventListener("input", () => destInput.setCustomValidity(""));

    // 价格离焦校验：[0, 10000]。
    form.price.addEventListener("blur", () => {
        const v = parsePriceInput(form.price.value);
        if (form.price.value !== "" && (!Number.isFinite(v) || v < 0 || v > MAX_PLAN_PRICE)) {
            form.price.setCustomValidity(!Number.isFinite(v) ? "请输入有效价格" : (v < 0 ? "价格不能为负数" : "单人价格上限为10000元"));
            form.price.reportValidity();
        } else {
            form.price.setCustomValidity("");
            if (Number.isFinite(v)) form.price.value = formatPrice(v);
        }
    });
    form.price.addEventListener("input", () => form.price.setCustomValidity(""));

    // 定员数离焦校验：[10, 50]。
    form.capacity.addEventListener("blur", () => {
        const v = Number(form.capacity.value);
        if (form.capacity.value !== "" && (v < 10 || v > 50)) {
            form.capacity.setCustomValidity(v < 10 ? "定员数不能少于10人" : "人数上限为50人");
            form.capacity.reportValidity();
        } else {
            form.capacity.setCustomValidity("");
        }
    });
    form.capacity.addEventListener("input", () => form.capacity.setCustomValidity(""));

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        const errorEl = $("planEditError");
        errorEl.classList.add("hidden");

        if (destInput.value.length > 10) {
            destInput.setCustomValidity("目的地不能超过10个字符");
            destInput.reportValidity();
            return;
        }
        destInput.setCustomValidity("");

        const price = parsePriceInput(form.price.value);
        if (!Number.isFinite(price) || price < 0 || price > MAX_PLAN_PRICE) {
            form.price.setCustomValidity(!Number.isFinite(price) ? "请输入有效价格" : (price < 0 ? "价格不能为负数" : "单人价格上限为10000元"));
            form.price.reportValidity();
            return;
        }
        form.price.setCustomValidity("");

        const sy = form.startYear.value, sm = form.startMonth.value, sd = form.startDay.value;
        const ey = form.endYear.value, em = form.endMonth.value, ed = form.endDay.value;
        if (!sy || !sm || !sd) {
            errorEl.textContent = "请填写完整的启程日";
            errorEl.classList.remove("hidden");
            return;
        }
        if (!ey || !em || !ed) {
            errorEl.textContent = "请填写完整的返回日";
            errorEl.classList.remove("hidden");
            return;
        }

        const startDate = `${sy.padStart(4, "0")}-${sm.padStart(2, "0")}-${sd.padStart(2, "0")}`;
        const endDate = `${ey.padStart(4, "0")}-${em.padStart(2, "0")}-${ed.padStart(2, "0")}`;
        if (creating && startDate < minStartDate) {
            errorEl.textContent = "启程日最早可以选择明天";
            errorEl.classList.remove("hidden");
            return;
        }
        const data = new FormData(form);
        data.set("startDate", startDate);
        data.set("endDate", endDate);
        data.set("price", String(price));
        ["startYear", "startMonth", "startDay", "endYear", "endMonth", "endDay"].forEach(k => data.delete(k));
        data.set("published", form.published.value === "true" ? "true" : "false");
        const planId = form.id.value;

        try {
            await api(planId ? `/plans/${planId}` : "/plans", {method: "POST", body: data});
            go("plans.jsp");
        } catch (error) {
            errorEl.textContent = error.message || "保存失败";
            errorEl.classList.remove("hidden");
        }
    });

    return true;
}

// 初始化旅行计划申请页：加载计划信息及已有申请数据，planId 参数由列表页传入。
async function initPlanApplyPage() {
    if (!$("planApplyView")) return false;
    try {
        currentUser = await api("/auth/me");
    } catch {
        go("index.jsp");
        return false;
    }
    bindGlobalNavEvents();
    updateGlobalNav();
    await refreshConsultNavBadge();

    const params = new URLSearchParams(window.location.search);
    const planId = params.get("planId");
    if (!planId) {
        go("plans.jsp");
        return false;
    }

    const form = $("planApplyForm");
    form.planId.value = planId;

    // 在标题下展示计划基本信息。
    try {
        const plan = await api(`/plans/${planId}`);
        $("applyPlanInfo").innerHTML = `
            <div class="apply-summary-item">
                <span>目的地</span>
                <strong>${escapeHtml(plan.destination || "-")}</strong>
            </div>
            <div class="apply-summary-item">
                <span>行程时间</span>
                <strong>${escapeHtml(formatDateRange(plan.startDate, plan.endDate))}</strong>
            </div>
            <div class="apply-summary-item">
                <span>价格</span>
                <strong>${escapeHtml(formatPrice(plan.price))}</strong>
            </div>`;
    } catch { /* 展示失败不阻断申请流程 */ }

    // 如果已有有效申请则预填随行人员，否则新建一行空行。
    try {
        const apps = await api("/my-applications");
        const active = apps.find((app) => Number(app.planId) === Number(planId) && Number(app.status) === 0);
        if (active) {
            form.applicationId.value = active.id;
            form.optionText.value = active.optionText || "";
            const rows = await api(`/applications/${active.id}/companions`);
            const fallbackCount = Math.max(Number(active.applicantCount) || 1, 1);
            const source = rows.length ? rows : Array.from({length: fallbackCount}, (_, index) => index === 0 ? {name: currentUser.name || ""} : ({}));
            const [selfRow = {name: currentUser.name || ""}, ...extraRows] = source;
            addCompanionRow(selfRow, "planApplyCompanionsRows", {
                lockedName: true,
                hideDelete: true,
                requireGenderChoice: true
            });
            extraRows.forEach((row) => addCompanionRow(row, "planApplyExtraRows"));
        } else {
            addCompanionRow({name: currentUser.name || ""}, "planApplyCompanionsRows", {
                lockedName: true,
                hideDelete: true,
                requireGenderChoice: true
            });
        }
    } catch {
        addCompanionRow({name: currentUser.name || ""}, "planApplyCompanionsRows", {
            lockedName: true,
            hideDelete: true,
            requireGenderChoice: true
        });
    }

    $("planApplyBackBtn").onclick = () => go("plans.jsp");
    $("planApplyCancelBtn").onclick = () => go("plans.jsp");
    $("addPlanApplyCompanionBtn").onclick = () => addCompanionRow({}, "planApplyExtraRows");

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        const errorEl = $("planApplyError");
        errorEl.classList.add("hidden");
        const missingGender = Array.from($("planApplyForm").querySelectorAll(".companion-row"))
            .map((row) => row.querySelector("select"))
            .find((select) => select && !select.value);
        if (missingGender) {
            missingGender.reportValidity();
            toast("请选择性别");
            return;
        }
        const rows = [
            ...collectCompanionRows("planApplyCompanionsRows"),
            ...collectCompanionRows("planApplyExtraRows")
        ];
        if (!rows.length) {
            toast("请至少添加一名随行人员");
            return;
        }
        try {
            const app = await api(`/plans/${form.planId.value}/apply`, {
                method: "POST",
                body: JSON.stringify({applicantCount: rows.length, optionText: form.optionText.value})
            });
            await api(`/applications/${app.id}/companions`, {method: "POST", body: JSON.stringify(rows)});
            go("plans.jsp");
        } catch (error) {
            errorEl.textContent = error.message || "申请保存失败";
            errorEl.classList.remove("hidden");
        }
    });

    return true;
}

async function initGlobalNavPage() {
    if (!$("logoutBtn") && !$("myAppsBtn") && !$("passwordBtn")) return false;
    try {
        currentUser = await api("/auth/me");
    } catch {
        go("index.jsp");
        return false;
    }
    bindGlobalNavEvents();
    updateGlobalNav();
    await refreshConsultNavBadge();
    return true;
}

// 页面入口：依次检测各页面根元素，匹配到对应页面后执行其初始化函数。
initLoginPage()
    .then((started) => started || initPlanEditPage())
    .then((started) => started || initPlanApplyPage())
    .then((started) => started || initPlansPage())
    .then((started) => started || initGlobalNavPage())
    .catch((error) => toast(error.message));
