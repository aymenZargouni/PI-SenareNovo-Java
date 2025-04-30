package ed.sanarenovo.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Offre {
    private int id;
    private String titre;
    private String description;
    private Date datePublication;
    private Date dateExpiration;

    // Constructeurs
    public Offre() {
    }

    public Offre(int id, String titre, String description, Date datePublication, Date dateExpiration) {

        this.id = id;
        this.titre = titre;
        this.description = description;
        this.datePublication = datePublication;
        this.dateExpiration = dateExpiration;
    }



    public Offre(String text, String text1, java.sql.Date date) {
    }

    // Getters et setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getDatePublication() { return datePublication; }
    public void setDatePublication(Date datePublication) { this.datePublication = datePublication; }

    public Date getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(Date dateExpiration) { this.dateExpiration = dateExpiration; }


    @Override
    public String toString() {
        return "Offre{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", datePublication=" + datePublication +
                ", dateExpiration=" + dateExpiration +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Offre offre = (Offre) o;
        return id == offre.id && Objects.equals(titre, offre.titre) && Objects.equals(description, offre.description) && Objects.equals(datePublication, offre.datePublication) && Objects.equals(dateExpiration, offre.dateExpiration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, titre, description, datePublication, dateExpiration);
    }
}