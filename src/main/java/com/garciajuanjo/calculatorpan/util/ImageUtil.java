package com.garciajuanjo.calculatorpan.util;

import com.garciajuanjo.calculatorpan.App;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import static com.garciajuanjo.calculatorpan.util.Constant.*;

public class ImageUtil {

    public static void addImageLogo(ImageView imageView){
        Image image = new Image(App.class.getResource(imgLogo).toExternalForm());
        imageView.setImage(image);
        imageView.setFitWidth(200);
        imageView.setFitHeight(70);
    }

    public static void addImageButton(Button button, String nameImage){
        ImageView image = new ImageView(new Image(String.valueOf(App.class.getResource(nameImage))));
        image.setFitWidth(30);
        image.setFitHeight(30);
        button.setGraphic(image);
    }

    public static void addImageButton(Button button, String nameImage, double size){
        ImageView image = new ImageView(new Image(String.valueOf(App.class.getResource(nameImage))));
        image.setFitWidth(size);
        image.setFitHeight(size);
        button.setGraphic(image);
    }
}
