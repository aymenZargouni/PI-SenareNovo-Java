package ed.sanarenovo.entities;

import java.util.Date;

public class Medecin {
    private int id;
    private String fullname;
    private Date dateEmbauche;
    private String specilite;
    private User user; // One-to-one

    public Medecin() {
    }

    public Medecin(Date dateEmbauche, String fullname, int id, String specialite, User user) {
        this.dateEmbauche = dateEmbauche;
        this.fullname = fullname;
        this.id = id;
        this.specilite = specialite;
        this.user = user;
    }

    public Medecin(Date dateEmbauche, String fullname, int id, String specialite) {
        this.dateEmbauche = dateEmbauche;
        this.fullname = fullname;
        this.id = id;
        this.specilite = specialite;
    }

    public Date getDateEmbauche() {
        return dateEmbauche;
    }

    public void setDateEmbauche(Date dateEmbauche) {
        this.dateEmbauche = dateEmbauche;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSpecilite() {
        return specilite;
    }

    public void setSpecilite(String specialite) {
        this.specilite = specialite;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
