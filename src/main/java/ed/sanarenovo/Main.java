package ed.sanarenovo;

import ed.sanarenovo.entities.Claim;
import ed.sanarenovo.entities.Equipment;
import ed.sanarenovo.entities.Historique;
import ed.sanarenovo.services.EquipmentService;
import ed.sanarenovo.services.HistoriqueService;
import ed.sanarenovo.utils.MyConnection;
import ed.sanarenovo.utils.ReportGenerator;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        MyConnection mc1 = MyConnection.getInstance();
        MyConnection mc2 = MyConnection.getInstance();

        // Afficher les hashCodes des deux instances de MyConnection pour confirmer qu'il s'agit de la même instance (Singleton)
        System.out.println("HashCode de la connexion (mc1) : " + mc1.hashCode());
        System.out.println("HashCode de la connexion (mc2) : " + mc2.hashCode());

        // 1. Créer un nouvel équipement


        Equipment newEquipment = new Equipment("macbook", "appel2025", Date.valueOf("2025-01-01"), 5150.2);

        // Ajouter l'équipement à la base de données
// Ajouter l'équipement à la base de données
        EquipmentService equipmentService = new EquipmentService();
//        equipmentService.addEntity(newEquipment);
        Equipment updatedEquipment = new Equipment();
        updatedEquipment.setId(16);  // ID de l'équipement à mettre à jour
        updatedEquipment.setName("New Name");
        updatedEquipment.setModel("New Model");
        updatedEquipment.setStatus("Updated Status");
        updatedEquipment.setDateAchat(Date.valueOf("2025-01-01"));
        updatedEquipment.setPrix(1200.0);

// Appeler la méthode updateEntity
        equipmentService.updateEntity(updatedEquipment, 16);  // Utilisez l'ID pour cibler


        List<Equipment> allEquipments = equipmentService.getAll();

        // Afficher les informations des équipements récupérés
        for (Equipment equipment : allEquipments) {
            System.out.println("Equipment ID: " + equipment.getId());
            System.out.println("Name: " + equipment.getName());
            System.out.println("Model: " + equipment.getModel());
            System.out.println("Status: " + equipment.getStatus());
            System.out.println("Date Achat: " + equipment.getDateAchat());
            System.out.println("Prix: " + equipment.getPrix());
            System.out.println("Date Reparation: " + equipment.getDateReparation());
            System.out.println("Rapport Detaille: " + equipment.getRapportDetaille());
            System.out.println("----------------------------------");
        }
        //ClaimService claimService = new ClaimService();
        //List<Claim> toutesReclamations = claimService.getAll();
        //for (Claim claim : toutesReclamations) {
        //    System.out.println("Claim ID: " + claim.getId());
        //    System.out.println("reclamtion "+claim.getReclamation());
        }
        //Claim claimToUpdate = ClaimService.getClaimById(4);
        //if (claimToUpdate != null) {
            // Modifier les propriétés
            //claimToUpdate.setReclamation("Nouvelle description");
            //claimToUpdate.setCreatedAt(new Timestamp(System.currentTimeMillis()));

            // Appeler la mise à jour
          //claimService.updateEntity(claimToUpdate, claimToUpdate.getId());
          //claimService.deleteEntity(17);
            //claimService.claimEquipment(12,4);

        //    int technicienConnecteId = 3; // ID du technicien connecté

          //TechnicienInterface interfaceTech = new TechnicienInterface(technicienConnecteId);
        // interfaceTech.afficherMenu();
            HistoriqueService hs = new HistoriqueService();

            // Synchroniser les historiques depuis equipment
            //hs.syncHistoriqueFromEquipment();

            // Afficher les historiques
            List<Historique> historiques = hs.getAllHistoriques();
            //for (Historique h : historiques) {
             //   System.out.println("Équipement: " + h.getEquipment().getName() +
               //         ", Modèle: " + h.getEquipment().getModel() +
             //           ", Date réparation: " + h.getDateReparation() +
              //          ", Rapport: " + h.getRapportDetaille());
            }
            //ReportGenerator rg = new ReportGenerator();
            //rg.generateReport(3);  // Remplacer 1 par l'ID de l'équipement pour lequel tu veux générer un rapport
          //  hs.generateReport(1);
//            CoordinateurInterface  interfacecor = new CoordinateurInterface();
//            interfacecor.showReportDownloadMenu();
    //}}}