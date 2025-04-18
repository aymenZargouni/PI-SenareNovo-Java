package ed.sanarenovo.services;

import ed.sanarenovo.entities.Category;
import ed.sanarenovo.interfaces.ICategory;
import ed.sanarenovo.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryServices implements ICategory<Category> {

    @Override
    public void addCategory(Category category) {
        String query = "INSERT INTO category (name) VALUES (?)";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, category.getName());
            pst.executeUpdate();
            System.out.println("Catégorie ajoutée avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout de la catégorie : " + e.getMessage());
        }
    }

    @Override
    public void deleteCategory(Category category) {
        String query = "DELETE FROM category WHERE id = ?";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(query);
            pst.setInt(1, category.getId());
            int rowsDeleted = pst.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Catégorie supprimée avec succès !");
            } else {
                System.out.println("Aucune catégorie trouvée avec cet ID !");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression de la catégorie : " + e.getMessage());
        }
    }

    @Override
    public void updateCategory(Category category, int id) {
        String query = "UPDATE category SET name = ? WHERE id = ?";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, category.getName());
            pst.setInt(2, id);
            int rowsUpdated = pst.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Catégorie mise à jour avec succès !");
            } else {
                System.out.println("Aucune catégorie trouvée avec cet ID !");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la mise à jour de la catégorie : " + e.getMessage());
        }
    }

    @Override
    public List<Category> getCategorys() {
        List<Category> categories = new ArrayList<>();
        String query = "SELECT * FROM category";
        try {
            Statement st = MyConnection.getInstance().getCnx().createStatement();
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                Category category = new Category(rs.getInt("id"), rs.getString("name"));
                categories.add(category);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des catégories : " + e.getMessage());
        }
        return categories;
    }

    @Override
    public Category getCategoryById(int id) {
        String query = "SELECT * FROM category WHERE id = ?";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(query);
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new Category(rs.getInt("id"), rs.getString("name"));
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération de la catégorie : " + e.getMessage());
        }
        return null;
    }
}


