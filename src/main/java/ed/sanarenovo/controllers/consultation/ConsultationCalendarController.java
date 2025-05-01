package ed.sanarenovo.controllers.consultation;
import ed.sanarenovo.entities.consultation;
import ed.sanarenovo.utils.MyConnection;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;

import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.geometry.Pos;
import java.io.IOException;

public class ConsultationCalendarController implements Initializable {
    @FXML private Label monthYearLabel;
    @FXML private GridPane calendarGrid;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Label selectedDateRangeLabel;
    @FXML private Button clearFilterButton;
    @FXML private TableView<consultation> consultationTableView;

    private YearMonth currentYearMonth;
    private LocalDate startDate = null;
    private LocalDate endDate = null;

    private ObservableList<consultation> consultationData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        currentYearMonth = YearMonth.now();
        updateCalendar();
        loadConsultationsFromDatabase();

        // Configuration des colonnes du tableau
        consultationTableView.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("fullname"));
        consultationTableView.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("date"));
        consultationTableView.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("motif"));
        consultationTableView.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("typeConsultation"));
        consultationTableView.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("status"));

        prevButton.setOnAction(e -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            updateCalendar();
        });

        nextButton.setOnAction(e -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            updateCalendar();
        });

        clearFilterButton.setOnAction(e -> clearDateFilter());
    }

    private void loadConsultationsFromDatabase() {
        consultationData.clear();
        String query = "SELECT c.*, p.fullname FROM consultation c LEFT JOIN patient p ON c.patient_id = p.id";

        try (Connection cnx = MyConnection.getInstance().getCnx();
             Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                consultation c = new consultation();
                c.setId(rs.getInt("id"));
                c.setDate(rs.getString("date"));
                c.setMotif(rs.getString("motif"));
                c.setTypeConsultation(rs.getString("typeconsultation"));
                c.setStatus(rs.getString("status"));
                c.setPatientName(rs.getString("fullname"));

                consultationData.add(c);
            }

            consultationTableView.setItems(consultationData);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement des consultations", Alert.AlertType.ERROR);
        }
    }

    private void updateCalendar() {
        monthYearLabel.setText(currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        calendarGrid.getChildren().clear();

        String[] dayNames = {"Dim", "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam"};
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(dayNames[i]);
            dayLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #666666;");
            dayLabel.setAlignment(Pos.CENTER);
            calendarGrid.add(dayLabel, i, 0);
        }

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;

        for (int i = 1; i <= currentYearMonth.lengthOfMonth(); i++) {
            LocalDate date = currentYearMonth.atDay(i);
            Button dateBtn = new Button(String.valueOf(i));
            dateBtn.setMaxWidth(Double.MAX_VALUE);
            dateBtn.setMaxHeight(Double.MAX_VALUE);

            updateDateButtonStyle(dateBtn, date);
            dateBtn.setOnAction(e -> handleDateSelection(date));

            calendarGrid.add(dateBtn, (dayOfWeek + i - 1) % 7, (dayOfWeek + i - 1) / 7 + 1);
        }
    }

    private void handleDateSelection(LocalDate selectedDate) {
        if (startDate == null || (endDate != null || selectedDate.isBefore(startDate))) {
            startDate = selectedDate;
            endDate = null;
        } else {
            if (selectedDate.isAfter(startDate)) {
                endDate = selectedDate;
            } else {
                endDate = startDate;
                startDate = selectedDate;
            }
        }

        updateSelectedDateRangeLabel();
        updateCalendar();
        filterConsultationsByDate();
    }

    private void filterConsultationsByDate() {
        if (startDate == null) {
            consultationTableView.setItems(consultationData);
            return;
        }

        ObservableList<consultation> filteredList = FXCollections.observableArrayList();
        for (consultation cons : consultationData) {
            LocalDate consDate = LocalDate.parse(cons.getDate());
            if (endDate == null) {
                if (consDate.equals(startDate)) {
                    filteredList.add(cons);
                }
            } else {
                if ((consDate.isEqual(startDate) || consDate.isAfter(startDate)) &&
                        (consDate.isEqual(endDate) || consDate.isBefore(endDate))) {
                    filteredList.add(cons);
                }
            }
        }

        consultationTableView.setItems(filteredList);
    }

    private void updateDateButtonStyle(Button dateBtn, LocalDate date) {
        boolean isSelected = (startDate != null && date.equals(startDate)) ||
                (endDate != null && date.equals(endDate));
        boolean isInRange = startDate != null && endDate != null &&
                date.isAfter(startDate) && date.isBefore(endDate);
        boolean isToday = date.equals(LocalDate.now());

        final String baseStyle = "-fx-background-radius: 5; " +
                (isSelected ? "-fx-background-color: #FFE5EC; -fx-text-fill: #333333; -fx-font-weight: bold;" :
                        isInRange ? "-fx-background-color: #FFF5F7; -fx-text-fill: #333333;" :
                                isToday ? "-fx-background-color: #E6E6E6; -fx-text-fill: #333333;" :
                                        "-fx-background-color: transparent; -fx-text-fill: #333333;");

        dateBtn.setStyle(baseStyle);

        dateBtn.setOnMouseEntered(e -> {
            if (!isSelected && !isInRange) {
                dateBtn.setStyle(baseStyle + "-fx-background-color: #f0f0f0;");
            }
        });

        dateBtn.setOnMouseExited(e -> dateBtn.setStyle(baseStyle));
    }

    private void updateSelectedDateRangeLabel() {
        if (startDate == null && endDate == null) {
            selectedDateRangeLabel.setText("Aucun filtre de date");
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
        if (startDate != null && endDate != null) {
            selectedDateRangeLabel.setText(String.format("Du %s au %s",
                    startDate.format(formatter), endDate.format(formatter)));
        } else if (startDate != null) {
            selectedDateRangeLabel.setText(startDate.format(formatter));
        }
    }

    private void clearDateFilter() {
        startDate = null;
        endDate = null;
        updateSelectedDateRangeLabel();
        updateCalendar();
        consultationTableView.setItems(consultationData);
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void retour(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Youssef_views/cons.fxml"));
            Scene scene = ((Button) event.getSource()).getScene();
            scene.setRoot(root);
            URL cssUrl = getClass().getResource("/Youssef_views/design.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.out.println("⚠️ CSS file not found: /design.css");
            }
        } catch (IOException e) {
            showAlert("Erreur", "Erreur lors du retour: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}