package ed.sanarenovo.services;

import ed.sanarenovo.entities.Candidature;
import ed.sanarenovo.utils.MailSender;
import ed.sanarenovo.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CandidatureService {
    private Connection cnx;
    private static final Logger logger = LogManager.getLogger(CandidatureService.class);

    public CandidatureService() {
        cnx = MyConnection.getInstance().getCnx();
    }

    public Map<String, Integer> getNombreCandidaturesParOffre() {
        Map<String, Integer> stats = new HashMap<>();
        String query = "SELECT o.titre, COUNT(c.id) AS nb_candidatures " +
                "FROM offre o LEFT JOIN candidature c ON o.id = c.offre_id " +
                "GROUP BY o.titre";

        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                stats.put(rs.getString("titre"), rs.getInt("nb_candidatures"));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors du chargement des statistiques: " + e.getMessage());
        }

        return stats;
    }

    public void addCandidature(Candidature candidature) {
        String query = "INSERT INTO candidature(nom, prenom, email, cv, lettre_motivation, date_candidature, statut, offre_id, analysis_score) VALUES(?,?,?,?,?,?,?,?,?)";
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

            // Handle null analysis score
            if (candidature.getAnalysisScore() != null) {
                pst.setInt(9, candidature.getAnalysisScore());
            } else {
                pst.setNull(9, Types.INTEGER);
            }

            pst.executeUpdate();
            logger.info("Candidature ajoutée avec succès");
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

    public void updateStatut(int id, String newStatut) {
        String query = "UPDATE candidature SET statut=? WHERE id=?";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setString(1, newStatut);
            pst.setInt(2, id);
            int rowsAffected = pst.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Statut de la candidature mis à jour");

                Candidature candidature = getCandidatureById(id);
                if (candidature != null) {
                    String offreTitre = getOffreTitreById(candidature.getOffreId());

                    String subject = "📢 Mise à jour de votre candidature - " + offreTitre;
                    String body = "";

                    if ("accepté".equalsIgnoreCase(newStatut)) {
                        body += "🎉 Félicitations " + candidature.getPrenom() + " " + candidature.getNom() + " !\n\n";
                        body += "✅ Votre candidature pour le poste de **" + offreTitre + "** a été *acceptée*.\n";
                        body += "📞 Notre équipe RH vous contactera très bientôt pour la suite du processus.\n\n";
                        body += "Merci pour votre confiance 🙏\nBienvenue dans l'aventure ! 🚀\n\n";
                        body += "Cordialement,\nL'équipe RH 💼";
                    } else if ("refusé".equalsIgnoreCase(newStatut)) {
                        body += "👋 Bonjour " + candidature.getPrenom() + " " + candidature.getNom() + ",\n\n";
                        body += "📝 Nous vous remercions pour votre candidature au poste de **" + offreTitre + "**.\n";
                        body += "❌ Après étude de votre profil, nous sommes au regret de vous informer que votre candidature n'a pas été retenue.\n\n";
                        body += "💪 Nous vous souhaitons plein de succès dans vos projets futurs.\n\n";
                        body += "Cordialement,\nL'équipe RH 🤝";
                    } else {
                        body += "🔔 Bonjour " + candidature.getPrenom() + " " + candidature.getNom() + ",\n\n";
                        body += "ℹ️ Le statut de votre candidature pour le poste de **" + offreTitre + "** est maintenant : *" + newStatut + "*.\n\n";
                        body += "Merci de rester à l'écoute 📬\n\n";
                        body += "Cordialement,\nL'équipe RH 👨‍💼";
                    }

                    MailSender.sendEmail(
                            candidature.getEmail(),
                            subject,
                            body
                    );
                } else {
                    logger.warn("Candidature non trouvée pour l'envoi de mail");
                }
            }

        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

    private String getOffreTitreById(int offreId) {
        String query = "SELECT titre FROM offre WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, offreId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getString("titre");
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération du titre de l'offre : " + e.getMessage());
        }
        return "Poste inconnu";
    }

    public Candidature getCandidatureById(int id) {
        String query = "SELECT * FROM candidature WHERE id=?";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                Candidature c = new Candidature();
                c.setId(rs.getInt("id"));
                c.setPrenom(rs.getString("prenom"));
                c.setNom(rs.getString("nom"));
                c.setEmail(rs.getString("email"));
                c.setCv(rs.getString("cv"));
                c.setLettreMotivation(rs.getString("lettre_motivation"));
                c.setDateCandidature(rs.getDate("date_candidature"));
                c.setStatut(rs.getString("statut"));
                c.setOffreId(rs.getInt("offre_id"));
                c.setAnalysisScore(rs.getInt("analysis_score"));
                if (rs.wasNull()) {
                    c.setAnalysisScore(null);
                }
                return c;
            }

        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return null;
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
                c.setAnalysisScore(rs.getInt("analysis_score"));
                if (rs.wasNull()) {
                    c.setAnalysisScore(null);
                }
                candidatures.add(c);
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
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
                c.setAnalysisScore(rs.getInt("analysis_score"));
                if (rs.wasNull()) {
                    c.setAnalysisScore(null);
                }
                candidatures.add(c);
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return candidatures;
    }

    public int countByStatut(String statut) {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM candidature WHERE statut = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setString(1, statut);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                count = rs.getInt(1);
            }

        } catch (SQLException e) {
            logger.error("Erreur lors du comptage des candidatures : " + e.getMessage());
        }

        return count;
    }

    public void updateCandidature(Candidature candidature) {
        String query = "UPDATE candidature SET nom=?, prenom=?, email=?, cv=?, lettre_motivation=?, date_candidature=?, statut=?, offre_id=?, analysis_score=? WHERE id=?";
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

            // Handle null analysis score
            if (candidature.getAnalysisScore() != null) {
                pst.setInt(9, candidature.getAnalysisScore());
            } else {
                pst.setNull(9, Types.INTEGER);
            }

            pst.setInt(10, candidature.getId());

            pst.executeUpdate();
            logger.info("Candidature mise à jour avec succès");
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise à jour de la candidature", e);
            throw new RuntimeException(e);
        }
    }
}