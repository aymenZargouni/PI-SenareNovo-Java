package ed.sanarenovo.entities;

import java.util.List;

public class dossiermedicale {
    private int id;
    private float imc;
    private String date;
    private String observations;
    private String ordonnance;

    public String getConsultationSummary() {
        if (consultations == null || consultations.isEmpty()) {
            return "Aucune";
        }

        StringBuilder sb = new StringBuilder();
        for (consultation c : consultations) {
            sb.append(c.getDate()).append(": ").append(c.getMotif()).append("; ");
        }
        return sb.toString().trim();
    }

    public List<consultation> getConsultations() {
        return consultations;
    }

    public void setConsultations(List<consultation> consultations) {
        this.consultations = consultations;
    }

    private List<consultation> consultations;

    public dossiermedicale() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getImc() {
        return imc;
    }

    public void setImc(float imc) {
        this.imc = imc;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public String getOrdonnance() {
        return ordonnance;
    }

    public void setOrdonnance(String ordonnance) {
        this.ordonnance = ordonnance;
    }


}
