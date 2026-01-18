package com.example.guifx;

import java.util.EnumSet;

/**
 *
 */
public enum VehicleColor {

    NONE,
    BLACK,
    WHITE,
    RED,
    YELLOW;

    //Returns every value, but NONE.
    //Needed for spawning, as both enum classes are used for filtering AND spawning simultaneously
    public static EnumSet<VehicleColor> spawnableAll(){
        return EnumSet.complementOf(EnumSet.of(VehicleColor.NONE));
    }
}
