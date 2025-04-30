package ed.sanarenovo.utils;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class MailSender {

    public static void sendEmail(String to, String subject, String body) {
        final String from = "aymen.zargouni1996@gmail.com"; // 🔁 Remplace avec ton email
        final String password = "ntyhkrmqqixlphud";      // 🔁 Mot de passe (ou mot de passe d'application si Gmail)

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com"); // ou ton serveur SMTP
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            System.out.println("E-mail envoyé à : " + to);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
