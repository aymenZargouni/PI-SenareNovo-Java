package ed.sanarenovo.utils;

import okhttp3.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.*;

public class ReclamationIA {

    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String API_KEY = "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"; // Remplace par ta clé OpenRouter

    public static String genererRapport(String description) throws IOException {
        OkHttpClient client = new OkHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        // Prompt détaillé pour un rapport professionnel et technique
        String prompt = "Rédige un rapport technique et professionnel sur la réclamation suivante. "
                + "Le rapport doit contenir :\n"
                + "- Un titre clair et formel\n"
                + "- Un résumé de la réclamation\n"
                + "- Une analyse technique du problème\n"
                + "- Des causes possibles\n"
                + "- Des recommandations ou solutions proposées\n"
                + "- Une conclusion\n\n"
                + "Voici la description : " + description;

        // Construire le JSON des messages
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "mistralai/mistral-7b-instruct");

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "user", "content", prompt));
        payload.put("messages", messages);

        String jsonPayload = mapper.writeValueAsString(payload);

        // Construire la requête HTTP
        Request request = new Request.Builder()
                .url(API_URL)
                .post(RequestBody.create(jsonPayload, MediaType.get("application/json")))
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("HTTP-Referer", "https://yourapp.com") // Requis par OpenRouter
                .addHeader("X-Title", "ReclamationApp")
                .build();

        // Exécuter la requête
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erreur API: " + response.code() + " - " + response.message());
            }

            String responseBody = response.body().string();
            JsonNode rootNode = mapper.readTree(responseBody);

            // Extraire le contenu généré
            return rootNode.path("choices").get(0).path("message").path("content").asText();
        }
    }
}
