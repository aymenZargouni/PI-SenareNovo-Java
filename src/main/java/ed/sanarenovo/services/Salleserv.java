package ed.sanarenovo.services;

import ed.sanarenovo.entities.Salle;
import ed.sanarenovo.interfaces.Uisalle;
import ed.sanarenovo.utils.MyConnection;

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
        String requete = "DELETE FROM salle WHERE id = ?";
        try (PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            pst.setInt(1, salle.getId());
            int rows = pst.executeUpdate();
            if (rows > 0) {
                System.out.println("Salle supprimée avec succès.");
            } else {
                System.out.println("Aucune salle trouvée avec cet ID.");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression de la salle : " + e.getMessage());
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


}
