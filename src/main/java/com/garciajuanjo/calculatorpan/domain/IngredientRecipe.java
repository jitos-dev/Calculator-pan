package com.garciajuanjo.calculatorpan.domain;
import lombok.Data;

import java.util.Objects;

@Data
public final class IngredientRecipe implements Comparable<IngredientRecipe>{

    private Ingredient ingredient;
    private float percentage;

    public IngredientRecipe(){

    }

    public IngredientRecipe(Ingredient ingredient, float percentage) {
        this.ingredient = ingredient;
        this.percentage = percentage;
    }

    @Override
    public String toString(){
        return ingredient.getName().concat(" ").concat(String.valueOf(percentage).concat(" %"));
    }

    @Override
    public boolean equals(Object o){
        if (o == null) return false;
        if (!(o instanceof FlourPrefermento)) return false;

        IngredientRecipe ingredientRecipe = (IngredientRecipe) o;
        return ingredient.equals(ingredientRecipe.ingredient) && percentage == ingredientRecipe.percentage;
    }

    @Override
    public int hashCode(){
        return Objects.hash(ingredient, percentage);
    }


    @Override
    public int compareTo(IngredientRecipe o) {
        int number;
        number = this.ingredient.getTypeIngredient().ordinal() - o.ingredient.getTypeIngredient().ordinal();

        //si son iguales ordenamos por orden alfabético
        if (number == 0){
            number = this.getIngredient().toString().compareTo(o.getIngredient().toString());
        }
        return number;
    }
}
