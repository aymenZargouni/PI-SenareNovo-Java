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

        int totalDossiers = countDossiers();
        int totalConsultations = countConsultations();
        double moyenneConsultationsParDossier = totalDossiers > 0 ? (double) totalConsultations / totalDossiers : 0.0;
        Map<String, Integer> consultationsParType = countConsultationsParType();

        stats.put("total_dossiers", totalDossiers);
        stats.put("total_consultations", totalConsultations);
        stats.put("moyenne_consultations_par_dossier", moyenneConsultationsParDossier);
        stats.put("consultations_par_type", consultationsParType);

        return stats;
    }

    private int countDossiers() {
        String sql = "SELECT COUNT(*) FROM dossiermedicale";
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

    private int countConsultations() {
        String sql = "SELECT COUNT(*) FROM consultation";
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

    private Map<String, Integer> countConsultationsParType() {
        Map<String, Integer> typeCounts = new HashMap<>();
        String sql = "SELECT typeconsultation, COUNT(*) FROM consultation GROUP BY typeconsultation";

        try (Connection cnx = MyConnection.getInstance().getCnx();
             Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String type = rs.getString("typeconsultation");
                int count = rs.getInt(2);
                typeCounts.put(type, count);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return typeCounts;
    }
}
