package com.garciajuanjo.calculatorpan.domain;

import javafx.collections.ObservableList;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
public class Ingredient implements Comparable<Ingredient> {

    public enum TypeIngredient {
        PREFERMENTO, HARINA, OTRO
    }

    private int idIngredient;
    private String name;
    private String description;
    protected TypeIngredient typeIngredient;
    protected List<Provider> providers;
    private int isActive; //1-activo 0-no activo

    public Ingredient(){
        this.typeIngredient = TypeIngredient.OTRO;
        this.providers = new ArrayList<>();
        this.isActive = 1;
    }

    public Ingredient(String name, String description){
        this.name = name;
        this.description = description;
        this.typeIngredient = TypeIngredient.OTRO;
        this.providers = new ArrayList<>();
        this.isActive = 1;
    }

    public Ingredient(String name, String description, Provider provider){
        this.name = name;
        this.description = description;
        this.typeIngredient = TypeIngredient.OTRO;
        this.providers = new ArrayList<>();
        this.providers.add(provider);
        this.isActive = 1;
    }

    public Ingredient(String name, String description, ObservableList<Provider> providers){
        this.name = name;
        this.description = description;
        this.typeIngredient = TypeIngredient.OTRO;
        this.providers = providers;
        this.isActive = 1;
    }

    public Ingredient(int idIngredient, String name, String description, ObservableList<Provider> providers){
        this.idIngredient = idIngredient;
        this.name = name;
        this.description = description;
        this.typeIngredient = TypeIngredient.OTRO;
        this.providers = providers;
        this.isActive = 1;
    }

    @Override
    public String toString(){
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof Ingredient)) return false;

        Ingredient ingredient = (Ingredient) o;
        return this.name.equals(ingredient.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }



    //los ordeno por la enumeración para cuando los listo en el listView de las recetas me salgan ordenados
    @Override
    public int compareTo(Ingredient o) {
        int number;
        number = this.getTypeIngredient().ordinal() - o.getTypeIngredient().ordinal();

        //si son iguales ordenamos por orden alfabético
        if (number == 0){
            number = this.toString().compareTo(o.toString());
        }
        return number;
    }

}
