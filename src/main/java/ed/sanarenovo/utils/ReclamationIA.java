package ed.sanarenovo.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;

public class ReclamationIA {

    // Modèle optimisé pour le français et les rapports techniques
    private static final String API_URL = "https://api-inference.huggingface.co/models/moussaKam/barthez-orangesum-abstract";
    private static final String API_TOKEN = "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";
    private static final int MAX_RETRIES = 5;
    private static final int RETRY_DELAY = 45;

    public static String genererRapport(String description) {
        try {
            String prompt = buildOptimizedPrompt(description);
            System.out.println("[DEBUG] Prompt généré:\n" + prompt); // Log de débogage

            for (int i = 0; i < MAX_RETRIES; i++) {
                HttpResponse<String> response = sendApiRequest(createRequest(prompt));
                System.out.println("[DEBUG] Réponse brute: " + response.body()); // Log de la réponse

                if (response.statusCode() == 200) {
                    String result = parseSuccessfulResponse(response.body());
                    if (!result.isEmpty()) {
                        return postProcessResponse(result);
                    }
                } else {
                    handleErrorResponse(response);
                }

                if (i < MAX_RETRIES - 1) {
                    System.out.println("Nouvelle tentative dans " + RETRY_DELAY + " secondes...");
                    TimeUnit.SECONDS.sleep(RETRY_DELAY);
                }
            }
            return "Échec de génération après " + MAX_RETRIES + " tentatives";

        } catch (Exception e) {
            System.err.println("Erreur critique: ");
            e.printStackTrace();
            return "Erreur système: " + e.getClass().getSimpleName();
        }
    }

    private static String buildOptimizedPrompt(String description) {
        return String.format(
                "Génère un rapport technique détaillé en français avec cette structure:\n" +
                        "Problème: %s\n" +
                        "Cause probable:\n" +
                        "Diagnostic technique:\n" +
                        "Solution recommandée:\n" +
                        "Pièces nécessaires:\n" +
                        "---\n" +
                        "Utilise un style professionnel et des termes techniques précis.",
                description
        );
    }

    private static String createRequest(String prompt) {
        JSONObject parameters = new JSONObject();
        parameters.put("max_new_tokens", 350);
        parameters.put("temperature", 0.7);
        parameters.put("top_k", 40);
        parameters.put("num_return_sequences", 1);

        JSONObject request = new JSONObject();
        request.put("inputs", prompt);
        request.put("parameters", parameters);

        return request.toString();
    }

    private static HttpResponse<String> sendApiRequest(String jsonInput) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Authorization", "Bearer " + API_TOKEN)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonInput))
                .build();

        return HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static String parseSuccessfulResponse(String responseBody) {
        try {
            JSONArray jsonArray = new JSONArray(responseBody);
            if (jsonArray.length() > 0) {
                JSONObject firstItem = jsonArray.getJSONObject(0);
                return firstItem.optString("generated_text", "");
            }
            return "";
        } catch (Exception e) {
            System.err.println("Erreur d'analyse de la réponse: " + e.getMessage());
            return "";
        }
    }

    private static void handleErrorResponse(HttpResponse<String> response) {
        System.err.println("Erreur API - Code: " + response.statusCode());
        System.err.println("Détails: " + response.body());

        try {
            JSONObject errorResponse = new JSONObject(response.body());
            if (errorResponse.has("error")) {
                String errorType = errorResponse.optString("error", "Inconnue");
                System.err.println("Type d'erreur: " + errorType);
            }
        } catch (Exception e) {
            System.err.println("Format d'erreur non reconnu");
        }
    }

    private static String postProcessResponse(String text) {
        return text.replaceAll("(\\n\\s*){2,}", "\n") // Nettoyage des sauts de ligne
                .replaceAll("\\s{2,}", " ")        // Suppression des espaces multiples
                .trim();
    }
}