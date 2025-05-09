package ed.sanarenovo.entities;

import java.sql.Date;

public class rv {
    private int id;
    private Patient patient;
    private Medecin medecin;
    private Date dateR;
    private String motif;
    private String statut;

    public rv() {
    }

    public rv(int id, Patient patient, Medecin medecin, Date dateR, String motif, String statut) {
        this.id = id;
        this.patient = patient;
        this.medecin = medecin;
        this.dateR = dateR;
        this.motif = motif;
        this.statut = statut;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Medecin getMedecin() {
        return medecin;
    }

    public void setMedecin(Medecin medecin) {
        this.medecin = medecin;
    }

    public Date getDateR() {
        return dateR;
    }

    public void setDateR(Date dateR) {
        this.dateR = dateR;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }
}
