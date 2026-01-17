package com.example.guifx;

    /**
    *VehicleState is a model class for Vehicles in the Simulation
    */
public class VehicleModel {

    private double x;
    private double y;
    private double angle;

    private String id;
    private String typeId;
    private String routeId;
    private String laneId;
    private VehicleColor color;
    private VehicleState state;
    private double depart;
    private double pos;
    private double speed;

    
    /**
    *@param id, typeId, routeId, laneId
    */
    public VehicleModel(String id, String typeId, String routeId, String laneId, double depart, VehicleColor color) {
        this.id = id;
        this.typeId = typeId;
        this.routeId = routeId;
        this.laneId = laneId;
        this.depart = depart;
        this.color = color;
        state = VehicleState.QUEUED;
        this.pos = 0.0;
        this.speed = 1.0;
        this.x = 0.0;
        this.y = 0.0;
        this.angle = 0.0;
    }

        //Getter

    /**
    *@return id of car
    */
    public String getId(){
        return id;
    }
    
    /**
    *@return typeId of car
    */
    public String getTypeId(){
        return typeId;
    }

    
    /**
    *@return routeId
    */
    public String getRouteId(){
        return routeId;
    }
    
    /**
    *@return laneId
    */
    public String getLaneId(){ return laneId; }
    
    /**
    *@return Time of Departure of the Vehicle
    */
    public double getDepart(){
        return depart;
    }
    
    /**
    *@return pos of car
    */
    public double getPos(){
        return pos;
    }
    
    /**
    *@return speed of car
    */
    public double getSpeed(){
        return speed;
    }

    public double getX(){
        return x;
    }

    public double getY(){
        return y;

    }
    public double getAngle(){
        return angle;
    }

    public VehicleColor getColor() {
        return color;
    }

    public VehicleState getState(){
        return state;
    }


        /**
    *@param laneId
    */
    public void setLaneId(String laneId){ this.laneId = laneId; }
    
    /**
    *@param speed
    */
    public void setSpeed(double speed){
        this.speed = speed;
    }

    public void setPosition(double x, double y){
        this.x =x;
        this.y = y;
    }
    public void setAngle(double angle){
        this.angle = angle;
    }
    public void setTypeId(String typeId){
        this.typeId=typeId;
    }

    public void setRouteId(String routeId){
        this.routeId = routeId;
    }

    public void setColor(VehicleColor color) {
        this.color = color;
    }

    public void setState(VehicleState state) {
        this.state = state;
    }


}
