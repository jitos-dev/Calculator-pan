package com.garciajuanjo.calculatorpan.util;

import com.garciajuanjo.calculatorpan.App;

public class Constant {

    public final static String nameAplicationAndVersion = App.getProperty("nombre_aplicacion")
            .concat(" ").concat(App.getProperty("mi_version"));

    /*---------------RESOURCES--------------------------------*/
    public final static String viewApp = "views/App.fxml";
    public final static String viewIngredient = "views/NewIngredient.fxml";
    public final static String viewFlour = "views/NewFlour.fxml";
    public final static String viewPrefermento = "views/NewPrefermento.fxml";
    public final static String viewProvider = "views/NewProvider.fxml";
    public final static String viewRecipe = "views/NewRecipe.fxml";
    public final static String viewAlert = "views/Alert.fxml";

    public final static String file_css = "css/style.css";
    public final static String file_properties = "Aplication.properties";

    public final static String imgFavicon = "images/favicon.png";
    public final static String imgInformation = "images/informacion.png";
    public final static String imgLogo = "images/logo.png";
    public final static String imgError = "images/error.png";
    public final static String imgCorrect = "images/correcto.png";
    public final static String imgAlert = "images/alerta.png";
    public final static String imgBtNew = "images/nuevo.png";
    public final static String imgBtEdit = "images/editar.png";
    public final static String imgBtDelete = "images/borrar.png";
    public final static String imgBtSave = "images/guardar.png";
    public final static String imgBtApply = "images/aplicar.png";
    public final static String imgBtRefresh = "images/refrescar.png";

    /*-----------------NOMBRES DE LAS TAB-------------------------*/
    public final static String nameTabApp = App.getProperty("nombre_aplicacion");
    public final static String nameTabIngredient = "Nuevo Ingrediente";
    public final static String nameTabFlour = "Nueva Harina";
    public final static String nameTabPrefermento = "Nuevo Prefermento";
    public final static String nameTabProvider = "Nuevo Proveedor";
    public final static String nameTabRecipe = "Nueva Receta";

    /*-----------------PROMP TEXT-------------------------*/
    public final static String writeNameHere = "Escriba aquí el nombre";
    public final static String nonMandatoryField = "Este campo no es obligatorio";
    public final static String zero = "0";
    public final static String zeroDecimal = "0.00";
    public final static String obligatoryField = "% Campo obligatorio";

    /*-------------------ERRORES----------------------------*/
    public final static String errorLoad = "Error al cargar la vista, compruebe que no se han borrado archivos";
    public final static String errorConectDb = "Error al conectar con la base de datos";

}
