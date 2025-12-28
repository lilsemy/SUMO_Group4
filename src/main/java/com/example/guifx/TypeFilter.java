package com.example.guifx;

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
}
