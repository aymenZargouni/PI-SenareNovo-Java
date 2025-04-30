package ed.sanarenovo.entities;

public class consultation {
    private int id;
    private String date;
    private String motif;
    private String typeConsultation;
    private String status;
    private int dossiermedicaleId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public String getTypeConsultation() {
        return typeConsultation;
    }

    public void setTypeConsultation(String typeConsultation) {
        this.typeConsultation = typeConsultation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getDossiermedicaleId() {
        return dossiermedicaleId;
    }

    public void setDossiermedicaleId(int dossiermedicaleId) {
        this.dossiermedicaleId = dossiermedicaleId;
    }
}
