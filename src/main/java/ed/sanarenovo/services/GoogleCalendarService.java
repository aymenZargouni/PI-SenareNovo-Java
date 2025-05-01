package ed.sanarenovo.services;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import ed.sanarenovo.entities.Claim;
import ed.sanarenovo.utils.CredentialService;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

public class GoogleCalendarService {
    private static final String APPLICATION_NAME = "SanareNovo Claims";

    public void addClaimEvent(Claim claim) throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Calendar service = new Calendar.Builder(
                HTTP_TRANSPORT,
                GsonFactory.getDefaultInstance(),
                CredentialService.getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        Event event = new Event()
                .setSummary("Réclamation #" + claim.getId())
                .setDescription(buildEventDescription(claim))
                .setColorId("6"); // Couleur orange

        EventDateTime start = new EventDateTime()
                .setDateTime(new com.google.api.client.util.DateTime(claim.getCreatedAt().getTime()));
        EventDateTime end = new EventDateTime()
                .setDateTime(new com.google.api.client.util.DateTime(claim.getCreatedAt().getTime() + 3600000));

        event.setStart(start);
        event.setEnd(end);

        if (claim.getTechnicien() != null && claim.getTechnicien().getUser() != null && claim.getTechnicien().getUser().getEmail() != null){
            event.setAttendees(Collections.singletonList(
                    new EventAttendee().setEmail(claim.getTechnicien().getUser().getEmail())
            ));
        }

        service.events().insert("primary", event).execute();
    }

    private String buildEventDescription(Claim claim) {
        return "Équipement: " + claim.getEquipment().getName() + "\n\n" +
                "Description: " + claim.getReclamation() + "\n\n" +
                "Statut: " + claim.getEquipment().getStatus() + "\n" +
                "Technicien: " + (claim.getTechnicien() != null ?
                claim.getTechnicien().getNom() : "Non assigné");
    }
}