package ed.sanarenovo.tests;

import ed.sanarenovo.entities.Patient;
import ed.sanarenovo.entities.User;
import ed.sanarenovo.services.AuthService;
import ed.sanarenovo.services.PatientService;
import ed.sanarenovo.utils.UserSession;

public class AuthTestClass {
    public static void main(String[] args) {
        AuthService us = new AuthService();
        User u = us.login("aymntest123@gmail.com", "aymntest123");

        if (u != null) {
            UserSession.startSession(u);
            System.out.println("Connexion réussie ✅");
            System.out.println("Bienvenue " + u.getEmail());
            System.out.println("Rôle : " + u.getRoles());
        } else {
            System.out.println("Échec de connexion ❌");
        }

        // Test d’accès :
        if (UserSession.getInstance() != null) {
            System.out.println("Utilisateur connecté : " + UserSession.getInstance().getUser().getEmail());
        }

        // Déconnexion :
        UserSession.logout();
        System.out.println("Déconnecté");


    PatientService patientService = new PatientService();


    User user = new User();
        user.setEmail("patient.test1111111111@example.com");
        user.setPassword("123456");
        user.setRoles("[\"ROLE_PATIENT\"]");
        user.setBlocked(false);

    Patient patient = new Patient();
        patient.setFullname("Test Patient");
        patient.setGender("Homme");
        patient.setAdress("123 Rue Exemple");
        patient.setUser(user); 

        patientService.register(patient);

    }
}

