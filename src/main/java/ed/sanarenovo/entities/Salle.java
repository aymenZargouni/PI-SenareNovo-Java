package ed.sanarenovo.entities;

public class Salle {
    private int id;
    private int service_id;
    private String type;
    private boolean etat;
    public Salle() {}
    public Salle(int id, int service_id, String type, boolean etat) {
        this.id = id;
        this.service_id = service_id;
        this.type = type;
        this.etat = etat;

    }
    public int getId()
    {
        return id ;
    }
    public void setId(int id) {
        this.id = id;
    }
   public int getService_id() {
        return service_id;
   }
   public void setService_id(int  service_id) {
        this.service_id = service_id;
   }
    public  String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public boolean isEtat() {
        return etat;
    }
    public void setEtat(boolean etat) {
        this.etat = etat;
    }
    public String toString() {
        return ""+id+" "+service_id+" "+type+" "+etat;
    }
}
