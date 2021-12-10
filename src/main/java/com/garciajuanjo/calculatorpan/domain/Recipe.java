package com.garciajuanjo.calculatorpan.domain;

import javafx.collections.ObservableList;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Data
public final class Recipe {

    private int idRecipe;
    private String name;
    private String observations;
    private List<IngredientRecipe> ingredients;
    private int isActive;

    public Recipe(){
        ingredients = new ArrayList<>();
        this.isActive = 1;
    }

    public Recipe(String name, String observations){
        this.name = name;
        this.observations = observations;
        ingredients = new ArrayList<>();
        this.isActive = 1;
    }

    public Recipe(String name, String observations, ObservableList<IngredientRecipe> ingredients){
        this.name = name;
        this.observations = observations;
        this.ingredients = ingredients;
        this.isActive = 1;
    }

    public Recipe(String name, String observations, IngredientRecipe... ingredients){
        this.name = name;
        this.observations = observations;
        this.ingredients = new ArrayList<>(Arrays.asList(ingredients));
        this.isActive = 1;
    }

    public Recipe(int idRecipe, String name, String observations, ObservableList<IngredientRecipe> ingredients){
        this.idRecipe = idRecipe;
        this.name = name;
        this.observations = observations;
        this.ingredients = ingredients;
        this.isActive = 1;
    }

    @Override
    public String toString(){
        return name;
    }

    @Override
    public boolean equals(Object o){
        if (o == null) return false;
        if (!(o instanceof Recipe)) return false;

        Recipe recipe = (Recipe) o;
        return name.equals(recipe.getName());
    }

    @Override
    public int hashCode(){
        return Objects.hashCode(name);
    }

}