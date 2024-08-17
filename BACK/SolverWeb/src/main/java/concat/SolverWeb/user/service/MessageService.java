package concat.SolverWeb.user.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    @Autowired
    private JavaMailSender emailSender;

    @Value("${email.from.address}")
    private String fromEmail;

    @Value("${email.from.name}")
    private String fromName;

    public void sendSimpleMessage(String to, String subject, String text) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

        helper.setFrom(new InternetAddress(fromEmail, fromName));
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text);

        emailSender.send(message);
    }
}
