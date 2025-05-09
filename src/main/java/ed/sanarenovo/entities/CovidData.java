// Représente une entrée de données COVID-19 avec:
// Pays
// Province
// Coordonnées géographiques (lat, lon)
// Nombre de cas
package ed.sanarenovo.entities;

import java.util.Objects;

public class CovidData {
    private String country;
    private String province;
    private double lat;
    private double lon;
    private int cases;


    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public int getCases() {
        return cases;
    }

    public void setCases(int cases) {
        this.cases = cases;
    }

    public CovidData(String country, String province, double lat, double lon, int cases) {
        this.country = country;
        this.province = province;
        this.lat = lat;
        this.lon = lon;
        this.cases = cases;
    }

    @Override
    public String toString() {
        return "CovidData{" +
                "country='" + country + '\'' +
                ", province='" + province + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                ", cases=" + cases +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CovidData covidData = (CovidData) o;
        return Double.compare(lat, covidData.lat) == 0 && Double.compare(lon, covidData.lon) == 0 && cases == covidData.cases && Objects.equals(country, covidData.country) && Objects.equals(province, covidData.province);
    }

    @Override
    public int hashCode() {
        return Objects.hash(country, province, lat, lon, cases);
    }


}