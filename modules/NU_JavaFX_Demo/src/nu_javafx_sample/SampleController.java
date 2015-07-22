/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nu_javafx_sample;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 *
 * @author Shashank
 */
public class SampleController implements Initializable {
    
    private UploaderService us;
    private Stage stage;
    
        
    void intialize( UploaderService us,Stage primaryStage){
        this.us = us; this.stage = primaryStage;
        
        //Set the default file to upload
        //String defaultFilePath = "C:\\neembuuuploader\\neembuuuploader-gitcode\\modules\\NU_JavaFX_Demo\\external_plugins\\metadata.json";
        //filepath.setText(defaultFilePath);
        //f = new File(defaultFilePath);
        
        //guess what these do ?
        start.visibleProperty().bind(browseButton.visibleProperty());
        copyDownloadLink.visibleProperty().bind(makeStringLengthBinding(downloadLink.textProperty()));
        copyDeleteLink.visibleProperty().bind(makeStringLengthBinding(deleteLink.textProperty()));
        downloadLinkLabel.visibleProperty().bind(makeStringLengthBinding(downloadLink.textProperty()));
        deleteLinkLabel.visibleProperty().bind(makeStringLengthBinding(deleteLink.textProperty()));
        downloadLink.visibleProperty().bind(makeStringLengthBinding(downloadLink.textProperty()));
        deleteLink.visibleProperty().bind(makeStringLengthBinding(deleteLink.textProperty()));
    }
    
    private BooleanBinding makeStringLengthBinding(final StringProperty p){
        return new BooleanBinding() {
            {super.bind(p);}
            
            @Override protected boolean computeValue() {
                int length = p.get().length();
                return length > 0;
            }
        };
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //mean either one of the button will be visible
    }

    @FXML private Label filepath;
    @FXML private Button browseButton;
    @FXML private Button start;
    @FXML ProgressBar progress;
    @FXML private Button copyDownloadLink;
    @FXML TextField downloadLink;
    @FXML private Button copyDeleteLink;
    @FXML TextField deleteLink;
    @FXML private Label downloadLinkLabel;
    @FXML private Label deleteLinkLabel;

    private File f;
    
    @FXML void browse(ActionEvent event) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose file to upload");
        final Button openButton = new Button("Open a Picture...");        
        
        /*openButton.setOnAction(
            new EventHandler<ActionEvent>() {
                @Override public void handle(final ActionEvent e) {
                    File file = fileChooser.showOpenDialog(stage);
                    if (file != null) {
                        f = file;
                    }
                }
            });*/
 
        f = fileChooser.showOpenDialog(stage);
        if(f==null)return;
        filepath.setText(f.getAbsolutePath());
        browseButton.setVisible(false);
        /*final GridPane inputGridPane = new GridPane();
 
        GridPane.setConstraints(openButton, 0, 0);
        inputGridPane.setHgap(6);
        inputGridPane.setVgap(6);
        inputGridPane.getChildren().addAll(openButton);
 
        final Pane rootGroup = new VBox(12);
        rootGroup.getChildren().addAll(inputGridPane);
        rootGroup.setPadding(new Insets(12, 12, 12, 12));*/
    }
    
    @FXML void startUpload(ActionEvent event){
        us.handleFile(f);
        browseButton.setVisible(true);
    }

    @FXML void copyDownloadLink(ActionEvent event) {
        putIntoClipboard(downloadLink.getText());
    }

    @FXML void copyDeleteLink(ActionEvent event) {
        putIntoClipboard(deleteLink.getText());
    }
    
    private void putIntoClipboard(String t){
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(t);
        clipboard.setContent(content);
    }

}
