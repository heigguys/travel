const API_BASE = window.API_BASE || "http://localhost:8080/api";
let currentUser = null;
let plans = [];

const $ = (id) => document.getElementById(id);

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

function toast(message) {
    $("toast").textContent = message;
    $("toast").classList.remove("hidden");
    setTimeout(() => $("toast").classList.add("hidden"), 2600);
}

function showApp() {
    $("loginView").classList.add("hidden");
    $("appView").classList.remove("hidden");
    $("userInfo").textContent = `${currentUser.name}（${currentUser.employeeNo} / ${currentUser.role}）`;
    $("newPlanBtn").classList.toggle("hidden", currentUser.role !== "ADMIN");
}

async function loadMe() {
    try {
        currentUser = await api("/auth/me");
        showApp();
        await loadPlans();
    } catch {
        $("loginView").classList.remove("hidden");
        $("appView").classList.add("hidden");
    }
}

async function loadPlans() {
    const params = new URLSearchParams();
    if ($("keywordInput").value) params.set("keyword", $("keywordInput").value);
    if ($("statusFilter").value) params.set("status", $("statusFilter").value);
    if ($("sortSelect").value) params.set("sort", $("sortSelect").value);
    plans = await api("/plans?" + params.toString());
    renderPlans();
}

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

function openApplyDialog(planId) {
    $("applyForm").reset();
    $("applyForm").planId.value = planId;
    $("applyDialog").showModal();
}

async function saveApply(event) {
    event.preventDefault();
    const form = $("applyForm");
    const app = await api(`/plans/${form.planId.value}/apply`, {
        method: "POST",
        body: JSON.stringify({
            applicantCount: Number(form.applicantCount.value),
            optionText: form.optionText.value
        })
    });
    $("applyDialog").close();
    toast("申请已保存");
    await loadPlans();
    openCompanionsDialog(app.id, Number(form.applicantCount.value));
}

async function openCompanionsDialog(applicationId, count = 1) {
    $("companionsForm").applicationId.value = applicationId;
    const rows = await api(`/applications/${applicationId}/companions`);
    $("companionsRows").innerHTML = "";
    const source = rows.length ? rows : Array.from({length: count}, () => ({}));
    source.forEach(addCompanionRow);
    $("companionsDialog").showModal();
}

function addCompanionRow(row = {}) {
    const div = document.createElement("div");
    div.className = "companion-row";
    div.innerHTML = `
        <input placeholder="姓名" value="${row.name || ""}" required>
        <select><option ${row.gender === "女" ? "selected" : ""}>女</option><option ${row.gender === "男" ? "selected" : ""}>男</option></select>
        <input placeholder="身份证号" value="${row.idCard || ""}" required>
        <select><option value="true" ${row.bedNeeded !== false ? "selected" : ""}>占床</option><option value="false" ${row.bedNeeded === false ? "selected" : ""}>不占床</option></select>
        <button type="button">删除</button>`;
    div.querySelector("button").onclick = () => div.remove();
    $("companionsRows").appendChild(div);
}

async function saveCompanions(event) {
    event.preventDefault();
    const rows = Array.from($("companionsRows").querySelectorAll(".companion-row")).map((row) => {
        const inputs = row.querySelectorAll("input,select");
        return {name: inputs[0].value, gender: inputs[1].value, idCard: inputs[2].value, bedNeeded: inputs[3].value === "true"};
    });
    await api(`/applications/${$("companionsForm").applicationId.value}/companions`, {method: "POST", body: JSON.stringify(rows)});
    $("companionsDialog").close();
    toast("随行人员已保存");
}

async function openConsultDialog(planId) {
    $("consultForm").reset();
    $("consultForm").planId.value = planId;
    await renderMessages(planId);
    $("consultDialog").showModal();
}

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

async function sendConsult(event) {
    event.preventDefault();
    const form = $("consultForm");
    await api(`/plans/${form.planId.value}/consultations`, {method: "POST", body: JSON.stringify({content: form.content.value})});
    form.content.value = "";
    await renderMessages(form.planId.value);
}

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

async function cancelApplication(id) {
    await api(`/applications/${id}/cancel`, {method: "POST"});
    toast("申请已取消");
    await openMyApps();
    await loadPlans();
}

function escapeHtml(text) {
    return String(text).replace(/[&<>"']/g, (char) => ({'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'}[char]));
}

document.addEventListener("click", async (event) => {
    const button = event.target.closest("button");
    if (!button) return;
    if (button.dataset.close !== undefined) button.closest("dialog").close();
    const action = button.dataset.action;
    const id = Number(button.dataset.id);
    if (action === "edit") openPlanDialog(plans.find((plan) => plan.id === id));
    if (action === "delete") await deletePlan(id);
    if (action === "apply") openApplyDialog(id);
    if (action === "consult") await openConsultDialog(id);
    if (button.dataset.app) await openCompanionsDialog(Number(button.dataset.app), Number(button.dataset.count));
    if (button.dataset.cancel) await cancelApplication(Number(button.dataset.cancel));
});

function setupPasswordToggle(input, toggle, onInput = () => {}) {
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

function showPasswordMessage(message, success = false) {
    const messageEl = $("passwordMessage");
    messageEl.textContent = message;
    messageEl.classList.toggle("form-error", !success);
    messageEl.classList.toggle("form-success", success);
    messageEl.classList.remove("hidden");
}

function hidePasswordMessage() {
    $("passwordMessage").classList.add("hidden");
}

setupPasswordToggle($("loginForm").password, $("loginPasswordToggle"), () => $("loginError").classList.add("hidden"));
const passwordForm = $("passwordForm");
const syncOldPasswordToggle = setupPasswordToggle(passwordForm.oldPassword, $("oldPasswordToggle"), hidePasswordMessage);
const syncNewPasswordToggle = setupPasswordToggle(passwordForm.newPassword, $("newPasswordToggle"), hidePasswordMessage);

$("loginForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    $("loginError").classList.add("hidden");
    try {
        currentUser = await api("/auth/login", {method: "POST", body: JSON.stringify({employeeNo: form.employeeNo.value, password: form.password.value})});
        showApp();
        await loadPlans();
    } catch {
        $("loginError").classList.remove("hidden");
    }
});
$("logoutBtn").onclick = async () => { await api("/auth/logout", {method: "POST"}); location.reload(); };
$("searchBtn").onclick = loadPlans;
$("newPlanBtn").onclick = () => openPlanDialog();
$("planForm").addEventListener("submit", savePlan);
$("applyForm").addEventListener("submit", saveApply);
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
$("passwordForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = $("passwordForm");
    hidePasswordMessage();
    try {
        await api("/auth/password", {method: "POST", body: JSON.stringify({oldPassword: form.oldPassword.value, newPassword: form.newPassword.value})});
        showPasswordMessage("\u5bc6\u7801\u4fee\u6539\u6210\u529f", true);
        $("passwordDialog").close();
        toast("\u5bc6\u7801\u4fee\u6539\u6210\u529f");
    } catch (error) {
        showPasswordMessage(error.message || "\u5bc6\u7801\u4fee\u6539\u5931\u8d25");
    }
});
$("myAppsRows").addEventListener("click", () => {});
loadMe().catch((error) => toast(error.message));
