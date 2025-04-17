package ed.sanarenovo.entities;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.sql.Date;

public class Equipment {

    private Integer id;
    private String name;
    private String model;
    private String status = "reparé";
    private Date dateAchat;
    private double prix;
    private Date dateReparation;
    private String rapportDetaille;
    private String rapportPath; //peut etre je vais l'utiliser la prochaine fois

    // Constructeur

    public Equipment() {
    }

    public Equipment(String name, String model, Date dateAchat, Double prix) {
        this.name = name;
        this.model = model;
        this.dateAchat = dateAchat;
        this.prix = prix;
    }

    public Equipment(Integer equipment_id, String status, String rapportDetaille, Date dateReparation) {
        this.id = equipment_id;
        this.status = status;
        this.rapportDetaille = rapportDetaille;
        this.dateReparation = dateReparation;
    }

    // Getters et Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getDateAchat() {
        return dateAchat;
    }

    public void setDateAchat(Date dateAchat) {
        this.dateAchat = dateAchat;
    }

    public Double getPrix() {
        return prix;
    }

    public void setPrix(Double prix) {
        this.prix = prix;
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

    public String getRapportPath() {
        return rapportPath;
    }

    public void setRapportPath(String rapportPath) {
        this.rapportPath = rapportPath;
    }

    //utiliser pour la liaison dynamique avec javafx
    public StringProperty nameProperty() {
        return new SimpleStringProperty(this.name);
    }

    public StringProperty statusProperty() {
        return new SimpleStringProperty(this.status);
    }

}
