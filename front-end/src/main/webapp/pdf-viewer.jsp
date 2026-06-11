<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>旅游计划 PDF 查看</title>
    <style>
        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            font-family: Arial, "Microsoft YaHei", sans-serif;
            color: #1f2937;
            background: #f6f8fb;
        }

        .pdf-toolbar {
            display: flex;
            align-items: center;
            gap: 12px;
            min-height: 60px;
            padding: 10px 20px;
            background: #fff;
            border-bottom: 1px solid #d8dee9;
            flex-wrap: wrap;
        }

        .pdf-title {
            margin-right: auto;
            font-weight: 700;
        }

        .pdf-action {
            border: 1px solid #d8dee9;
            border-radius: 6px;
            padding: 8px 14px;
            background: #fff;
            color: #1f2937;
            cursor: pointer;
            text-decoration: none;
            font: inherit;
        }

        .pdf-action:hover {
            background: #f3f6fb;
        }

        .pdf-frame {
            display: block;
            width: 100%;
            height: calc(100vh - 61px);
            border: 0;
            background: #fff;
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
<body>
<div class="pdf-toolbar">
    <strong id="title" class="pdf-title">旅游计划 PDF</strong>
    <a id="downloadBtn" class="pdf-action" href="javascript:void(0)">下载PDF</a>
    <button id="printBtn" class="pdf-action" type="button">打印PDF</button>
</div>

<iframe id="pdfFrame" class="pdf-frame" title="旅游计划 PDF 预览"></iframe>

<script>
    const API_BASE = window.API_BASE || (
        window.location.protocol + "//" + (window.location.hostname || "localhost") + ":8080/api"
    );
    const params = new URLSearchParams(window.location.search);
    const id = params.get("id");
    const planNo = params.get("planNo") || "travel-plan";
    const pdfUrl = id ? API_BASE + "/plans/" + encodeURIComponent(id) + "/file" : "";

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
