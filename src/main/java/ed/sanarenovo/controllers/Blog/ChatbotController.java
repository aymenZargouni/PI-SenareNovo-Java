package ed.sanarenovo.controllers.Blog;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.IOException;
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

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Blog/BlogClient.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }
}