package com.example.guifx;


/**
 * VehicleUIState is a Record that holds the drawing data for ONE vehicle.
 * We named it "VehicleUIState" because "VehicleState" is already used by an
 * enum in this project.
 */
public record VehicleUiState (String id,double x, double y , double angle, String type, VehicleColor color){

}
