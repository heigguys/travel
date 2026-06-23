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

        html,
        body {
            width: 100%;
            height: 100%;
        }

        body {
            margin: 0;
            font-family: "Segoe UI", Arial, "Microsoft YaHei", sans-serif;
            color: #172033;
            background:
                radial-gradient(circle at 18% 0%, rgba(21, 94, 239, .08), transparent 26%),
                linear-gradient(180deg, #f7faff 0%, #f4f7fb 52%, #eef3f8 100%);
            overflow: hidden;
        }

        .pdf-frame {
            display: block;
            width: 100vw;
            height: 100vh;
            margin: 0;
            border: 0;
            border-radius: 0;
            background: #fff;
            box-shadow: none;
        }
    </style>
</head>
<body class="pdf-body">
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

<%@ include file="WEB-INF/jsp/fragments/password-dialog.jspf" %>

<div id="toast" class="toast hidden"></div>
<script src="assets/js/app.js"></script>
<script>
    const PDF_API_BASE = window.API_BASE || (
        window.location.protocol + "//" + (window.location.hostname || "localhost") + ":8080/api"
    );
    const params = new URLSearchParams(window.location.search);
    const id = params.get("id");
    const pdfUrl = id ? PDF_API_BASE + "/plans/" + encodeURIComponent(id) + "/file" : "";

    if (pdfUrl) {
        document.getElementById("pdfFrame").src = pdfUrl;
    } else {
        alert("缺少旅游计划 ID，无法加载 PDF");
    }
</script>
</body>
</html>
