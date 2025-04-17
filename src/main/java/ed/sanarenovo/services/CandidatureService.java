package ed.sanarenovo.services;

import ed.sanarenovo.entities.Candidature;
import ed.sanarenovo.utils.MyConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CandidatureService {
    private Connection cnx;

    public CandidatureService() {

            cnx = MyConnection.getInstance().getCnx();
    }

    public void addCandidature(Candidature candidature) {
        String query = "INSERT INTO candidature(nom, prenom, email, cv, lettre_motivation, date_candidature, statut, offre_id) VALUES(?,?,?,?,?,?,?,?)";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setString(1, candidature.getNom());
            pst.setString(2, candidature.getPrenom());
            pst.setString(3, candidature.getEmail());
            pst.setString(4, candidature.getCv());
            pst.setString(5, candidature.getLettreMotivation());
            pst.setDate(6, new java.sql.Date(candidature.getDateCandidature().getTime()));
            pst.setString(7, candidature.getStatut());
            pst.setInt(8, candidature.getOffreId());
            pst.executeUpdate();
            System.out.println("Candidature ajoutée avec succès");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void updateStatut(int id, String newStatut) {
        String query = "UPDATE candidature SET statut=? WHERE id=?";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setString(1, newStatut);
            pst.setInt(2, id);
            pst.executeUpdate();
            System.out.println("Statut de la candidature mis à jour");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public List<Candidature> getCandidaturesByOffre(int offreId) {
        List<Candidature> candidatures = new ArrayList<>();
        String query = "SELECT * FROM candidature WHERE offre_id=?";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setInt(1, offreId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Candidature c = new Candidature();
                c.setId(rs.getInt("id"));
                c.setNom(rs.getString("nom"));
                c.setPrenom(rs.getString("prenom"));
                c.setEmail(rs.getString("email"));
                c.setCv(rs.getString("cv"));
                c.setLettreMotivation(rs.getString("lettre_motivation"));
                c.setDateCandidature(rs.getDate("date_candidature"));
                c.setStatut(rs.getString("statut"));
                c.setOffreId(rs.getInt("offre_id"));
                candidatures.add(c);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return candidatures;
    }

    public List<Candidature> getAllCandidatures() {
        List<Candidature> candidatures = new ArrayList<>();
        String query = "SELECT * FROM candidature";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                Candidature c = new Candidature();
                c.setId(rs.getInt("id"));
                c.setNom(rs.getString("nom"));
                c.setPrenom(rs.getString("prenom"));
                c.setEmail(rs.getString("email"));
                c.setCv(rs.getString("cv"));
                c.setLettreMotivation(rs.getString("lettre_motivation"));
                c.setDateCandidature(rs.getDate("date_candidature"));
                c.setStatut(rs.getString("statut"));
                c.setOffreId(rs.getInt("offre_id"));
                candidatures.add(c);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return candidatures;
    }
}