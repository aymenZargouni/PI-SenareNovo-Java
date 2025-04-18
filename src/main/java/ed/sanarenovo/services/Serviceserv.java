package ed.sanarenovo.services;
import ed.sanarenovo.entities.Service;
import ed.sanarenovo.interfaces.UiService;

import ed.sanarenovo.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class Serviceserv implements UiService <Service> {

    @Override
    public void addService(Service service) {
        try {
            String requete = "INSERT INTO service (nom, chef_service, nbr_salle, capacite, etat) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, service.getNom());
            pst.setString(2, service.getChef_service());
            pst.setInt(3, service.getNbr_salle());
            pst.setInt(4, service.getCapacite());
            pst.setBoolean(5, service.isEtat());
            pst.executeUpdate();
            System.out.println("Service ajouté avec succès.");
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout du service : " + e.getMessage());
        }
    }

    public void deleteService(Service service) {
        try {
            Connection conn = MyConnection.getInstance().getCnx();
            if (conn == null) {
                System.out.println("Connexion à la base de données échouée !");
                return;
            }

            String requete = "DELETE FROM service WHERE id = ?";
            PreparedStatement pst = conn.prepareStatement(requete);
            pst.setInt(1, service.getId());

            System.out.println("Suppression du service avec ID : " + service.getId());

            int rows = pst.executeUpdate();
            if (rows > 0) {
                System.out.println("Service supprimé avec succès.");
            } else {
                System.out.println("Aucun service trouvé avec cet ID.");
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression : " + e.getMessage());
        }
    }
    @Override
    public void updateService(Service service, int id) {
        try {
            String requete = "UPDATE service SET nom = ?, chef_service = ?, nbr_salle = ?, capacite = ?, etat = ? WHERE id = ?";
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, service.getNom());
            pst.setString(2, service.getChef_service());
            pst.setInt(3, service.getNbr_salle());
            pst.setInt(4, service.getCapacite());
            pst.setBoolean(5, service.isEtat());
            pst.setInt(6, id);
            pst.executeUpdate();
            System.out.println("Service mis à jour avec succès.");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la mise à jour du service : " + e.getMessage());
        }
    }

    @Override
    public List<Service> getService() {
        return List.of();
    }

    public List<Service> getServices() {
        List<Service> data = new ArrayList<>();
        try {
            String requete = "SELECT * FROM service";
            Statement st = MyConnection.getInstance().getCnx().createStatement();
            ResultSet rs = st.executeQuery(requete);
            while (rs.next()) {
                Service s = new Service(14);
                s.setId(rs.getInt("id"));
                s.setNom(rs.getString("nom"));
                s.setChef_service(rs.getString("chef_service"));
                s.setNbr_salle(rs.getInt("nbr_salle"));
                s.setCapacite(rs.getInt("capacite"));
                s.setEtat(rs.getBoolean("etat"));
                data.add(s);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des services : " + e.getMessage());
        }

        return data;
    }
    @Override
     public Service getServiceById(int id) {
        Service s = null;
        try {
            String requete = "SELECT * FROM service WHERE id = ?";
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                s = new Service(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("chef_service"),
                        rs.getInt("nbr_salle"),
                        rs.getInt("capacite"),
                        rs.getBoolean("etat")
                );
            }
        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
        return s;

    }
    public List<Integer> getAllServiceIds() {
        List<Integer> ids = new ArrayList<>();
        String req = "SELECT id FROM service";

        try (Statement st = MyConnection.getInstance().getCnx().createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                ids.add(rs.getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ids;
    }}







