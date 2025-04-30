package ed.sanarenovo.services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;

public class AvertissementSMSTwilio {

    public static final String ACCOUNT_SID = "ACeec530d5ca86b0cead16c44621d33638";
    public static final String AUTH_TOKEN = "11ca04c2930b14b89d5ea63b86b66bcc";
    public static final String TWILIO_PHONE_NUMBER = "+19403264417";

    public static void sendSMS(String toPhoneNumber, String messageBody) {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        Message message = Message.creator(
                new com.twilio.type.PhoneNumber(toPhoneNumber),
                new com.twilio.type.PhoneNumber(TWILIO_PHONE_NUMBER),
                messageBody
        ).create();
        System.out.println("SMS envoyé: " + message.getSid());
    }
}

