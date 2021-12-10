package com.garciajuanjo.calculatorpan.controller;

import com.garciajuanjo.calculatorpan.App;
import com.garciajuanjo.calculatorpan.domain.Provider;
import com.garciajuanjo.calculatorpan.util.ImageUtil;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.SearchableComboBox;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import static com.garciajuanjo.calculatorpan.util.Constant.*;
import static com.garciajuanjo.calculatorpan.util.Css.BOX_SHADOW_NONE;
import static com.garciajuanjo.calculatorpan.util.ImageUtil.addImageButton;
import static com.garciajuanjo.calculatorpan.util.ImputUtil.*;
import static com.garciajuanjo.calculatorpan.util.MessageUtil.*;

public class NewProviderController implements Initializable {

    @FXML
    private Button btSave, btNewProvider, btEdit, btDelete, btCancel;
    @FXML
    private TextField tfName, tfDirection, tfPhone;
    @FXML
    private TextArea taObservations;
    @FXML
    private Label lbStatus;
    @FXML
    private SearchableComboBox<Provider> scProviders;
    @FXML
    private ImageView imgLogo;

    private boolean clickEdit;

    private ObjectProperty<Provider> originalProvider;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        rechargeComboBoxProviders();

        addListenerComboProviders();

        addListenerTextFields(tfName, tfDirection, tfPhone);

        initStateButtons(true);

        isEditableTextFields(false);

        // para quitar el scroll de la derecha del textArea
        taObservations.setWrapText(true);

        //cambiamos las imágenes de los botones
        addImageButtons();

        //añadimos el logo
        ImageUtil.addImageLogo(imgLogo);

        originalProvider = new SimpleObjectProperty<>();
        originalProvider.bind(scProviders.getSelectionModel().selectedItemProperty());

        lbStatus.setText(nameAplicationAndVersion);
    }

    /**
     * Acción del botón de refrescar. Resetea los valores como cuando abrimos la Tab por primera vez
     * @param event
     */
    @FXML
    void btCancelAction(Event event) throws IOException {
        resetValues();
        initStateButtons(true);
        isEditableTextFields(false);
        resetStyles();
    }

    /**
     * Actión del boton para crear un nuevo proveedor.
     * @param event
     */
    @FXML
    void btNewProviderListener(Event event){
        clickEdit = false;
        resetValues();
        initStateButtons(false);
        isEditableTextFields(true);
        btEdit.setDisable(true);
        btDelete.setDisable(true);
        tfName.requestFocus();
    }

    /**
     * Acción del botón de editar un proveedor
     * @param event
     */
    @FXML
    void btEditAction(Event event){
        clickEdit = true;
        isEditableTextFields(true);
        btSave.setDisable(false);
        scProviders.setDisable(true);
        btNewProvider.setDisable(true);
        btDelete.setDisable(true);
        btEdit.setDisable(true);
        tfName.requestFocus();
        //ponemos de nuevo los datos del proveedor seleccionado en los campos por si el usuario mientras pincha
        //en editar cambia algo
        rechargeData(originalProvider.get());
    }

    /**
     * Actión del botón de borrar un proveedor. Comprobamos que no es el proveedor genérico, sacamos un mensaje de
     * confirmación, si acepta lo borramos y si no nada.
     * @param event
     */
    @FXML
    void btDeleteAction(Event event){
        Provider provider = scProviders.getSelectionModel().getSelectedItem();
        boolean confirmation = false;

        if (provider.getName().equals("Proveedor generico")){
            MyAlert("El proveedor genérico no se puede borrar", TypeAlert.WARNING);

        } else {
            confirmation = MyAlertConfirmation("¿Seguro que quiere eliminar este proveedor?\n" + provider);
        }

        if (confirmation){
            try {
                boolean delete = App.getProviderDao().deleteProvider(provider);

                if (!delete) {
                    MyAlert("No se pudo borrar el proveedor, revise su conexión a la base de datos",
                            TypeAlert.WARNING);
                    return;
                }

                //Abrimos un alert que muestra que el proveedor se ha borrado con exito
                MyAlert("¡¡ Proveedor borrado con éxito !!", TypeAlert.INFORMATION);

                resetValues();
                initStateButtons(true);
                isEditableTextFields(false);
                scProviders.getSelectionModel().clearSelection();
                rechargeComboBoxProviders();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                MyAlertDataBase();
            }
        }
    }

    /**
     * Actión del botón de guardar. Comprobamos que todos los campos esten correctos acorde a los valores que pueden
     * aceptar y si es así guardamos el proveedor
     * @param event
     */
    @FXML
    void btSaveListener(Event event) {
        boolean allFieldsCorrect = isAllFieldCorrect();


        if (!allFieldsCorrect){
            MyAlert("Tiene campos obligatorios", TypeAlert.WARNING);

        } else if (taObservations.getText().length() > 500){
            MyAlert("El campo observaciones no puede superar los 500 caracteres\n" +
                    "Actualmente en uso: " + taObservations.getText().length() + " caracteres.", TypeAlert.WARNING);

        } else {
            saveIngredient();
        }
    }

    /**
     * Comprueba cuando pinchamos en el botón de guardar si está editando o creando un nuevo proveedor. En
     * función de la opción comprueba por ejemplo si ya existe el proveedor, si es el genérico etc.
     */
    private void saveIngredient(){
        try {
            // si no ha pulsado en editar, es que va a dar de alta un nuevo proveedor y comprobamos si existe
            if (App.getProviderDao().existProvider(getProvider().getName(), true) && !clickEdit){
                MyAlert("Ya existe un proveedor con ese nombre", TypeAlert.WARNING);

            } else {
                boolean election = true;

                if (clickEdit){

                    // si no ha cambiado el nombre del proveedor cuando pincha en editar
                    if (originalProvider.get().getName().equals(getProvider().getName())){
                        boolean update = App.getProviderDao().updateProvider(getProviderUpdate());

                        if (!update) {
                            MyAlert("No se pudo modificar el proveedor, revise su conexión con la base de datos",
                                    TypeAlert.WARNING);
                            return;
                        }

                    } else {
                        /*
                        Si cambia el nombre le sacamos una pantalla diciendo que no se puede cambiar que si quiere dar de
                        alta un nuevo proveedor con el nuevo nombre
                         */
                        election = MyAlertConfirmation("El nombre del proveedor no se pude cambiar \n" +
                                " ¿Quiere dar de alta un nuevo proveedor con los datos introducidos?");

                        // si pincha en aceptar y existe ya un proveedor con ese nombre (no se puede crear)
                        if (election && App.getProviderDao().existProvider(getProvider().getName(), true)){
                            MyAlert("Ya existe un proveedor con ese nombre", TypeAlert.WARNING);
                            return;
                        }

                        // si pincha en aceptar y no exite un proveedor con ese nombre lo añadimos a la bd
                        if (election){

                            //comprobamos primero si existe inactivo para modificarlo
                            if(App.getProviderDao().existProvider(getProvider().getName(), false)) {
                                Provider provider = getProvider();
                                provider.setIdProvider(App.getProviderDao().getIdProvider(provider.getName()));
                                App.getProviderDao().updateProvider(provider);

                            } else {
                                App.getProviderDao().addProvider(getProvider());
                            }
                        }
                    }

                    // si entramos en este es porque no ha pinchado en editar y quiere dar de alta uno nuevo
                } else {

                    //miramos si existe el proveedor inactivo y si es asi lo modificamos en vez de crear uno nuevo
                    if(App.getProviderDao().existProvider(getProvider().getName(), false)) {
                        Provider provider = getProvider();
                        provider.setIdProvider(App.getProviderDao().getIdProvider(provider.getName()));
                        App.getProviderDao().updateProvider(provider);

                    } else {
                        App.getProviderDao().addProvider(getProvider());
                    }
                }

                // si no ha cambiado el nombre, o si lo ha cambiado y quiere crear uno nuevo
                if (election){
                    MyAlert("¡¡ Proveedor guardado con éxito !!", TypeAlert.INFORMATION);

                    resetValues();
                    initStateButtons(true);
                    isEditableTextFields(false);
                    scProviders.getSelectionModel().clearSelection();
                    rechargeComboBoxProviders();
                }
            }

        } catch (SQLException throwables) {
            MyAlertDataBase();
            throwables.printStackTrace();
        }
    }

    /**
     * Recoge los datos de los TextFields y los ComboBox. Lo utilizo cuando pinchamos en el botón
     * de guardar después de comprobar que los datos de cada campo son correctos.
     * @return new Proveedor
     */
    private Provider getProvider(){
        String name = tfName.getText().trim();
        name = cleanText(name);

        String direction = tfDirection.getText().trim();
        direction = cleanText(direction);

        String phone = tfPhone.getText().trim();
        phone = cleanText(phone);

        String observations = taObservations.getText().trim();
        observations = cleanText(observations);
        observations = StringUtils.defaultIfEmpty(observations, "Sin observaciones");

        return new Provider(name, direction, phone, observations);
    }

    /**
     * Método que utilizo cuando está editando un proveedor. Recojo los datos con el método getProvider pero el id
     * del proveedor tiene que ser el mismo del proveedor que estamos editando. Ese id lo cogemos del ObjecProperty.
     * @return new Proveedor editado pero con el mismo id
     */
    private Provider getProviderUpdate(){
        Provider provider = getProvider();
        provider.setIdProvider(originalProvider.get().getIdProvider());

        return provider;
    }

    /**
     * Comprobamos que todos los campos obligatorios son correctos
     * @return true o false si los campos son correctos
     */
    private boolean isAllFieldCorrect(){
        boolean isAllFieldsCorrect = true;
        if (isNotValidTfName(tfName)) isAllFieldsCorrect = false;
        if (isNotValidTfDirection(tfDirection)) isAllFieldsCorrect = false;
        if (isNotValidTfPhone(tfPhone)) isAllFieldsCorrect = false;

        return isAllFieldsCorrect;
    }

    private void rechargeComboBoxProviders(){
        try {
            scProviders.setItems(App.getProviderDao().getAllProviders());
        }catch (SQLException sqle){
            sqle.printStackTrace();
            MyAlertDataBase();
        }
    }

    /**
     * Recarga todos los datos del proveedor que pasamos por parámetro en función de lo que haya en la BBDD.
     */
    private void rechargeData(Provider provider){
        tfName.setText(provider.getName());
        tfDirection.setText(provider.getDirection());
        tfPhone.setText(provider.getPhone());
        taObservations.setText(provider.getObservations());
    }

    /**
     * Acción cuando pinchamos con el ratón o presionamos una tecla en el comboBox de los proveedores
     */
    private void addListenerComboProviders(){
        scProviders.setOnAction(event -> {
            actionsComboProviders();
        });

        scProviders.setOnKeyTyped(keyEvent -> {
            actionsComboProviders();
        });
    }

    /**
     * Acción sobre el comboBox de los proveedores.
     */
    private void actionsComboProviders(){
        Provider provider = scProviders.getSelectionModel().getSelectedItem();
        if (provider != null){
            rechargeData(provider);
            btNewProvider.setDisable(false);
            btSave.setDisable(true);
            btEdit.setDisable(false);
            btDelete.setDisable(false);
        }
    }

    /**
     * Estado inicial de todos los botones y el comboBox de proveedores.
     * @param init true o false
     */
    private void initStateButtons(Boolean init){
        btNewProvider.setDisable(!init);
        btSave.setDisable(init);
        btEdit.setDisable(init);
        btDelete.setDisable(init);
        scProviders.setDisable(!init);
    }

    /**
     * Hace que los campos para recoger datos esten habilitados o deshabilitados dependiendo de lo
     * que le pasemos como parámetro
     * @param editable True o false dependiendo de lo que queramos
     */
    private void isEditableTextFields(Boolean editable){
        tfName.setEditable(editable);
        tfDirection.setEditable(editable);
        tfPhone.setEditable(editable);
        taObservations.setEditable(editable);
    }

    /**
     * Resetea los campos del formulario a su estado inicial.
     */
    private void resetValues(){
        clearAllTextFields(tfName, tfDirection, tfPhone);
        taObservations.clear();

        tfName.setPromptText(writeNameHere);
        taObservations.setPromptText(nonMandatoryField);

        scProviders.getSelectionModel().clearSelection();
    }

    /**
     * Resetea los estilos css del formulario a su estado inicial.
     */
    private void resetStyles(){
        scProviders.setStyle(BOX_SHADOW_NONE);
        tfName.setStyle(BOX_SHADOW_NONE);
        tfDirection.setStyle(BOX_SHADOW_NONE);
        tfPhone.setStyle(BOX_SHADOW_NONE);
    }

    /**
     * Añade la imagen a los botones. Le pasamos el botón, la imagen y el tamaño que queramos para el botón.
     */
    private void addImageButtons(){
        addImageButton(btNewProvider, imgBtNew);
        addImageButton(btSave, imgBtSave);
        addImageButton(btEdit, imgBtEdit);
        addImageButton(btDelete, imgBtDelete);
        addImageButton(btCancel, imgBtRefresh);
    }

}
