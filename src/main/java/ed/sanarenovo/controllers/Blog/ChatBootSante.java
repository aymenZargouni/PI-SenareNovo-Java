package ed.sanarenovo.controllers.Blog;

import ed.sanarenovo.services.MedicalChatbotService;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;

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
}

