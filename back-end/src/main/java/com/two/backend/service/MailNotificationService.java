package com.two.backend.service;

import com.two.backend.model.Application;
import com.two.backend.model.TravelPlan;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
/**
 * 邮件通知服务，负责向已申请旅行计划的员工发送计划取消通知。
 */
public class MailNotificationService {
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.mail.from:no-reply@zhimingsoft.com}")
    private String from;

    public MailNotificationService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    /**
     * 向所有已申请员工发送旅行计划取消通知。
     *
     * @param plan 被取消的旅行计划
     * @param applicants 有效申请员工列表
     */
    public void sendPlanCancelNotice(TravelPlan plan, List<Application> applicants) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            throw new BusinessException("邮件服务未配置，请先配置 spring.mail.host");
        }
        for (Application applicant : applicants) {
            if (applicant.getEmail() == null || applicant.getEmail().isBlank()) {
                continue;
            }
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(applicant.getEmail());
            message.setSubject("旅游计划取消通知：" + plan.getDestination());
            message.setText("""
                    %s 您好：

                    您已申请的旅游计划已取消。

                    计划编号：%s
                    目的地：%s
                    启程日：%s
                    返回日：%s

                    请知悉。如有疑问，请联系管理员。
                    """.formatted(
                    applicant.getUserName(),
                    plan.getPlanNo(),
                    plan.getDestination(),
                    plan.getStartDate(),
                    plan.getEndDate()
            ));
            try {
                mailSender.send(message);
            } catch (MailException exception) {
                throw new BusinessException("邮件通知发送失败：" + applicant.getEmail());
            }
        }
    }
}
