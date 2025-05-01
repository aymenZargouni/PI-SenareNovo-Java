package ed.sanarenovo.entities;

import java.util.Date;
import java.util.Map;


public class Candidature {
    private Map<String, Double> analysisResults; // Nouveau champ

    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String cv; // Chemin du fichier PDF
    private String texteCv; // Contenu extrait du CV (optionnel)
    private String lettreMotivation;
    private Date dateCandidature;
    private Offre offre;
    private String statut; // EN_ATTENTE, ACCEPTEE, REFUSEE
    private Integer analysisScore;

    // Getters and setters




    public Candidature() {
        this.statut = "EN_ATTENTE";
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

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCv() {
        return cv;
    }

    public void setCv(String cv) {
        this.cv = cv;
    }



    public void setTexteCv(String texteCv) {
        this.texteCv = texteCv;
    }

    public String getLettreMotivation() {
        return lettreMotivation;
    }

    public void setLettreMotivation(String lettreMotivation) {
        this.lettreMotivation = lettreMotivation;
    }

    public Date getDateCandidature() {
        return dateCandidature;
    }

    public void setDateCandidature(Date dateCandidature) {
        this.dateCandidature = dateCandidature;
    }

    public Offre getOffre() {
        return offre;
    }

    public void setOffre(Offre offre) {
        this.offre = offre;
    }

    public int getOffreId() {
        return (offre != null) ? offre.getId() : 0;
    }

    public void setOffreId(int offreId) {
        if (this.offre == null) {
            this.offre = new Offre();
        }
        this.offre.setId(offreId);
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    // Ajout d'un getter pour récupérer le titre de l'offre
    public String getTitreOffre() {
        return offre != null ? offre.getTitre() : "Offre non disponible";
    }
    public Map<String, Double> getAnalysisResults() {
        return analysisResults;
    }

    public void setAnalysisResults(Map<String, Double> analysisResults) {
        this.analysisResults = analysisResults;
    }

    public Double getRelevanceScore() {
        return analysisResults != null ? analysisResults.get("relevanceScore") : null;
    }
    // Dans Candidature.java

    public Integer getAnalysisScore() {
        return analysisScore;
    }

    public void setAnalysisScore(Integer analysisScore) {
        this.analysisScore = analysisScore;
    }

}
