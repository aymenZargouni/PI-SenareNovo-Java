// PriceTableCell.java
package ed.sanarenovo.utils;

import ed.sanarenovo.controllers.GestionEquipment.EquipmentController;
import ed.sanarenovo.entities.Equipment;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class PriceTableCell implements Callback<TableColumn<Equipment, Double>, TableCell<Equipment, Double>> {

    private final EquipmentController controller;

    public PriceTableCell(EquipmentController controller) {
        this.controller = controller;
    }

    @Override
    public TableCell<Equipment, Double> call(TableColumn<Equipment, Double> param) {
        return new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);

                if (empty || price == null) {
                    setText(null);
                } else {
                    if ("TND".equals(controller.getCurrentCurrency())) {
                        setText(String.format("%.2f TND", price * 3.3));
                    } else {
                        setText(String.format("%.2f €", price));
                    }
                }
            }
        };
    }
}