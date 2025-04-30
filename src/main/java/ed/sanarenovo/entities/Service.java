package ed.sanarenovo.entities;


public class Service {

    private int id;
    private String nom;
    private String chef_service;
    private int nbr_salle;
    private int capacite;
    private boolean etat;


    public Service(int i) {

    }
    public Service( int id ,String nom, String chef_service, int nbr_salle, int capcite, boolean etat) {
        this.id=id;
    this.nom = nom;
        this.chef_service = chef_service;
        this.nbr_salle = nbr_salle;
        this.capacite = capcite;
        this.etat = etat;
    }

    public Service(String nom, String chef_service, int nbr_salle, boolean etat, int capcite) {
        this.nom = nom;
        this.chef_service = chef_service;
        this.nbr_salle = nbr_salle;
        this.etat = etat;
        this.capacite = capcite;
    }


    public Service(String nom, int nbr_salle, int capcite, boolean etat) {
        this.nom = nom;
        this.nbr_salle = nbr_salle;
        this.capacite = capcite;
        this.etat = etat;
    }

    public Service(int nbr_salle, int capcite, boolean etat) {
        this.nbr_salle = nbr_salle;
        this.capacite = capcite;
        this.etat = etat;
    }

    public boolean isEtat() {
        return etat;
    }

    public void setEtat(boolean etat) {
        this.etat = etat;
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

    public String getChef_service() {
        return chef_service;
    }

    public void setChef_service(String chef_service) {
        this.chef_service = chef_service;
    }

    public int getNbr_salle() {
        return nbr_salle;
    }

    public void setNbr_salle(int nbr_salle) {
        this.nbr_salle = nbr_salle;
    }

    public int getCapacite() {
        return capacite;
    }

    public void setCapacite(int capcite) {
        this.capacite = capcite;
    }

    @Override
    public String toString() {
        return "Service{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", chef_service='" + chef_service + '\'' +
                ", nbr_salle=" + nbr_salle +
                ", capcite=" + capacite +
                ", etat=" + etat +
                '}';
    }
}