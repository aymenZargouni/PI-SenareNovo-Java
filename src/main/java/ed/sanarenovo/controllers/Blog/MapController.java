// Gère spécifiquement l'affichage de la carte interactive.
package ed.sanarenovo.controllers.Blog;

import ed.sanarenovo.entities.CovidData;
import javafx.fxml.FXML;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import java.util.List;
import com.google.gson.Gson;

public class MapController {
    @FXML private WebView mapView;

    // Charge le template de base de la carte
    public void initialize() {
        mapView.getEngine().loadContent(getMapHtml());
        JSObject bridge = (JSObject) mapView.getEngine().executeScript("window");
        bridge.setMember("javaConnector", new JavaConnector());
    }
    // Retourne le code HTML initial de la carte
    private String getMapHtml() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <link rel="stylesheet" href="https://unpkg.com/leaflet@1.7.1/dist/leaflet.css"/>
                <script src="https://unpkg.com/leaflet@1.7.1/dist/leaflet.js"></script>
            </head>
            <body>
                <div id="map" style="width: 100%; height: 600px;"></div>
                <script>
                    var map = L.map('map').setView([20, 0], 2);
                    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);
                    
                    window.updateData = function(data) {
                        data.forEach(item => {
                            L.circle([item.lat, item.lon], {
                                radius: Math.sqrt(item.cases) * 100,
                                color: '#e74c3c',
                                fillOpacity: 0.5
                            }).addTo(map).bindPopup(`<b>${item.country}</b><br>Cas: ${item.cases}`);
                        });
                    };
                </script>
            </body>
            </html>
            """;
    }
    // Classe interne pour la communication Java-JavaScript
    public class JavaConnector {
        private final Gson gson = new Gson();

        public void sendDataToMap(List<CovidData> data) {
            String json = gson.toJson(data);
            mapView.getEngine().executeScript("updateData(" + json + ")");
        }
    }
}