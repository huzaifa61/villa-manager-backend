package com.villamanager.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class EmailService {

    @Autowired
    private ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    @Value("${SENDGRID_API_KEY:}")
    private String sendGridApiKey;

    @Value("${MAIL_FROM:}")
    private String mailFrom;

    public String sendInviteEmail(String email, String token, String roleName, String inviterName) {
        String inviteUrl = frontendUrl + "/accept-invite?token=" + token;
        String body = "Hello,\n\n" + inviterName + " invited you to Villa Manager Pro as " + roleName
                + ".\n\nAccept your invite here:\n" + inviteUrl + "\n\nThis invite expires in 7 days.";

        // Try SendGrid first (works on Railway)
        if (sendGridApiKey != null && !sendGridApiKey.isBlank()) {
            try {
                String from = mailFrom != null && !mailFrom.isBlank() ? mailFrom : fromAddress;
                Email fromEmail = new Email(from);
                Email toEmail = new Email(email);
                Content content = new Content("text/plain", body);
                Mail mail = new Mail(fromEmail, "You're invited to Villa Manager Pro", toEmail, content);
                SendGrid sg = new SendGrid(sendGridApiKey);
                Request request = new Request();
                request.setMethod(Method.POST);
                request.setEndpoint("mail/send");
                request.setBody(mail.build());
                Response response = sg.api(request);
                if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                    System.out.println("SendGrid email sent to " + email);
                    return inviteUrl;
                } else {
                    System.out.println("SendGrid error: " + response.getStatusCode() + " - " + response.getBody());
                }
            } catch (IOException ex) {
                System.out.println("SendGrid exception: " + ex.getMessage());
            }
        }

        // Fallback to SMTP
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null || fromAddress == null || fromAddress.isBlank()) {
            System.out.println("Villa Manager invite email for " + email + ": " + inviteUrl);
            return inviteUrl;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(email);
            message.setSubject("You're invited to Villa Manager Pro");
            message.setText(body);
            mailSender.send(message);
        } catch (MailException ex) {
            System.out.println("Unable to send invite email. Error: " + ex.getMessage() + ". Invite link for " + email + ": " + inviteUrl);
        }
        return inviteUrl;
    }
}
