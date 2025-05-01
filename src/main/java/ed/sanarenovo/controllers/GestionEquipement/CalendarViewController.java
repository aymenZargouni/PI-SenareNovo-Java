package ed.sanarenovo.controllers.GestionEquipement;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import ed.sanarenovo.entities.Claim;
import ed.sanarenovo.services.ClaimService;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

public class CalendarViewController {

    @FXML
    private BorderPane calendarContainer;
    private final ClaimService claimService = new ClaimService();

    public void initialize() {
        try {
            // 1. Création du calendrier
            CalendarView calendarView = new CalendarView();
            calendarView.setShowAddCalendarButton(false);
            calendarView.setShowPrintButton(false);
            calendarView.showMonthPage();

            // 2. Création du calendrier des réclamations
            Calendar reclamationsCalendar = new Calendar("Réclamations");
            reclamationsCalendar.setStyle(Calendar.Style.STYLE1); // Rouge

            // 3. Chargement des données
            List<Claim> claims = claimService.getAll();
            System.out.println("Nombre de réclamations chargées: " + claims.size());
            loadClaimsIntoCalendar(reclamationsCalendar, claims);

            // 4. Configuration de la source
            CalendarSource calendarSource = new CalendarSource("SanareNovo");
            calendarSource.getCalendars().add(reclamationsCalendar);
            calendarView.getCalendarSources().add(calendarSource);

            // 5. Ajout à la vue
            calendarContainer.setCenter(calendarView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadClaimsIntoCalendar(Calendar calendar, List<Claim> claims) {
        calendar.clear();

        for (Claim claim : claims) {
            try {
                Entry<String> entry = new Entry<>("Réclamation #" + claim.getId());

                LocalDateTime start = claim.getCreatedAt().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
                LocalDateTime end = start.plusHours(1);

                entry.setInterval(start.toLocalDate(), start.toLocalTime(),
                        end.toLocalDate(), end.toLocalTime());

                entry.setLocation(buildEntryDescription(claim));
                entry.setCalendar(calendar);

                calendar.addEntry(entry);
            } catch (Exception e) {
                System.err.println("Erreur avec la réclamation #" + claim.getId());
                e.printStackTrace();
            }
        }
    }

    private String buildEntryDescription(Claim claim) {
        return "Équipement: " + claim.getEquipment().getName() + "\n" +
                "Description: " + claim.getReclamation() + "\n" +
                "Technicien: " + (claim.getTechnicien() != null ?
                claim.getTechnicien().getNom() : "Non assigné");
    }
}