package ed.sanarenovo.entities;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Claim {

    private Integer id;
    private String reclamation;
    private LocalDateTime createdAt;
    private Equipment equipment;
    private Technicien technicien;

    // Constructeur
    public Claim() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters & Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getReclamation() {
        return reclamation;
    }

    public void setReclamation(String reclamation) {
        this.reclamation = reclamation;
    }

    public Timestamp getCreatedAt() {
        return Timestamp.valueOf(createdAt);
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt.toLocalDateTime();
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }

    public Technicien getTechnicien() {
        return technicien;
    }

    public void setTechnicien(Technicien technicien) {
        this.technicien = technicien;
    }
}
