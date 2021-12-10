package com.garciajuanjo.calculatorpan.controller;

import com.garciajuanjo.calculatorpan.App;
import com.garciajuanjo.calculatorpan.domain.*;
import com.garciajuanjo.calculatorpan.util.ImageUtil;
import com.garciajuanjo.calculatorpan.util.ImputUtil;
import com.garciajuanjo.calculatorpan.util.MessageUtil;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.collections4.comparators.ComparableComparator;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.SearchableComboBox;

import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.garciajuanjo.calculatorpan.domain.Ingredient.TypeIngredient;
import static com.garciajuanjo.calculatorpan.util.Constant.*;
import static com.garciajuanjo.calculatorpan.util.Css.*;
import static com.garciajuanjo.calculatorpan.util.ImageUtil.addImageButton;
import static com.garciajuanjo.calculatorpan.util.ImputUtil.isDecimal;
import static com.garciajuanjo.calculatorpan.util.ImputUtil.isNotValidTfName;
import static com.garciajuanjo.calculatorpan.util.MessageUtil.*;

public class NewRecipeController implements Initializable {

    @FXML
    private ListView<HBox> lvSelected;
    @FXML
    private Button btSave, btCancel, btNewRecipe, btRemove, btEdit;
    @FXML
    private TextArea taObservation;
    @FXML
    private Label lbStatus, lbNameRecipe, lbFlours;
    @FXML
    private HBox hbRecipes, hbNameFlour;
    @FXML
    private CheckComboBox<Prefermento> chPrefermentos;
    @FXML
    private CheckComboBox<Flour> chFlours;
    @FXML
    private CheckComboBox<Ingredient> chIngredients;
    @FXML
    private ImageView imgLogo;

    private SearchableComboBox<Recipe> recipes;
    private TextField tfNameRecipe;
    private ObservableList<HBox> hbSelected;
    private ImageView informacion;

    private boolean clickEdit;

    private ObjectProperty<Recipe> originalRecipe;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //inicializamos el searcheableCombo de las recetas, lo rellenamos con la bd y lo mostramos
        initComboRecipes();
        rechargeComboRecipes();

        //cuando la ventana coge el foco recargamos los datos por si hay algún cambio
        reloadAllData();

        //inicializamos el TextField para el nombre de las recetas, le damos su medida y su listener
        initTfNameRecipes();
        addListenerTexField(tfNameRecipe);

        hbSelected = FXCollections.observableArrayList();

        //ponemos el icono de la interrogación en el hbox
        addIconInformation();

        // Inicializamo con sus ajustes el PopOver
        initPoopOver();

        //estado inicial de los botones y CheckComboBox
        initStateButtons(true);

        //añadimos el listener del SearcheableComboBox de las recetas
        addListenerComboRecipes();

        // para quitar el scroll de la derecha del textArea
        taObservation.setWrapText(true);

        //añadimos los iconos a los botones
        addImageButtons();

        //añadimos la imagen del logo
        ImageUtil.addImageLogo(imgLogo);

        //inicializamos el ObjectProperty y le añadimos la escucha para cuando pinchen en el lv de recetas
        originalRecipe = new SimpleObjectProperty<>();
        originalRecipe.bind(recipes.getSelectionModel().selectedItemProperty());

        //listener de lo combo de ingredientes cuando pinchamos sobre ellos
        addSelectedListView();

        clickEdit = false;

        lbStatus.setText(nameAplicationAndVersion);
    }

    /**
     * Acción del botón de refrescar. Resetea los valores como cuando abrimos la Tab por primera vez
     * @param event
     */
    @FXML
    public void btCancelAction(Event event) {
        resetValues();
        initStateButtons(true);
        taObservation.setEditable(false);
        lvSelected.setStyle(BOX_SHADOW_NONE);
        tfNameRecipe.setStyle(COLOUR_TEXT_BLACK);
    }

    /**
     * Actión del botón de para crear una nueva receta.
     * @param event
     */
    @FXML
    public void btNewRecipeAction(Event event){
        resetValues();
        tfNameRecipe.clear();
        tfNameRecipe.setEditable(true);
        hbRecipes.getChildren().setAll(tfNameRecipe);
        initStateButtons(false);
        btEdit.setDisable(true);
        btRemove.setDisable(true);
        lbNameRecipe.setText("Nombre");
        taObservation.setEditable(true);
        clickEdit = false;

        try {
            chIngredients.getItems().setAll(App.getIngredientDao().getAllIngredients(true));
            chFlours.getItems().setAll(App.getFlourDao().getAllFlours(true));
            chPrefermentos.getItems().setAll(App.getPrefermentoDao().getAllPrefermentos(true));

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            MyAlertDataBase();
        }
    }

    /**
     * Action del botón de editar una receta.
     * @param event
     */
    @FXML
    public void btEditAction(Event event){
        clickEdit = true;
        btSave.setDisable(false);
        btRemove.setDisable(true);
        btEdit.setDisable(true);
        btNewRecipe.setDisable(true);
        btCancel.setDisable(false);
        chPrefermentos.setDisable(false);
        chFlours.setDisable(false);
        chIngredients.setDisable(false);
        taObservation.setEditable(true);
        tfNameRecipe.setText(originalRecipe.get().getName());
        tfNameRecipe.setEditable(true);
        removeListenerTextField(tfNameRecipe);
        hbRecipes.getChildren().setAll(tfNameRecipe);

        //activamos todos los textFields de los porcentages por si quiere modificarlos
        lvSelected.getItems().stream()
                .map(hBox -> (TextField) hBox.getChildren().get(0))
                .forEach(textField -> textField.setEditable(true));

    }

    /**
     * Actión del botón de borrar una receta. Comprobamos que no es la Masa base(receta genérica), sacamos un mensaje de
     * confirmación, si acepta la borramos y si no nada.
     * @param event
     */
    @FXML
    public void btRemoveAction(Event event){
        Recipe recipe = recipes.getSelectionModel().getSelectedItem();
        boolean confirmation = false;

        if (recipe.getName().equals("Masa base")){
            MyAlert("La Masa base no se puede borrar", TypeAlert.WARNING);

        } else {
            confirmation = MyAlertConfirmation("¿Seguro que quiere eliminar esta receta?\n" + recipe);
        }

        if (confirmation){
            try {
                boolean delete = App.getRecipeDao().deleteRecipe(recipe, false);

                if (!delete) {
                    MyAlert("No se pudo borrar la receta, revise su conexión a la base de datos",
                            TypeAlert.WARNING);
                    return;
                }

                MyAlert("¡¡ Receta borrada con éxito !!", TypeAlert.INFORMATION);
                resetValues();
                initStateButtons(true);
                rechargeComboRecipes();

            } catch (SQLException throwables) {
                throwables.printStackTrace();
                MyAlertDataBase();
            }
        }
    }

    /**
     * Actión del botón de guardar. Comprobamos que todos los campos esten correctos acorde a los valores que pueden
     * aceptar y si es así guardamos la receta
     * @param event
     */
    @FXML
    public void btSaveAction(Event event){
        // si alguna caja de texto no tiene valor o no es un decimal la ponemos en rojo
        changeColourTextField();

        boolean allFieldsCorrect = isAllFieldsCorrect();

        if (!allFieldsCorrect){
            MyAlert("Tiene campos obligatorios", TypeAlert.WARNING);

        } else if (taObservation.getText().length() > 500){
            MyAlert("El campo observaciones no puede superar los 500 caracteres\n" +
                    "Actualmente en uso: " + taObservation.getText().length() + " caracteres.", TypeAlert.WARNING);

        } else {
            saveIngredient();
        }
    }

    /**
     * Comprueba cuando pinchamos en el botón de guardar si está editando o creando una nueva receta. En
     * función de la opción comprueba por ejemplo si ya existe la receta, etc.
     */
    private void saveIngredient(){
        try {
            if (chFlours.getCheckModel().getCheckedItems().isEmpty()){
                MyAlert("No tiene ninguna harina seleccionada", TypeAlert.WARNING);
                return;

            } else if (!sumFloursEquals100()) {
                MyAlert("El porcentaje de la harina o la suma de todos tiene que ser 100", TypeAlert.WARNING);
                return;
            }

            if (clickEdit){
                //si no ha cambiado el nombre y está correcto la modificamos
                if(originalRecipe.get().getName().equals(getRecipe().getName())){
                    updateRecipe(getRecipeUpdate());

                } else {
                    // si ya existe ya una receta activa con ese nombre (no se puede modificar)
                    if(App.getRecipeDao().existRecipe(getRecipe().getName(), true)){
                        MyAlert("Ya existe una receta con ese nombre, no se pudo guardar los cambios", TypeAlert.WARNING);
                        tfNameRecipe.clear();
                        tfNameRecipe.requestFocus();
                        return;

                    } else if(App.getRecipeDao().existRecipe(getRecipe().getName(), false)){
                        // si entra en este es porque existe pero está inactiva
                        //cojemos la que está inactiva y la eliminamos para que no haya dos con el mismo nombre
                        Recipe recipe = App.getRecipeDao().getRecipe(getRecipe().getName());
                        App.getRecipeDao().deleteRecipe(recipe,true);
                        //borramos los ingredientes de la tabla ingredientsRecipe
                        recipe.setName(originalRecipe.get().getName());
                        App.getRecipeDao().deleteIngredientRecipe(recipe);

                        //modificamos la otra con el nombre nuevo
                        updateRecipe(getRecipeUpdate());

                    } else {
                        // si ha pinchado en editar y está correcto la modificamos
                        updateRecipe(getRecipeUpdate());
                    }
                }

            } else {
                if (App.getRecipeDao().existRecipe(getRecipe().getName(), true)) {
                    MessageUtil.MyAlert("Ya existe una receta con ese nombre", TypeAlert.WARNING);
                    tfNameRecipe.clear();
                    tfNameRecipe.requestFocus();
                    return;

                }else if(App.getRecipeDao().existRecipe(getRecipe().getName(), false)){
                    //si existe la receta pero no está activa la modificamos
                    Recipe recipe = App.getRecipeDao().getRecipe(getRecipe().getName());
                    recipe.setIsActive(1);
                    recipe.setIngredients(getRecipe().getIngredients());
                    App.getRecipeDao().updateRecipe(recipe);

                } else {
                    //si no existe la añadimos
                    App.getRecipeDao().addRecipe(getRecipe());
                }
            }

            MyAlert("¡¡ Receta guardada con éxito !!", TypeAlert.INFORMATION);
            resetValues();
            initStateButtons(true);
            rechargeComboRecipes();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            MyAlertDataBase();
        }
    }

    /**
     * Modifica la receta en función de los datos que recoge de los combo box de ingredientes cuando pinchamos en
     * el botón de guardar si antes hemos pinchado en editar
     * @throws SQLException
     */
    private void updateRecipe(Recipe recipe) throws SQLException {
        // si ha pinchado en editar y está correcto la modificamos
        boolean update = App.getRecipeDao().updateRecipe(recipe);

        if (!update) {
            MyAlert("No se pudo modificar la receta, revise su conexión con la base de datos",
                    TypeAlert.WARNING);
            return;
        }
    }

    /**
     * Cuando guardamos una nueva receta comprueba que la suma de los porcentajes de las harinas de la receta sea 100.
     * @return true si es 100 o false si no es 100
     */
    private boolean sumFloursEquals100(){
        AtomicInteger count = new AtomicInteger();
        chFlours.getCheckModel().getCheckedItems().forEach(flour -> {
            lvSelected.getItems().forEach(hBox -> {
                TextField tf = (TextField) hBox.getChildren().get(0);
                Label label = (Label) hBox.getChildren().get(1);
                if (label.getText().equals(flour.getName())){
                    count.addAndGet(Integer.parseInt(tf.getText()));
                }
            });

        });
        return count.get() == 100;
    }

    /**
     * Recoge los datos de los TextFields y los ComboBox. Lo utilizo cuando pinchamos en el botón
     * de guardar después de comprobar que los datos de cada campo son correctos.
     * @return new Recipe
     */
    private Recipe getRecipe(){
        String name = tfNameRecipe.getText().trim();
        name = ImputUtil.cleanText(name);

        //lista para los ingredientes de la receta
        ObservableList<IngredientRecipe> ingredients = FXCollections.observableArrayList();

        //Cogemos todos los ingredientes seleccionados y los guardamos en la lista con su porcentaje para la receta
        ingredients.addAll(addPercentageIngredient());

        //cogemos los demás datos para crear la receta
        String observations = taObservation.getText().trim();
        observations = ImputUtil.cleanText(observations);

        if (observations.isEmpty()) {
            observations = "sin observaciones";
        }

        return new Recipe(name, observations, ingredients);
    }

    /**
     * Método que utilizo cuando está editando una receta. Recojo los datos con el método getRecipe pero el id de la
     * receta tiene que ser el mismo de la receta que estamos editando. Ese id lo cogemos del ObjecProperty.
     * @return new Recipe editada pero con el mismo id
     */
    private Recipe getRecipeUpdate(){
        Recipe recipe = getRecipe();
        recipe.setIdRecipe(originalRecipe.get().getIdRecipe());

        return recipe;
    }

    /**
     * Comprueba que todos los campos estén correctos a la hora de guardar cambios al editar o guardar una nueva receta.
     * @return true o false
     */
    private boolean isAllFieldsCorrect(){
        boolean isAllFieldsCorrect = true;

        if (isNotValidTfName(tfNameRecipe)) {
            isAllFieldsCorrect = false;
        }

        if (lvSelected.getItems().isEmpty()){
            isAllFieldsCorrect = false;
            lvSelected.setStyle(BOX_SHADOW_RED);
        }

        if (!isAllPercentagesCorrect()){
            isAllFieldsCorrect = false;
        }
        if (!valueTextFieldsCorrects()){
            isAllFieldsCorrect = false;
        }
        return isAllFieldsCorrect;
    }

    /**
     * Recoge los datos que tenemos en el listView de ingredientes seleccionado y los pasa a un ObservableList de
     * IngredientRecipe cada uno con su porcentaje
     * @return lista de IngredientRecipe
     */
    private ObservableList<IngredientRecipe> addPercentageIngredient(){
        ObservableList<IngredientRecipe> listIngredientRecipe = FXCollections.observableArrayList();
        ObservableList<Ingredient> ingredients = allIngredientsChecked();

        lvSelected.getItems().forEach(hBox -> {
            TextField textField = (TextField) hBox.getChildren().get(0);
            Label label = (Label) hBox.getChildren().get(1);
            IngredientRecipe ingredientRecipe = new IngredientRecipe();

            //filtramos lo que hay en ingredients a ver cual coincide para asignarle su ingrediente y porcentaje
            Optional<Ingredient> ingredient = ingredients.stream()
                    .filter(ing -> ing.getName().equals(label.getText()))
                    .findFirst();

            // para evitar los nulos
            if (ingredient.isPresent()) {
                ingredientRecipe.setIngredient(ingredient.get());
                ingredientRecipe.setPercentage(Float.parseFloat(textField.getText()));
            }

            listIngredientRecipe.add(ingredientRecipe);
        });

        return listIngredientRecipe;
    }

    /**
     * Recarga los datos del ComboBox de ingredientes en función de los ingredientes que tengamos guardados en la BBDD
     */
    private void rechargeCheckComboBoxIngredient(){
        try {
            chIngredients.getItems().setAll(App.getIngredientDao().getAllIngredients(false));

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            MyAlertDataBase();
        }
    }

    /**
     * Recarga los datos del ComboBox de harinas en función de las harinas que tengamos guardadas en la BBDD
     */
    private void rechargeCheckComboBoxFlours(){
        try {
            chFlours.getItems().setAll(App.getFlourDao().getAllFlours(false));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            MyAlertDataBase();
        }
    }

    /**
     * Recarga los datos del ComboBox de prefermentos en función de los prefermentos que tengamos guardados en la BBDD
     */
    private void rechargeCheckComboBoxPrefermento(){
        try {
            chPrefermentos.getItems().setAll(App.getPrefermentoDao().getAllPrefermentos(true));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            MyAlertDataBase();
        }
    }

    /**
     * Recarga los datos del ComboBox de recetas en función de las recetas que tengamos guardados en la BBDD
     */
    private void rechargeComboRecipes(){
        try {
            recipes.getItems().setAll(App.getRecipeDao().getAllRecipes());
        } catch (SQLException sql){
            sql.printStackTrace();
            MyAlertDataBase();
        }
    }

    /**
     * Añade las acciones cuando pinchamos con el ratón sobre el ComboBox de las recetas o cuando se realiza
     * una accion que en este caso es cuando vamos pasando con las flechas arriba y abajo (SetOnKeyTyped)
     */
    private void addListenerComboRecipes(){
        recipes.setOnAction(event -> {
            actionsComboRecipes();
        });

        recipes.setOnKeyTyped(keyEvent -> {
            actionsComboRecipes();
        });
    }

    /**
     * Método que recoge las acciones que realizamos cuando hay una acción sobre el comboBox de recetas
     */
    private void actionsComboRecipes(){
        Recipe recipe = recipes.getSelectionModel().getSelectedItem();
        if (recipe != null){
            rechargeData(recipe);
            btNewRecipe.setDisable(false);
            btSave.setDisable(true);
            btEdit.setDisable(false);
            btRemove.setDisable(false);
        }
    }

    /**
     * Cada vez que pinchemos en un CheckComboBox de cualquier ingrediente lo añade o lo elimina de ListView de seleccionados
     */
    private void addSelectedListView(){
        ObservableSet<Ingredient> ingredients = FXCollections.observableSet();
        ObservableSet<HBox> ingredientsSelected = FXCollections.observableSet(); //lo que hay en el list view al empezar

        //si pincha en editar lo hacemos diferente porque respetamos los que ya tiene con sus porcentajes
        chPrefermentos.getCheckModel().getCheckedItems().addListener((ListChangeListener<Prefermento>) change -> {
            if (!clickEdit) getIngredientsCombo(ingredients);

            else getIngredientsListView(ingredientsSelected, change);
        });

        chFlours.getCheckModel().getCheckedItems().addListener((ListChangeListener<Flour>) change -> {
            if (!clickEdit) getIngredientsCombo(ingredients);

            else getIngredientsListView(ingredientsSelected, change);
        });

        chIngredients.getCheckModel().getCheckedItems().addListener((ListChangeListener<Ingredient>) change -> {
            if (!clickEdit) getIngredientsCombo(ingredients);

            else getIngredientsListView(ingredientsSelected, change);
        });
    }

    /**
     * Recoge lo que tengamos en los CheckComboBox de los ingredientes ya ordenado
     * @param ingredients lista de ingredientes
     */
    private void getIngredientsCombo(ObservableSet<Ingredient> ingredients){
        //limpiamos lo que tenga la lista para evitar repetidos y la llenamos con lo que haya en los comboBox
        ingredients.clear();
        ingredients.addAll(chPrefermentos.getCheckModel().getCheckedItems());
        ingredients.addAll(chFlours.getCheckModel().getCheckedItems());
        ingredients.addAll(chIngredients.getCheckModel().getCheckedItems());

        //la pasamos a un set ordenada por medio del comparator implementado en la clase Ingredient
        Set<Ingredient> ingredientSet = ingredients.stream()
                .sorted(new ComparableComparator<>())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        addIngredientsHbSelected(ingredientSet);
    }


    /**
     * Añade a cada Ingrediente de la lista que pasamos como parámetro su textField y lo añade al HbSelected para
     * luego pasarlo al ListView de seleccionados.
     * @param list lista de ingredientes seleccionados
     */
    private void addIngredientsHbSelected(Set<Ingredient> list){
        //lista para ver cuantas harinas tiene seleccionadas.
        ObservableList<Flour> listFlours = chFlours.getCheckModel().getCheckedItems();
        hbSelected.clear();

        list.forEach(ingredient -> {
            TextField textField = new TextField();
            textField.setPrefWidth(50);
            addListenerTexField(textField);

            //si solo tiene una harina le ponemos el 100 directamente
            if (listFlours.size() == 1 && ingredient.getTypeIngredient() == TypeIngredient.HARINA) {
                textField.setText("100");
            }

            hbSelected.add(getHboxIngredientRecipe(ingredient.getName(), textField));
        });

        lvSelected.setItems(hbSelected);

        //le quitamos el estilo por si está en rojo ya que vamos a añadir elementos
        lvSelected.setStyle(null);
    }

    /**
     * Inicializa los TextField que acompañan a los Ingredientes en el HbSelected. Le añadimos su listener y los añadimos
     * al HbSelected
     * @param ingredient ingrediente que queremos añadir al HbSelected
     */
    private void addIngredientHbSelected(Ingredient ingredient){
        TextField textField = new TextField();
        textField.setPrefWidth(50);
        addListenerTexField(textField);
        hbSelected.add(getHboxIngredientRecipe(ingredient.getName(), textField));
    }

    /**
     * Añade o elimina ingredientes al ListView de seleccionado cuando pinchamos sobre ellos si hemos pulsado en editar.
     * Si pulsamos en editar y quiere añadir o eliminar algún ingrediente el resto conserva su porcentaje y no se borra.
     * @param ingredientsSelected ingredientes que tiene seleccionados
     * @param change lista de ingredientes cuando pincha sobre algún CheckComboBox para saber si elimina o añade
     */
    private void getIngredientsListView(ObservableSet<HBox> ingredientsSelected, ListChangeListener.Change<? extends Ingredient> change){
        //cogemos lo que hay en el listView. Cuando añadamos o eliminemos tenemos que añadirlos como estaban
        //más lo que añada
        ingredientsSelected.addAll(lvSelected.getItems());

        //cojo lo que hay en los combo de los ingredientes
        ObservableList<Ingredient> ingredients = FXCollections.observableArrayList();
        ingredients.addAll(allIngredientsChecked());

        //Con el while y el método next() (hay que ponerlo porque si no da error) y luego con wasAdeed y wasRemoved vemos si elimina o añade
        while (change.next()){

            if (change.wasAdded()){
                List<String> namesIngredients = lvSelected.getItems().stream()
                        .map(hBox -> (Label) hBox.getChildren().get(1))
                        .map(Label::getText)
                        .collect(Collectors.toList());

                ingredients.forEach(ingredient -> {
                    //si no esta en la lista original, es porque es el que ha añadido
                    if (!namesIngredients.contains(ingredient.getName())){
                        //si ya tenemos el que ha añadido, lo añadimos con su caja de texto al hbSelected
                        addIngredientHbSelected(ingredient);
                    }
                });
            }

            if (change.wasRemoved()){
                //si elimina cogemos lo que hay en los comboBox de los ingredientes y lo pasamos a string
                List<String> namesIngredients = allIngredientsChecked().stream()
                        .map(Ingredient::getName)
                        .collect(Collectors.toList());

                //borramos el que no este en la lista de nombres de ingredientes, que es el que ha eliminado
                lvSelected.getItems().removeIf(hBox -> {
                    Label label = (Label) hBox.getChildren().get(1);
                    return !namesIngredients.contains(label.getText());
                });
            }
        }
    }

    /**
     * Añade a un Hbox (que es cada fila del listView de seleccionados ) un TextField y un Label con el nombre del
     * ingrediente
     * @param nameIngredient ingrediente
     * @param textField textField
     * @return Hbox para pasarlo al ListView de seleccionados
     */
    private HBox getHboxIngredientRecipe(String nameIngredient, TextField textField){
        Label label = new Label(nameIngredient);
        HBox hBox = new HBox();
        hBox.setSpacing(20);
        hBox.getChildren().addAll(textField, label);

        return hBox;
    }

    /**
     * Elimina el estilo css al textField que pasamo como parámetro al pinchar sobre el o pulsar una tecla
     * @param textField textField
     */
    private void addListenerTexField(TextField textField){
        textField.setOnMouseClicked(mouseEvent -> {
            textField.setStyle(BOX_SHADOW_NONE);
        });

        textField.setOnKeyPressed(keyEvent -> {
            textField.setStyle(BOX_SHADOW_NONE);
        });
    }

    /**
     * Elimina el listener del textField que pasamos como parámetro al hacer click sobre el
     * @param textField textField
     */
    private void removeListenerTextField(TextField textField){
        textField.setOnMouseClicked(mouseEvent -> {});
    }

    /**
     * Cambia el color del borde del los textField del listView de seleccionado cuando hay un error
     */
    private void changeColourTextField(){
        lvSelected.getItems().stream()
                .map(hBox -> (TextField) hBox.getChildren().get(0))
                .forEach(textField -> {
                    if (textField.getText().isEmpty() || !isDecimal(textField.getText())){
                        textField.setStyle(BOX_SHADOW_RED);
                        textField.setPromptText("%");
                    }
                });
    }

    /**
     * Comprueba si el valor de los TextFields del listView de seleccionado es correcto (es decimal)
     * @return true si es decimal y false si no lo es
     */
    private boolean valueTextFieldsCorrects() {
        return lvSelected.getItems().stream()
                .map(hBox -> (TextField) hBox.getChildren().get(0))
                .anyMatch(textField -> isDecimal(textField.getText()));
    }

    /**
     * Comprueba si todos por porcentajes del ListView de seleccionados son correctos (no están vacios y el borde no
     * esta en rojo por algún fallo)
     * @return true si son correctos  y false si no lo son
     */
    private boolean isAllPercentagesCorrect(){
        return lvSelected.getItems().stream()
                .map(hBox -> (TextField) hBox.getChildren().get(0))
                .noneMatch(textField -> textField.getText().isEmpty() || textField.getStyle().equals(BOX_SHADOW_RED));
    }

    /**
     * Recoge todos los ingredientes de todos los CheckComboBox que están seleccionados
     * @return lista con los seleccionados
     */
    private ObservableList<Ingredient> allIngredientsChecked(){
        ObservableList<Ingredient> ingredientsChecks = FXCollections.observableArrayList();
        ingredientsChecks.addAll(chPrefermentos.getCheckModel().getCheckedItems());
        ingredientsChecks.addAll(chFlours.getCheckModel().getCheckedItems());
        ingredientsChecks.addAll(chIngredients.getCheckModel().getCheckedItems());

        return ingredientsChecks;
    }

    /**
     * Cambia los datos de los campos de texto por los de la receta que le  pasemos por parámetro. Lo utilizamos cuando
     * pinchamos en el ComboBox de las recetas  que tenemos para que vaya mostrando los datos de cada una
     * @param recipe receta que queremos mostrar en los campos
     */
    private void rechargeData(Recipe recipe) {
        //limpiamos todos los check de comboBox para que no se repitan datos
        clearCheckComboIngredients();

        //Limpio lo que hay en los combo de ingredientes
        chIngredients.getItems().clear();
        chFlours.getItems().clear();
        chPrefermentos.getItems().clear();

        ObservableList<Ingredient> ingredients = FXCollections.observableArrayList();
        ObservableList<Flour> flours = FXCollections.observableArrayList();
        ObservableList<Prefermento> prefermentos = FXCollections.observableArrayList();

        ingredients.addAll(ingredienRecipeToIngredient(recipe));
        flours.addAll(ingredienRecipeToFlour(recipe));
        prefermentos.addAll(ingredienRecipeToPrefermento(recipe));

        if(!ingredients.isEmpty()){
            try {
                ObservableList<Ingredient> allIngredients = App.getIngredientDao().getAllIngredients(false); //todos los ingredientes de la bd
                allIngredients.forEach(ingredient -> {

                    //si no esta activo pero la receta lo contiene lo añadimos
                    if (ingredient.getIsActive() == 0 && ingredients.contains(ingredient)){
                        chIngredients.getItems().add(ingredient);

                    } else if(ingredient.getIsActive() == 1) { //si esta activo lo añadimos
                        chIngredients.getItems().add(ingredient);
                    }
                });

                ingredients.forEach(ingredient -> chIngredients.getCheckModel().check(ingredient));
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        } else {
            try{
                ObservableList<Ingredient> allIngredients = App.getIngredientDao().getAllIngredients(true);
                allIngredients.forEach(ingredient -> {
                    chIngredients.getItems().add(ingredient);
                });

            }catch (SQLException sqle){
                sqle.printStackTrace();
            }
        }

        if (!flours.isEmpty()){
            try {
                ObservableList<Flour> allFlours = App.getFlourDao().getAllFlours(false);

                allFlours.forEach(flour -> {
                    //si no esta activo pero la receta lo contiene lo añadimos
                    if (flour.getIsActive() == 0 && flours.contains(flour)){
                        chFlours.getItems().add(flour);

                    } else if(flour.getIsActive() == 1) { //si esta activo lo añadimos
                        chFlours.getItems().add(flour);
                    }
                });

            }catch (SQLException sqle) {
                sqle.printStackTrace();
            }
            flours.forEach(flour -> chFlours.getCheckModel().check(flour));
        }

        if (!prefermentos.isEmpty()){
            try {
                ObservableList<Prefermento> allPrefermentos = App.getPrefermentoDao().getAllPrefermentos(false);

                allPrefermentos.forEach(prefermento -> {
                    //si no esta activo pero la receta lo contiene lo añadimos
                    if (prefermento.getIsActive() == 0 && prefermentos.contains(prefermento)){
                        chPrefermentos.getItems().add(prefermento);

                    } else if(prefermento.getIsActive() == 1) { //si esta activo lo añadimos
                        chPrefermentos.getItems().add(prefermento);
                    }
                });

            }catch (SQLException sqle) {
                sqle.printStackTrace();
            }
            prefermentos.forEach(prefermento -> chPrefermentos.getCheckModel().check(prefermento));

        } else {
            try {
                ObservableList<Prefermento> allPrefermentos = App.getPrefermentoDao().getAllPrefermentos(true);
                allPrefermentos.forEach(prefermento -> {
                    chPrefermentos.getItems().add(prefermento);
                });
            }catch(SQLException sqle){
                sqle.printStackTrace();
            }
        }

        //primero limpiamos lo que haya en el hbSelected para que no se repitan
        hbSelected.clear();

        //le añaddimos las cajas de texto y los añadimos al hbSelected
        rechargeTextFields((ObservableList<IngredientRecipe>) recipe.getIngredients());

        lvSelected.setItems(hbSelected);

        taObservation.setText(recipe.getObservations());
        taObservation.setEditable(false);
    }

    /**
     * Recarga los textField del listView de seleccionados cuando pinchamos sobre una receta para mostrar sus ingredientes.
     * Pone el valor del porcentaje de cada uno de ellos.
     * @param list lista de IngredientRecipe de la receta
     */
    private void rechargeTextFields(ObservableList<IngredientRecipe> list){
        list.stream()
                .sorted(new ComparableComparator<>())
                .forEach(ingredientRecipe -> {
                    TextField textField = new TextField();
                    textField.setPrefWidth(50);
                    textField.setEditable(false);

                    //si el porcentaje es mayor de un 4% lo redondeamos.
                    if (ingredientRecipe.getPercentage() > 4) {
                        textField.setText(String.valueOf(Math.round(ingredientRecipe.getPercentage())));

                    } else {
                        textField.setText(String.valueOf(ingredientRecipe.getPercentage()));
                    }

                    hbSelected.add(getHboxIngredientRecipe(ingredientRecipe.getIngredient().getName(), textField));
                });
    }

    /**
     * Inicializa el comboBox de las recetas, le damos tamaño y lo añadimos al Hbox
     */
    private void initComboRecipes(){
        recipes = new SearchableComboBox<>();
        recipes.setPrefSize(hbRecipes.getPrefWidth(), hbRecipes.getPrefHeight());
        hbRecipes.getChildren().setAll(recipes);
    }

    /**
     * Inicializa el TextField para el nombre de las recetas y le damos su tamaño
     */
    private void initTfNameRecipes(){
        tfNameRecipe = new TextField();
        tfNameRecipe.setPrefSize(hbRecipes.getPrefWidth(), hbRecipes.getPrefHeight());
    }

    /**
     * Método que pone los botones en deshabilitado y habilitado en función del boolean que pasemos como parámetro.
     * @param init boolean con true o false dependiendo del estado.
     */
    private void initStateButtons(boolean init){
        btNewRecipe.setDisable(!init);
        btSave.setDisable(init);
        btEdit.setDisable(init);
        btRemove.setDisable(init);
        chPrefermentos.setDisable(init);
        chFlours.setDisable(init);
        chIngredients.setDisable(init);
        taObservation.setEditable(init);
    }

    /**
     * Resetea los valores de los campos al estado inicial, cuando abrimos la Tab
     */
    private void resetValues(){
        clearAllFields();
        hbRecipes.getChildren().setAll(recipes);
        lbNameRecipe.setText("Recetas");
        taObservation.setPromptText(nonMandatoryField);
        lvSelected.getItems().clear();
        clearCheckComboIngredients();
        recipes.getSelectionModel().clearSelection();
    }

    /**
     * Limpia los valores del ListView de seleccionados y del TextArea de observaciones
     */
    private void clearAllFields(){
        taObservation.clear();
        lvSelected.getItems().clear();
    }

    /**
     * Limpia los seleccionados de los CheckComboBox de los ingredientes
     */
    private void clearCheckComboIngredients(){
        chIngredients.getCheckModel().clearChecks();
        chFlours.getCheckModel().clearChecks();
        chPrefermentos.getCheckModel().clearChecks();
    }

    /**
     * Convierte los IngredientRecipe de la receta que son de TypeIngredient.OTRO en Ingredient
     * @param recipe receta
     * @return lista de ingredientes
     */
    private ObservableList<Ingredient> ingredienRecipeToIngredient(Recipe recipe){
        List<Ingredient> list = recipe.getIngredients().stream()
                .map(IngredientRecipe::getIngredient)
                .filter(ingredient -> ingredient.getTypeIngredient() == TypeIngredient.OTRO)
                .collect(Collectors.toList());

        return FXCollections.observableArrayList(list);
    }

    /**
     * Convierte los IngredientRecipe de la receta que son de TypeIngredient.HARINA en Flour
     * @param recipe receta
     * @return lista de ingredientes
     */
    private ObservableList<Flour> ingredienRecipeToFlour(Recipe recipe){
        List<Flour> list = recipe.getIngredients().stream()
                .filter(ingredientRecipe -> ingredientRecipe.getIngredient().getTypeIngredient() == TypeIngredient.HARINA)
                .map(IngredientRecipe::getIngredient)
                .map(ingredient -> (Flour) ingredient )
                .collect(Collectors.toList());

        return FXCollections.observableArrayList(list);
    }

    /**
     * Convierte los IngredientRecipe de la receta que son de TypeIngredient.PREFERMENTO en Prefermento
     * @param recipe receta
     * @return lista de ingredientes
     */
    private ObservableList<Prefermento> ingredienRecipeToPrefermento(Recipe recipe){
        List<Prefermento> list = recipe.getIngredients().stream()
                .filter(ingredientRecipe -> ingredientRecipe.getIngredient().getTypeIngredient() == TypeIngredient.PREFERMENTO)
                .map(IngredientRecipe::getIngredient)
                .map(ingredient -> (Prefermento) ingredient)
                .collect(Collectors.toList());

        return FXCollections.observableArrayList(list);
    }

    /**
     * Inicializa el PopOver que hay a la derecha del label de Harinas
     */
    private void initPoopOver(){
        Label label1 = new Label("   El porcentage de harina    ");
        Label label2 = new Label("   o la suma de todos tiene   ");
        Label label3 = new Label("   que ser 100. ");

        VBox vBox = new VBox(label1, label2, label3);

        PopOver popOver = new PopOver(vBox);

        informacion.setOnMouseEntered(mouseEvent -> {
            popOver.show(informacion, lbFlours.getLayoutX()-20);
        });

        informacion.setOnMouseExited(mouseEvent -> {
            popOver.hide();
        });
    }

    /**
     * Añade la imagen a los botones. Le pasamos el botón, la imagen y el tamaño que queramos para el botón
     */
    private void addImageButtons(){
        addImageButton(btNewRecipe, imgBtNew, 25);
        addImageButton(btSave, imgBtSave, 25);
        addImageButton(btEdit, imgBtEdit, 25);
        addImageButton(btRemove, imgBtDelete, 25);
        addImageButton(btCancel, imgBtRefresh, 25);
    }

    /**
     * Inicializa el ImageView para el icono de información que tenemos al lado del label de Porcentajes de las harinas
     */
    private void addIconInformation(){
        informacion = new ImageView(new Image(App.class.getResource(imgInformation).toExternalForm()));
        informacion.setCursor(Cursor.HAND);
        informacion.setFitWidth(20);
        informacion.setFitHeight(20);
        hbNameFlour.getChildren().add(informacion);
    }

    /**
     * Vuelve a cargar los datos en los combo cuando la Tab gana el foco. Lo utilizamos porque si mientras
     * que está abierta la Tab el usuario crea por ejemplo una nueva harina que la muestre para poder utilizarla
     */
    private void reloadAllData(){
        App.getController().getTabs().stream()
                .filter(tab -> tab.getId().equals(nameTabRecipe))
                .forEach(tab -> {
                    tab.setOnSelectionChanged(event -> {
                        if (tab.isSelected()) {
                            rechargeCheckComboBoxIngredient();
                            rechargeCheckComboBoxFlours();
                            rechargeCheckComboBoxPrefermento();
                        }
                    });
                });
    }
}
