package ed.sanarenovo.controllers.Blog;

import ed.sanarenovo.services.MedicalChatbotService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
public class ChatBootSante {
    @FXML
    private ComboBox<String> questionComboBox;
    @FXML
    private TextArea answerTextArea;

    @FXML
    public void initialize() {
        List<String> questions = new ArrayList<>();
        MedicalChatbotService.getQuestions().forEach(questions::add);

        questionComboBox.getItems().addAll(questions);

        questionComboBox.setOnAction(event -> {
            String selectedQuestion = questionComboBox.getValue();
            if (selectedQuestion != null) {
                String answer = MedicalChatbotService.getAnswer(selectedQuestion);
                answerTextArea.setText(answer);
            }
        });
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Blog/BlogClient.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }
}
