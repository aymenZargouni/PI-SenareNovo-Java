package ed.sanarenovo.services;

import ed.sanarenovo.entities.dossiermedicale;
import ed.sanarenovo.entities.consultation;
import ed.sanarenovo.utils.MyConnection;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class StatistiqueService {

    public Map<String, Object> getMedicalStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // Statistiques de base
        int totalDossiers = countDossiers();
        int totalConsultations = countConsultations();
        double moyenneConsultationsParDossier = totalDossiers > 0 ? (double) totalConsultations / totalDossiers : 0.0;

        // Nouveaux indicateurs
        int dossiersSansConsultation = countDossiersSansConsultation();
        double tauxRemplissageIMC = calculateTauxRemplissageIMC();

        // Répartitions
        Map<String, Integer> consultationsParType = countConsultationsParType();
        Map<String, Integer> consultationsParStatus = countConsultationsParStatus();
        Map<String, Long> imcDistribution = getImcDistribution();
        Map<String, Integer> consultationsParMois = getConsultationsParMois();

        stats.put("total_dossiers", totalDossiers);
        stats.put("total_consultations", totalConsultations);
        stats.put("moyenne_consultations_par_dossier", moyenneConsultationsParDossier);
        stats.put("dossiers_sans_consultation", dossiersSansConsultation);
        stats.put("taux_remplissage_imc", tauxRemplissageIMC);
        stats.put("consultations_par_type", consultationsParType);
        stats.put("consultations_par_status", consultationsParStatus);
        stats.put("imc_distribution", imcDistribution);
        stats.put("consultations_par_mois", consultationsParMois);

        return stats;
    }

    private int countDossiers() {
        String sql = "SELECT COUNT(*) FROM dossiermedicale";
        return getCountFromQuery(sql);
    }

    private int countConsultations() {
        String sql = "SELECT COUNT(*) FROM consultation";
        return getCountFromQuery(sql);
    }

    private int countDossiersSansConsultation() {
        String sql = "SELECT COUNT(*) FROM dossiermedicale d " +
                "WHERE NOT EXISTS (SELECT 1 FROM consultation c WHERE c.dossiermedicaleId = d.id)";
        return getCountFromQuery(sql);
    }

    private double calculateTauxRemplissageIMC() {
        String sqlTotal = "SELECT COUNT(*) FROM dossiermedicale";
        String sqlAvecIMC = "SELECT COUNT(*) FROM dossiermedicale WHERE imc > 0";

        int total = getCountFromQuery(sqlTotal);
        int avecIMC = getCountFromQuery(sqlAvecIMC);

        return total > 0 ? (double) avecIMC / total : 0.0;
    }

    private Map<String, Integer> countConsultationsParType() {
        return getGroupedCounts("SELECT typeConsultation, COUNT(*) FROM consultation GROUP BY typeConsultation", "typeConsultation");
    }

    private Map<String, Integer> countConsultationsParStatus() {
        return getGroupedCounts("SELECT status, COUNT(*) FROM consultation GROUP BY status", "status");
    }

    private Map<String, Long> getImcDistribution() {
        Map<String, Long> distribution = new HashMap<>();

        String sql = "SELECT " +
                "SUM(CASE WHEN imc < 18.5 THEN 1 ELSE 0 END) as maigreur, " +
                "SUM(CASE WHEN imc >= 18.5 AND imc < 25 THEN 1 ELSE 0 END) as normal, " +
                "SUM(CASE WHEN imc >= 25 AND imc < 30 THEN 1 ELSE 0 END) as surpoids, " +
                "SUM(CASE WHEN imc >= 30 THEN 1 ELSE 0 END) as obesite " +
                "FROM dossiermedicale WHERE imc > 0";

        try (Connection cnx = MyConnection.getInstance().getCnx();
             Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                distribution.put("Maigreur", rs.getLong("maigreur"));
                distribution.put("Normal", rs.getLong("normal"));
                distribution.put("Surpoids", rs.getLong("surpoids"));
                distribution.put("Obésité", rs.getLong("obesite"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return distribution;
    }

    private Map<String, Integer> getConsultationsParMois() {
        return getGroupedCounts(
                "SELECT DATE_FORMAT(date, '%Y-%m') as mois, COUNT(*) " +
                        "FROM consultation GROUP BY DATE_FORMAT(date, '%Y-%m') ORDER BY mois",
                "mois"
        );
    }

    // Méthodes utilitaires
    private int getCountFromQuery(String sql) {
        try (Connection cnx = MyConnection.getInstance().getCnx();
             Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Map<String, Integer> getGroupedCounts(String sql, String groupColumn) {
        Map<String, Integer> counts = new HashMap<>();

        try (Connection cnx = MyConnection.getInstance().getCnx();
             Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String groupValue = rs.getString(groupColumn);
                int count = rs.getInt(2);
                counts.put(groupValue != null ? groupValue : "Non spécifié", count);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return counts;
    }
}