const API_BASE = window.API_BASE || "http://localhost:8080/api";
// 当前登录用户和计划列表缓存，供计划页渲染与事件处理复用。
let currentUser = null;
let plans = [];

// 简化 DOM 查询写法，所有调用都按元素 id 获取节点。
const $ = (id) => document.getElementById(id);

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

// 根据当前用户信息刷新计划页顶部状态，并按角色控制管理员入口。
function showPlansPage() {
    $("userInfo").textContent = `${currentUser.name}（${currentUser.employeeNo} / ${currentUser.role}）`;
    $("newPlanBtn").classList.toggle("hidden", currentUser.role !== "ADMIN");
}

// 根据筛选条件加载旅行计划列表。
async function loadPlans() {
    const params = new URLSearchParams();
    if ($("keywordInput").value) params.set("keyword", $("keywordInput").value);
    if ($("statusFilter").value) params.set("status", $("statusFilter").value);
    if ($("sortSelect").value) params.set("sort", $("sortSelect").value);
    plans = await api("/plans?" + params.toString());
    renderPlans();
}

// 渲染旅行计划表格，管理员会额外看到公开状态和编辑/删除操作。
function renderPlans() {
    const admin = currentUser.role === "ADMIN";
    const headers = ["状态", "旅游计划编号", "目的地", "往返日期", "价格", "定员数", "申请总人数", "我的申请人数"];
    if (admin) headers.push("公开状态");
    headers.push("操作");
    $("planHeader").innerHTML = headers.map((h) => `<th>${h}</th>`).join("");
    $("planRows").innerHTML = plans.map((plan) => {
        const fileLink = plan.filePath
            ? `<a href="${API_BASE}/plans/${plan.id}/file" target="_blank">${plan.planNo}</a>`
            : plan.planNo;
        const adminCells = admin ? `<td>${plan.published ? "已公开" : "未公开"}</td>` : "";
        const adminActions = admin
            ? `<button data-action="edit" data-id="${plan.id}">编辑</button><button class="danger" data-action="delete" data-id="${plan.id}">删除</button>`
            : "";
        return `<tr>
            <td>${plan.status || ""}</td>
            <td>${fileLink}</td>
            <td>${plan.destination}</td>
            <td>${plan.startDate} - ${plan.endDate}</td>
            <td>${plan.price}</td>
            <td>${plan.capacity}</td>
            <td>${plan.applicantTotal || 0}</td>
            <td>${plan.myApplicantCount || 0}</td>
            ${adminCells}
            <td class="actions">
                ${adminActions}
                <button data-action="apply" data-id="${plan.id}">申请</button>
                <button data-action="consult" data-id="${plan.id}">咨询</button>
            </td>
        </tr>`;
    }).join("");
}

// 打开新增或编辑旅行计划弹窗，并把已有计划数据回填到表单。
function openPlanDialog(plan = null) {
    $("planDialogTitle").textContent = plan ? "编辑旅行计划" : "添加旅行计划";
    const form = $("planForm");
    form.reset();
    form.id.value = plan?.id || "";
    form.destination.value = plan?.destination || "";
    form.startDate.value = plan?.startDate || "";
    form.endDate.value = plan?.endDate || "";
    form.price.value = plan?.price || "";
    form.capacity.value = plan?.capacity || "";
    form.published.checked = Boolean(plan?.published);
    $("planDialog").showModal();
}

// 保存旅行计划表单；有 id 时编辑，没有 id 时新增。
async function savePlan(event) {
    event.preventDefault();
    const form = $("planForm");
    const data = new FormData(form);
    data.set("published", form.published.checked ? "true" : "false");
    const id = form.id.value;
    await api(id ? `/plans/${id}` : "/plans", {method: "POST", body: data});
    $("planDialog").close();
    toast("旅行计划已保存");
    await loadPlans();
}

// 打开申请弹窗，直接回填当前用户已保存的申请和随行人员信息。
async function openApplyDialog(planId) {
    const form = $("applyForm");
    form.reset();
    form.planId.value = planId;
    form.applicationId.value = "";
    $("applyCompanionsRows").innerHTML = "";

    const apps = await api("/my-applications");
    const active = apps.find((app) => Number(app.planId) === Number(planId) && app.status === "ACTIVE");
    if (active) {
        form.applicationId.value = active.id;
        form.optionText.value = active.optionText || "";
        const rows = await api(`/applications/${active.id}/companions`);
        const fallbackCount = Math.max(Number(active.applicantCount) || 1, 1);
        const source = rows.length ? rows : Array.from({length: fallbackCount}, () => ({}));
        source.forEach((row) => addCompanionRow(row, "applyCompanionsRows"));
    } else {
        addCompanionRow({}, "applyCompanionsRows");
    }
    $("applyDialog").showModal();
}

// 保存旅行申请；申请人数由人员信息行数自动计算，并同时保存随行人员。
async function saveApply(event) {
    event.preventDefault();
    const form = $("applyForm");
    const rows = collectCompanionRows("applyCompanionsRows");
    if (!rows.length) {
        toast("请至少添加一名随行人员");
        return;
    }
    try {
        const app = await api(`/plans/${form.planId.value}/apply`, {
            method: "POST",
            body: JSON.stringify({
                applicantCount: rows.length,
                optionText: form.optionText.value
            })
        });
        await api(`/applications/${app.id}/companions`, {method: "POST", body: JSON.stringify(rows)});
        $("applyDialog").close();
        toast("申请已保存");
        await loadPlans();
    } catch (error) {
        toast(error.message || "申请保存失败");
    }
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

// 动态新增一行随行人员输入项。
function addCompanionRow(row = {}, containerId = "companionsRows") {
    const div = document.createElement("div");
    div.className = "companion-row";
    div.innerHTML = `
        <input placeholder="姓名" value="${escapeHtml(row.name || "")}" required>
        <select><option value="女" ${row.gender === "女" ? "selected" : ""}>女</option><option value="男" ${row.gender === "男" ? "selected" : ""}>男</option></select>
        <input placeholder="身份证号" value="${escapeHtml(row.idCard || "")}" required>
        <select><option value="true" ${row.bedNeeded !== false ? "selected" : ""}>占床</option><option value="false" ${row.bedNeeded === false ? "selected" : ""}>不占床</option></select>
        <button type="button">删除</button>`;
    div.querySelector("button").onclick = () => div.remove();
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
    $("companionsDialog").close();
    toast("随行人员已保存");
}

// 打开咨询弹窗并加载当前计划的历史消息。
async function openConsultDialog(planId) {
    $("consultForm").reset();
    $("consultForm").planId.value = planId;
    await renderMessages(planId);
    $("consultDialog").showModal();
}

// 渲染咨询消息列表，并对消息正文做 HTML 转义。
async function renderMessages(planId) {
    const messages = await api(`/plans/${planId}/consultations`);
    $("messages").innerHTML = messages.map((msg) => `
        <div class="message ${msg.senderRole === "ADMIN" ? "admin" : ""}">
            <strong>${msg.senderRole === "ADMIN" ? "管理员" : msg.userName}</strong>
            <p>${escapeHtml(msg.content)}</p>
            <small>${msg.createdAt || ""}</small>
        </div>
    `).join("") || "<p class='muted'>暂无咨询内容</p>";
}

// 发送咨询消息，成功后清空输入框并刷新消息列表。
async function sendConsult(event) {
    event.preventDefault();
    const form = $("consultForm");
    await api(`/plans/${form.planId.value}/consultations`, {method: "POST", body: JSON.stringify({content: form.content.value})});
    form.content.value = "";
    await renderMessages(form.planId.value);
}

// 删除旅行计划前先加载申请人预览，提示确认后再执行删除。
async function deletePlan(planId) {
    const applicants = await api(`/plans/${planId}/delete-preview`);
    const suffix = applicants.length
        ? "\n\n已有员工申请：\n" + applicants.map((a) => `${a.userName} ${a.email}`).join("\n") + "\n\n确认删除将自动取消这些申请。"
        : "";
    if (!confirm("确认删除该旅行计划？" + suffix)) return;
    await api(`/plans/${planId}/delete`, {method: "POST"});
    toast("旅行计划已删除");
    await loadPlans();
}

// 打开“我的申请”弹窗，展示当前用户申请及可执行操作。
async function openMyApps() {
    const apps = await api("/my-applications");
    $("myAppsRows").innerHTML = apps.map((app) => `
        <div class="message">
            <strong>${app.planNo} ${app.destination}</strong>
            <p>人数：${app.applicantCount}，状态：${app.status}，备注：${app.optionText || ""}</p>
            <button data-app="${app.id}" data-count="${app.applicantCount}">修改人员信息</button>
            ${app.status === "ACTIVE" ? `<button class="danger" data-cancel="${app.id}">取消申请</button>` : ""}
        </div>`).join("") || "<p class='muted'>暂无申请</p>";
    $("myAppsDialog").showModal();
}

// 取消指定申请，并刷新我的申请和计划列表。
async function cancelApplication(id) {
    await api(`/applications/${id}/cancel`, {method: "POST"});
    toast("申请已取消");
    await openMyApps();
    await loadPlans();
}

// 转义用户输入内容，避免咨询消息中的 HTML 被浏览器执行。
function escapeHtml(text) {
    return String(text).replace(/[&<>"']/g, (char) => ({'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'}[char]));
}

// 绑定密码输入框的显示/隐藏按钮，并在输入为空时自动恢复隐藏状态。
function setupPasswordToggle(input, toggle, onInput = () => {}) {
    if (!input || !toggle) return () => {};
    const sync = () => {
        const hasPassword = input.value.length > 0;
        toggle.classList.toggle("hidden", !hasPassword);
        if (!hasPassword) {
            input.type = "password";
            toggle.textContent = "\uD83D\uDC41\uFE0F";
            toggle.setAttribute("aria-label", "show password");
        }
    };

    input.addEventListener("input", () => {
        onInput();
        sync();
    });

    toggle.addEventListener("click", () => {
        const showingPassword = input.type === "text";
        input.type = showingPassword ? "password" : "text";
        toggle.textContent = showingPassword ? "\uD83D\uDC41\uFE0F" : "\uD83D\uDE48";
        toggle.setAttribute("aria-label", showingPassword ? "show password" : "hide password");
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

// 初始化登录页：已登录自动进入计划页，登录成功后跳转 plans.jsp。
async function initLoginPage() {
    const loginForm = $("loginForm");
    if (!loginForm) return false;

    setupPasswordToggle(loginForm.password, $("loginPasswordToggle"), () => $("loginError").classList.add("hidden"));
    loginForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        $("loginError").classList.add("hidden");
        try {
            currentUser = await api("/auth/login", {
                method: "POST",
                body: JSON.stringify({employeeNo: loginForm.employeeNo.value, password: loginForm.password.value})
            });
            go("plans.jsp");
        } catch {
            $("loginError").classList.remove("hidden");
        }
    });

    try {
        currentUser = await api("/auth/me");
        go("plans.jsp");
    } catch {
        return true;
    }
    return true;
}

// 绑定计划页按钮、弹窗和表单事件。
function bindPlansPageEvents() {
    document.addEventListener("click", async (event) => {
        const button = event.target.closest("button");
        if (!button) return;
        if (button.dataset.close !== undefined) button.closest("dialog").close();
        const action = button.dataset.action;
        const id = Number(button.dataset.id);
        if (action === "edit") openPlanDialog(plans.find((plan) => plan.id === id));
        if (action === "delete") await deletePlan(id);
        if (action === "apply") await openApplyDialog(id);
        if (action === "consult") await openConsultDialog(id);
        if (button.dataset.app) await openCompanionsDialog(Number(button.dataset.app), Number(button.dataset.count));
        if (button.dataset.cancel) await cancelApplication(Number(button.dataset.cancel));
    });

    const passwordForm = $("passwordForm");
    const syncOldPasswordToggle = setupPasswordToggle(passwordForm.oldPassword, $("oldPasswordToggle"), hidePasswordMessage);
    const syncNewPasswordToggle = setupPasswordToggle(passwordForm.newPassword, $("newPasswordToggle"), hidePasswordMessage);

    $("logoutBtn").onclick = async () => {
        await api("/auth/logout", {method: "POST"});
        go("index.jsp");
    };
    $("searchBtn").onclick = loadPlans;
    $("newPlanBtn").onclick = () => openPlanDialog();
    $("planForm").addEventListener("submit", savePlan);
    $("applyForm").addEventListener("submit", saveApply);
    $("addApplyCompanionBtn").onclick = () => addCompanionRow({}, "applyCompanionsRows");
    $("addCompanionBtn").onclick = () => addCompanionRow();
    $("companionsForm").addEventListener("submit", saveCompanions);
    $("consultForm").addEventListener("submit", sendConsult);
    $("closeConsultBtn").onclick = async () => {
        await api(`/plans/${$("consultForm").planId.value}/consultations/close`, {method: "POST"});
        toast("对话已结束");
    };
    $("myAppsBtn").onclick = openMyApps;
    $("exportPdfBtn").onclick = () => window.open(API_BASE + "/my-applications/export.pdf", "_blank");
    $("passwordBtn").onclick = () => {
        hidePasswordMessage();
        syncOldPasswordToggle();
        syncNewPasswordToggle();
        $("passwordDialog").showModal();
    };
    passwordForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        hidePasswordMessage();
        try {
            await api("/auth/password", {method: "POST", body: JSON.stringify({oldPassword: passwordForm.oldPassword.value, newPassword: passwordForm.newPassword.value})});
            showPasswordMessage("密码修改成功", true);
            $("passwordDialog").close();
            toast("密码修改成功");
        } catch (error) {
            showPasswordMessage(error.message || "密码修改失败");
        }
    });
    $("myAppsRows").addEventListener("click", () => {});
}

// 初始化计划页：未登录跳回登录页，已登录则加载用户信息和计划列表。
async function initPlansPage() {
    if (!$("appView")) return false;
    bindPlansPageEvents();
    try {
        currentUser = await api("/auth/me");
        showPlansPage();
        await loadPlans();
    } catch {
        go("index.jsp");
    }
    return true;
}

// 页面入口：根据当前 JSP 上存在的根元素选择对应初始化流程。
initLoginPage().then((startedLogin) => {
    if (!startedLogin) initPlansPage();
}).catch((error) => toast(error.message));
