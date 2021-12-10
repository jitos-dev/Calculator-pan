package com.garciajuanjo.calculatorpan.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

public class AlertController {

    @FXML
    private Pane alertPane;
    @FXML
    private ImageView imgAlert;
    @FXML
    private Text textAlert;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button okButton, cancelButton;


    public Pane getAlertPane() {
        return alertPane;
    }

    public Button getOkButton() {
        return okButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public HBox getHbButtons(){
        return this.hbButtons;
    }

    public void setImgAlert(Image img){
        this.imgAlert.setImage(img);
    }

    public void setTextAlert(String text){
        this.textAlert.setText(text);
    }
}
