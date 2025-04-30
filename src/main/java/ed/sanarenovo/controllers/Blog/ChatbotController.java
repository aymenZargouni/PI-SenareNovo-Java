package ed.sanarenovo.controllers.Blog;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ChatbotController {

    @FXML private TextArea chatArea;
    @FXML private TextField userInput;
    @FXML private Button sendButton;

    private static final String API_KEY = "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";

    @FXML
    private void sendMessage() {
        String userMessage = userInput.getText().trim();
        if (userMessage.isEmpty()) return;

        chatArea.appendText("Vous: " + userMessage + "\n");
        userInput.clear();
        chatArea.appendText("Bot: ...\n");

        new Thread(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                String json = """
                        {
                          "model": "deepseek/deepseek-r1:free",
                          "messages": [
                            { "role": "user", "content": "%s" }
                          ]
                        }
                        """.formatted(userMessage);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://openrouter.ai/api/v1/chat/completions"))
                        .header("Authorization", "Bearer " + API_KEY)
                        .header("Content-Type", "application/json")
                        .header("HTTP-Referer", "https://www.SenareNovo.com")
                        .header("X-Title", "SenareNovo")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                JSONObject jsonResponse = new JSONObject(response.body());
                String botReply = jsonResponse.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");

                Platform.runLater(() -> {
                    // Remplace "Bot: ..." par la vraie réponse
                    int lastIndex = chatArea.getText().lastIndexOf("Bot: ...");
                    if (lastIndex != -1) {
                        chatArea.replaceText(lastIndex, lastIndex + 8, "Bot: " + botReply + "\n");
                    } else {
                        chatArea.appendText("Bot: " + botReply + "\n");
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> chatArea.appendText("Bot: Erreur - " + e.getMessage() + "\n"));
            }
        }).start();
    }
}