package ed.sanarenovo.services;

import com.google.gson.Gson;
import ed.sanarenovo.entities.CovidData;
import java.util.List;

public class JsonUtils {
    private static final Gson gson = new Gson();

    public static String toJson(List<CovidData> data) {
        return gson.toJson(data);
    }
}