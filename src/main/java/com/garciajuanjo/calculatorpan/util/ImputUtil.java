package com.garciajuanjo.calculatorpan.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.CheckComboBox;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.garciajuanjo.calculatorpan.util.Css.BOX_SHADOW_NONE;
import static com.garciajuanjo.calculatorpan.util.Css.BOX_SHADOW_RED;

public class ImputUtil {

    public static String cleanText(String text){
        text = Normalizer.normalize(text, Normalizer.Form.NFD);
        text = text.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return text;
    }

    public static void clearAllTextFields(TextField... textField){
        List<TextField> clear = Arrays.asList(textField);
        clear.forEach(TextInputControl::clear);
    }

    private static boolean isEmpty(TextField textField){
        if (textField.getText().isEmpty()) {
            textField.setStyle(BOX_SHADOW_RED);
            return true;
        }
        return false;
    }

    public static boolean isNotValidTfName(TextField textField){
        if (isNullOrBoxSadowRed(textField)){
            return true;
        }
        if (isEmpty(textField)){
            textField.setPromptText("Este campo es obligatorio");
            return true;

        } else if (textField.getText().length() > 50){
            textField.clear();
            textField.setStyle(BOX_SHADOW_RED);
            textField.setPromptText("max 50 caracteres");
            return true;
        }
        return false;
    }

    public static boolean isNotValidTfDirection(TextField textField){
        if (isNullOrBoxSadowRed(textField)){
            textField.setPromptText("Este campo es obligatorio");
            return true;
        }
        if (isEmpty(textField)){
            textField.setPromptText("Este campo es obligatorio");
            return true;

        } else if (textField.getText().length() > 65){
            textField.clear();
            textField.setStyle(BOX_SHADOW_RED);
            textField.setPromptText("max 65 caracteres");
            return true;
        }
        return false;
    }

    public static boolean isNotValidTfPhone(TextField textField){
        if (isNullOrBoxSadowRed(textField)){
            textField.setPromptText("Campo numérico sin decimales, 9 caracteres");
            return true;
        }
        if (isEmpty(textField)) {
            textField.setPromptText("Campo numérico sin decimales, 9 caracteres");
            return true;

        } else if (!StringUtils.isNumeric(textField.getCharacters())){
            textField.clear();
            textField.setStyle(BOX_SHADOW_RED);
            textField.setPromptText("Campo numérico sin decimales");
            return true;

        } else if (textField.getText().length() != 9){
            textField.clear();
            textField.setStyle(BOX_SHADOW_RED);
            textField.setPromptText("9 caracteres");
            return true;
        }
        return false;
    }

    public static boolean isNotValidCheckComboBox(CheckComboBox<?> checkComboBox){
        if (checkComboBox.getCheckModel().getCheckedItems().isEmpty()){
            checkComboBox.setStyle(BOX_SHADOW_RED);
            return true;
        }
        return checkComboBox.getStyle().equals(BOX_SHADOW_RED);
    }

    public static boolean isNotValidCbType(ComboBox<String> cbType){
        if (cbType.getSelectionModel().isEmpty()){
            cbType.setStyle(BOX_SHADOW_RED);
            return true;
        }
        return cbType.getStyle().equals(BOX_SHADOW_RED);
    }

    public static boolean isNotValidTfYeast(TextField textField){
        String number = StringUtils.replaceChars(textField.getText(), "," , ".");

        if (isNullOrBoxSadowRed(textField)){
            textField.setPromptText("campo numérico con decimales");
            return true;
        }
        if (isEmpty(textField)) {
            textField.setPromptText("campo numérico con decimales");
            return true;

        } else if (!isDecimal(textField.getText())){
            textField.clear();
            textField.setStyle(BOX_SHADOW_RED);
            textField.setPromptText("campo numérico con decimales");
            return true;

        } else if (Float.parseFloat(number) < 0 || Float.parseFloat(number) > 40){
            textField.clear();
            textField.setStyle(BOX_SHADOW_RED);
            textField.setPromptText("valor entre 0 y 40");
            return true;
        }
        return false;
    }

    public static boolean isNotValidTfProtein(TextField textField){
        String number = StringUtils.replaceChars(textField.getText(), ",", ".");

        if (isNullOrBoxSadowRed(textField)){
            textField.setPromptText("campo numérico con decimales");
            return true;
        }
        if (isEmpty(textField)) {
            textField.setPromptText("campo numérico con decimales");
            return true;

        } else if (!isDecimal(textField.getText())){
            textField.clear();
            textField.setStyle(BOX_SHADOW_RED);
            textField.setPromptText("campo numérico con decimales");
            return true;

        } else if (Float.parseFloat(number) < 0 || Float.parseFloat(number) > 40){
            textField.clear();
            textField.setStyle(BOX_SHADOW_RED);
            textField.setPromptText("valor entre 0 y 40");
            return true;
        }
        return false;
    }

    public static boolean isNotValidTfWatter(TextField textField){
        if (isNullOrBoxSadowRed(textField)){
            textField.setPromptText("campo numérico sin decimales");
            return true;
        }
        if (isEmpty(textField)) {
            textField.setPromptText("campo numérico sin decimales");
            return true;

        } else if (!StringUtils.isNumeric(textField.getCharacters())){
            textField.clear();
            textField.setStyle(BOX_SHADOW_RED);
            textField.setPromptText("campo numérico sin decimales");
            return true;

        } else if (Float.parseFloat(textField.getText()) < 0 || Float.parseFloat(textField.getText()) > 150){
            textField.clear();
            textField.setStyle(BOX_SHADOW_RED);
            textField.setPromptText("valor entre 0 y 150");
            return true;
        }
        return false;
    }

    public static boolean isNotValidTfStrength(TextField textField){
        if (isNullOrBoxSadowRed(textField)){
            textField.setPromptText("campo numérico sin decimales");
            return true;
        }
        if (isEmpty(textField)) {
            textField.setPromptText("campo numérico sin decimales");
            return true;

        } else if (!StringUtils.isNumeric(textField.getCharacters())){
            textField.clear();
            textField.setStyle(BOX_SHADOW_RED);
            textField.setPromptText("campo numérico sin decimales");
            return true;

        } else if (Float.parseFloat(textField.getText()) < 0 || Float.parseFloat(textField.getText()) > 700){
            textField.clear();
            textField.setStyle(BOX_SHADOW_RED);
            textField.setPromptText("valor entre 0 y 700");
            return true;
        }
        return false;
    }

    public static boolean isNotValidTfPercentajes(VBox vBox) {
        AtomicBoolean isNotValid = new AtomicBoolean(false);
        vBox.getChildren().stream()
                .map(vb -> (HBox) vb)
                .forEach( hBox -> {
                    TextField textField = (TextField) hBox.getChildren().get(0);
                    if (isNullOrBoxSadowRed(textField)) isNotValid.set(true);
                    if (textField.getText().isEmpty()){
                        textField.setStyle(BOX_SHADOW_RED);
                        isNotValid.set(true);
                    } else if (!StringUtils.isNumeric(textField.getCharacters())){
                        textField.setStyle(BOX_SHADOW_RED);
                        isNotValid.set(true);
                    } else if (Integer.parseInt(textField.getText()) < 0 || Integer.parseInt(textField.getText()) > 100){
                        textField.setStyle(BOX_SHADOW_RED);
                        isNotValid.set(true);
                    }
                });
        return isNotValid.get();
    }

    private static boolean isNullOrBoxSadowRed(TextField textField){
        if (textField == null) return true;
        if (textField.getStyle().equals(BOX_SHADOW_RED)) return true;
        return false;
    }

    public static boolean isDecimal(String number){
        try {
            //primero vemos ti tiene una coma y lo cambiamos por un punto para que nos nos de error y aacepte los dos
            String replace = StringUtils.replaceChars(number, ",", ".");
            Float.parseFloat(replace);
            Double.parseDouble(replace);
        } catch (NumberFormatException nfe){
            return false;
        }
        return true;
    }

    public static void addListenerTextFields(TextField... textFields){
        ObservableList<TextField> listTextFields = FXCollections.observableArrayList(textFields);
        listTextFields.forEach(textField -> {
            textField.setOnMousePressed(mouseEvent -> {
                textField.setStyle(BOX_SHADOW_NONE);
            });
            textField.setOnKeyPressed(keyEvent -> {
                textField.setStyle(BOX_SHADOW_NONE);
            });
        });

    }

}
