package ed.sanarenovo.entities;

import java.sql.Date;

public class Historique {
    private int id;
    private Equipment equipment;
    private Date dateReparation;
    private String rapportDetaille;

    public Historique() {}

    public Historique(int id, Equipment equipment, Date dateReparation, String rapportDetaille) {
        this.id = id;
        this.equipment = equipment;
        this.dateReparation = dateReparation;
        this.rapportDetaille = rapportDetaille;
    }

    // Getters et setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }

    public Date getDateReparation() {
        return dateReparation;
    }

    public void setDateReparation(Date dateReparation) {
        this.dateReparation = dateReparation;
    }

    public String getRapportDetaille() {
        return rapportDetaille;
    }

    public void setRapportDetaille(String rapportDetaille) {
        this.rapportDetaille = rapportDetaille;
    }
}
