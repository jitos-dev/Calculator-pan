package com.garciajuanjo.calculatorpan.controller;

import com.garciajuanjo.calculatorpan.App;
import com.garciajuanjo.calculatorpan.domain.Flour;
import com.garciajuanjo.calculatorpan.domain.FlourPrefermento;
import com.garciajuanjo.calculatorpan.domain.Ingredient;
import com.garciajuanjo.calculatorpan.domain.Prefermento;
import com.garciajuanjo.calculatorpan.util.ImageUtil;
import com.garciajuanjo.calculatorpan.util.ImputUtil;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.SearchableComboBox;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static com.garciajuanjo.calculatorpan.util.Constant.*;
import static com.garciajuanjo.calculatorpan.util.Css.*;
import static com.garciajuanjo.calculatorpan.util.ImageUtil.addImageButton;
import static com.garciajuanjo.calculatorpan.util.ImputUtil.*;
import static com.garciajuanjo.calculatorpan.util.MessageUtil.*;

public class NewPrefermentoController implements Initializable{

    @FXML
    private TextField tfName, tfWatter, tfYeast;
    @FXML
    private TextArea taDescription;
    @FXML
    private Button btSave, btCancel, btNewPrefermento, btEdit, btDelete;
    @FXML
    private Label lbStatus, lbPercentajes;
    @FXML
    private HBox hbPorcentajes;
    @FXML
    private VBox vbPercentajes;
    @FXML
    private SearchableComboBox<Prefermento> scPrefermentos;
    @FXML
    private CheckComboBox<Flour> cbFlour;
    @FXML
    private ImageView imgLogo;

    private ImageView informacion;
    private Boolean clickEdit;

    private ObjectProperty<Prefermento> originalPrefermento;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //deshabilitado desde el principio
        cbFlour.setDisable(true);

        //rellenamos el comboBox de los prefermentos con los datos de la base de datos
        rechargeComboBoxPrefermentos();

        //cuando la ventana coge el foco recargamos los datos por si hay algún cambio
        reloadAllData();

        //Añadimos los listener
        addListenerComboPrefermentos();
        addListenerComboFlour();
        addListenerTextFields();

        //ponemos el icono de la interrogación en el hbox
        addIconInformation();

        //Ponemos los botones en el estado inicial
        initStateButtons(true);

        isEditableTextFields(false);

        // para quitar el scroll de la derecha del textArea
        taDescription.setWrapText(true);

        // Inicializamo con sus ajustes el PopOver
        initPoopOver();

        //El estado inicial del lbPercentages para el PopOver deshabilitado
        lbPercentajes.setDisable(true);

        //añadimos las imagenes a los botones
        addImageButtons();

        //añadimos el logo
        ImageUtil.addImageLogo(imgLogo);

        originalPrefermento = new SimpleObjectProperty<>();
        originalPrefermento.bind(scPrefermentos.getSelectionModel().selectedItemProperty());

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
        lbPercentajes.setDisable(true);
        cbFlour.setDisable(true);
        resetStyles();
    }

    /**
     * Actión del boton de para crear un nuevo prefermento.
     * @param event
     */
    @FXML
    void btNewPrefermentoAction(Event event){
        clickEdit = false;
        resetValues();
        initStateButtons(false);
        isEditableTextFields(true);
        lbPercentajes.setDisable(false);
        cbFlour.setDisable(false);
        btEdit.setDisable(true);
        btDelete.setDisable(true);
        tfName.requestFocus();

        rechargeComboBoxFlour();
    }

    /**
     * Acción del botón de editar un prefermento
     * @param event
     */
    @FXML
    void btEditAction(Event event){
        clickEdit = true;
        isEditableTextFields(true);
        btSave.setDisable(false);
        scPrefermentos.setDisable(true);
        btNewPrefermento.setDisable(true);
        btDelete.setDisable(true);
        btEdit.setDisable(true);
        tfName.requestFocus();

        //ponemos de nuevo los datos del ingrediente seleccionado en los campos por si el usuario mientras pincha
        //en editar cambia algo
        rechargeComboBoxFlour(originalPrefermento.get().getListFlour());
        rechargeData(originalPrefermento.get());
        cbFlour.setDisable(false);
        editTfPercentageFlour();
        checkFloursPrefermento();
    }

    /**
     * Actión del botón de borrar un prefermento. Comprobamos que no es el prefermento genérico, sacamos un mensaje de
     * confirmación, si acepta lo borramos y si no nada.
     * @param event
     */
    @FXML
    void btDeleteAction(Event event){
        Prefermento prefermento = scPrefermentos.getSelectionModel().getSelectedItem();
        boolean confirmation = false;

        if (prefermento.getName().equals("Prefermento generico")){
            MyAlert("El prefermento genérico no se puede borrar", TypeAlert.WARNING);

        } else if(prefermento.getName().equals("Prefermento masa madre")) {
            MyAlert("El prefermento masa madre no se puede borrar", TypeAlert.WARNING);

        } else {
            confirmation = MyAlertConfirmation("¿Seguro que quiere eliminar este prefermento?\n" + prefermento);
        }

        if (confirmation){
            try {
                prefermento.setIdIngredient(App.getPrefermentoDao().getIdPrefermento(prefermento.getName()));
                boolean delete = App.getPrefermentoDao().deletePrefermento(prefermento);

                if (!delete) {
                    MyAlert("No se pudo borrar el prefermento, revise su conexión a la base de datos", TypeAlert.WARNING);
                    return;
                }

                //Abrimos un alert que muestra que el ingrediente se ha borrado con exito
                MyAlert("¡¡ Prefermento borrado con éxito !!", TypeAlert.INFORMATION);

                resetValues();
                initStateButtons(true);
                isEditableTextFields(false);
                cbFlour.setDisable(true);
                scPrefermentos.getSelectionModel().clearSelection();
                rechargeComboBoxPrefermentos();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                MyAlertDataBase();
            }
        }
    }

    /**
     * Actión del botón de guardar. Comprobamos que todos los campos esten correctos acorde a los valores que pueden
     * aceptar y si es así guardamos el prefermento
     * @param event
     */
    @FXML
    void btSaveAction(Event event) {
        boolean allFieldsCorrect = isAllFieldsCorrect();

        if (!allFieldsCorrect) {
            MyAlert("Tiene campos obligatorios", TypeAlert.WARNING);

        } else if (taDescription.getText().length() > 500) {
            MyAlert("El campo observaciones no puede superar los 500 caracteres\n" +
                    "Actualmente en uso: " + taDescription.getText().length() + " caracteres.", TypeAlert.WARNING);

            //si es 1 pasamos directamente porque le damos ya por defecto el valor de 100
        } else if (!isSumFloursEquals100()) {
            addStylesPercentages();
            MyAlert("La suma de los valores de los porcentajes de las harinas del prefemento\n" +
                    "tiene que ser 100", TypeAlert.WARNING);

        } else {
            saveIngredient();
        }
    }

    /**
     * Comprueba cuando pinchamos en el botón de guardar si está editando o creando un nuevo prefermento. En
     * función de la opción comprueba por ejemplo si ya existe el prefermento, si es el genérico etc.
     */
    private void saveIngredient(){
        try {
            // si no ha pulsado en editar, es que va a dar de alta un nuevo prefermento y comprobamos si existe
            if (App.getPrefermentoDao().existPrefermento(getPrefermento().getName(), true) && !clickEdit){
                MyAlert("Ya existe un prefermento con ese nombre", TypeAlert.WARNING);

            } else {
                boolean election = true;

                if (clickEdit){
                    // si no ha cambiado el nombre del prefermento cuando pincha en editar lo modificamos
                    if (originalPrefermento.get().getName().equals(getPrefermento().getName())){
                        boolean update = App.getPrefermentoDao().updatePrefermento(getPrefermentoUpdate());

                        if (!update) {
                            MyAlert("No se pudo modificar el prefermento, revise su conexión con la base de datos",
                                    TypeAlert.WARNING);
                            return;
                        }

                    } else {
                        /*
                        Si cambia el nombre le sacamos una pantalla diciendo que no se puede cambiar que si quiere dar de
                        alta un nuevo prefermento con el nuevo nombre
                         */
                        election = MyAlertConfirmation("El nombre del prefermento no se puede cambiar \n" +
                                " ¿Quiere dar de alta un nuevo prefermento con los datos introducidos?");

                        // si pincha en aceptar y existe ya un prefermento con ese nombre (no se puede crear)
                        if (election && App.getPrefermentoDao().existPrefermento(getPrefermentoUpdate().getName(), true)){
                            MyAlert("Ya existe un prefermento con ese nombre", TypeAlert.WARNING);
                            return;
                        }

                        // si pincha en aceptar y no exite un prefermento con ese nombre lo añadimos a la bd
                        if (election){

                            //comprobamos primero si existe el prefemento inactivo para modificarlo
                            if(App.getPrefermentoDao().existPrefermento(getPrefermento().getName(), false)) {
                                Prefermento prefermento = getPrefermento();
                                prefermento.setIdIngredient(App.getPrefermentoDao().getIdPrefermento(prefermento.getName()));
                                App.getPrefermentoDao().updatePrefermento(prefermento);

                            } else {
                                App.getPrefermentoDao().addPrefermento(getPrefermento());
                            }
                        }
                    }

                    // si entramos en este es porque no ha pinchado en editar y quiere dar de alta uno nuevo
                } else {

                    //miramos si existe el prefermento inactivo y si es asi lo modificamos en vez de crear uno nuevo
                    if(App.getPrefermentoDao().existPrefermento(getPrefermento().getName(), false)) {
                        Prefermento prefermento = getPrefermento();
                        prefermento.setIdIngredient(App.getPrefermentoDao().getIdPrefermento(prefermento.getName()));
                        App.getPrefermentoDao().updatePrefermento(prefermento);

                    } else {
                        App.getPrefermentoDao().addPrefermento(getPrefermento());
                    }
                }

                // si no ha cambiado el nombre, o si lo ha cambiado y quiere crear uno nuevo
                if (election){
                    //Abrimos un alert que muestra que el ingrediente se ha guardado con exito
                    MyAlert("¡¡ Prefermento guardado con éxito !!", TypeAlert.INFORMATION);

                    resetValues();
                    initStateButtons(true);
                    isEditableTextFields(false);
                    cbFlour.setDisable(true);
                    scPrefermentos.getSelectionModel().clearSelection();
                    rechargeComboBoxPrefermentos();
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
     * @return new Prefermento
     */
    private Prefermento getPrefermento(){
        String name = tfName.getText().trim();
        name = ImputUtil.cleanText(name);

        //Aquí cojemos las harinas y el porcentaje de cada una del Vbox
        ObservableList<FlourPrefermento> flourPrefermentos = getFloursPrefermento();

        int percentage = Integer.parseInt(tfWatter.getText());

        String description = taDescription.getText();
        description = ImputUtil.cleanText(description);
        description = StringUtils.defaultIfEmpty(description, "Sin descripcion");

        String strYeast = StringUtils.replaceChars(tfYeast.getText(), ",", "."); //para que también valga la coma
        float yeast = Float.parseFloat(strYeast);

        return new Prefermento(name, percentage, description, yeast, flourPrefermentos);
    }

    /**
     * Método que utilizo cuando está editando un prefermento. Recojo los datos con el método getPrefermento pero el id
     * del prefermento tiene que ser el mismo del prefermento que estamos editando. Ese id lo cogemos del ObjecProperty.
     * @return new Prefermento editado pero con el mismo id
     */
    private Prefermento getPrefermentoUpdate(){
        Prefermento prefermento = getPrefermento();
        prefermento.setIdIngredient(originalPrefermento.get().getIdIngredient());

        return prefermento;
    }

    /**
     * Comprobamos que todos los campos obligatorios son correctos
     * @return true o false si los campos son correctos
     */
    private boolean isAllFieldsCorrect(){
        boolean isAllFieldsCorrect = true;
        if (isNotValidTfName(tfName)) isAllFieldsCorrect = false;
        if (isNotValidCheckComboBox(cbFlour)) isAllFieldsCorrect = false;
        if (isNotValidTfYeast(tfYeast)) isAllFieldsCorrect = false;
        if (isNotValidTfWatter(tfWatter)) isAllFieldsCorrect = false;
        if (isNotValidTfPercentajes(vbPercentajes)){
            addStylesPercentages();
            return false;
        }

        return isAllFieldsCorrect;
    }

    /**
     * Recarga todos los prefermentos del comboBox de los prefermentos en función de lo que haya en la BBDD.
     */
    private void rechargeComboBoxPrefermentos(){
        try {
            scPrefermentos.setItems(App.getPrefermentoDao().getAllPrefermentos(true));
        }catch (SQLException sqle){
            sqle.printStackTrace();
            MyAlertDataBase();
        }
    }

    /**
     * Cambia los datos de los campos de texto por los del prefermento que le
     * pasemos por parámetro. Lo utilizamos cuando pinchamos en el ComboBox de los prefermentos
     * que tenemos para que vaya mostrando los datos de cada uno
     * @param prefermento prefermento que queremos mostrar en los campos
     */
    private void rechargeData(Prefermento prefermento){
        tfName.setText(prefermento.getName());
        lbPercentajes.setDisable(false);
        cbFlour.setDisable(true);

        vbPercentajes.getChildren().clear();
        prefermento.getListFlour().forEach(flourPrefermento -> {
            TextField textField = new TextField(String.valueOf(flourPrefermento.getPercentage()));
            textField.setPrefSize(40, 25);
            textField.setEditable(false);

            Label label = new Label(flourPrefermento.getFlour().getName());
            label.setPadding(new Insets(0, 0, 0, 15));

            HBox hBox = new HBox(textField, label);
            hBox.setPadding(new Insets(0, 0, 10, 0));

            vbPercentajes.getChildren().add(hBox);
        });

        tfWatter.setText(String.valueOf(prefermento.getPercentageWatter()));
        tfYeast.setText(String.valueOf(prefermento.getYeast()));
        taDescription.setText(prefermento.getDescription());
    }

    /**
     * Recarga todos las harinas del comboBox de las harinas en función de lo que haya en la BBDD.
     */
    private void rechargeComboBoxFlour(){
        try {
            cbFlour.getItems().clear();
            cbFlour.getItems().setAll(App.getFlourDao().getAllFlours(true));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            MyAlertDataBase();
        }
    }

    /**
     * Recarga todos las harinas del comboBox de las harinas en función de lo que haya en la BBDD.
     */
    private void rechargeComboBoxFlour(List<FlourPrefermento> floursPrefermento){
        try {
            cbFlour.getItems().clear();

            ObservableList<Flour> allflours = App.getFlourDao().getAllFlours(false);
            List<Flour> floursPre = floursPrefermento.stream().map(FlourPrefermento::getFlour).collect(Collectors.toList());

            allflours.forEach(flour -> {
                if(flour.getIsActive() == 0 && floursPre.contains(flour)) {
                    cbFlour.getItems().add(flour);
                } else if(flour.getIsActive() == 1){
                    cbFlour.getItems().add(flour);
                }
            });

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            MyAlertDataBase();
        }
    }

    /**
     * Nos da la posición del último valor que el usuario elige al pinchar sobre el
     * checkComboBox de las harinas. Solo puede elegir 3 opciones y cuando pincha en una cuarta utilizo esta
     * posición para limpiar el check.
     * @param lista1 lista con los tres primeros parámetros escogidos
     * @param lista2 lista con los tres primeros parámetros escogidos más uno que es el que elimino.
     * @return número distinto de las dos listas
     */
    private int lastValue(ObservableList<Integer> lista1, ObservableList<Integer> lista2){
        int number = 0;
        for (Integer integer : lista2) {
            if (!lista1.contains(integer)) {
                number = integer;
            }
        }
        return number;
    }

    /**
     * Acción sobre el comboBox de las harinas.
     */
    private void addListenerComboFlour(){
        ObservableList<Integer> lista1 = FXCollections.observableArrayList();
        ObservableList<Integer> lista2 = FXCollections.observableArrayList();

        cbFlour.getCheckModel().getCheckedItems().addListener((ListChangeListener<Flour>) change -> {
            cbFlour.setStyle(BOX_SHADOW_NONE);

            // cuando ha seleccionado el tercero usamos set para limpiar la lista y añadimos los que tiene seleccionados
            if (cbFlour.getCheckModel().getCheckedItems().size() == 3) {
                lista1.setAll(cbFlour.getCheckModel().getCheckedIndices());
            }

            // cuando selecciona el cuarto usamos set para limpiar la lista y añadimos los que tiene seleccionados
            if (cbFlour.getCheckModel().getCheckedItems().size() > 3){
                lista2.setAll(cbFlour.getCheckModel().getCheckedIndices());

                // limpiamos el check del último valor para que solo pueda seleccionar 3
                Platform.runLater( ()-> {
                    cbFlour.getCheckModel().clearCheck(lastValue(lista1,lista2));
                });
            }

            /*
            A partir de aquí es cuando añadimos las cajas de texto y los label que salen cuando
            se va pinchando sobre el checkComboBox
             */
            List<String> namesFlours = cbFlour.getCheckModel().getCheckedItems().stream()
                    .distinct()
                    .map(Ingredient::getName)
                    .collect(Collectors.toList());

            /*
            Una vez que tenemos la lista solo con los nombres de las harinas que va eligiendo el usuario
            primero limpiamos lo que hay en el vBox para que no haya repetidos y luego vamos  añadiendo
            las cajas de texto con sus etiquetas.
             */
            vbPercentajes.getChildren().clear();
            namesFlours.forEach(nameFlour -> {

                TextField textField = new TextField();
                textField.setPrefSize(40, 25);

                Label label = new Label(nameFlour);
                label.setPadding(new Insets(0, 0, 0, 15));

                HBox hBox = new HBox(textField, label);
                hBox.setPadding(new Insets(0, 0, 10, 0));

                vbPercentajes.getChildren().add(hBox);

                //si solo tiene una harina ponemos directamente 100%
                if(namesFlours.size() == 1){
                    textField.setText("100");
                }

            });
            //una vez que añadido  a los TextField le ponemos su Listener
            addListenerTfPercentajes();
        });
    }

    /**
     * Accíones sobre el comboBox de los prefermentos
     */
    private void actionsComboPrefermentos(){
        Prefermento prefermento = scPrefermentos.getSelectionModel().getSelectedItem();
        if (prefermento != null){
            rechargeData(prefermento);
            btNewPrefermento.setDisable(false);
            btSave.setDisable(true);
            btEdit.setDisable(false);
            btDelete.setDisable(false);
        }
    }

    /**
     * Acción cuando pinchamos con el ratón o presionamos una tecla en el comboBox de los prefermentos
     */
    private void addListenerComboPrefermentos(){
        scPrefermentos.setOnAction(event -> {
            actionsComboPrefermentos();
        });

        scPrefermentos.setOnKeyTyped(keyEvent -> {
            actionsComboPrefermentos();
        });
    }

    /**
     * Acciones sobre los textFields de los porcentages de las harinas del prefemento
     */
    private void addListenerTfPercentajes(){
        vbPercentajes.getChildren().stream()
                .map(vb -> (HBox) vb)
                .map(hb -> (TextField) hb.getChildren().get(0))
                .forEach( textField -> {
                    textField.setOnMousePressed(event -> textField.setStyle(BOX_SHADOW_NONE));
                    textField.setOnKeyPressed(keyEvent -> textField.setStyle(BOX_SHADOW_NONE));
                });
    }

    /**
     * Acciones sobre el textField de TfName
     */
    private void addListenerTfName(){
        tfName.setOnMousePressed(mouseEvent -> tfName.setStyle(BOX_SHADOW_NONE));
        tfName.setOnKeyPressed(keyEvent -> tfName.setStyle(BOX_SHADOW_NONE));
    }

    /**
     * Acciones sobre el textField de TfYeast
     */
    private void addListenerTfYeast(){
        tfYeast.setOnMousePressed(mouseEvent -> tfYeast.setStyle(BOX_SHADOW_NONE));
        tfYeast.setOnKeyPressed(keyEvent -> tfYeast.setStyle(BOX_SHADOW_NONE));
    }

    /**
     * Acciones sobre el textField de TfWatter
     */
    private void addListenerTfWatter(){
        tfWatter.setOnMousePressed(mouseEvent -> tfWatter.setStyle(BOX_SHADOW_NONE));
        tfWatter.setOnKeyPressed(keyEvent -> tfWatter.setStyle(BOX_SHADOW_NONE));
    }

    /**
     * Añade las acciones cuando se carga la tab
     */
    private void addListenerTextFields(){
        addListenerTfName();
        addListenerTfWatter();
        addListenerTfYeast();
    }

    /**
     * Estado inicial de todos los botones y el comboBox de prefermentos y harinas.
     * @param init true o false
     */
    private void initStateButtons(Boolean init){
        btNewPrefermento.setDisable(!init);
        btSave.setDisable(init);
        scPrefermentos.setDisable(!init);
        btEdit.setDisable(init);
        btDelete.setDisable(init);
        scPrefermentos.setDisable(!init);
        cbFlour.setDisable(init);
    }

    /**
     * Hace que los campos para recoger datos esten habilitados o deshabilitados dependiendo de lo
     * que le pasemos como parámetro
     * @param editable True o false dependiendo de lo que queramos
     */
    private void isEditableTextFields(Boolean editable){
        tfName.setEditable(editable);
        tfYeast.setEditable(editable);
        tfWatter.setEditable(editable);
        taDescription.setEditable(editable);
    }

    /**
     * Resetea los campos del formulario a su estado inicial.
     */
    private void resetValues(){
        clearAllTextFields(tfName, tfYeast, tfWatter);
        taDescription.clear();

        tfName.setPromptText(writeNameHere);
        taDescription.setPromptText(nonMandatoryField);
        tfYeast.setPromptText(zeroDecimal);
        tfWatter.setPromptText(obligatoryField);

        scPrefermentos.getSelectionModel().clearSelection();
        cbFlour.getCheckModel().clearChecks();
        vbPercentajes.getChildren().clear();
    }

    /**
     * Resetea los estilos css del formulario a su estado inicial.
     */
    private void resetStyles(){
        cbFlour.setStyle(BOX_SHADOW_NONE);
        tfName.setStyle(BOX_SHADOW_NONE);
        tfYeast.setStyle(COLOUR_TEXT_BLACK);
        tfWatter.setStyle(BOX_SHADOW_NONE);
    }

    /**
     * Añade estilos css a los TextFields de los porcentages de las harinas cuando hay algún fallo
     */
    private void addStylesPercentages(){
        vbPercentajes.getChildren().stream()
                .map(vb -> (HBox) vb)
                .forEach( hBox -> hBox.getChildren().get(0).setStyle(BOX_SHADOW_RED));
    }

    /**
     * Suma los porcentages de las harinas del prefermento. Para que el resultado sea correcto
     * la suma de todos ellos tiene que ser 100
     * @return si la suma de los valores de las cajas de texto del prefermento es igusl a 100
     */
    private boolean isSumFloursEquals100(){
        int value = vbPercentajes.getChildren().stream()
                .map(node -> (HBox) node)
                .map(hBox -> (TextField) hBox.getChildren().get(0))
                .map(TextField::getText)
                .filter(s -> !s.isEmpty())
                .mapToInt(Integer::parseInt)
                .sum();

        return value == 100;
    }

    /**
     * Inicializa el PopOver que hay a la derecha del label de Porcentajes de las harinas
     */
    private void initPoopOver(){
        Label label1 = new Label("   Si elije distintos tipos    ");
        Label label2 = new Label("   de harina la suma de los    ");
        Label label3 = new Label("   porcentajes de todas tiene  ");
        Label label4 = new Label("   que ser 100.                ");

        VBox vBox = new VBox(label1, label2, label3, label4);

        PopOver popOver = new PopOver(vBox);

        informacion.setOnMouseEntered(mouseEvent -> {
            popOver.show(informacion, lbPercentajes.getLayoutX()-20);
        });

        informacion.setOnMouseExited(mouseEvent -> {
            popOver.hide();
        });
    }

    /**
     * Recoge los datos de las harinas del prefermento cuando guardamos un nuevo prefermento o lo modificamos
     * @return lista de FlourPrefermento
     */
    private ObservableList<FlourPrefermento> getFloursPrefermento(){
        ObservableList<FlourPrefermento> flourPrefermentos = FXCollections.observableArrayList();

        if (cbFlour.getCheckModel().getCheckedItems().size() == 1){
            FlourPrefermento fp = new FlourPrefermento();
            fp.setFlour(cbFlour.getCheckModel().getCheckedItems().get(0));
            fp.setPercentage(100);
            flourPrefermentos.add(fp);
        } else {
            vbPercentajes.getChildren().stream()
                    .map(vbox-> (HBox) vbox)
                    .forEach(hBox -> {
                        try {
                            FlourPrefermento fp = new FlourPrefermento();
                            TextField textField = (TextField) hBox.getChildren().get(0);
                            Label label = (Label) hBox.getChildren().get(1);

                            Flour flour = App.getFlourDao().getFlour(label.getText());
                            fp.setFlour(flour);

                            fp.setPercentage(Integer.parseInt(textField.getText()));

                            flourPrefermentos.add(fp);
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                            MyAlertDataBase();
                        }
                    });
        }

        return flourPrefermentos;
    }

    /**
     * Hace que los TextFields de los porcentages de las harinas sean editables. Se usa cuando queremos crear
     * un nuevo prefermento o modificar el seleccionado
     */
    private void editTfPercentageFlour(){
        vbPercentajes.getChildren().forEach(vbox ->{
            HBox hBox = (HBox) vbox;
            TextField textField = (TextField) hBox.getChildren().get(0);
            textField.setEditable(true);
        });
    }

    /**
     * Coje la lista de harinas del prefermento y las marca en el combo de las harinas cuando pinchamos en editar
     */
    private void checkFloursPrefermento(){
        Prefermento prefermento = scPrefermentos.getSelectionModel().getSelectedItem();

        //recorremos la lista de floursPrefermento del prefermento y vamos marcando cada uno de ellos el el cbFlour
        prefermento.getListFlour().forEach(flourPrefermento -> {
            cbFlour.getCheckModel().check(flourPrefermento.getFlour());
        });

        //una vez que los tenemos todos marcados recorremos el vbox de los porcentages a ver si encontramos la harina
        // para ponerle su porcentage
        prefermento.getListFlour().forEach(flourPrefermento -> {
            vbPercentajes.getChildren().stream()
                    .map(vbox -> (HBox) vbox)
                    .forEach(hBox -> {
                        Label label = (Label) hBox.getChildren().get(1);
                        //si el texto del Label (nombre de la harina) es el mismo que el que hemos marcado
                        // (la harina del flourPrefermento) añdimos el porcentage al TextField
                        if (label.getText().equals(flourPrefermento.getFlour().getName())){
                            TextField textField = (TextField) hBox.getChildren().get(0);
                            textField.setText(String.valueOf(flourPrefermento.getPercentage()));
                        }
                    });
        });
    }

    /**
     * Añade la imagen a los botones. Le pasamos el botón, la imagen y el tamaño que queramos para el botón.
     */
    private void addImageButtons(){
        addImageButton(btNewPrefermento, imgBtNew, 25);
        addImageButton(btSave, imgBtSave, 25);
        addImageButton(btEdit, imgBtEdit, 25);
        addImageButton(btDelete, imgBtDelete, 25);
        addImageButton(btCancel, imgBtRefresh, 25);
    }

    /**
     * Inicializa el ImageView para el icono de información que tenemos al lado del label de Porcentages de las harinas
     */
    private void addIconInformation(){
        informacion = new ImageView(new Image(App.class.getResource(imgInformation).toExternalForm()));
        informacion.setCursor(Cursor.HAND);
        informacion.setFitWidth(20);
        informacion.setFitHeight(20);
        hbPorcentajes.getChildren().add(informacion);
    }

    /**
     * Vuelve a cargar los datos en los combo cuando la Tab gana el foco. Lo utilizamos porque si mientras
     * que está abierta la Tab el usuario crea por ejemplo una nueva harina que la muestre para poder utilizarla
     */
    private void reloadAllData(){
        App.getController().getTabs().stream()
                .filter(tab -> tab.getId().equals(nameTabPrefermento))
                .forEach(tab -> {
                    tab.setOnSelectionChanged(event -> {
                        if (tab.isSelected()) {
                            resetValues();
                            initStateButtons(true);
                            isEditableTextFields(false);
                            lbPercentajes.setDisable(true);
                            cbFlour.setDisable(true);
                            resetStyles();
                            rechargeComboBoxFlour();
                        }
                    });
                });
    }
}
