package ed.sanarenovo.controllers.Blog;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ed.sanarenovo.entities.Category;
import ed.sanarenovo.services.CategoryServices;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;


public class CategoryController {
    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private TableView<Category> tableCategory;
    @FXML private TableColumn<Category, Integer> colId;
    @FXML private TableColumn<Category, String> colName;
    @FXML private TextField txtName;

    private final CategoryServices categoryService = new CategoryServices();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());

        colName.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));

        refreshTable();
    }

    private void refreshTable() {
        ObservableList<Category> categories = FXCollections.observableArrayList(
                categoryService.getCategorys()
        );
        tableCategory.setItems(categories);
    }

    @FXML
    private void addCategory() {
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Champ requis", "Le nom doit être rempli.");
            return;
        }
        if (name.length() < 3) {
            showAlert(Alert.AlertType.WARNING, "Nom invalide", "Le nom doit contenir au moins 3 caractères.");
            return;
        }

        Category category = new Category(0, name);
        categoryService.addCategory(category);
        refreshTable();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    private void clearFields() {
        txtName.clear();
    }

    @FXML
    private void updateCategory() {
        Category selected = tableCategory.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setName(txtName.getText());
            categoryService.updateCategory(selected, selected.getId());
            refreshTable();
        }
    }

    @FXML
    private void deleteCategory() {
        Category selected = tableCategory.getSelectionModel().getSelectedItem();
        if (selected != null) {
            categoryService.deleteCategory(selected);
            refreshTable();
        }
    }
}
