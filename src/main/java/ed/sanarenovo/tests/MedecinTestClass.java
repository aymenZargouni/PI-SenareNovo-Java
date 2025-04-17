package ed.sanarenovo.tests;

import ed.sanarenovo.entities.Medecin;
import ed.sanarenovo.entities.User;
import ed.sanarenovo.services.MedecinService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MedecinTestClass {
    public static void main(String[] args) {
        MedecinService medecinService = new MedecinService();

        try {
            /*
            // 📅 Format for date input
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date dateEmbauche = sdf.parse("2022-09-15");

            // 👤 Create User
            User user = new User();
            user.setEmail("medecin1@example.com");
            user.setPassword("securePassword123");
            user.setRoles("[\"ROLE_MEDECIN\"]");
            user.setBlocked(false);

            // 🧑‍⚕️ Create Medecin
            Medecin medecin = new Medecin();
            medecin.setFullname("Dr. Salma Ben Ali");
            medecin.setDateEmbauche(dateEmbauche);
            medecin.setSpecilite("Cardiologie");
            medecin.setUser(user);

            // 🔁 Test Add
            medecinService.add(medecin);



             */

            // 📄 Test Get All
            List<Medecin> medecins = medecinService.getAll();
            for (Medecin m : medecins) {
                System.out.println("ID: " + m.getId());
                System.out.println("Nom: " + m.getFullname());
                System.out.println("Spécialité: " + m.getSpecilite());
                System.out.println("Email: " + m.getUser().getEmail());
                System.out.println("------");
            }

            /*
            // ✏️ Test Update (update the last added medecin)
            if (!medecins.isEmpty()) {
                Medecin lastMed = medecins.get(medecins.size() - 1);
                lastMed.setFullname("Dr. Salma B. Ali");
                lastMed.setSpecialite("Neurologie");
                medecinService.update(lastMed);
            }

             */
            // ❌ Test Delete (delete the last added medecin)


            if (!medecins.isEmpty()) {
                int idToDelete = medecins.get(medecins.size() - 1).getId();
                medecinService.delete(19);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
