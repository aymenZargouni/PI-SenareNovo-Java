package ed.sanarenovo.entities;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.List;

public class Technicien {
    private int id;
    private String nom;
    private String phoneNumber;
    private User user;
    private List<Claim> claims;

  
    public Technicien() {
        this.claims = new ArrayList<>();
    }

    public Technicien(String nom, String phoneNumber, User user) {
        this.nom = nom;
        this.phoneNumber = phoneNumber;
        this.user = user;
    }

    public Technicien(int id, String nom, String phoneNumber, User user) {
        this.id = id;
        this.nom = nom;
        this.phoneNumber = phoneNumber;
        this.user = user;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public StringProperty nomProperty() {
        return new SimpleStringProperty(this.nom);
    }
}