package ed.sanarenovo.services;

import ed.sanarenovo.entities.Offre;
import ed.sanarenovo.utils.MyConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OffreService {

    private Connection cnx;
    public OffreService() {
        cnx = MyConnection.getInstance().getCnx();
    }

    public void addOffre(Offre offre) {
        System.out.println("Ajout d'une offre avec le titre: " + offre.getTitre()); // Afficher le titre

        if (offre.getTitre() == null || offre.getTitre().isEmpty()) {
            System.out.println("Le titre de l'offre est vide !");
            return; // On arrête l'exécution si le titre est vide
        }

        String query = "INSERT INTO offre(titre, description, date_publication, date_expiration) VALUES(?,?,?,?)";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setString(1, offre.getTitre());
            pst.setString(2, offre.getDescription());

            // Vérification de la date de publication
            if (offre.getDatePublication() == null) {
                offre.setDatePublication(new java.util.Date()); // On assigne la date actuelle si null
            }
            pst.setDate(3, new java.sql.Date(offre.getDatePublication().getTime()));

            // Vérification de la date d'expiration
            if (offre.getDateExpiration() == null) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTime(offre.getDatePublication());
                cal.add(java.util.Calendar.DAY_OF_YEAR, 30); // Date d'expiration dans 30 jours
                offre.setDateExpiration(cal.getTime());
            }
            pst.setDate(4, new java.sql.Date(offre.getDateExpiration().getTime()));

            pst.executeUpdate();
            System.out.println("Offre ajoutée avec succès");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    public void updateOffre(Offre offre) {
        String query = "UPDATE offre SET titre=?, description=?, date_expiration=? WHERE id=?";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setString(1, offre.getTitre());
            pst.setString(2, offre.getDescription());
            pst.setDate(3, new java.sql.Date(offre.getDateExpiration().getTime()));
            pst.setInt(4, offre.getId());
            pst.executeUpdate();
            System.out.println("Offre mise à jour avec succès");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void deleteOffre(int id) {
        String query = "DELETE FROM offre WHERE id=?";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("Offre supprimée avec succès");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public List<Offre> getAllOffres() {
        List<Offre> offres = new ArrayList<>();
        String query = "SELECT * FROM offre";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                Offre o = new Offre();
                o.setId(rs.getInt("id"));
                o.setTitre(rs.getString("titre"));
                o.setDescription(rs.getString("description"));
                o.setDatePublication(rs.getDate("date_publication"));
                o.setDateExpiration(rs.getDate("date_expiration"));
                offres.add(o);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return offres;
    }

    public Offre getOffreById(int id) {
        Offre offre = null;
        String query = "SELECT * FROM offre WHERE id=?";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                offre = new Offre();
                offre.setId(rs.getInt("id"));
                offre.setTitre(rs.getString("titre"));
                offre.setDescription(rs.getString("description"));
                offre.setDatePublication(rs.getDate("date_publication"));
                offre.setDateExpiration(rs.getDate("date_expiration"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return offre;
    }
}