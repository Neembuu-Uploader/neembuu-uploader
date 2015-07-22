/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nu_javafx_sample;

import java.io.IOException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 *
 * @author Shashank
 */
public class NU_JavaFx_Sample extends Application {

    private UploaderService us;
    private SampleController controller;
    
    public NU_JavaFx_Sample() {
        us = new UploaderServiceImpl(new UI() {
            @Override public void updateProgress(final double d) {
                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        controller.progress.setProgress(d);
                    }});}
            @Override public void updateStatus(String status) {
                throw new UnsupportedOperationException("Not supported yet."); 
            }
            @Override public void setDeleteLink(final String link) {
                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        controller.deleteLink.setText(link);
                    }});}
            @Override public void setDownloadLink(final String link) {
                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        controller.downloadLink.setText(link);
                    }});}
        });
    }

    @Override
    public void start(Stage primaryStage)throws IOException {
        primaryStage.setTitle("Neembuu Uploader 3.0 (Sample)");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/nu_javafx_sample/sample_ui.fxml"));

        AnchorPane  myPane = (AnchorPane ) loader.load();
        controller = loader.<SampleController>getController();
        controller.intialize(us,primaryStage);

        Scene myScene = new Scene(myPane);
        primaryStage.setScene(myScene);
        primaryStage.show() ;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        InitializeStuff.intializeGlobalStuff();
        launch(args);
    }

}
