package ed.sanarenovo.services;

import ed.sanarenovo.entities.Salle;
import ed.sanarenovo.interfaces.Uisalle;
import ed.sanarenovo.utils.MyConnection;
import ed.sanarenovo.utils.QrCodeGenerator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Salleserv implements Uisalle<Salle> {

    public void addSalle(Salle salle) {
        String requeteSalle = "INSERT INTO salle (service_id, type, etat) VALUES (?, ?, ?)";
        String requeteUpdateService = "UPDATE service SET nbr_salle = nbr_salle + 1 WHERE id = ?";

        try (Connection conn = MyConnection.getInstance().getCnx();
             PreparedStatement pstSalle = conn.prepareStatement(requeteSalle);
             PreparedStatement pstUpdate = conn.prepareStatement(requeteUpdateService)) {

            // Insertion de la salle
            pstSalle.setInt(1, salle.getService_id());
            pstSalle.setString(2, salle.getType());
            pstSalle.setBoolean(3, salle.isEtat());
            pstSalle.executeUpdate();

            // Mise à jour du nombre de salles dans le service
            pstUpdate.setInt(1, salle.getService_id());
            pstUpdate.executeUpdate();

            System.out.println("Salle ajoutée avec succès et le nombre de salles du service a été mis à jour.");

        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout de la salle ou la mise à jour du service : " + e.getMessage());
        }
    }


    public void deleteSalle(Salle salle) {
        String getServiceIdQuery = "SELECT service_id FROM salle WHERE id = ?";
        String deleteSalleQuery = "DELETE FROM salle WHERE id = ?";
        String updateServiceQuery = "UPDATE service SET nbr_salle = nbr_salle - 1 WHERE id = ?";

        try (Connection cnx = MyConnection.getInstance().getCnx()) {

            int serviceId = -1;

            // 1. Récupérer l'id du service de la salle
            try (PreparedStatement pst = cnx.prepareStatement(getServiceIdQuery)) {
                pst.setInt(1, salle.getId());
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    serviceId = rs.getInt("service_id");
                } else {
                    System.out.println("Aucune salle trouvée avec cet ID.");
                    return;
                }
            }

            // 2. Supprimer la salle
            try (PreparedStatement pst = cnx.prepareStatement(deleteSalleQuery)) {
                pst.setInt(1, salle.getId());
                int rows = pst.executeUpdate();
                if (rows == 0) {
                    System.out.println("Échec de la suppression de la salle.");
                    return;
                }
            }

            // 3. Décrémenter le nombre de salles dans le service
            try (PreparedStatement pst = cnx.prepareStatement(updateServiceQuery)) {
                pst.setInt(1, serviceId);
                pst.executeUpdate();
            }

            System.out.println("Salle supprimée et nombre de salles décrémenté avec succès.");

        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression : " + e.getMessage());
        }
    }




    @Override
    public void updateSalle(Salle salle) {

            String sql = "UPDATE salle SET type = ?, etat = ?  WHERE id = ?";
            try (Connection conn = MyConnection.getInstance().getCnx();
                 PreparedStatement pst = conn.prepareStatement(sql)) {

                pst.setString(1, salle.getType());
                pst.setBoolean(2, salle.isEtat());
                pst.setInt(3, salle.getId());

                pst.executeUpdate();

            } catch (SQLException ex) {
                ex.printStackTrace(); // déjà affiché dans ton log
            }


    }


    public List<Salle> getService() {
        return getSalles();
    }

    public List<Salle> getSalles() {
        List<Salle> data = new ArrayList<>();
        String requete = "SELECT s.id, s.service_id, s.type, s.etat, sv.nom AS service_nom " +
                "FROM salle s " +
                "JOIN service sv ON s.service_id = sv.id";
        try (Statement st = MyConnection.getInstance().getCnx().createStatement();
             ResultSet rs = st.executeQuery(requete)) {
            while (rs.next()) {
                Salle salle = new Salle();
                salle.setId(rs.getInt("id"));
                salle.setService_id(rs.getInt("service_id"));
                salle.setType(rs.getString("type"));
                salle.setEtat(rs.getBoolean("etat"));
                // Affiche aussi le nom du service (jointure)
                System.out.println("Salle ID: " + salle.getId() +
                        ", Type: " + salle.getType() +
                        ", Etat: " + salle.isEtat() +
                        ", Service: " + rs.getString("service_nom"));
                data.add(salle);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des salles : " + e.getMessage());
        }
        return data;
    }

    @Override
    public Salle getSalleById(int id) {
        Salle salle = null;
        String requete = "SELECT s.id, s.service_id, s.type, s.etat, sv.nom AS service_nom " +
                "FROM salle s " +
                "JOIN service sv ON s.service_id = sv.id " +
                "WHERE s.id = ?";
        try (PreparedStatement pst =MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                salle = new Salle();
                salle.setId(rs.getInt("id"));
                salle.setService_id(rs.getInt("service_id"));
                salle.setType(rs.getString("type"));
                salle.setEtat(rs.getBoolean("etat"));
                System.out.println("Salle ID: " + salle.getId() +
                        ", Type: " + salle.getType() +
                        ", Etat: " + salle.isEtat() +
                        ", Service: " + rs.getString("service_nom"));
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération de la salle par ID : " + e.getMessage());
        }
        return salle;
    }
    public void generateQrForSalle(Salle salle) {
        String contenuQR = "ID: " + salle.getId()
                + "\nType: " + salle.getType()
                + "\nÉtat: " + (salle.isEtat() ? "Active" : "Inactif")
                + "\nService ID: " + salle.getService_id();
        String cheminFichier = "qrcodes/salle_" + salle.getId() + ".png";

        QrCodeGenerator.generateQRCode(contenuQR, cheminFichier);
    }

}
