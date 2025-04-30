package ed.sanarenovo.utils;

import ed.sanarenovo.entities.Equipment;
import javafx.scene.control.TableCell;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class StatusTableCell extends TableCell<Equipment, String> {
    @Override
    protected void updateItem(String status, boolean empty) {
        super.updateItem(status, empty);

        if (empty || status == null) {
            setGraphic(null);
            setText(null);
            return;
        }

        Label label = new Label(status);
        label.setFont(Font.font(14));

        ImageView icon = new ImageView();
        icon.setFitWidth(18);
        icon.setFitHeight(18);

        // Style selon le status
        switch (status.toLowerCase()) {
            case "reparé":
                label.setTextFill(Color.GREEN);
                icon.setImage(new Image(getClass().getResource("/Sabrineviews/icons/maintenance.png").toExternalForm()));
                break;
            case "panne":
                label.setTextFill(Color.RED);
                icon.setImage(new Image(getClass().getResource("/Sabrineviews/icons/broken.png").toExternalForm()));
                break;
            case "maintenance":
                label.setTextFill(Color.ORANGE);
                icon.setImage(new Image(getClass().getResource("/Sabrineviews/icons/repair.png").toExternalForm()));
                break;
            default:
                label.setTextFill(Color.BLACK);
                break;
        }

        HBox hBox = new HBox(5, icon, label);
        setGraphic(hBox);
    }
}
