package com.example.guifx;

import java.util.EnumSet;

public enum TypeFilter {

    NONE(""),
    CAR("car"),
    TRUCK("truck"),
    BUS("bus");

    private final String typeId;

    TypeFilter(String typeId) {
        this.typeId = typeId;
    }

    public String getTypeId() {
        return typeId;
    }

    /*Returns every value, but NONE.
    Needed for spawning, as both enum classes are used for filtering AND spawning simultaneously*/
    public static EnumSet<TypeFilter> spawnableAll(){
        return EnumSet.complementOf(EnumSet.of(TypeFilter.NONE));
    }
}
