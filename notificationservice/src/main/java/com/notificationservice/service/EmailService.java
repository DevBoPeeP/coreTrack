package com.notificationservice.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${notification.email.from}")
    private String fromAddress;

    @Value("${notification.email.from-name}")
    private String fromName;

    @Value("${notification.email.enabled}")
    private boolean emailEnabled;

    /**
     * Sends an HTML email using a Thymeleaf template asynchronously.
     *
     * @param to           recipient email address
     * @param subject      email subject
     * @param templateName name of Thymeleaf template (without .html)
     * @param context      variables to inject into template
     */
    @Async("notificationExecutor")
    public void sendTemplatedEmail(String to, String subject,
                                   String templateName, Context context) {
        if (!emailEnabled) {
            log.debug("Email disabled — skipping email to {}", to);
            return;
        }

        try {
            String htmlContent = templateEngine.process(templateName, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email sent to={} subject={}", to, subject);

        } catch (MessagingException e) {
            log.error("Failed to send email to={} subject={}", to, subject, e);
        } catch (Exception e) {
            log.error("Unexpected error sending email to={}", to, e);
        }
    }

    // ── Convenience helpers for each notification type ───────

    public void sendTaskCreatedEmail(String to, String recipientName,
                                     String taskTitle, String projectName) {
        Context ctx = new Context();
        ctx.setVariable("recipientName", recipientName);
        ctx.setVariable("taskTitle", taskTitle);
        ctx.setVariable("projectName", projectName);
        sendTemplatedEmail(to, "New Task Created: " + taskTitle, "task-created", ctx);
    }

    public void sendTaskAssignedEmail(String to, String recipientName,
                                      String taskTitle, String assignedBy) {
        Context ctx = new Context();
        ctx.setVariable("recipientName", recipientName);
        ctx.setVariable("taskTitle", taskTitle);
        ctx.setVariable("assignedBy", assignedBy);
        sendTemplatedEmail(to, "You've been assigned: " + taskTitle, "task-assigned", ctx);
    }

    public void sendTaskUpdatedEmail(String to, String recipientName,
                                     String taskTitle) {
        Context ctx = new Context();
        ctx.setVariable("recipientName", recipientName);
        ctx.setVariable("taskTitle", taskTitle);
        sendTemplatedEmail(to, "Task Updated: " + taskTitle, "task-updated", ctx);
    }

    public void sendCommentMentionEmail(String to, String recipientName,
                                        String authorName, String taskTitle,
                                        String commentPreview) {
        Context ctx = new Context();
        ctx.setVariable("recipientName", recipientName);
        ctx.setVariable("authorName", authorName);
        ctx.setVariable("taskTitle", taskTitle);
        ctx.setVariable("commentPreview", commentPreview);
        sendTemplatedEmail(to, authorName + " mentioned you in a comment", "comment-mention", ctx);
    }

    public void sendTeamAddedEmail(String to, String recipientName,
                                   String teamName) {
        Context ctx = new Context();
        ctx.setVariable("recipientName", recipientName);
        ctx.setVariable("teamName", teamName);
        sendTemplatedEmail(to, "You've been added to team: " + teamName, "team-added", ctx);
    }

    public void sendProjectCreatedEmail(String to, String recipientName,
                                        String projectName) {
        Context ctx = new Context();
        ctx.setVariable("recipientName", recipientName);
        ctx.setVariable("projectName", projectName);
        sendTemplatedEmail(to, "New Project: " + projectName, "project-created", ctx);
    }
}
