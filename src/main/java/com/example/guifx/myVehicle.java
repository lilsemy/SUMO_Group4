package com.example.guifx;



public class myVehicle {

    private String id;
    private String typeId;
    private String routeId;
    private byte laneId;

    private int depart;
    private double pos;
    private double speed;

    // constructors



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

    public String getId(){
        return id;
    }
    public String getTypeId(){
        return typeId;
    }
    public String getRouteId(){
        return routeId;
    }
    public byte getLaneId(){
        return laneId;
    }
    public int getDepart(){
        return depart;
    }
    public double getPos(){
        return pos;
    }
    public double getSpeed(){
        return speed;
    }

    //Setter
    //i am not sure if we need setters for all
    public void setLaneId(byte laneId){
        this.laneId = laneId;
    }
    public void setSpeed(double speed){
        this.speed = speed;
    }





}
