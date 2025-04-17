package ed.sanarenovo.entities;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.List;

public class Technicien extends User {

    private int id;
    private String nom;
    private User user;
    private String phoneNumber;
    private List<Claim> claims;


    public Technicien() {
        this.claims = new ArrayList<>();
    }

    // Getters & Setters

    public Integer getId() {
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public StringProperty nomProperty() {
        return new SimpleStringProperty(this.nom);
    }
}
