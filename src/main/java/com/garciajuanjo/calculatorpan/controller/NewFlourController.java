package com.garciajuanjo.calculatorpan.controller;

import com.garciajuanjo.calculatorpan.App;
import com.garciajuanjo.calculatorpan.domain.Flour;
import com.garciajuanjo.calculatorpan.domain.Provider;
import com.garciajuanjo.calculatorpan.util.ImageUtil;
import com.garciajuanjo.calculatorpan.util.ImputUtil;
import com.garciajuanjo.calculatorpan.util.MessageUtil;
import com.garciajuanjo.calculatorpan.util.MockData;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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

import static com.garciajuanjo.calculatorpan.domain.Flour.TypeFlour;
import static com.garciajuanjo.calculatorpan.util.Constant.*;
import static com.garciajuanjo.calculatorpan.util.Css.BOX_SHADOW_NONE;
import static com.garciajuanjo.calculatorpan.util.ImageUtil.addImageButton;
import static com.garciajuanjo.calculatorpan.util.ImputUtil.*;
import static com.garciajuanjo.calculatorpan.util.MessageUtil.*;

public class NewFlourController implements Initializable{

    @FXML
    private TextField tfName, tfProtein, tfStrength;
    @FXML
    private TextArea taDescription;
    @FXML
    private Button btSave, btCancel, btNewFlour, btEdit, btDelete;
    @FXML
    private Label lbStatus;
    @FXML
    private SearchableComboBox<Flour> scFlours;
    @FXML
    private CheckComboBox<Provider> cbProviders;
    @FXML
    private ComboBox<String> cbType;
    @FXML
    private ImageView imgLogo;

    private Boolean clickEdit;
    private ObjectProperty<Flour> originalFlour;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Cargamos los datos en los comboBox
        rechargeComboBoxType();
        rechargeComboBoxFlours();

        //cuando la ventana coge el foco recargamos los datos por si hay algún cambio
        reloadAllData();

        // añadimos los listener a los comboBox
        addListenerComboFlour();
        addListenerComboType();
        addListnerComboProvider();

        //añadimos los listener a los textfields
        addListenerTextFields(tfName, tfProtein, tfStrength);

        //Ponemos los botones en el estado inicial
        initStateButtons(true);

        //Deshabilitamos los textfields
        isEditableTextFields(false);

        // para quitar el scroll de la derecha del textArea
        taDescription.setWrapText(true);

        lbStatus.setText(nameAplicationAndVersion);

        //añadimos la imagen a los botones
        addImageButtons();

        //añadimos el logo
        ImageUtil.addImageLogo(imgLogo);

        //Object property que enlazamos con el Combo de las harinas para saber siempre cual es la última seleccionada
        originalFlour = new SimpleObjectProperty<>();
        originalFlour.bind(scFlours.getSelectionModel().selectedItemProperty());
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
        resetStyles();
    }

    /**
     * Actión del botón para crear una nueva harina.
     * @param event
     */
    @FXML
    void btNewFlourAction(Event event){
        clickEdit = false;
        resetValues();
        initStateButtons(false);
        isEditableTextFields(true);
        tfName.requestFocus();
        btEdit.setDisable(true);
        btDelete.setDisable(true);

        //cargamos el combo de los proveedores por si ha creado uno después de abrir la ventana
        rechargeComboBoxProviders();
    }

    /**
     * Action del botón de editar una harina.
     * @param event
     */
    @FXML
    void btEditAction(Event event){
        clickEdit = true;
        isEditableTextFields(true);
        btSave.setDisable(false);
        scFlours.setDisable(true);
        btNewFlour.setDisable(true);
        btDelete.setDisable(true);
        btEdit.setDisable(true);
        tfName.requestFocus();

        //ponemos de nuevo los datos de la harina seleccionada en los campos por si el usuario mientras pincha
        //en editar cambia algo
        rechargeData(originalFlour.get());
    }

    /**
     * Actión del botón de borrar una harina. Comprobamos que no es la harina genérica, sacamos un mensaje de
     * confirmación y si acepta la borramos y si no nada.
     * @param event
     */
    @FXML
    void btDeleteAction(Event event){
        Flour flour = scFlours.getSelectionModel().getSelectedItem();
        boolean confirmation = false;

        if (flour.getName().equals("Harina generica")){
            MyAlert("La harina genérica no se puede borrar", TypeAlert.WARNING);

        } else {
            confirmation = MessageUtil.MyAlertConfirmation("¿Seguro que quiere eliminar esta harina?\n" + flour);
        }

        if (confirmation){
            try {

                boolean delete = App.getFlourDao().deleteFlour(flour);

                if (!delete) {
                    MyAlert("No se pudo borrar la harina, revise su conexión a la base de datos", TypeAlert.WARNING);
                    return;
                }

                //Abrimos un alert que muestra que la harina se ha borrado con exito
                MyAlert("¡¡ Harina borrada con éxito !!", TypeAlert.INFORMATION);

                resetValues();
                initStateButtons(true);
                isEditableTextFields(false);
                cbProviders.setDisable(true);
                scFlours.getSelectionModel().clearSelection();
                rechargeComboBoxFlours();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                MessageUtil.MyAlertDataBase();
            }
        }
    }

    /**
     * Actión del botón de guardar. Comprobamos que todos los campos esten correctos acorde a los valores que pueden
     * aceptar y si es así guardamos la harina
     * @param event
     */
    @FXML
    void btSaveAction(ActionEvent event) {
        boolean allFieldsCorrect = isAllFieldsCorrect();

        if (!allFieldsCorrect){
            MyAlert("Tiene campos obligatorios", TypeAlert.WARNING);

        } else if (taDescription.getText().length() > 500){
            MyAlert("El campo observaciones no puede superar los 500 caracteres\n" +
                    "Actualmente en uso: " + taDescription.getText().length() + " caracteres.", TypeAlert.WARNING);

        } else {
            saveIngredient();
        }
    }

    /**
     * Comprueba cuando pinchamos en el botón de guardar si está editando o creando una nueva harina. En
     * función de la opción comprueba por ejemplo si ya existe la harina, si es la genérica etc.
     */
    private void saveIngredient(){
        try {
            // si no ha pulsado en editar, es que va a dar de alta una nueva harina y comprobamos si existe
            if (App.getFlourDao().existFlour(getFlour().getName(), true) && !clickEdit) {
                MyAlert("Ya existe una harina con ese nombre", TypeAlert.WARNING);

            } else {
                boolean election = true;
                if (clickEdit){
                    // si no ha cambiado el nombre de la harina cuando pincha en editar la modificamos
                    if (originalFlour.get().getName().equals(getFlour().getName())){
                        boolean update = App.getFlourDao().updateFlour(getFlourUpdate());

                        if (!update) {
                            MyAlert("No se pudo modificar la harina, revise su conexión con la base de datos",
                                    TypeAlert.WARNING);
                            return;
                        }

                    } else {
                        /*
                        Si cambia el nombre le sacamos una pantalla diciendo que no se puede cambiar que si quiere dar de
                        alta una nueva harina con el nuevo nombre
                         */
                        election = MyAlertConfirmation("El nombre de la harina no se puede cambiar \n" +
                                " ¿Quiere dar de alta una nueva harina con los datos introducidos?");

                        // si pincha en aceptar y existe ya una harina con ese nombre (no se puede crear)
                        if (election && App.getFlourDao().existFlour(getFlour().getName(), true)){
                            MyAlert("Ya existe una harina con ese nombre", TypeAlert.WARNING);
                            return;
                        }

                        // si pincha en aceptar y no exite una harina con ese nombre la añadimos a la bbdd
                        if (election){

                            //comprobamos primero si existe la harina inactiva para modificarla
                            if(App.getFlourDao().existFlour(getFlour().getName(), false)) {
                                Flour flour = getFlour();
                                flour.setIdIngredient(App.getFlourDao().getIdFlour(flour.getName()));
                                App.getFlourDao().updateFlour(flour);

                            } else { //si no existe damos de alta una nueva
                                App.getFlourDao().addFlour(getFlour());
                            }
                        }
                    }

                    // si entramos en este es porque no ha pinchado en editar y quiere dar de alta una nueva
                } else {

                    //comprobamos primero si existe la harina inactiva para modificarla
                    if(App.getFlourDao().existFlour(getFlour().getName(), false)) {
                        Flour flour = getFlour();
                        flour.setIdIngredient(App.getFlourDao().getIdFlour(flour.getName()));
                        App.getFlourDao().updateFlour(flour);

                    } else { //si no existe damos de alta una nueva
                        App.getFlourDao().addFlour(getFlour());
                    }
                }

                // si no ha cambiado el nombre, o si lo ha cambiado y quiere crear uno nuevo
                if (election){
                    //Abrimos un alert que muestra que la harina se ha guardado con exito
                    MyAlert("¡¡ Harina guardada con éxito !!", TypeAlert.INFORMATION);

                    resetValues();
                    initStateButtons(true);
                    isEditableTextFields(false);
                    cbProviders.setDisable(true);
                    scFlours.getSelectionModel().clearSelection();
                    rechargeComboBoxFlours();
                }
            }

        } catch (SQLException throwables) {
            MyAlertDataBase();
            throwables.printStackTrace();
        }
    }

    /**
     * Este método recoge los datos de los TextFields y los ComboBox. Lo utilizo cuando pinchamos en el botón
     * de guardar después de comprobar que los datos de cada campo son correctos.
     * @return new Flour
     */
    private Flour getFlour(){
        String name = tfName.getText().trim();
        name = ImputUtil.cleanText(name);

        String description = taDescription.getText().trim();
        description = ImputUtil.cleanText(description);
        description = StringUtils.defaultIfEmpty(description, "Sin descripcion");

        String strProtein = StringUtils.replaceChars(tfProtein.getText(), ",", ".");
        float protein = Float.parseFloat(strProtein);

        int strength = Integer.parseInt(tfStrength.getText());

        TypeFlour typeFlour = TypeFlour.valueOf(cbType.getSelectionModel().getSelectedItem().toUpperCase());

        ObservableList<Provider> providers = FXCollections.observableArrayList();
        providers.addAll(cbProviders.getCheckModel().getCheckedItems());
        //si no le asigna ningún proveedor a la harina le asignamos el genérico
        if (providers.isEmpty()){
            MockData mockData = new MockData();
            providers.add(mockData.getProviderGeneric());
        }

        return new Flour(name, description, providers, protein, strength, typeFlour);
    }

    /**
     * Método que utilizo cuando está editando una harina. Recojo los datos con el método getFlour pero el id de la
     * harina tiene que ser el mismo de la harina que estamos editando. Ese id lo cogemos del ObjecProperty.
     * @return new Flour editada pero con el mismo id
     */
    private Flour getFlourUpdate(){
        Flour flour = getFlour();
        flour.setIdIngredient(originalFlour.get().getIdIngredient());

        return flour;
    }

    /**
     * Comprueba que todos los campos estén correctos a la hora de guardar cambios, al editar o guardar una nueva harina.
     * @return true o false
     */
    private boolean isAllFieldsCorrect(){
        boolean isAllFieldsCorrect = true;
        if (isNotValidTfName(tfName)) isAllFieldsCorrect = false;
        if (isNotValidCheckComboBox(cbProviders)) isAllFieldsCorrect = false;
        if (isNotValidCbType(cbType)) isAllFieldsCorrect = false;
        if (isNotValidTfStrength(tfStrength)) isAllFieldsCorrect = false;
        if (isNotValidTfProtein(tfProtein)) isAllFieldsCorrect = false;

        return isAllFieldsCorrect;
    }

    /**
     * Recarga el ComboBox con los tipos de harina que tengamos en la enumeración de TypeFlour.
     */
    private void rechargeComboBoxType(){
        TypeFlour[] values = TypeFlour.values();
        for (TypeFlour tipe:values) {
            cbType.getItems().add(StringUtils.capitalize(tipe.name().toLowerCase()));
        }
    }

    /**
     * Recarga los datos del ComboBox de proveedores en función de los proveedores que tengamos guardados en la BBDD
     */
    private void rechargeComboBoxProviders(){
        try {
            cbProviders.getItems().setAll(App.getProviderDao().getAllProviders());
        } catch (SQLException throwables) {
            MyAlertDataBase();
            throwables.printStackTrace();
        }
    }

    /**
     * Recarga los datos del ComboBox de las harinas en funcion de las que tengamos en la BBDD
     */
    private void rechargeComboBoxFlours(){
        try {
            scFlours.setItems(App.getFlourDao().getAllFlours(true));
        }catch (SQLException sqle){
            sqle.printStackTrace();
            MyAlertDataBase();
        }
    }

    /**
     * Método que recoge las acciones que realizamos cuando hay una acción sobre el comboBox de harinas
     */
    private void actionsComboFlours(){
        Flour flour = scFlours.getSelectionModel().getSelectedItem();
        if (flour != null){
            rechargeData(flour);
            btNewFlour.setDisable(false);
            btSave.setDisable(true);
            btEdit.setDisable(false);
            btDelete.setDisable(false);
            cbProviders.setDisable(false);
            cbType.setDisable(false);
        }
    }

    /**
     * Añade las acciones cuando pinchamos con el ratón sobre el ComboBox de las harinas o cuando se realiza
     * una acción, que en este caso es cuando vamos pasando con las flechas arriba y abajo (SetOnKeyTyped)
     */
    private void addListenerComboFlour(){
        scFlours.setOnAction(event -> {
            actionsComboFlours();
        });

        scFlours.setOnKeyTyped(keyEvent -> {
            actionsComboFlours();
        });
    }

    /**
     * Cuando pinchamos en el comboBox de los tipos de harina y seleccionamos uno cambia
     * el estilo y le quita el box_shadow_red por si lo tuviera. Lo puede tener porque al intentar guardar cambios no
     * haya seleccionado ninguno
     */
    private void addListenerComboType(){
        cbType.valueProperty().addListener((observableValue, s, t1) ->
                cbType.setStyle(BOX_SHADOW_NONE));
    }

    /**
     * Cuando pinchamos en el comboBox de los proveedores y seleccionamos uno cambia
     * el estilo y le quita el box_shadow_red por si lo tuviera. Lo puede tener porque al intentar guardar cambios no
     * haya seleccionado ninguno
     */
    private void addListnerComboProvider(){
        cbProviders.getCheckModel().getCheckedItems().addListener((ListChangeListener<Provider>) change -> {
            cbProviders.setStyle(BOX_SHADOW_NONE);
        });
    }

    /**
     * Cambia los datos de los campos de texto por los de la harina que le
     * pasemos por parámetro. Lo utilizamos cuando pinchamos en el ComboBox de las harinas
     * que tenemos para que vaya mostrando los datos de cada una
     * @param flour harina que queremos mostrar en los campos
     */
    private void rechargeData(Flour flour){
        tfName.setText(flour.getName());

        //limpiamos si hay proveedores seleccionados en el comboBox de los proveedores para que no se repitan
        cbProviders.getCheckModel().clearChecks();

        // si no esta vacia la lista de proveedores la mostramos
        if(!flour.getProviders().isEmpty()){
            flour.getProviders().forEach(provider -> {
                cbProviders.getCheckModel().check(provider);
            });
        }
        flour.getProviders().forEach(provider -> {
            cbProviders.getCheckModel().check(provider);
        });

        String typeFlour = StringUtils.capitalize(flour.getTypeFlour().toString().toLowerCase());
        cbType.getSelectionModel().select(typeFlour);

        tfProtein.setText(String.valueOf(flour.getProtein()));
        tfStrength.setText(String.valueOf(flour.getStrength()));
        taDescription.setText(flour.getDescription());
    }

    /**
     * Método que pone los botones en deshabilitado y habilitado en función del boolean que pasemos como parámetro.
     * @param init boolean con true o false dependiendo del estado que queramos.
     */
    private void initStateButtons(Boolean init){
        btNewFlour.setDisable(!init);
        btSave.setDisable(init);
        btEdit.setDisable(init);
        btDelete.setDisable(init);
        scFlours.setDisable(!init);
        cbProviders.setDisable(init);
        cbType.setDisable(init);
    }

    /**
     * Hace que los campos para recoger datos esten habilitados o deshabilitados dependiendo de lo
     * que le pasemos como parámetro
     * @param active True o false dependiendo de lo que queramos
     */
    private void isEditableTextFields(Boolean active){
        tfName.setEditable(active);
        tfProtein.setEditable(active);
        tfStrength.setEditable(active);
        taDescription.setEditable(active);
    }

    /**
     * Resetea los valores de los campos al estado inicial, cuando abrimos la Tab
     */
    private void resetValues(){
        clearAllTextFields(tfName, tfProtein, tfStrength);
        taDescription.clear();

        tfName.setPromptText(writeNameHere);
        taDescription.setPromptText(nonMandatoryField);
        tfProtein.setPromptText(zeroDecimal);
        tfStrength.setPromptText(zero);

        scFlours.getSelectionModel().clearSelection();
        cbType.getSelectionModel().clearSelection();
        cbProviders.getCheckModel().clearChecks();
    }

    /**
     * Resetea los estilos css de todos los campos al estado inicial, cuando abrimos la Tab
     */
    private void resetStyles(){
        tfProtein.setStyle(BOX_SHADOW_NONE);
        tfStrength.setStyle(BOX_SHADOW_NONE);
        tfName.setStyle(BOX_SHADOW_NONE);

        cbType.setStyle(BOX_SHADOW_NONE);
        cbProviders.setStyle(BOX_SHADOW_NONE);
    }

    /**
     * Añade la imagen a los botones. Le pasamos el botón, la imagen y el tamaño que queramos para el botón
     */
    private void addImageButtons(){
        addImageButton(btNewFlour, imgBtNew, 25);
        addImageButton(btSave, imgBtSave, 25);
        addImageButton(btEdit, imgBtEdit, 25);
        addImageButton(btDelete, imgBtDelete, 25);
        addImageButton(btCancel, imgBtRefresh, 25);
    }

    /**
     * Método que vuelve a cargar los datos en los combo cuando la Tab gana el foco. Lo utilizamos porque, si mientras
     * que está abierta la Tab el usuario crea por ejemplo un nuevo proveedor que lo muestre para poder utilizarlo
     */
    private void reloadAllData(){
        App.getController().getTabs().stream()
                .filter(tab -> tab.getId().equals(nameTabFlour))
                .forEach(tab -> {
                    tab.setOnSelectionChanged(event -> {
                        if (tab.isSelected()) {
                            resetValues();
                            initStateButtons(true);
                            isEditableTextFields(false);
                            resetStyles();
                            rechargeComboBoxProviders();
                        }
                    });
                });
    }
}
