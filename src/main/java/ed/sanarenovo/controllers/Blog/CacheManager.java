package ed.sanarenovo.controllers.Blog;

import com.fasterxml.jackson.databind.ObjectMapper;
import ed.sanarenovo.entities.CovidData;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.*;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

public class CacheManager {
    private static final String CACHE_FILE = "covid_cache.json";
    private static final int CACHE_HOURS = 6;

    public static boolean isCacheValid() {
        File file = new File(CACHE_FILE);
        if (!file.exists()) return false;

        long lastModified = file.lastModified();
        return ChronoUnit.HOURS.between(
                LocalDateTime.ofEpochSecond(lastModified, 0, ZoneOffset.UTC),
                LocalDateTime.now()
        ) < CACHE_HOURS;
    }

    public static void saveToCache(List<CovidData> data) throws IOException {
        new ObjectMapper().writeValue(new File(CACHE_FILE), data);
    }

    public static List<CovidData> loadFromCache() throws IOException {
        return Arrays.asList(
                new ObjectMapper().readValue(new File(CACHE_FILE), CovidData[].class)
        );
    }
}