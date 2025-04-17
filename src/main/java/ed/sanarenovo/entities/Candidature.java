package ed.sanarenovo.entities;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class Candidature {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String cv;
    private String texteCv;
    private String lettreMotivation;
    private Date dateCandidature;
    private Offre offre;
    private String statut; // EN_ATTENTE, ACCEPTEE, REFUSEE
    private List<String> competences;

    public Candidature() {
        this.competences = new ArrayList<>();
        this.statut = "EN_ATTENTE";
    }

    // Getters et setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public void setCv(String cv) { this.cv = cv; }

    public String getCv() { return cv; }
    public void setTexteCv(String texteCv) { this.texteCv = texteCv; }

    public String getLettreMotivation() { return lettreMotivation; }
    public void setLettreMotivation(String lettreMotivation) { this.lettreMotivation = lettreMotivation; }

    public Date getDateCandidature() { return dateCandidature; }
    public void setDateCandidature(Date dateCandidature) { this.dateCandidature = dateCandidature; }

    public Offre getOffre() { return offre; }
    public void setOffre(Offre offre) { this.offre = offre; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public List<String> getCompetences() { return competences; }
    public void setCompetences(List<String> competences) { this.competences = competences; }
    public void addCompetence(String competence) { this.competences.add(competence); }

    // Méthode pour l'ID de l'offre
    public int getOffreId() {
        return (offre != null) ? offre.getId() : 0;
    }

    public void setOffreId(int offreId) {
        if (this.offre == null) {
            this.offre = new Offre();
        }
        this.offre.setId(offreId);
    }


}