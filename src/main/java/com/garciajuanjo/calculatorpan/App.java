package com.garciajuanjo.calculatorpan;

import com.garciajuanjo.calculatorpan.controller.AppController;
import com.garciajuanjo.calculatorpan.dao.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.fxmisc.cssfx.CSSFX;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.io.FileReader;
import java.util.Properties;

import static com.garciajuanjo.calculatorpan.util.Constant.*;

public class App extends Application {

    public static void main(String[] args) { launch();
    }

    private static FlourDao flourDao;
    private static ProviderDao providerDao;
    private static PrefermentoDao prefermentoDao;
    private static IngredientDao ingredientDao;
    private static RecipeDao recipeDao;
    private static AppDao appDao;
    private static Parent parent;
    private static AppController controller;
    private static Properties properties;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(App.class.getResource(viewApp));
        parent = loader.load();
        controller = loader.getController();

        Scene scene = new Scene(parent);

        //añadimos la hoja de estilos css nuestra y la de bostrap a la scene
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        scene.getStylesheets().add(App.class.getResource(file_css).toString());
        stage.setScene(scene);

        //Para que no se pueda maximizar ni cambiar de tamaño la ventana
        stage.setResizable(false);

        //icono de la barra de título
        stage.getIcons().add(new Image(String.valueOf(App.class.getResource(imgFavicon))));
        stage.setTitle(nameAplicationAndVersion);

        stage.show();

        CSSFX.start();
    }

    @Override
    public void init() throws Exception {
        super.init();
        appDao = new AppDao();
        flourDao = new FlourDao();
        providerDao = new ProviderDao();
        prefermentoDao = new PrefermentoDao();
        ingredientDao = new IngredientDao();
        recipeDao = new RecipeDao();
        appDao.conect();
        properties = new Properties();
        properties.load(new FileReader(file_properties));
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        appDao.close();
    }

    public static FlourDao getFlourDao(){
        return flourDao;
    }

    public static ProviderDao getProviderDao(){
        return providerDao;
    }

    public static PrefermentoDao getPrefermentoDao() { return prefermentoDao; }

    public static IngredientDao getIngredientDao() { return ingredientDao; }

    public static RecipeDao getRecipeDao() { return recipeDao; }

    public static AppDao getAppDao() { return appDao; }

    public static Parent getParent() { return parent; }

    public static AppController getController() { return controller; }

    public static Properties getProperties() { return properties; }

    public static String getProperty(String name){
        return properties.getProperty(name);
    }

}
