package com.example.guifx;

    /**
    *myVehicle is a
    */

public class myVehicle {

    private String id;
    private String typeId;
    private String routeId;
    private byte laneId;

    private int depart;
    private double pos;
    private double speed;

    // constructors


    /**
    *@param id, typeId, routeId, laneId, depart, pos, speed
    */
    
    public myVehicle(String id, String typeId, String routeId, byte laneId, int depart, double pos, double speed){
        this.id =  id;
        this.typeId = typeId;
        this.routeId = routeId;
        this.laneId = laneId;
        this.depart = depart;
        this.pos = pos;
        this.speed = speed;
    }

    // default constructor for depart, pos, speed
    
    /**
    *@param id, typeId, routeId, laneId
    */
    public myVehicle(String id, String typeId, String routeId, byte laneId ){
        this.id =  id;
        this.typeId = typeId;
        this.routeId = routeId;
        this.laneId = laneId;
        this.depart = 0;
        this.pos = 0.0;
        this.speed = 1.0;
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
    *@return depart
    */
    public int getDepart(){
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





}
