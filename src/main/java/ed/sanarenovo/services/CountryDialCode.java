package ed.sanarenovo.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ed.sanarenovo.entities.Country;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CountryDialCode {

    private static final Map<String, String> countryToDial = new HashMap<>();

    static {
        loadCountryDialCodes();
    }

    private static void loadCountryDialCodes() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = CountryDialCode.class.getClassLoader().getResourceAsStream("CountryCodes.json");

            if (is != null) {
                List<Country> countries = mapper.readValue(is, new TypeReference<List<Country>>() {});
                for (Country country : countries) {
                    countryToDial.put(country.getCode(), country.getDial_code());
                }
            } else {
                System.err.println("country-codes.json not found in resources!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getDialCode(String countryCode) {
        return countryToDial.getOrDefault(countryCode.toUpperCase(), "");
    }
}

