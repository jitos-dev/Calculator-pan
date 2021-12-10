package com.garciajuanjo.calculatorpan.util;

import com.garciajuanjo.calculatorpan.domain.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class MockData {

    public Provider getProviderGeneric(){
        return new Provider("Proveedor generico","Sin direccion", "000000000", "Sin observaciones");
    }

    public Ingredient getIngredientGeneric(){
        ObservableList<Provider> providers = FXCollections.observableArrayList(getProviderGeneric());
        return new Ingredient("Ingrediente generico", "Sin descripcion", providers);
    }

    public Ingredient getIngredientGeneric(String name){
        ObservableList<Provider> providers = FXCollections.observableArrayList(getProviderGeneric());
        return new Ingredient(name, "Sin descripcion", providers);
    }

    public Flour getFlourGeneric(){
        ObservableList<Provider> providers = FXCollections.observableArrayList(getProviderGeneric());
        return new Flour("Harina generica", "Sin descripcion", providers, 9, 250, Flour.TypeFlour.NORMAL);
    }

    public Prefermento getPrefermentoGeneric(){
        FlourPrefermento flourPrefermento = new FlourPrefermento(getFlourGeneric(),100);
        ObservableList<FlourPrefermento> flours = FXCollections.observableArrayList(flourPrefermento);
        return new Prefermento("Prefermento generico", 100, "Sin descripcion", 0.5f, flours);
    }

    public Ingredient getIngredientAgua(){
        return new Ingredient("Agua", "Sin descripcion", getProviderGeneric());
    }

    public Ingredient getIngredientSal(){
        return new Ingredient("Sal","Sin descripcion", getProviderGeneric());
    }

    public Ingredient getIngredientYeast(){
        return new Ingredient("Levadura fresca","Sin descripcion", getProviderGeneric());
    }

    public FlourPrefermento getFlourPrefermento(){
        return new FlourPrefermento(getFlourGeneric(), 100);
    }

    public Prefermento getPrefermentoMasaMadre(){
        return new Prefermento("Prefermento masa madre", 100, "Sin descripcion", 0f, getFlourPrefermento());
    }

    public Recipe getRecipeGeneric(){
        IngredientRecipe ingredientHarina = new IngredientRecipe(getFlourGeneric(), 100);
        IngredientRecipe ingredientMasaMadre = new IngredientRecipe(getPrefermentoMasaMadre(), 20);
        IngredientRecipe ingredientAgua = new IngredientRecipe(getIngredientAgua(), 65);
        IngredientRecipe ingredientSal = new IngredientRecipe(getIngredientSal(), 2);

        return new Recipe("Masa base", "Sin observaciones", ingredientHarina, ingredientMasaMadre, ingredientAgua,
                ingredientSal);
    }

}
