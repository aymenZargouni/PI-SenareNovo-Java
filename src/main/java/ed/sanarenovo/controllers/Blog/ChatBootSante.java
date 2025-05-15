package ed.sanarenovo.controllers.Blog;

import ed.sanarenovo.services.MedicalChatbotService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
    private Button btnRetour;
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
    private void retourBlog(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/Blog/BlogClient.fxml"));
        Stage stage = (Stage) btnRetour.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}

