package ed.sanarenovo.services;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import ed.sanarenovo.entities.Claim;
import ed.sanarenovo.entities.Equipment;
import ed.sanarenovo.entities.Technicien;
import ed.sanarenovo.interfaces.IService;
import ed.sanarenovo.utils.CredentialService;
import ed.sanarenovo.utils.MyConnection;
import ed.sanarenovo.utils.SmsService;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.sql.Date;
import java.util.List;
import com.google.api.services.calendar.model.EventReminder;


public class ClaimService implements IService<Claim> {
    public boolean containsBadWords(String text) {
        List<String> badWords = Arrays.asList(
                "merde", "putain", "con", "salop", "chiant"
        );
        for (String word : badWords) {
            if (text.toLowerCase().contains(word)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addEntity(Claim claim) {
        if (containsBadWords(claim.getReclamation())) {
            System.err.println("Réclamation refusée : langage inapproprié détecté.");
            return;
        } else {
            try {
                String sql = "INSERT INTO claim (reclamation, equipment_id, technicien_id, created_at) VALUES (?, ?, ?, ?)";
                PreparedStatement statement = MyConnection.getInstance().getCnx()
                        .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

                statement.setString(1, claim.getReclamation());
                statement.setInt(2, claim.getEquipment().getId());
                statement.setInt(3, claim.getTechnicien().getId());
                statement.setTimestamp(4, claim.getCreatedAt());

                statement.executeUpdate();

                // Récupérer l'ID généré
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    claim.setId(generatedKeys.getInt(1));
                }

                System.out.println("Réclamation enregistrée avec succès!");

                // Ajout au calendrier
                addToGoogleCalendar(claim);

            } catch (SQLException e) {
                System.err.println("Erreur ajout réclamation: " + e.getMessage());
            } catch (GeneralSecurityException | IOException e) {
                System.err.println("Erreur lors de l'ajout au calendrier: " + e.getMessage());
            }
        }
    }

private void addToGoogleCalendar(Claim claim) throws GeneralSecurityException, IOException {
    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    Calendar service = new Calendar.Builder(
            HTTP_TRANSPORT,
            GsonFactory.getDefaultInstance(),
            CredentialService.getCredentials(HTTP_TRANSPORT))
            .setApplicationName("SanareNovo Claims")
            .build();

    // Formater la description avec plus de détails
    String description = "Détails de la réclamation:\n\n" +
            "ID: " + claim.getId() + "\n" +
            "Équipement: " + claim.getEquipment().getName() + "\n" +
            "Statut: " + claim.getEquipment().getStatus() + "\n" +
            "Description: " + claim.getReclamation() + "\n\n" +
            "Technicien assigné: " +
            (claim.getTechnicien() != null ? claim.getTechnicien().getNom() : "Non assigné");

    Event event = new Event()
            .setSummary("[Réclamation #" + claim.getId() + "] " + claim.getEquipment().getName())
            .setDescription(description)
            .setColorId("6"); // Couleur orange pour les réclamations

    Date startDate = new Date(claim.getCreatedAt().getTime());
    Date endDate = new Date(startDate.getTime() + 3600000); // +1h

    event.setStart(new EventDateTime().setDateTime(new com.google.api.client.util.DateTime(startDate)));
    event.setEnd(new EventDateTime().setDateTime(new com.google.api.client.util.DateTime(endDate)));

    if (claim.getTechnicien() != null && claim.getTechnicien().getEmail() != null) {
        event.setAttendees(Collections.singletonList(
                new EventAttendee().setEmail(claim.getTechnicien().getEmail())
                        .setDisplayName(claim.getTechnicien().getNom())
        ));

        // Configuration des rappels (corrigé)
        EventReminder[] reminderOverrides = new EventReminder[] {
                new EventReminder().setMethod("email").setMinutes(10),
                new EventReminder().setMethod("popup").setMinutes(30)
        };

        Event.Reminders reminders = new Event.Reminders()
                .setUseDefault(false)
                .setOverrides(Arrays.asList(reminderOverrides));

        event.setReminders(reminders);
    }

    service.events().insert("primary", event).execute();
}

    // Toutes les autres méthodes restent inchangées
    @Override
    public void updateEntity(Claim claim, int id) {
        if (isClaimOlderThan24Hours(id)) {
            System.err.println("Impossible de modifier une réclamation après 24 heures");
            return;
        }
        String query = "UPDATE claim SET reclamation = ?, created_at = ? WHERE id = ?";

        try (Connection connection = MyConnection.getInstance().getCnx();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, claim.getReclamation());
            statement.setTimestamp(2, claim.getCreatedAt());
            statement.setInt(3, id);

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Réclamation mise à jour avec succès");
            } else {
                System.out.println("Aucune réclamation trouvée avec cet ID");
            }

        } catch (SQLException e) {
            System.err.println("Erreur mise à jour réclamation: " + e.getMessage());
        }
    }

    @Override
    public void deleteEntity(int id) {
        if (isClaimOlderThan24Hours(id)) {
            System.err.println("Impossible de supprimer une réclamation après 24 heures");
            return;
        }

        String sql = "DELETE FROM claim WHERE id = ?";

        try (Connection connection = MyConnection.getInstance().getCnx();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Réclamation supprimée avec succès.");
            } else {
                System.out.println("Aucune réclamation trouvée avec l'ID : " + id);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la réclamation : " + e.getMessage());
        }
    }

    public boolean isClaimOlderThan24Hours(int claimId) {
        String query = "SELECT created_at FROM claim WHERE id = ?";

        try (Connection connection = MyConnection.getInstance().getCnx();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, claimId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                Timestamp createdAt = rs.getTimestamp("created_at");
                long claimTime = createdAt.getTime();
                long currentTime = System.currentTimeMillis();
                long diffHours = (currentTime - claimTime) / (60 * 60 * 1000);

                return diffHours > 24;
            }
        } catch (SQLException e) {
            System.err.println("Erreur vérification âge réclamation: " + e.getMessage());
        }
        return true;
    }

    @Override
    public List<Claim> getAll() {
        List<Claim> claims = new ArrayList<>();
        String query = "SELECT c.*, e.status as equipment_status, t.nom as technicien_nom " +
                "FROM claim c " +
                "JOIN equipment e ON c.equipment_id = e.id " +
                "LEFT JOIN technicien t ON c.technicien_id = t.id " +
                "ORDER BY c.created_at DESC";

        try (Connection connection = MyConnection.getInstance().getCnx();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Claim claim = new Claim();
                claim.setId(resultSet.getInt("id"));
                claim.setReclamation(resultSet.getString("reclamation"));
                claim.setCreatedAt(resultSet.getTimestamp("created_at"));

                Equipment equipment = new Equipment();
                equipment.setId(resultSet.getInt("equipment_id"));
                equipment.setStatus(resultSet.getString("equipment_status"));
                claim.setEquipment(equipment);

                if (resultSet.getInt("technicien_id") > 0) {
                    Technicien technicien = new Technicien();
                    technicien.setId(resultSet.getInt("technicien_id"));
                    technicien.setNom(resultSet.getString("technicien_nom"));
                    claim.setTechnicien(technicien);
                }

                claims.add(claim);
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération réclamations: " + e.getMessage());
            e.printStackTrace();
        }
        return claims;
    }

    public void claimEquipment(Equipment equipment, int technicienId, String description) {
        if (containsBadWords(description)) {
            System.err.println("Réclamation refusée : langage inapproprié détecté.");
            return;
        }
        ClaimService claimService = new ClaimService();

        Claim claim = new Claim();
        claim.setEquipment(equipment);
        claim.setReclamation(description);
        claim.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        equipment.setStatus("panne");

        Technicien technicien = getTechnicienById(technicienId);
        if (technicien == null) {
            System.out.println("Technicien non trouvé avec l'ID: " + technicienId);
            return;
        }
        claim.setTechnicien(technicien);

        try (Connection connection = MyConnection.getInstance().getCnx()) {

            // Mise à jour du statut de l’équipement dans la base de données
            String updateSql = "UPDATE equipment SET status = ? WHERE id = ?";
            PreparedStatement updateStatement = connection.prepareStatement(updateSql);
            updateStatement.setString(1, "panne");
            updateStatement.setInt(2, equipment.getId());
            updateStatement.executeUpdate();

            // Enregistrement de la réclamation
            String sql = "INSERT INTO claim (reclamation, equipment_id, technicien_id, created_at) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, claim.getReclamation());
            statement.setInt(2, claim.getEquipment().getId());
            statement.setInt(3, technicienId);
            statement.setTimestamp(4, claim.getCreatedAt());

            statement.executeUpdate();
            System.out.println("Réclamation enregistrée avec succès!");

            // Ajout au calendrier
            addToGoogleCalendar(claim);

            // Envoi SMS
            SmsService smsService = new SmsService();
            String message = "Bonjour " + technicien.getNom() + ",\n\n"
                    + "Une nouvelle réclamation a été enregistrée pour l'équipement : "
                    + claim.getEquipment().getName() + " (ID: " + claim.getEquipment().getId() + ").\n"
                    + "Statut actuel : " + claim.getEquipment().getStatus() + ".\n\n"
                    + "Merci de traiter cette demande dans les plus brefs délais.\n\n"
                    + "Cordialement,\nVotre service technique";
            smsService.sendSms(technicien.getPhoneNumber(), message);

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Erreur lors de l'enregistrement de la réclamation.");
        } catch (GeneralSecurityException | IOException e) {
            System.err.println("Erreur calendrier: " + e.getMessage());
        }
    }



    public static Technicien getTechnicienById(int id) {
        try (Connection connection = MyConnection.getInstance().getCnx()) {
            String sql = "SELECT * FROM technicien WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, id);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        Technicien technicien = new Technicien();
                        technicien.setId(rs.getInt("id"));
                        technicien.setNom(rs.getString("nom"));
                        technicien.setPhoneNumber(rs.getString("phone_number"));
                        return technicien;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Claim getClaimById(int claimId) {
        String query = "SELECT c.*, e.name as equipment_name, e.status as equipment_status " +
                "FROM claim c " +
                "JOIN equipment e ON c.equipment_id = e.id " +
                "WHERE c.id = ?";

        try (Connection connection = MyConnection.getInstance().getCnx();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, claimId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Claim claim = new Claim();
                    claim.setId(rs.getInt("id"));
                    claim.setReclamation(rs.getString("reclamation"));
                    claim.setCreatedAt(rs.getTimestamp("created_at"));

                    Equipment equipment = new Equipment();
                    equipment.setId(rs.getInt("id"));
                    equipment.setName(rs.getString("equipment_name"));
                    equipment.setStatus(rs.getString("equipment_status"));
                    claim.setEquipment(equipment);

                    int technicienId = rs.getInt("id");
                    if (!rs.wasNull()) {
                        Technicien technicien = getTechnicienById(technicienId);
                        claim.setTechnicien(technicien);
                    }
                    return claim;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la réclamation: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public List<Claim> getClaimsForTechnicien(int technicienId, String... statuses) {
        List<Claim> claims = new ArrayList<>();

        if (statuses == null || statuses.length == 0) {
            System.err.println("Aucun statut fourni pour la récupération des réclamations.");
            return claims;
        }

        String placeholders = String.join(",", Collections.nCopies(statuses.length, "?"));
        String query = "SELECT c.*, e.status AS equipment_status " +
                "FROM claim c " +
                "JOIN equipment e ON c.equipment_id = e.id " +
                "WHERE c.technicien_id = ? AND e.status IN (" + placeholders + ")";

        try (Connection connection = MyConnection.getInstance().getCnx();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, technicienId);
            for (int i = 0; i < statuses.length; i++) {
                statement.setString(i + 2, statuses[i]);
            }
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Claim claim = new Claim();
                claim.setId(rs.getInt("id"));
                claim.setReclamation(rs.getString("reclamation"));
                claim.setCreatedAt(rs.getTimestamp("created_at"));

                Equipment equipment = new Equipment();
                equipment.setId(rs.getInt("equipment_id"));
                equipment.setStatus(rs.getString("equipment_status"));
                claim.setEquipment(equipment);

                claims.add(claim);
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération réclamations: " + e.getMessage());
        }

        return claims;
    }

    public void updateEquipmentWithReport(int equipmentId, String status, Date repairDate, String rapport) {
        String query = "UPDATE equipment SET status = ?, date_reparation = ?, rapport_detaille = ? WHERE id = ?";
        try (Connection conn = MyConnection.getInstance().getCnx();
             PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, status);
            pst.setDate(2,  repairDate);
            pst.setString(3, rapport);
            pst.setInt(4, equipmentId);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
 }
}
}