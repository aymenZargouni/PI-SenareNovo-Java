package ed.sanarenovo.utils;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SmsService {

    // SID et Token de ton compte Twilio (remplace par tes informations)
    public static final String ACCOUNT_SID = "AC685b3f0e6c0dce07152a8a2c6caff16f";
    public static final String AUTH_TOKEN = "1f846a3f8bed775f14f6ff36984d7231";

    public SmsService() {
        // Initialiser Twilio avec ton SID et Auth Token
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    public void sendSms(String toPhoneNumber, String message) {
        // Numéro Twilio (doit être celui que tu as acheté sur Twilio)
        String fromPhoneNumber = "+16192730964";

        // Envoi du message via Twilio
        Message messageSent = Message.creator(
                new PhoneNumber(toPhoneNumber), // Numéro destinataire
                new PhoneNumber(fromPhoneNumber), // Numéro Twilio
                message // Message à envoyer
        ).create();

        // Affichage de l'ID du message envoyé
        System.out.println("Message envoyé avec succès. SID: " + messageSent.getSid());
    }
}
