package com.garciajuanjo.calculatorpan.util;

import com.garciajuanjo.calculatorpan.App;
import com.garciajuanjo.calculatorpan.controller.AlertController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.garciajuanjo.calculatorpan.util.Constant.*;

public class MessageUtil {

    public enum TypeAlert {
        INFORMATION, WARNING, ERROR
    }

    public static void MyAlert(String message, TypeAlert typeAlert) {
        try {
            App.getParent().setEffect(new GaussianBlur(7));

            FXMLLoader loader = new FXMLLoader(App.class.getResource(viewAlert));

            //creamos el Parent y le damos los estilos css
            Parent parent = loader.load();

            parent.setStyle("-fx-base: #fcdbbd; -fx-border-color: rgba(0,0,0,.5); -fx-border-width: 2;");

            AlertController controller = loader.getController();
            controller.setImgAlert(new Image(App.class.getResource(imgError).toString()));
            controller.setTextAlert(message);
            controller.getHbButtons().setVisible(false);

            addImageAlert(typeAlert, controller);

            Scene scene = new Scene(parent);
            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(scene);
            stage.show();

            stage.setOnHidden(windowEvent -> App.getParent().setEffect(null));

            Platform.runLater( () -> {
                try {
                    Thread.sleep(3000);
                    stage.close();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            AlertWarning(errorLoad);
        }
    }

    public static boolean  MyAlertConfirmation(String message){
        AtomicBoolean election = new AtomicBoolean();
        try {
            App.getParent().setEffect(new GaussianBlur(7));

            FXMLLoader loader = new FXMLLoader(App.class.getResource(viewAlert));

            //creamos el Parent y le damos los estilos css
            Parent parent = loader.load();
            parent.setStyle("-fx-base: #C09f80");

            AlertController controller = loader.getController();
            controller.setTextAlert(message);
            controller.setImgAlert(new Image(App.class.getResource(imgInformation).toString()));

            Scene scene = new Scene(parent);
            Stage stage = new Stage();
            stage.getIcons().add(new Image(App.class.getResource(imgFavicon).toString()));
            stage.setResizable(false);
            stage.setScene(scene);

            stage.setOnHidden(windowEvent -> App.getParent().setEffect(null));



            controller.getOkButton().setOnAction(event -> {
                election.set(true);
                stage.close();
            });
            controller.getCancelButton().setOnAction(event -> {
                election.set(false);
                stage.close();
            });

            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            AlertWarning(errorLoad);
        }
        return election.get();
    }

    public static void MyAlertDataBase(){
        MyAlert(errorConectDb, TypeAlert.ERROR);
    }

    public static void MyAlertErrorLoad(){
        MyAlert(errorLoad, TypeAlert.ERROR);
    }

    private static void addImageAlert(TypeAlert typeAlert, AlertController controller){
        switch(typeAlert){
            case INFORMATION:
                controller.setImgAlert(new Image(App.class.getResource(imgCorrect).toString()));
                break;
            case WARNING:
                controller.setImgAlert(new Image(App.class.getResource(imgAlert).toString()));
                break;
            case ERROR:
                controller.setImgAlert(new Image(App.class.getResource(imgError).toString()));
                break;
        }
    }

    private static void AlertWarning(String message){
        Alert alert = new Alert(Alert.AlertType.WARNING, message);
        alert.getDialogPane().setPrefSize(450,120);
        alert.setHeaderText(null);
        alert.setTitle(null);
        alert.show();
    }
}
