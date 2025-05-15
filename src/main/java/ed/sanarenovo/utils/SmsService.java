package ed.sanarenovo.utils;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SmsService {

    //    public static final String ACCOUNT_SID = "ACab532677926f8de73dbf5b1d7c9a0239";
//    public static final String AUTH_TOKEN = "54c93b48d197748d99aa3dda3c4ef09c";

    public static final String ACCOUNT_SID = "ACc15a142471a04fd9a9ad6c4bdd0e1cb5";
    public static final String AUTH_TOKEN = "e9cdc197059cc8825707ffd5e2e2c68d";

    public SmsService() {
        // Initialiser Twilio avec ton SID et Auth Token
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    public void sendSms(String toPhoneNumber, String message) {
        // Numéro Twilio (doit être celui que tu as acheté sur Twilio)
        String fromPhoneNumber = "+18578323588";

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