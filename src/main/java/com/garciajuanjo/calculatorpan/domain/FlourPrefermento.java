package com.garciajuanjo.calculatorpan.domain;

import lombok.Data;
import java.util.Objects;

@Data
public final class FlourPrefermento {

    private Flour flour;
    private int percentage;
    private int isActive;

    public FlourPrefermento() {
    }

    public FlourPrefermento(Flour flour, int percentage){
        this.flour = flour;
        this.percentage = percentage;
        this.isActive = 1;
    }

    @Override
    public String toString(){
        return flour.getName().concat(" ").concat(String.valueOf(percentage));
    }

    @Override
    public boolean equals(Object o){
        if (o == null) return false;
        if (!(o instanceof FlourPrefermento)) return false;

        FlourPrefermento flourPrefermento = (FlourPrefermento)o;
        return flour.equals(flourPrefermento.flour) && percentage == flourPrefermento.percentage;
    }

    @Override
    public int hashCode(){
        return Objects.hash(flour, percentage);
    }

}
