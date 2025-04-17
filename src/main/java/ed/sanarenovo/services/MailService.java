package ed.sanarenovo.services;

import ed.sanarenovo.entities.Candidature;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class MailService {
    public void sendMail(Candidature candidature, String newStatut) {
        final String username = "votre.email@gmail.com";
        final String password = "votreMotDePasse";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(candidature.getEmail()));
            message.setSubject("Mise à jour de votre candidature");

            String content = "Bonjour " + candidature.getPrenom() + " " + candidature.getNom() + ",\n\n";
            content += "Le statut de votre candidature a été mis à jour : " + newStatut + "\n\n";
            content += "Cordialement,\nL'équipe de recrutement";

            message.setText(content);
            Transport.send(message);
            System.out.println("Email envoyé avec succès à " + candidature.getEmail());

        } catch (MessagingException e) {
            System.out.println("Erreur lors de l'envoi de l'email: " + e.getMessage());
        }
    }
}