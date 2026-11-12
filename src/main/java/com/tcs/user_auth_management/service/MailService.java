package com.tcs.user_auth_management.service;

import com.tcs.user_auth_management.config.mailSender.MailConfigProperties;
import com.tcs.user_auth_management.model.dto.DtoMailMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MailService {
  private final JavaMailSender javaMailSender;
  private final MailConfigProperties properties;

  public MailService(JavaMailSender javaMailSender, MailConfigProperties properties) {
    this.javaMailSender = javaMailSender;
    this.properties = properties;
  }

  public void sendMail(DtoMailMessage message) {
    try {
      SimpleMailMessage mail = new SimpleMailMessage();
      mail.setFrom(properties.getMailHost());
      mail.setTo(message.email());
      mail.setSubject(message.subject());
      mail.setText(message.message());
      javaMailSender.send(mail);
      log.info("Email sent successfully to: {}", message.email());
    } catch (Exception e) {
      log.error("Failed to send email to {}: {}", message.email(), e.getMessage(), e);
      throw e;
    }
  }

  public void sendForgotPassword(
      String email, String resetToken, String username, boolean isByLink) {
    String message =
        isByLink
            ? buildPasswordResetLinkEmail(username, buildUrlResetToken(resetToken))
            : buildPasswordResetEmailOneTimeToken(username, resetToken);
    sendMail(new DtoMailMessage(email, "Reset Your Password", message));
  }

  @Async
  public void asyncSendForgotPassword(String email, String username, String resetToken) {
    sendForgotPassword(email, resetToken, username, false);
  }

  @Async
  public void asyncSendEmailVerify(String username, String email,String token) {
    sendMail(new DtoMailMessage(email,"Verify Email",buildVerificationEmail(username,token)));
  }

  public String buildPasswordResetEmailOneTimeToken(String username, String token) {
    return String.format(
        """
            Hello %s,

            We received a request to reset the password for your account.
            Please use the following one-time reset token to complete the process:

            Reset Token:
            
            %s

            ⚠️ This token can only be used once and will expire soon for your security.

            If you did not request a password reset, please ignore this email. Your account will remain secure.

            Thank you,
            TCS System Support Team
            """,
        username, token);
  }

  public String buildPasswordResetLinkEmail(String username, String resetLink) {
    return String.format(
        """
            Hello %s,

            We received a request to reset the password for your account.
            Please click the link below to set a new password:

            Reset Password Link: %s

            ⚠️ This link can only be used once and will expire soon for your security.

            If you did not request a password reset, please ignore this email. Your account will remain secure.

            Thank you,
            TCS System Support Team
            """,
        username, resetLink);
  }

  public String buildVerificationEmail(String username, String token) {
    return String.format(
        """
                Hello %s,

                Welcome to TCS System! Please verify your email address.

                Verify Email Token:

                %s

                ⚠️ This link is valid for a limited time. Please complete verification promptly.

                If you did not create an account, please ignore this email. Your email address will not be used.

                Thank you,
                TCS System Support Team
                """,
        username, token);
  }

  public String buildUrlResetToken(String resetToken) {
    return "https://your-app.com/reset-password?token=" + resetToken;
  }
}
