package gui;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import logic.Languifier;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
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
    Button uploadButton;

    @FXML
    MenuItem train_Provided;

    @FXML
    MenuItem train_single;

    @FXML
    TextField language;

    @FXML
    TextField elapsedTime;

    @FXML
    TextField fileName;

    @FXML
    ProgressBar progressBar;

    @FXML
    ProgressIndicator progressInd;

    Languifier lang;

    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        lang = new Languifier();
        inputButton.setOnAction(this::handleInputButtonAction);
        uploadButton.setOnAction(this::handleUploadButtonAction);
        train_Provided.setOnAction(this::handleProvidedDataTraining);
        train_single.setOnAction(this::handleSingleDataTraining);
    }

    public void setStage(Stage stage)
    {
        this.mainStage = stage;
    }

    private void handleInputButtonAction(ActionEvent actionEvent) {
        String msg = textInput.getText();
        checkLanguage(msg);

    }

    private void handleUploadButtonAction(ActionEvent actionEvent) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        File selectedFile = fileChooser.showOpenDialog(mainStage);

        try {
            byte[] encoded = Files.readAllBytes(Paths.get(selectedFile.getPath()));
            fileName.setText(selectedFile.getName());
            checkLanguage(new String(encoded));
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void checkLanguage(String msg){

        Platform.runLater(() -> progressBar.setProgress(-0.2f));
        Platform.runLater(() -> progressInd.setProgress(0.0f));
        long start = System.currentTimeMillis();

        Task dataLoading = new Task<Object>() {
            {
                setOnSucceeded(workerStateEvent -> {

                });

                setOnFailed(workerStateEvent -> getException().printStackTrace());
            }

            @Override
            protected Object call() throws Exception {

                LinkedHashMap local = lang.combineGramsData(true, msg);
                LinkedHashMap db = lang.combineGramsData(false, "english");
                Platform.runLater(() -> progressBar.setProgress(0.1f));
                Platform.runLater(() -> progressInd.setProgress(0.1f));
                int rankingEn = lang.compareRankings(local, db);

                db.clear();
                db = lang.combineGramsData(false, "spanish");
                Platform.runLater(() -> progressBar.setProgress(0.2f));
                Platform.runLater(() -> progressInd.setProgress(0.2f));
                int rankingEs = lang.compareRankings(local, db);

                db.clear();
                db = lang.combineGramsData(false, "french");
                Platform.runLater(() -> progressBar.setProgress(0.3f));
                Platform.runLater(() -> progressInd.setProgress(0.3f));
                int rankingFr = lang.compareRankings(local, db);

                db.clear();
                db = lang.combineGramsData(false, "hungarian");
                Platform.runLater(() -> progressBar.setProgress(0.4f));
                Platform.runLater(() -> progressInd.setProgress(0.4f));
                int rankingHu = lang.compareRankings(local, db);

                db.clear();
                db = lang.combineGramsData(false, "italian");
                Platform.runLater(() -> progressBar.setProgress(0.5f));
                Platform.runLater(() -> progressInd.setProgress(0.5f));
                int rankingIt = lang.compareRankings(local, db);

                db.clear();
                db = lang.combineGramsData(false, "portuguese");
                Platform.runLater(() -> progressBar.setProgress(0.6f));
                Platform.runLater(() -> progressInd.setProgress(0.6f));
                int rankingPt = lang.compareRankings(local, db);

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
                Platform.runLater(() -> progressBar.setProgress(1f));
                Platform.runLater(() -> progressInd.setProgress(1f));
                long elapsedTimeMillis = System.currentTimeMillis()-start;
                float elapsedTimeSec = elapsedTimeMillis/1000F;
                elapsedTime.setText(String.valueOf(elapsedTimeSec)+ " seconds");
                language.setText(lang);
                return null;
            }
        };

        Thread loadingThread = new Thread(dataLoading, "data-loader");
        loadingThread.setDaemon(true);
        loadingThread.start();

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
