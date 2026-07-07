package com.villamanager.service;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    public String sendInviteEmail(String email, String token, String roleName, String inviterName) {
        String inviteUrl = frontendUrl + "/accept-invite?token=" + token;
        String body = "Hello,\n\n" + inviterName + " invited you to Villa Manager Pro as " + roleName
                + ".\n\nAccept your invite here:\n" + inviteUrl + "\n\nThis invite expires in 7 days.";

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
