package com.garciajuanjo.calculatorpan.controller;

import com.garciajuanjo.calculatorpan.App;
import com.garciajuanjo.calculatorpan.domain.Ingredient;
import com.garciajuanjo.calculatorpan.domain.Provider;
import com.garciajuanjo.calculatorpan.util.ImageUtil;
import com.garciajuanjo.calculatorpan.util.ImputUtil;
import com.garciajuanjo.calculatorpan.util.MockData;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.SearchableComboBox;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import static com.garciajuanjo.calculatorpan.util.Constant.*;
import static com.garciajuanjo.calculatorpan.util.Css.BOX_SHADOW_NONE;
import static com.garciajuanjo.calculatorpan.util.ImageUtil.addImageButton;
import static com.garciajuanjo.calculatorpan.util.ImputUtil.clearAllTextFields;
import static com.garciajuanjo.calculatorpan.util.ImputUtil.isNotValidTfName;
import static com.garciajuanjo.calculatorpan.util.MessageUtil.*;

public class NewIngredientController implements Initializable {

    @FXML
    private TextField tfName;
    @FXML
    private TextArea taDescription;
    @FXML
    private Button btSave, btCancel, btNewIngredient, btEdit, btDelete;
    @FXML
    private Label lbStatus;
    @FXML
    private SearchableComboBox<Ingredient> scIngredients;
    @FXML
    private CheckComboBox<Provider> cbProviders;
    @FXML
    private ImageView imgLogo;

    private Boolean clickEdit;

    private ObjectProperty<Ingredient> originalIngredient;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //recargamos el combo de los ingredientes
        rechargeComboBoxIngredients();

        //cuando la ventana coge el foco recargamos los datos por si hay algún cambio
        reloadAllData();

        //Añadimos el listener al comboBox de ingredientes y al TextField del nombre
        addListenerComboIngredients();
        addListenerTfName();

        //Ponemos los botones en el estado inicial
        initStateButtons(true);

        //Deshabilitamos los textfields
        isEditableTextFields(false);

        //Quitamos el scroll de la derecha del textArea
        taDescription.setWrapText(true);

        //Añadimos las imagenes a los botones
        addImageButtons();

        //Añadimos la imagen del logo
        ImageUtil.addImageLogo(imgLogo);

        originalIngredient = new SimpleObjectProperty<>();
        originalIngredient.bind(scIngredients.getSelectionModel().selectedItemProperty());

        lbStatus.setText(nameAplicationAndVersion);
    }

    /**
     * Acción del botón de refrescar. Resetea los valores como cuando abrimos la Tab por primera vez
     * @param event
     */
    @FXML
    void btCancelAction(Event event){
        resetValues();
        initStateButtons(true);
        isEditableTextFields(false);
        tfName.setStyle(BOX_SHADOW_NONE);
    }

    /**
     * Actión del boton de para crear un nuevo ingrediente.
     * @param event
     */
    @FXML
    void btNewIngredientAction(Event event){
        clickEdit = false;
        resetValues();
        initStateButtons(false);
        isEditableTextFields(true);
        btEdit.setDisable(true);
        btDelete.setDisable(true);
        tfName.requestFocus();
    }

    /**
     * Acción del botón de editar un ingrediente
     * @param event
     */
    @FXML
    void btEditAction(Event event){
        clickEdit = true;
        isEditableTextFields(true);
        btSave.setDisable(false);
        scIngredients.setDisable(true);
        btNewIngredient.setDisable(true);
        btDelete.setDisable(true);
        btEdit.setDisable(true);
        tfName.requestFocus();

        //ponemos de nuevo los datos del ingrediente seleccionado en los campos por si el usuario mientras pincha
        //en editar cambia algo
        rechargeData(originalIngredient.get());
    }

    /**
     * Actión del botón de borrar un Ingrediente. Comprobamos que no es el ingrediente genérico, la sal o el agua,
     * sacamos un mensaje de confirmación, si acepta lo borramos y si no nada.
     * @param event
     */
    @FXML
    void btDeleteAction(Event event){
        Ingredient ingredient = scIngredients.getSelectionModel().getSelectedItem();

        //si no es el ingrediente genérico lo borramos
        if(ingredient.getName().equals("Ingrediente generico")){
            MyAlert("El ingrediente genérico no se puede borrar", TypeAlert.WARNING);
            return;
        }

        if(ingredient.getName().equals("Agua")) {
            MyAlert("El ingrediente Agua no se puede borrar", TypeAlert.WARNING);
            return;
        }

        if(ingredient.getName().equals("Sal")) {
            MyAlert("El ingrediente Sal no se puede borrar", TypeAlert.WARNING);
            return;
        }

        boolean confirmation = MyAlertConfirmation("¿Seguro que quiere eliminar este ingrediente?\n" + ingredient);

        if (confirmation){
            try {
                boolean delete = App.getIngredientDao().deleteIngredient(ingredient);

                if (!delete) {
                    MyAlert("No se pudo borrar el ingrediente, revise su conexia la base de datos", TypeAlert.WARNING);
                    return;
                }

                MyAlert("¡¡Ingrediente borrado con éxito!!", TypeAlert.INFORMATION);

                resetValues();
                initStateButtons(true);
                isEditableTextFields(false);
                cbProviders.setDisable(true);
                scIngredients.getSelectionModel().clearSelection();
                rechargeComboBoxIngredients();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                MyAlertDataBase();
            }
        }
    }

    /**
     * Actión del botón de guardar. Comprobamos que todos los campos esten correctos acorde a los valores que pueden
     * aceptar, si es así guardamos el ingrediente
     * @param event
     */
    @FXML
    void btSaveAction(Event event) {
        boolean allFieldsCorrect = isAllFieldsCorrects();

        if (taDescription.getText().length() > 500){
            MyAlert("El campo observaciones no puede superar los 500 caracteres\n" +
                    "Actualmente en uso: " + taDescription.getText().length() + " caracteres.", TypeAlert.WARNING);

        }else if (!allFieldsCorrect){
            MyAlert("Tiene campos obligatorios", TypeAlert.WARNING);

        } else {
            saveIngredient();
        }
    }

    /**
     * Comprueba cuando pinchamos en el botón de guardar si está editando o creando un nuevo ingrediente. En
     * función de la opción comprueba por ejemplo si ya existe el ingrediente, si es el genérico etc.
     */
    private void saveIngredient(){
        try {
            // si no ha pulsado en editar, es que va a dar de alta un nuevo ingrediente y comprobamos si existe
            if (App.getIngredientDao().existIngredient(getIngredient().getName(), true) && !clickEdit){
                MyAlert("Ya existe un ingrediente con ese nombre", TypeAlert.WARNING);

            } else {
                boolean election = true;

                if (clickEdit){
                    //si ha cambiado el nombre del ingrediente cuando pincha en editar
                    if (!originalIngredient.get().getName().equals(getIngredient().getName())){
                        /*
                        Si cambia el nombre le sacamos una pantalla diciendo que no se puede cambiar que si quiere dar de
                        alta un nuevo ingrediente con el nuevo nombre
                         */
                        election = MyAlertConfirmation("El nombre del ingrediente no se pude cambiar \n" +
                                "quiere dar de alta un nuevo ingrediente con los cambios introducidos?");

                        // si pincha en aceptar y existe ya un ingrediente con ese nombre (no se puede crear)
                        if (election && App.getIngredientDao().existIngredient(getIngredient().getName(), true)){
                            MyAlert("Ya existe un ingrediente con ese nombre", TypeAlert.WARNING);
                            return;
                        }

                        // si pincha en aceptar y no exite un ingrediente con ese nombre lo añadimos a la bd o lo
                        // modificamos si está inactivo
                        if (election){

                            //comprobamos si existe inactivo para modificarlo
                            if(App.getIngredientDao().existIngredient(getIngredient().getName(), false)) {
                                App.getIngredientDao().updateIngredient(getIngredient());

                            } else{
                                App.getIngredientDao().addIngredient(getIngredient()); //si no existe lo damos de alta
                            }
                        }

                    } else {
                        // Por último modificamos el ingrediente
                        boolean update = App.getIngredientDao().updateIngredient(getIngredientUpdate());

                        if (!update) {
                            MyAlert("No se pudo modificar el ingrediente, revise su conexicon la base de datos",
                                    TypeAlert.WARNING);
                            return;
                        }
                    }

                    // si entramos en este es porque no ha pinchado en editar y quiere dar de alta uno nuevo
                } else {
                    //miramos si existe el ingrediente inactivo y si es asi lo modificamos en vez de crear uno nuevo
                    if(App.getIngredientDao().existIngredient(getIngredient().getName(), false)) {
                        Ingredient ingredient = getIngredient();
                        ingredient.setIdIngredient(App.getIngredientDao().getIdIngredient(ingredient.getName()));
                        App.getIngredientDao().updateIngredient(ingredient);

                    } else {
                        App.getIngredientDao().addIngredient(getIngredient()); //si no lo damos de alta
                    }
                }
                // si no ha cambiado el nombre, o si lo ha cambiado y quiere crear uno nuevo
                if (election){
                    MyAlert("Ingrediente guardado con éxito!!", TypeAlert.INFORMATION);

                    resetValues();
                    initStateButtons(true);
                    isEditableTextFields(false);
                    cbProviders.setDisable(true);
                    scIngredients.getSelectionModel().clearSelection();
                    rechargeComboBoxIngredients();
                }
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            MyAlertDataBase();
        }
    }

    /**
     * Recoge los datos de los TextFields y los ComboBox. Lo utilizo cuando pinchamos en el botón
     * de guardar después de comprobar que los datos de cada campo son correctos.
     * @return new Ingredient
     */
    private Ingredient getIngredient(){
        String name = tfName.getText().trim();
        name = ImputUtil.cleanText(name);

        ObservableList<Provider> listProvider = FXCollections.observableArrayList();
        listProvider.addAll(cbProviders.getCheckModel().getCheckedItems());

        //si no pone ningún proveedor le asignamos el genérico
        if(listProvider.isEmpty()){
            MockData mockData = new MockData();
            listProvider.add(mockData.getProviderGeneric());
        }

        String description = taDescription.getText().trim();
        description = ImputUtil.cleanText(description);
        description = StringUtils.defaultIfEmpty(description, "Sin descripcion");

        return new Ingredient(name, description, listProvider);
    }

    /**
     * Método que utilizo cuando está editando un ingrediente. Recojo los datos con el método getIngredient pero el id
     * del ingrediente tiene que ser el mismo del ingrediente que estamos editando. Ese id lo cogemos del ObjecProperty.
     * @return new Ingredient editado pero con el mismo id
     */
    private Ingredient getIngredientUpdate(){
        Ingredient ingredient = getIngredient();
        ingredient.setIdIngredient(originalIngredient.get().getIdIngredient());

        return ingredient;
    }

    /**
     * Comprobamos que todos los campos obligatorios son correctos
     * @return true o false si los campos son correctos
     */
    private boolean isAllFieldsCorrects(){
        boolean isAllFieldsCorrect = true;
        if (isNotValidTfName(tfName)) isAllFieldsCorrect = false;

        return isAllFieldsCorrect;
    }

    /**
     * Recarga todos los ingredientes del comboBox de los ingredientes en función de lo que haya en la BBDD.
     */
    private void rechargeComboBoxIngredients(){
        try {
            scIngredients.setItems(App.getIngredientDao().getAllIngredients(true));
        }catch (SQLException sqle){
            sqle.printStackTrace();
            MyAlertDataBase();
        }
    }

    /**
     * Recarga el comboBox de los proveedores en función de lo que haya en la BBDD
     */
    private void rechargeComboBoxProviders(){
        try {
            cbProviders.getItems().setAll(App.getProviderDao().getAllProviders());
        } catch (SQLException sqle){
            //TODO hacer con un boolean en properties para desactivar todos los printstacktrace
            sqle.printStackTrace();
            MyAlertDataBase();
        }
    }

    /**
     * Cambia los datos de los campos de texto por los del ingrediente que le
     * pasamos por parámetro. Lo utilizamos cuando pinchamos en el ComboBox de los ingredientes
     * que tenemos para que vaya mostrando los datos de cada uno
     * @param ingredient ingrediente que queremos mostrar en los campos
     */
    private void rechargeData(Ingredient ingredient){
        tfName.setText(ingredient.getName());

        //limpiamos si hay proveedores seleccionados en el comboBox de los proveedores para que no se repitan
        cbProviders.getCheckModel().clearChecks();

        //si la lista de proveedores no esta vacia la mostramos
        if (!ingredient.getProviders().isEmpty()){
            ingredient.getProviders().forEach(provider -> {
                cbProviders.getCheckModel().check(provider);
            });
        }

        taDescription.setText(ingredient.getDescription());
    }

    /**
     * Accción cuando pinchamos con el ratón o presionamos una tecla en el campo TfName
     */
    private void addListenerTfName(){
        tfName.setOnMousePressed(mouseEvent -> {
            tfName.setStyle(BOX_SHADOW_NONE);
        });
        tfName.setOnKeyPressed(keyEvent -> {
            tfName.setStyle(BOX_SHADOW_NONE);
        });
    }

    /**
     * Acción cuando pinchamos con el ratón o presionamos una tecla en el comboBox de los ingredientes
     */
    private void addListenerComboIngredients(){
        scIngredients.setOnAction(event -> {
            actionsComboIngredients();
        });

        scIngredients.setOnKeyTyped(keyEvent -> {
            actionsComboIngredients();
        });
    }

    /**
     * Acciones a realizar en el comboBox de ingredientes.
     */
    private void actionsComboIngredients(){
        Ingredient ingred = scIngredients.getSelectionModel().getSelectedItem();
        if (ingred != null){
            rechargeData(ingred);
            btNewIngredient.setDisable(false);
            btSave.setDisable(true);
            btEdit.setDisable(false);
            btDelete.setDisable(false);
            cbProviders.setDisable(false);
        }
    }

    /**
     * Estado inicial de todos los botones y el comboBox de ingredientes y proveedores.
     * @param init true o false
     */
    private void initStateButtons(Boolean init){
        btSave.setDisable(init);
        btNewIngredient.setDisable(!init);
        btEdit.setDisable(init);
        btDelete.setDisable(init);
        scIngredients.setDisable(!init);
        cbProviders.setDisable(init);
    }

    /**
     * Hace que los campos para recoger datos esten habilitados o deshabilitados dependiendo de lo
     * que le pasemos como parámetro
     * @param editable True o false
     */
    private void isEditableTextFields(Boolean editable){
        tfName.setEditable(editable);
        taDescription.setEditable(editable);
    }

    /**
     * Resetea los campos del formulario a su estado inicial.
     */
    private void resetValues(){
        clearAllTextFields(tfName);
        taDescription.clear();

        tfName.setPromptText(writeNameHere);
        taDescription.setPromptText(nonMandatoryField);

        cbProviders.getCheckModel().clearChecks();
        scIngredients.getSelectionModel().clearSelection();
    }

    /**
     * Añade la imagen a los botones. Le pasamos el botón, la imagen y el tamaño que queremos para el botón.
     */
    private void addImageButtons(){
        addImageButton(btNewIngredient, imgBtNew, 25);
        addImageButton(btSave, imgBtSave, 25);
        addImageButton(btEdit, imgBtEdit, 25);
        addImageButton(btDelete, imgBtDelete, 25);
        addImageButton(btCancel, imgBtRefresh, 25);
    }

    /**
     * Vuelve a cargar los datos en los combo cuando la Tab gana el foco. Lo utilizamos porque si mientras
     * que está abierta la Tab el usuario crea por ejemplo un nuevo proveedor que lo muestre para poder utilizarlo
     */
    private void reloadAllData(){
        App.getController().getTabs().stream()
                .filter(tab -> tab.getId().equals(nameTabIngredient))
                .forEach(tab -> {
                    tab.setOnSelectionChanged(event -> {
                        if (tab.isSelected()) {
                            resetValues();
                            initStateButtons(true);
                            isEditableTextFields(false);
                            tfName.setStyle(BOX_SHADOW_NONE);
                            rechargeComboBoxProviders();
                        }
                    });
                });
    }
}
