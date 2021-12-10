package com.garciajuanjo.calculatorpan.controller;

import com.garciajuanjo.calculatorpan.App;
import com.garciajuanjo.calculatorpan.domain.IngredientRecipe;
import com.garciajuanjo.calculatorpan.domain.Recipe;
import com.garciajuanjo.calculatorpan.util.ImageUtil;
import com.garciajuanjo.calculatorpan.util.ImputUtil;
import com.garciajuanjo.calculatorpan.util.MockData;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Precision;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static com.garciajuanjo.calculatorpan.util.Constant.*;
import static com.garciajuanjo.calculatorpan.util.MessageUtil.*;

public class AppController implements Initializable {

    @FXML
    private Pane pane;
    @FXML
    private ListView<HBox> lvIngredients;
    @FXML
    private ListView<Recipe> lvRecipes;
    @FXML
    private Button btApply;
    @FXML
    private Label lbStatus, lbSelected;
    @FXML
    private TextField tfQuantity;
    @FXML
    private TextArea taObservations;
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab tabCalculator;
    @FXML
    private ImageView imgLogo;

    private Tab ingredient, flour, prefermento, provider, recipe;
    private ObservableList<Tab> tabs;
    private ObjectProperty<Recipe> recipeSelected;

    private int numberRow;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tabCalculator.setUserData(nameTabApp);

        //Para que no salga la barra de scroll de la derecha en el text area
        taObservations.setWrapText(true);
        taObservations.setEditable(false);

        // si no existe alguno de los cuatro ingredientes basicos los crea.
        mockIngredients();

        //Cargamos las recetas en el listView
        rechargeListViewRecipes();

        //añadimos los listener a los textFields de la cantidad
        addListenerTextFieldQuantity();

        //inicializamos numberRow para recoger cual es el último textField que ha pinchado
        numberRow = -1;

        //añadimos la imagen al botón de aplicar
        ImageUtil.addImageButton(btApply, imgBtApply);

        //añadimos el logo
        ImageUtil.addImageLogo(imgLogo);

        //incializamos las tabs
        initializeTabs();

        //inicializamos el ObjectProperty y le añadimos la escucha para cuando pinchen en el lv de recetas
        recipeSelected = new SimpleObjectProperty<>();
        recipeSelected.bind(lvRecipes.getSelectionModel().selectedItemProperty());

        //listener para cuando cambiamos de tab que se recarguen los datos por si hay cambios
        reloadAllData();

        //ponemos el nombre de la aplicación y la versión del programa
        lbStatus.setText(nameAplicationAndVersion);
    }

    /**
     * Acción del botón para cuando introducimos un total de masa o de algún ingrediente nos lo desglose en el
     * resto de ingredientes
     * @param event
     */
    @FXML
    public void btApplyAction(Event event) {
        if (recipeSelected.get() == null) {
            return;
        }
        float sumPercentajes = sumPercentagesRecipe(recipeSelected.get());
        float totalMass;

        // si entra en este es porque ha pinchado en el textField de quantity
        if (numberRow == -2) {

            if (tfQuantity.getText().isEmpty()){
                MyAlert("Tiene que ingresar una cantidad total de masa o de algún ingrediente", TypeAlert.WARNING);

            }else if (!StringUtils.isNumeric(tfQuantity.getText().trim())){
                MyAlert("El campo toal de masa tiene que ser un número sin decimales", TypeAlert.WARNING);
                tfQuantity.clear();
                tfQuantity.requestFocus();

            } else {
                totalMass = Float.parseFloat(tfQuantity.getText().trim());

                // ponemos las cantidades de cada ingrediente en funcion de la cantidad de masa elegida
                addQuantityTextFields(recipeSelected.get().getIngredients(), sumPercentajes, totalMass);
            }

            //si ho ha pinchado en ningún textField ni en el de quantity numberRow será -1
        } else if (numberRow != -1) {
            HBox hBox = lvIngredients.getItems().get(numberRow);
            TextField textField = (TextField) hBox.getChildren().get(0);
            Label label = (Label) hBox.getChildren().get(1);
            //cojo el nombre del ingrediente que va despúes del espacio en blanco
            String nameIngredient = StringUtils.substringAfter(label.getText(), " ");

            //si no está vacio el textField en el que ha pinchado
            if (!textField.getText().isEmpty() && ImputUtil.isDecimal(textField.getText())) {
                AtomicLong percentage = new AtomicLong(0);
                recipeSelected.get().getIngredients().forEach(ingredientRecipe1 -> {
                    if (ingredientRecipe1.getIngredient().getName().equals(nameIngredient)) {
                        percentage.set((long) ingredientRecipe1.getPercentage());
                    }
                });

                /*Si cuando pone la cantidad del ingrediente pone una coma en vez de un punto para que no falle cambiamos
                la coma por un punto y así lo puede poner de las dos formas
                */
                String text = StringUtils.replaceChars(textField.getText(), ",", ".");
                totalMass = Float.parseFloat(text);
                addQuantityTextFields(recipeSelected.get().getIngredients(), percentage.floatValue(), totalMass);


                tfQuantity.setText(String.valueOf(Math.round((int) sumAllValues())));

            } else if(textField.getText().isEmpty()) {
                MyAlert("Tiene que ingresar una cantidad total de masa o de algún ingrediente", TypeAlert.WARNING);

            } else if (!ImputUtil.isDecimal(textField.getText())) {
                MyAlert("El campo introducido tiene que ser un número", TypeAlert.WARNING);
            }

        } else {
            //si no ha pinchado en nada le mostramos un mensaje
            MyAlert("Tiene que ingresar una cantidad total de masa o de algún ingrediente", TypeAlert.WARNING);
        }
    }

    /**
     * Acción de ratón cuando hacemos click sobre el ListView de las recetas. Cada vez que pinchamos sobre una receta
     * recargamos los datos de la misma en el ListView de ingredientes
     * @param mouseEvent
     */
    @FXML
    public void lvRecipesMouseClicked(MouseEvent mouseEvent){
        rechargeData();
    }

    /**
     * Acción sobre el ListView de las recetas cuando pulsamos las teclas de flecha arriba y flecha abajo. Lo que
     * hace es detectar la receta en la que se encuentra y recargar los datos en el ListView de los ingredientes
     * que correspondan con la receta
     * @param event
     */
    @FXML
    public void lvRecipesKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.UP){
            rechargeData();
        }

        if (event.getCode() == KeyCode.DOWN) {
            rechargeData();
        }
    }

    /**
     * Acción sobre el MenúItem de nuevo proveedor. Cuando pinchamos sobre él comprobamos si hay una tab en el TabPane
     * para el nuevo proveedor y si no está la añade, si esta no hace nada.
     * @param event
     */
    @FXML
    public void miProvidersNewListener(Event event) {
        try {
            if (isNotContainTab(nameTabProvider)) {
                loadTab(provider, viewProvider);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            MyAlertErrorLoad();
        }
    }

    /**
     * Acción sobre el MenúItem de nueva harna. Cuando pinchamos sobre él comprobamos si hay una tab en el TabPane
     * para la nueva hairina y si no está la añade, si esta no hace nada.
     * @param event
     */
    @FXML
    public void miNewIngredientFlourListener(Event event) {
        try {
            if (isNotContainTab(nameTabFlour)) {
                loadTab(flour, viewFlour);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            MyAlertErrorLoad();
        }
    }

    /**
     * Acción sobre el MenúItem de nuevo prefermento. Cuando pinchamos sobre él comprobamos si hay una tab en el TabPane
     * para el nuevo prefermento y si no está la añade, si esta no hace nada.
     * @param event
     */
    @FXML
    public void miNewIngredientPrefermentoListener(Event event) {
        try {
            if (isNotContainTab(nameTabPrefermento)) {
                loadTab(prefermento, viewPrefermento);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            MyAlertErrorLoad();
        }
    }

    /**
     * Acción sobre el MenúItem de nuevo ingrediente. Cuando pinchamos sobre él comprobamos si hay una tab en el TabPane
     * para el nuevo ingrediente y si no está la añade, si esta no hace nada.
     * @param event
     */
    @FXML
    public void miNewIngredientOtherListener(Event event) {
        try {
            if (isNotContainTab(nameTabIngredient)) {
                loadTab(ingredient, viewIngredient);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            MyAlertErrorLoad();
        }
    }

    /**
     * Acción sobre el MenúItem de nueva receta. Cuando pinchamos sobre él comprobamos si hay una tab en el TabPane
     * para la nueva receta y si no está la añade, si esta no hace nada.
     * @param event
     */
    @FXML
    public void miRecipesListener(Event event) {
        try {
            if (isNotContainTab(nameTabRecipe)) {
                loadTab(recipe, viewRecipe);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            MyAlertErrorLoad();
        }

    }

    /**
     * Comprueba si está la Tab con el nombre que pasamos como parámetro.
     * @param name nombre de la Tab
     * @return si está o no la Tab
     */
    private boolean isNotContainTab(String name){
        return tabPane.getTabs().stream().noneMatch(tab -> tab.getUserData().equals(name));
    }

    /**
     * Añade el ingrediente, harina, prefermento, proveedor y receta genéricos de la app.
     */
    private void mockIngredients(){
        MockData mockData = new MockData();
        try {
            if (!App.getIngredientDao().existIngredient(mockData.getIngredientGeneric().getName(), true)){
                App.getIngredientDao().addIngredient(mockData.getIngredientGeneric());
            }

            if (!App.getFlourDao().existFlour(mockData.getFlourGeneric().getName(), true)){
                App.getFlourDao().addFlour(mockData.getFlourGeneric());
            }

            if (!App.getPrefermentoDao().existPrefermento(mockData.getPrefermentoGeneric().getName(), true)){
                App.getPrefermentoDao().addPrefermento(mockData.getPrefermentoGeneric());
            }

            if (!App.getProviderDao().existProvider(mockData.getProviderGeneric().getName(), true)){
                App.getProviderDao().addProvider(mockData.getProviderGeneric());
            }

            if (!App.getIngredientDao().existIngredient(mockData.getIngredientAgua().getName(), true)){
                App.getIngredientDao().addIngredient(mockData.getIngredientAgua());
            }

            if (!App.getIngredientDao().existIngredient(mockData.getIngredientSal().getName(), true)){
                App.getIngredientDao().addIngredient(mockData.getIngredientSal());
            }

            if (!App.getIngredientDao().existIngredient(mockData.getIngredientYeast().getName(), true)){
                App.getIngredientDao().addIngredient(mockData.getIngredientYeast());
            }

            if (!App.getPrefermentoDao().existPrefermento(mockData.getPrefermentoMasaMadre().getName(), true)){
                App.getPrefermentoDao().addPrefermento(mockData.getPrefermentoMasaMadre());
            }

            if (!App.getRecipeDao().existRecipe(mockData.getRecipeGeneric().getName(), false)){
                App.getRecipeDao().addRecipe(mockData.getRecipeGeneric());
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            MyAlertDataBase();
        }
    }

    /**
     * Inicializa las Tabs dándole un nombre. Además, propiedades com id, usserDada etc.
     */
    private void initializeTabs() {
        ingredient = new Tab(nameTabIngredient);
        flour = new Tab(nameTabFlour);
        prefermento = new Tab(nameTabPrefermento);
        provider = new Tab(nameTabProvider);
        recipe = new Tab(nameTabRecipe);

        //Meto todas las tab en el ObservableList para recorrerlas con un foreach y no repetir código
        this.tabs = FXCollections.observableArrayList(ingredient, flour, prefermento, provider, recipe, tabCalculator);
        tabs.forEach(tab -> {
            tab.setClosable(true);
            tab.setId(tab.getText());
            tab.setUserData(tab.getText());
        });

        //las hacemos todas que se puedan cerrar menos la principal que siempre estará abierta
        tabCalculator.setClosable(false);
    }

    /**
     * Método para cargar la vista en las Tab.
     * @param tab Tab en la que queremos cargar la vista
     * @param nameView Nombre de la vista que queremos cargar
     * @throws IOException Excepción si no encuentra la vista
     */
    private void loadTab(Tab tab, String nameView) throws IOException {
        //para cargar la vista que queramos en la pestaña
        tab.setContent(FXMLLoader.load(App.class.getResource(nameView)));
        tabPane.getTabs().add(tab);

        //Cuando abramos una pestaña que se seleccione
        tabPane.getSelectionModel().select(tab);
    }

    /**
     * Método que recarga el ListView de recetas con las recetas de la base de datos
     */
    private void rechargeListViewRecipes(){
        try {
            lvRecipes.setItems(App.getRecipeDao().getAllRecipes());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            MyAlertDataBase();
        }
    }

    /**
     * Suma todos los porcentajes de los ingredientes de la receta. Lo necesito para hacer los cálculos y saber cuantos
     * gramos de cada ingrediente hacen falta en función del peso de masa total que necesitemos
     * @param recipe receta para obtener su lista de ingredientes
     * @return suma de los porcentages de todos sus ingredientes
     */
    private float sumPercentagesRecipe(Recipe recipe){
        AtomicLong atomicLong = new AtomicLong(0);
        recipe.getIngredients().stream()
                .map(IngredientRecipe::getPercentage)
                .forEach(number -> atomicLong.addAndGet(number.longValue()));

        return atomicLong.get();
    }

    /**
     * Recargamos los datos en los ListView y el TextArea en función de la receta que seleccione del ListView de
     * recetas.
     */
    private void rechargeData(){
        //cada vez que pinche en una receta reseteamos numberRow
        numberRow = -1;

        //limpiamos si hay algo en el textFields de la cantidad de masa
        tfQuantity.clear();

        Recipe recipe = lvRecipes.getSelectionModel().getSelectedItem();

        lvIngredients.getItems().clear();

        lbSelected.setText(recipe.getName());
        taObservations.setText(recipe.getObservations());
        lvIngredients.getItems().addAll(addTextFieldsIngredients(recipe.getIngredients()));
    }

    /**
     * Por cada ingrediente de la receta le añadimos un TextField para poner la cantida y un Label con el nombre del
     * mismo y el porcentaje.
     * @param ingredients Lista de ingredientes de la receta
     * @return ObservbleList con HBox que contienen el TextField y el Label de cada uno
     */
    private ObservableList<HBox> addTextFieldsIngredients(List<IngredientRecipe> ingredients){
        ObservableList<HBox> list = FXCollections.observableArrayList();
        AtomicInteger count = new AtomicInteger(0);
        ingredients.forEach(ingredientRecipe -> {
            TextField textField = new TextField();
            textField.setPrefWidth(60);
            textField.setUserData(count.getAndIncrement());
            addListenerTextFieldsQuantity(textField);
            Label label = new Label("(" + ingredientRecipe.getPercentage() + "%) " + ingredientRecipe.getIngredient().toString());
            HBox hBox = new HBox();
            hBox.setSpacing(20);
            hBox.getChildren().addAll(textField, label);
            list.add(hBox);
        });
        return list;
    }

    /**
     * Método que hace los cálculos de la cantidad en gramos de cada ingrediente en función de la cantidad de masa total
     * o la cantidad de alguno de los ingredientes.
     * @param ingredients lista de ingredientes de la receta
     * @param sumPercentages suma de los porcentages de la receta
     * @param totalMass total de masa que necesitamos de la receta
     */
    private void addQuantityTextFields(List<IngredientRecipe> ingredients, float sumPercentages, float totalMass){
        //limpiamos lo que haya en el listView de ingredientes
        lvIngredients.getItems().clear();

        //esto es un contador para que cada vez que pinche en un textFields le añadimos cual es con usserData y luego poder recuperarlo
        AtomicInteger count = new AtomicInteger(0);

        ingredients.forEach(ingredientRecipe -> {
            TextField textField = new TextField();
            textField.setPrefWidth(60);
            textField.setUserData(count.getAndIncrement());
            addListenerTextFieldsQuantity(textField);

            String quantity;

            //si el porcentage del ingrediente es menor o igual a 5 no redondeamos y lo mostramos con un decimal
            if(ingredientRecipe.getPercentage() <= 5){
                float number = (totalMass * ingredientRecipe.getPercentage()) / sumPercentages;
                quantity = String.valueOf(Precision.round(number, 1));

            } else {
                //Redondeamos el resultado para mostrarlo
                quantity = String.valueOf(Math.round((totalMass * ingredientRecipe.getPercentage()) / sumPercentages));
            }
            textField.setText(quantity);

            Label label = new Label("(" + ingredientRecipe.getPercentage() + "%) " + ingredientRecipe.getIngredient().toString());

            //Vamos creando los hbox con su textField (con la cantidad en gramos) y su label del ingrediente
            HBox hBox = new HBox();
            hBox.setSpacing(20);
            hBox.getChildren().addAll(textField, label);
            lvIngredients.getItems().add(hBox);
        });
    }

    /**
     * Listener de los TextFields de la cantidad en gramos de cada ingrediente. Cuando pinchamos sobre ellos le
     * asignamos a numberRow lo que contenga userData para saber cuál es el último que ha seleccionado
     * @param textField textField de la cantidad en gramos del ingrediente
     */
    private void addListenerTextFieldsQuantity(TextField textField){
        textField.setOnMouseClicked(mouseEvent -> {
            numberRow = Integer.parseInt(textField.getUserData().toString());
        });
    }

    /**
     * Listener del TextField de quantity (el del total de masa). Si pincha sobre el mismo ponemos numberRow en -2
     * para diferenciarlo de los demás y saber cual ha seleccionado para hacer los cálculos
     */
    private void addListenerTextFieldQuantity(){
        tfQuantity.setOnMouseClicked(mouseEvent -> numberRow = -2);
    }

    /**
     * Suma todos los valores de los TextFields de la cantidad de cada ingrediente cuando ya se han echo los cálculos
     * para mostrar el total de masa en el tfQuantity
     * @return suma de la cantidad de gramos de todos los ingredientes
     */
    private float sumAllValues(){
        //suma todos los valores de los textFielsd de quantity (del listView)  para mostrarlo en el textField de quantity
        AtomicReference<Float> total = new AtomicReference<>((float) 0);
        lvIngredients.getItems().stream()
                .map(hBox -> (TextField)hBox.getChildren().get(0))
                .map(textField -> Double.parseDouble(textField.getText()))
                .forEach(aFloat -> total.updateAndGet(v -> (float) (v + aFloat)));

         return total.get();
    }

    /**
     * Cada vez que la Tab gana el foco actualizamos los datos del ListView de recetas por si el usuario hace algún
     * cambio en alguna que se actualize al momento.
     */
    private void reloadAllData(){
        tabs.stream()
                .filter(tab -> tab.getId().equals(nameTabApp))
                .forEach(tab -> {
                    tab.setOnSelectionChanged(event -> {
                        //si hay alguna receta seleccionada
                        if (!lvIngredients.getItems().isEmpty()) {
                            rechargeListViewRecipes();
                            lvIngredients.setItems(addTextFieldsIngredients(recipeSelected.get().getIngredients()));

                            //si no hemos seleccionado nada tovavia solo recargamos el lv de recetas
                        } else {
                            rechargeListViewRecipes();
                        }
                    });
                });
    }

    public ObservableList<Tab> getTabs() {
        return this.tabs;
    }

}
