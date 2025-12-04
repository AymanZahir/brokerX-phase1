package com.brokerx.adapters.notification;

import com.brokerx.adapters.notification.BrokerxMailProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "brokerx.mail", name = "enabled", havingValue = "true")
public class SmtpOtpDelivery implements OtpDelivery {

  private static final Logger log = LoggerFactory.getLogger(SmtpOtpDelivery.class);

  private final JavaMailSender mailSender;
  private final String from;
  private final String toOverride;

  public SmtpOtpDelivery(
      JavaMailSender mailSender,
      BrokerxMailProperties props) {
    this.mailSender = mailSender;
    this.from = props.getFrom();
    this.toOverride = props.getToOverride();
  }

  @Override
  public void send(String email, String otp, String verificationId) {
    String target = toOverride != null && !toOverride.isBlank() ? toOverride : email;

    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(from);
    message.setTo(target);
    message.setSubject("BrokerX - Code de vérification");
    message.setText(
        """
        Bonjour,

        Voici votre code de vérification BrokerX : %s
        Il expire dans 15 minutes.

        ID de vérification : %s
        """.formatted(otp, verificationId));
    mailSender.send(message);
    log.info("OTP email sent to {}", target);
  }
}
