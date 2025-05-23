package ed.sanarenovo.entities;

import java.time.LocalDateTime;

public class User {

    private int id;
    private String email;
    private String password;
    private String roles;
    private boolean isBlocked = false;
    private String resetToken;
    private String resetTokenExpiresAt; //hich
    private LocalDateTime blockedUntil; //hich
    private int infractionCount;
    // Relationships (references only, to be implemented later)
    /*
    private Medecin medecin;
    private Patient patient;
    private List<Comment> comments = new ArrayList<>();
     */

    public User() {}

    public User(int id,String email, String password, String roles, boolean isBlocked) {
        this.email = email;
        this.id = id;
        this.isBlocked = isBlocked;
        this.password = password;
        this.roles = roles;
    }

// Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRoles() {
            return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean isBlocked) {
        this.isBlocked = isBlocked;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public String getResetTokenExpiresAt() {
        return resetTokenExpiresAt;
    }

    public void setResetTokenExpiresAt(String resetTokenExpiresAt) {
        this.resetTokenExpiresAt = resetTokenExpiresAt;
    }

    //hich
    public LocalDateTime getBlockedUntil() {
        return blockedUntil;
    }

    public void setBlockedUntil(LocalDateTime blockedUntil) {
        this.blockedUntil = blockedUntil;
    }

    public int getInfractionCount() {
        return infractionCount;
    }

    public void setInfractionCount(int infractionCount) {
        this.infractionCount = infractionCount; //hich
    }
    /*
    public Medecin getMedecin() {
        return medecin;
    }

    public void setMedecin(Medecin medecin) {
        this.medecin = medecin;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

     */
}

