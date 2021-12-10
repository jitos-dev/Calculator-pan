package com.garciajuanjo.calculatorpan.util;

public class Css {

    //Colores de texto
    public static final String COLOUR_TEXT_RED = "-fx-text-inner-color: rgba(255, 0, 0, 0.7);";
    public static final String COLOUR_TEXT_BLACK = "-fx-text-inner-color: black;";

    //Box shadow
    public final static String BOX_SHADOW_RED = "-fx-effect: dropshadow(three-pass-box, rgba(255, 0, 0, 0.5), 15, 0, 0, 0);";
    public final static String BOX_SHADOW_NONE = "-fx-box-shadow:none;";

    //Back Ground Color
    public final static String BACK_GROUND_COLOR_RGBA(String rgba){
        return "-fx-background-color: rgba(" + rgba + ");";
    }

    //Font weight bold
    public final static String FONT_WEIGHT_BOLD = "-fx-font-weight: bold;";

}
