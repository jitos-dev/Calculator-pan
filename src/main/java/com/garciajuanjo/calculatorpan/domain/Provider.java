package com.garciajuanjo.calculatorpan.domain;

import javafx.collections.ObservableList;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
public final class Provider {

    private int idProvider;
    private String name;
    private String direction;
    private String phone;
    private String observations;
    private List<Ingredient> ingredients;
    private int isActive;

    public Provider(){
        ingredients = new ArrayList<>();
        this.isActive = 1;
    }

    public Provider(String name, String direction, String phone, String observations) {
        this.name = name;
        this.direction = direction;
        this.phone = phone;
        this.observations = observations;
        ingredients = new ArrayList<>();
        this.isActive = 1;
    }

    public Provider(String name, String direction, String phone, String observations, ObservableList<Ingredient> ingredients) {
        this.name = name;
        this.direction = direction;
        this.phone = phone;
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
        if (!(o instanceof Provider)) return false;

        Provider provider = (Provider) o;
        return name.equals(provider.getName());
    }

    @Override
    public int hashCode(){
        return Objects.hash(name);
    }

}
