<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>旅游计划 PDF 查看</title>
    <link rel="stylesheet" href="assets/css/app.css">
    <style>
        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            font-family: "Segoe UI", Arial, "Microsoft YaHei", sans-serif;
            color: #172033;
            background:
                radial-gradient(circle at 18% 0%, rgba(21, 94, 239, .08), transparent 26%),
                linear-gradient(180deg, #f7faff 0%, #f4f7fb 52%, #eef3f8 100%);
        }

        .pdf-toolbar {
            display: flex;
            align-items: center;
            gap: 12px;
            min-height: 68px;
            margin: 14px;
            padding: 12px 16px;
            background: rgba(255, 255, 255, .94);
            border: 1px solid #e3e8ef;
            border-radius: 8px;
            box-shadow: 0 10px 28px rgba(15, 23, 42, .10);
            flex-wrap: wrap;
        }

        .pdf-title {
            margin-right: auto;
            color: #111827;
            font-weight: 700;
        }

        .pdf-action {
            min-height: 38px;
            border: 1px solid #cbd5e1;
            border-radius: 6px;
            padding: 8px 14px;
            background: #fff;
            color: #172033;
            cursor: pointer;
            text-decoration: none;
            font: inherit;
            font-weight: 600;
            box-shadow: 0 1px 2px rgba(15, 23, 42, .06);
            transition: border-color .16s ease, background-color .16s ease, transform .16s ease;
        }

        .pdf-action:hover {
            border-color: #155eef;
            background: #eef4ff;
            transform: translateY(-1px);
        }

        .pdf-frame {
            display: block;
            width: calc(100% - 28px);
            height: calc(100vh - 178px);
            margin: 0 14px 14px;
            border: 1px solid #e3e8ef;
            border-radius: 8px;
            background: #fff;
            box-shadow: 0 10px 28px rgba(15, 23, 42, .10);
        }

        @media (max-width: 560px) {
            .pdf-toolbar {
                align-items: stretch;
            }

            .pdf-title {
                width: 100%;
                margin-right: 0;
            }

            .pdf-action {
                flex: 1;
                text-align: center;
            }
        }
    </style>
</head>
<body class="pdf-body">
<nav class="global-nav">
    <div class="global-brand">公司旅行管理系统</div>
    <div id="userInfo" class="global-user"></div>
    <div class="global-actions">
        <button id="myAppsBtn" type="button">我的申请</button>
        <button id="passwordBtn" type="button">修改密码</button>
        <button id="logoutBtn" type="button">退出</button>
    </div>
</nav>
<nav class="sub-nav" aria-label="页面导览">
    <div class="sub-nav-inner">
        <a href="index.jsp?showLogin=1">登录</a>
        <span class="sub-nav-separator">›</span>
        <a href="plans.jsp">旅游计划一览</a>
        <span class="sub-nav-separator">›</span>
        <strong>旅游计划 PDF</strong>
    </div>
</nav>
<div class="pdf-toolbar">
    <strong id="title" class="pdf-title">旅游计划 PDF</strong>
    <a id="downloadBtn" class="pdf-action" href="javascript:void(0)">下载PDF</a>
    <button id="printBtn" class="pdf-action" type="button">打印PDF</button>
</div>

<iframe id="pdfFrame" class="pdf-frame" title="旅游计划 PDF 预览"></iframe>

<dialog id="companionsDialog">
    <form id="companionsForm" method="dialog" class="stack">
        <h2>修改随行人员信息</h2>
        <input type="hidden" name="applicationId">
        <div id="companionsRows" class="stack"></div>
        <button id="addCompanionBtn" type="button">新增随行人员</button>
        <div class="dialog-actions">
            <button value="cancel" type="button" data-close>取消</button>
            <button class="primary" type="submit">保存</button>
        </div>
    </form>
</dialog>

<dialog id="myAppsDialog">
    <div class="stack">
        <h2>我的申请</h2>
        <div id="myAppsRows" class="stack"></div>
        <div class="dialog-actions">
            <button id="exportPdfBtn" type="button">导出 PDF</button>
            <button type="button" data-close>关闭</button>
        </div>
    </div>
</dialog>

<dialog id="passwordDialog">
    <form id="passwordForm" method="dialog" class="stack">
        <h2>修改密码</h2>
        <label>原密码
            <span class="password-field">
                <input name="oldPassword" type="password" required>
                <button id="oldPasswordToggle" class="password-toggle hidden" type="button" aria-label="显示密码"></button>
            </span>
        </label>
        <label>新密码
            <span class="password-field">
                <input name="newPassword" type="password" required minlength="6">
                <button id="newPasswordToggle" class="password-toggle hidden" type="button" aria-label="显示密码"></button>
            </span>
        </label>
        <label>确认密码
            <span class="password-field">
                <input name="confirmPassword" type="password" required minlength="6">
                <button id="confirmPasswordToggle" class="password-toggle hidden" type="button" aria-label="显示密码"></button>
            </span>
        </label>
        <p id="passwordMessage" class="form-error hidden"></p>
        <div class="dialog-actions">
            <button value="cancel" type="button" data-close>取消</button>
            <button class="primary" type="submit">保存</button>
        </div>
    </form>
</dialog>

<div id="toast" class="toast hidden"></div>
<script src="assets/js/app.js"></script>
<script>
    const PDF_API_BASE = window.API_BASE || (
        window.location.protocol + "//" + (window.location.hostname || "localhost") + ":8080/api"
    );
    const params = new URLSearchParams(window.location.search);
    const id = params.get("id");
    const planNo = params.get("planNo") || "travel-plan";
    const pdfUrl = id ? PDF_API_BASE + "/plans/" + encodeURIComponent(id) + "/file" : "";

    document.getElementById("title").textContent = "旅游计划：" + planNo;

    if (pdfUrl) {
        document.getElementById("pdfFrame").src = pdfUrl;
    } else {
        alert("缺少旅游计划 ID，无法加载 PDF");
    }

    document.getElementById("downloadBtn").onclick = async function () {
        if (!pdfUrl) return;
        try {
            const response = await fetch(pdfUrl, {credentials: "include"});
            if (!response.ok) throw new Error("PDF下载失败");

            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const link = document.createElement("a");
            link.href = url;
            link.download = (planNo || "travel-plan") + ".pdf";
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(url);
        } catch (error) {
            alert(error.message || "PDF下载失败");
        }
    };

    document.getElementById("printBtn").onclick = function () {
        if (!pdfUrl) return;
        const printWindow = window.open(pdfUrl, "_blank", "width=900,height=700");
        if (!printWindow) {
            alert("请允许浏览器弹出窗口后再打印");
            return;
        }

        setTimeout(function () {
            try {
                printWindow.focus();
                printWindow.print();
            } catch {
                alert("浏览器限制了自动打印，请在打开的 PDF 页面中手动打印");
            }
        }, 1200);
    };
</script>
</body>
</html>
