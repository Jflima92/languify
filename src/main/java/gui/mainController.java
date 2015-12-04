package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import logic.Languifier;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class mainController implements Initializable {

    @FXML
    TextArea textInput;

    @FXML
    Button inputButton;

    Languifier lang;

    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        lang = new Languifier();
        inputButton.setOnAction(this::handleInputButtonAction);
    }

    private void handleInputButtonAction(ActionEvent actionEvent) {
        String msg = textInput.getText();
        //lang.mongoCharacterNGramGenerator(3, msg, "english");
//        System.out.println(lang.characterNGramGenerator(4, msg, "english"));
        /*System.out.println(lang.wordNGramGenerator(3, msg));*/
        try {
            lang.documentTraining("training_data/englishText_10000_20000");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
