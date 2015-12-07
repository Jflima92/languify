package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import logic.Languifier;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

public class mainController implements Initializable {

    @FXML
    private Stage mainStage;

    @FXML
    TextArea textInput;

    @FXML
    Button inputButton;

    @FXML
    MenuItem train_Provided;

    @FXML
    MenuItem train_single;

    @FXML
    TextField language;

    Languifier lang;

    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        lang = new Languifier();
        inputButton.setOnAction(this::handleInputButtonAction);
        train_Provided.setOnAction(this::handleProvidedDataTraining);
        train_single.setOnAction(this::handleSingleDataTraining);
    }

    public void setStage(Stage stage)
    {
        this.mainStage = stage;
    }

    private void handleInputButtonAction(ActionEvent actionEvent) {

        String msg = textInput.getText();
        LinkedHashMap local = lang.combineGramsData(true, msg);
        LinkedHashMap db = lang.combineGramsData(false, "english");
        LinkedHashMap db2 = lang.combineGramsData(false, "spanish");
        LinkedHashMap db3 = lang.combineGramsData(false, "french");
        LinkedHashMap db4 = lang.combineGramsData(false, "hungarian");
        LinkedHashMap db5 = lang.combineGramsData(false, "italian");
        LinkedHashMap db6 = lang.combineGramsData(false, "portuguese");

        int rankingEn = lang.compareRankings(local, db);
        int rankingEs = lang.compareRankings(local, db2);
        int rankingFr = lang.compareRankings(local, db3);
        int rankingHu = lang.compareRankings(local, db4);
        int rankingIt = lang.compareRankings(local, db5);
        int rankingPt = lang.compareRankings(local, db6);

        HashMap<String, Integer> rankings = new HashMap<>();
        rankings.put("English", rankingEn);
        rankings.put("Spanish", rankingEs);
        rankings.put("French", rankingFr);
        rankings.put("Hungarian", rankingHu);
        rankings.put("Italian", rankingIt);
        rankings.put("Portuguese", rankingPt);

        System.out.println("English: " + rankingEn);
        System.out.println("Spanish: " + rankingEs);
        System.out.println("French: " + rankingFr);
        System.out.println("Hungarian: " + rankingHu);
        System.out.println("Italian: " + rankingIt);
        System.out.println("Portuguese: " + rankingPt);

        ArrayList<String> ranks = new ArrayList<>(lang.sortByValues(rankings).keySet());
        String lang = ranks.get(ranks.size()-1);

        System.out.println(lang);
        language.setText(lang);


    }

    private void handleProvidedDataTraining(ActionEvent actionEvent) {
        ClassLoader classLoader = getClass().getClassLoader();

        File folder = new File(classLoader.getResource("training_data").getPath());
        File[] listOfFiles = folder.listFiles();
//        System.out.println(listOfFiles.length);

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                try {
                    lang.documentTraining("training_data/"+listOfFiles[i].getName());
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                System.out.println("File " + listOfFiles[i].getName());
            }
        }
    }
    private void handleSingleDataTraining(ActionEvent actionEvent) {
        ClassLoader classLoader = getClass().getClassLoader();

        File folder = new File(classLoader.getResource("training_data").getPath());
        File[] listOfFiles = folder.listFiles();
//        System.out.println(listOfFiles.length);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        File selectedFile = fileChooser.showOpenDialog(mainStage);
//        System.out.println(selectedFile.getName());
        try {
            lang.documentTraining("training_data/"+selectedFile.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (selectedFile != null) {
        }

    }


}
