package com.garciajuanjo.calculatorpan.domain;

import javafx.collections.ObservableList;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
public final class Prefermento extends Ingredient {

    private float yeast;
    private int percentageWatter;
    private List<FlourPrefermento> listFlour;

    public Prefermento(){
        this.typeIngredient = TypeIngredient.PREFERMENTO;
        this.providers = null;
        this.listFlour = new ArrayList<>();
    }

    public Prefermento(String name, int percentageWatter, String description, float yeast){
        super(name, description);
        this.percentageWatter = percentageWatter;
        this.yeast = yeast;
        this.typeIngredient = TypeIngredient.PREFERMENTO;
        this.providers = null;
        this.listFlour = new ArrayList<>();
    }

    public Prefermento(String name, int percentageWatter, String description, float yeast,
                       FlourPrefermento flourPrefermento){
        super(name, description);
        this.percentageWatter = percentageWatter;
        this.yeast = yeast;
        this.typeIngredient = TypeIngredient.PREFERMENTO;
        this.providers = null;
        this.listFlour = new ArrayList<>();
        this.listFlour.add(flourPrefermento);
    }

    public Prefermento(String name, int percentageWatter, String description, float yeast,
                       ObservableList<FlourPrefermento> listFlour){
        super(name, description);
        this.percentageWatter = percentageWatter;
        this.yeast = yeast;
        this.typeIngredient = TypeIngredient.PREFERMENTO;
        this.providers = null;
        this.listFlour = listFlour;
    }

    @Override
    public String toString(){
        return getName();
    }

    @Override
    public boolean equals(Object o){
        if (o == null) return false;
        if (!(o instanceof Prefermento)) return false;

        Prefermento prefermento = (Prefermento) o;

        return prefermento.getName().equals(getName());
    }

    @Override
    public int hashCode(){
        return Objects.hash(getName());
    }
}
