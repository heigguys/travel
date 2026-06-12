const API_BASE = window.API_BASE || `${window.location.protocol}//${window.location.hostname || "localhost"}:8080/api`;
// 当前登录用户和计划列表缓存，供计划页渲染与事件处理复用。
let currentUser = null;
let plans = [];

// 简化 DOM 查询写法，所有调用都按元素 id 获取节点。
const $ = (id) => document.getElementById(id);

// 将用户角色整数转换为显示文字（0=管理员，1=普通用户）。
const roleLabel = (role) => Number(role) === 0 ? "管理员" : "普通用户";

// 将申请状态整数转换为显示文字（0=申请成功，1=取消）。
const applicationStatusLabel = (status) => Number(status) === 0 ? "申请成功" : "取消";

// 将价格统一显示为人民币格式，例如 ¥2,280。
const formatPrice = (price) => {
    const value = Number(price);
    if (!Number.isFinite(value)) return "";
    return `¥${value.toLocaleString("en-US", {maximumFractionDigits: 0})}`;
};

// 将旅行计划状态整数转换为显示文字。
const planStatusLabel = (status) => {
    const map = {0: "可申请", 1: "已成团", 2: "进行中", 3: "已结束", 4: "未成团"};
    return map[Number(status)] ?? "";
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
    $("userInfo").textContent = `${currentUser.name}（${roleLabel(currentUser.role)}）`;
    $("newPlanBtn").classList.toggle("hidden", Number(currentUser.role) !== 0);
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
    const admin = Number(currentUser.role) === 0;
    const headers = ["状态", "旅游计划编号", "目的地", "往返日期", "价格", "定员数", "申请总人数", "我的申请人数"];
    if (admin) headers.push("公开状态");
    headers.push("操作");
    $("planHeader").innerHTML = headers.map((h) => `<th>${h}</th>`).join("");
    $("planRows").innerHTML = plans.map((plan) => {
        const pdfViewerUrl = `pdf-viewer.jsp?id=${encodeURIComponent(plan.id)}&planNo=${encodeURIComponent(plan.planNo)}`;
        const fileLink = plan.filePath
            ? `<a href="${pdfViewerUrl}" target="_blank" rel="noopener">${plan.planNo}</a>`
            : plan.planNo;
        const adminCells = admin ? `<td>${plan.published ? "已公开" : "未公开"}</td>` : "";
        const adminActions = admin
            ? `${actionButton("edit", plan.id, "编辑")}${actionButton("delete", plan.id, "删除", "danger")}`
            : "";
        return `<tr>
            <td>${planStatusLabel(plan.status)}</td>
            <td>${fileLink}</td>
            <td>${plan.destination}</td>
            <td>${plan.startDate} - ${plan.endDate}</td>
            <td>${formatPrice(plan.price)}</td>
            <td>${plan.capacity}</td>
            <td>${plan.applicantTotal || 0}</td>
            <td>${plan.myApplicantCount || 0}</td>
            ${adminCells}
            <td>
                <div class="plan-actions">
                    ${adminActions}
                    ${actionButton("apply", plan.id, "申请")}
                    ${actionButton("consult", plan.id, "咨询")}
                </div>
            </td>
        </tr>`;
    }).join("");
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
        <div class="message ${Number(msg.senderRole) === 0 ? "admin" : ""}">
            <strong>${Number(msg.senderRole) === 0 ? "管理员" : msg.userName}</strong>
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

// 删除旅行计划前先加载申请人预览，打开确认弹窗。
async function deletePlan(planId) {
    const applicants = await api(`/plans/${planId}/delete-preview`);
    $("deleteForm").planId.value = planId;
    $("deleteApplicants").innerHTML = applicants.length
        ? applicants.map((a) => `
            <div class="message">
                <strong>${escapeHtml(a.userName || "")}</strong>
                <p>${escapeHtml(a.email || "")}</p>
            </div>
        `).join("")
        : "<p class='muted'>暂无员工申请</p>";
    $("deleteDialog").showModal();
}

// 确认删除旅行计划，并刷新列表。
async function confirmDeletePlan(event) {
    event.preventDefault();
    const planId = $("deleteForm").planId.value;
    await api(`/plans/${planId}/delete`, {method: "POST"});
    $("deleteDialog").close();
    toast("旅行计划已删除");
    await loadPlans();
}

// 打开"我的申请"弹窗，展示当前用户申请及可执行操作。
async function openMyApps() {
    const apps = await api("/my-applications");
    $("myAppsRows").innerHTML = apps.map((app) => `
        <div class="message">
            <strong>${app.planNo} ${app.destination}</strong>
            <p>人数：${app.applicantCount}，状态：${applicationStatusLabel(app.status)}，备注：${app.optionText || ""}</p>
            <button data-app="${app.id}" data-count="${app.applicantCount}">修改人员信息</button>
            ${Number(app.status) === 0 ? `<button class="danger" data-cancel="${app.id}">取消申请</button>` : ""}
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
        } catch (error) {
            const message = error.message || "";
            $("loginError").textContent = message.includes("密码") ? "账号或密码错误" : "无法连接服务器，请检查后端地址或网络";
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

// 年填满4位自动跳月，月填满2位自动跳日；左右箭头键在格边界时跨格移动光标。
function setupDateAutoJump(fieldId) {
    const field = $(fieldId);
    if (!field) return;
    const [y, m, d] = field.querySelectorAll("input");
    const parts = [y, m, d];

    // 输入满位自动跳下一格
    y.addEventListener("input", () => { if (y.value.replace(/\D/g,"").length === 4) m.focus(); });
    m.addEventListener("input", () => { if (m.value.replace(/\D/g,"").length >= 2) d.focus(); });

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
}

// 绑定计划列表页按钮、弹窗和表单事件。
function bindPlansPageEvents() {
    document.addEventListener("click", async (event) => {
        const button = event.target.closest("button");
        if (!button) return;
        if (button.dataset.close !== undefined) button.closest("dialog").close();
        const action = button.dataset.action;
        const id = Number(button.dataset.id);
        // 编辑和申请跳转到独立页面，通过 URL 参数传递计划 ID。
        if (action === "edit") go("plan-edit.jsp?id=" + id);
        if (action === "delete") await deletePlan(id);
        if (action === "apply") go("plan-apply.jsp?planId=" + id);
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
    // 新增计划跳转到编辑页（无 id 参数 = 新增模式）。
    $("newPlanBtn").onclick = () => go("plan-edit.jsp");
    $("deleteForm").addEventListener("submit", confirmDeletePlan);
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
    showPlansPage();
    try {
        await loadPlans();
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

    const params = new URLSearchParams(window.location.search);
    const id = params.get("id");
    const form = $("planEditForm");
    $("planEditTitle").textContent = id ? "编辑旅行计划" : "添加旅行计划";

    if (id) {
        try {
            const plan = await api(`/plans/${id}`);
            form.id.value = plan.id;
            form.destination.value = plan.destination || "";
            // 将 YYYY-MM-DD 拆分回三个输入格。
            const [sy = "", sm = "", sd = ""] = (plan.startDate || "").split("-");
            form.startYear.value = sy;
            form.startMonth.value = sm ? parseInt(sm, 10) : "";
            form.startDay.value = sd ? parseInt(sd, 10) : "";
            const [ey = "", em = "", ed = ""] = (plan.endDate || "").split("-");
            form.endYear.value = ey;
            form.endMonth.value = em ? parseInt(em, 10) : "";
            form.endDay.value = ed ? parseInt(ed, 10) : "";
            form.price.value = plan.price || "";
            form.capacity.value = plan.capacity || "";
            form.published.checked = Boolean(plan.published);
        } catch (error) {
            toast(error.message || "加载计划失败");
        }
    }

    // 复用分段日期自动跳格逻辑。
    setupDateAutoJump("startDateField");
    setupDateAutoJump("endDateField");

    // 目的地离焦校验。
    const destInput = form.destination;
    destInput.addEventListener("blur", () => {
        destInput.setCustomValidity(destInput.value.length > 10 ? "目的地不能超过10个字符" : "");
        if (destInput.value.length > 10) destInput.reportValidity();
    });
    destInput.addEventListener("input", () => destInput.setCustomValidity(""));

    $("planEditBackBtn").onclick = () => go("plans.jsp");
    $("planEditCancelBtn").onclick = () => go("plans.jsp");

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

        const sy = form.startYear.value, sm = form.startMonth.value, sd = form.startDay.value;
        const ey = form.endYear.value, em = form.endMonth.value, ed = form.endDay.value;
        if (!sy || !sm || !sd || !ey || !em || !ed) {
            errorEl.textContent = "请填写完整的启程日和返回日";
            errorEl.classList.remove("hidden");
            return;
        }

        const startDate = `${sy.padStart(4, "0")}-${sm.padStart(2, "0")}-${sd.padStart(2, "0")}`;
        const endDate = `${ey.padStart(4, "0")}-${em.padStart(2, "0")}-${ed.padStart(2, "0")}`;
        const data = new FormData(form);
        data.set("startDate", startDate);
        data.set("endDate", endDate);
        ["startYear", "startMonth", "startDay", "endYear", "endMonth", "endDay"].forEach(k => data.delete(k));
        data.set("published", form.published.checked ? "true" : "false");
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
        $("applyPlanInfo").textContent = `${plan.destination}　${plan.startDate} ～ ${plan.endDate}　${formatPrice(plan.price)}`;
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
            const source = rows.length ? rows : Array.from({length: fallbackCount}, () => ({}));
            source.forEach((row) => addCompanionRow(row, "planApplyCompanionsRows"));
        } else {
            addCompanionRow({}, "planApplyCompanionsRows");
        }
    } catch {
        addCompanionRow({}, "planApplyCompanionsRows");
    }

    $("planApplyBackBtn").onclick = () => go("plans.jsp");
    $("planApplyCancelBtn").onclick = () => go("plans.jsp");
    $("addPlanApplyCompanionBtn").onclick = () => addCompanionRow({}, "planApplyCompanionsRows");

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        const errorEl = $("planApplyError");
        errorEl.classList.add("hidden");
        const rows = collectCompanionRows("planApplyCompanionsRows");
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

// 页面入口：依次检测各页面根元素，匹配到对应页面后执行其初始化函数。
initLoginPage()
    .then((started) => started || initPlanEditPage())
    .then((started) => started || initPlanApplyPage())
    .then((started) => started || initPlansPage())
    .catch((error) => toast(error.message));
