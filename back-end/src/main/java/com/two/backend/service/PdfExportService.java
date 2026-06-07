package com.two.backend.service;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.two.backend.model.Application;
import com.two.backend.model.User;
import java.io.ByteArrayOutputStream;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
/**
 * PDF 导出服务，负责把当前用户的旅行申请列表渲染为 PDF 字节。
 */
public class PdfExportService {
    /**
     * 导出当前用户的申请列表 PDF。
     *
     * @param user 当前用户
     * @param applications 当前用户申请列表
     * @return PDF 文件字节
     */
    public byte[] exportApplications(User user, List<Application> applications) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, outputStream);
            document.open();
            BaseFont baseFont = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font titleFont = new Font(baseFont, 18, Font.BOLD);
            Font textFont = new Font(baseFont, 11);
            document.add(new Paragraph("我的旅行申请列表", titleFont));
            document.add(new Paragraph("员工：" + user.getName() + "（" + user.getEmployeeNo() + "）", textFont));
            document.add(new Paragraph(" ", textFont));
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            for (String header : List.of("计划编号", "目的地", "人数", "状态", "选项")) {
                table.addCell(new Paragraph(header, textFont));
            }
            for (Application application : applications) {
                table.addCell(new Paragraph(nullToBlank(application.getPlanNo()), textFont));
                table.addCell(new Paragraph(nullToBlank(application.getDestination()), textFont));
                table.addCell(new Paragraph(String.valueOf(application.getApplicantCount()), textFont));
                table.addCell(new Paragraph(applicationStatusLabel(application.getStatus()), textFont));
                table.addCell(new Paragraph(nullToBlank(application.getOptionText()), textFont));
            }
            document.add(table);
            document.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new BusinessException("PDF 导出失败");
        }
    }

    /**
     * 将申请状态整数转换为可读文字。
     *
     * @param status 申请状态（0=有效，1=已取消）
     * @return 状态文字
     */
    private String applicationStatusLabel(Integer status) {
        if (Integer.valueOf(Application.STATUS_ACTIVE).equals(status))   return "申请成功";
        if (Integer.valueOf(Application.STATUS_CANCELED).equals(status)) return "取消";
        return "";
    }

    /**
     * 将可能为空的字符串转换为空串，避免 PDF 单元格出现 null 文本。
     *
     * @param value 原始文本
     * @return 非 null 文本
     */
    private String nullToBlank(String value) {
        return value == null ? "" : value;
    }
}
