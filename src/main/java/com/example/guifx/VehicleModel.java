package com.example.guifx;

    /**
    *VehicleState is a model class for Vehicles in the Simulation
    */
//Renamed "myVehicle", since it only stores states, it is a much more fitting name
public class VehicleModel {

    private double x;
    private double y;
    private double angle;

    private String id;
    private String typeId;
    private String routeId;
    private byte laneId;

    private double depart;
    private double pos;
    private double speed;

    // constructors


    /**
    *@param id, typeId, routeId, laneId, depart, pos, speed
    */
    
    public VehicleModel(String id, String typeId, String routeId, byte laneId, int depart, double pos, double speed){
        this.id =  id;
        this.typeId = typeId;
        this.routeId = routeId;
        this.laneId = laneId;
        this.depart = depart;
        this.pos = pos;
        this.speed = speed;

        this.x = 0.0;
        this.y = 0.0;
        this.angle = 0.0;
    }

    // default constructor for depart, pos, speed
    
    /**
    *@param id, typeId, routeId, laneId
    */
    public VehicleModel(String id, String typeId, String routeId, byte laneId, double depart ){
        this.id =  id;
        this.typeId = typeId;
        this.routeId = routeId;
        this.laneId = laneId;
        this.depart = depart;
        this.pos = 0.0;
        this.speed = 1.0;

        this.x = 0.0;
        this.y = 0.0;
        this.angle = 0.0;
    }

        public VehicleModel(String id) {
            this.id = id;
            this.typeId = "car";
            this.routeId = "";
            this.laneId = -1;
            this.depart = 0;
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
    public byte getLaneId(){
        return laneId;
    }
    
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
    //Setter
    //i am not sure if we need setters for all
    
    /**
    *@param laneId
    */
    public void setLaneId(byte laneId){
        this.laneId = laneId;
    }
    
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
}
