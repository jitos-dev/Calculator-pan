package com.garciajuanjo.calculatorpan.domain;

import javafx.collections.ObservableList;
import lombok.Data;

import java.util.Objects;

@Data
public final class Flour extends Ingredient {

    private float protein;
    private int strength;
    private TypeFlour typeFlour;

    public enum TypeFlour {
        NORMAL, INTEGRAL, T80
    }

    public Flour(){
        this.typeIngredient = TypeIngredient.HARINA;
    }

    public Flour(String name, String description, float protein, int strength,
                 TypeFlour typeFlour) {
        super(name, description);
        this.protein = protein;
        this.strength = strength;
        this.typeFlour = typeFlour;
        this.typeIngredient = TypeIngredient.HARINA;
    }

    public Flour(String name, String description, ObservableList<Provider> providers, float protein, int strength,
                 TypeFlour typeFlour) {
        super(name, description, providers);
        this.protein = protein;
        this.strength = strength;
        this.typeFlour = typeFlour;
        this.typeIngredient = TypeIngredient.HARINA;
    }

    @Override
    public String toString(){
        return getName();
    }

    @Override
    public boolean equals(Object o){
        if (o == null) return false;
        if (!(o instanceof Flour)) return false;

        Flour flour = (Flour) o;
        return flour.getName().equals(getName());
    }

    @Override
    public int hashCode(){
        return Objects.hash(getName());
    }

}
