package ed.sanarenovo.services;

import ed.sanarenovo.entities.CovidData;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Collectors;

public class DataFetcher {
    private static final String GLOBAL_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";

    public List<CovidData> fetchGlobalData() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GLOBAL_DATA_URL))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Normalisation des noms de pays
        Map<String, String> countryNormalization = Map.of(
                "US", "United States",
                "Korea, South", "South Korea",
                "UK", "United Kingdom"
        );

        List<CovidData> data = new ArrayList<>();
        try (StringReader reader = new StringReader(response.body());
             CSVParser parser = CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim()
                     .parse(reader)) {

            for (CSVRecord record : parser) {
                try {
                    String country = record.get("Country/Region");
                    country = countryNormalization.getOrDefault(country, country);

                    String province = record.get("Province/State");
                    double lat = parseCoordinate(record, "Lat");
                    double lon = parseCoordinate(record, "Long");

                    // Prendre la dernière colonne (cas confirmés)
                    int cases = parseCases(record);

                    data.add(new CovidData(country, province, lat, lon, cases));
                } catch (Exception e) {
                    System.err.println("Error processing record: " + record);
                }
            }
        }

        // Fusionner les données par pays
        return mergeByCountry(data);
    }

    private int parseCases(CSVRecord record) {
        try {
            return Integer.parseInt(record.get(record.size() - 1));
        } catch (Exception e) {
            System.err.println("Invalid cases for: " + record);
            return 0;
        }
    }

    private double parseCoordinate(CSVRecord record, String column) {
        try {
            String value = record.get(column);
            if (value == null || value.trim().isEmpty()) return 0.0;
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private List<CovidData> mergeByCountry(List<CovidData> rawData) {
        Map<String, CovidData> merged = new HashMap<>();

        for (CovidData item : rawData) {
            merged.merge(item.getCountry(), item, (existing, newItem) -> {
                existing.setCases(existing.getCases() + newItem.getCases());
                // Garder les coordonnées si elles manquent
                if (existing.getLat() == 0 && existing.getLon() == 0) {
                    existing.setLat(newItem.getLat());
                    existing.setLon(newItem.getLon());
                }
                return existing;
            });
        }

        return new ArrayList<>(merged.values());
    }
}