package com.example.guifx;

import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.cmd.Edge;
import de.tudresden.sumo.cmd.Simulation;
import it.polito.appeal.traci.SumoTraciConnection;

import java.io.IOException;
import java.util.*;


    /**
    *Statistik is a class to calculate relevant metrics over the simulation
    */

public class Statistik {
    private List<String> vehicleIDs;

    public void setVehicleIds(TraciConnect conn, VehiclesMangagement vehiclesManager) throws Exception {

        List<String> vehicleIDs = vehiclesManager.getIds(); //gibt Liste mit allen IDs der sich in Simulation befindlichen Fahrzeuge
        this.vehicleIDs=vehicleIDs;
    }

        /**
         *
         * @return Number of vehicles in Simulation (int)
         */
    public int getCars() {
            //Count the Cars
            return vehicleIDs.size();
    }


//    public void getStatistik(TraciConnect conn) {
//
//
//                try {
//                    VehiclesMangagement vm = new VehiclesMangagement(conn);
//
//                    // Hashmap für Abfahrtszeiten (speichert pro Fahrzeug-ID <string> die Zeit <double> -> um Reisezeit zu berechnen
//                    Map<String, Double> vehicleDepartureTimes = new HashMap<>();
//
//                        double totalSpeed = 0;
//                        for (String vehID : vehicleIDs) {
//                            double speed = (double) conn.do_job_get(Vehicle.getSpeed(vehID)); //Für jedes Vehicle wird Geschwindigkeit [m/s] geholt und auf (double) gecastet
//                            totalSpeed += speed; //Geschwindigkeiten aufsummieren
//                            // Abfahrtszeit merken (falls neu)
//                            vehicleDepartureTimes.putIfAbsent(vehID, (double) conn.do_job_get(Simulation.getTime())); // gibt aktuelle Simulationszeit zurück [s]
//                        }
//                        double averageSpeed = vehicleIDs.isEmpty() ? 0 : totalSpeed / vehicleIDs.size(); // Ternärer Operator zum Schutz vor Division durch Null (falls vehicleIDs.isEmpty()=1)
//                        System.out.println("Average speed: " + averageSpeed);
//
//                        // 2. Fahrzeugdichte pro Kante
//                        List<String> edgeIDs = (List<String>) conn.do_job_get(Edge.getIDList()); //Liste aller Kanten-IDs (Edges) der Netzwerktopologie
//                        for (String edgeID : edgeIDs) {
//                            if (edgeID.startsWith(":"))
//                                continue; // interne Kanten (starten mit ":") werden übersprungen
//                            int numVehicles = (int) conn.do_job_get(Edge.getLastStepVehicleNumber(edgeID)); //liefert Anzahl Fahrzeuge, die auf dieser Kante im letzten Zeitschritt gemessen wurden cast (int)
//                            System.out.println("Edge " + edgeID + " - Vehicles: " + numVehicles); // Ausgabe Anzahl Fahrzeuge je Kante
//                        }
//
//                        // 3. Stau-Erkennung (Hotspots) ab Geschwindigkeit weniger als 2.0 (willkürlich gewählt)
//                        for (String edgeID : edgeIDs) {
//                            double edgeSpeed = (double) conn.do_job_get(Edge.getLastStepMeanSpeed(edgeID)); //mittlere Geschwindigkeit aller Fahrzeuge auf dieser Kante im letzten Schritt [m/s]
//                            if (edgeSpeed < 2.0) { //wenn Mittelgeschwindigkeit < 2 m/s -> Stau
//                                System.out.println("Congestion detected on edge " + edgeID);
//                            }
//                        }
//
//                        // 4. Reisezeiten (wenn Fahrzeuge verschwinden)
//                        vehicleDepartureTimes.keySet().removeIf(vehID -> { // Lambda -> iteriert über alle gepseicherten VehIDs und entfernt jene, für die true (einmal Reisezeit ermittelt und interne Aufzeichnung gelöscht)
//                            if (!vehicleIDs.contains(vehID)) { //wenn ID nicht mehr in vehIDs ist -> Fahrzeug aus Simulation verschwunden
//                                try {
//                                    double arrivalTime = (double) conn.do_job_get(Simulation.getTime()); // aktuelle Zeit der Simulation als Ankuftszeit
//                                    double travelTime = arrivalTime - vehicleDepartureTimes.get(vehID); // berechnet Reisezeit als Traveltime = arrivalTime - Abfahrtszeit
//                                    System.out.println("Vehicle " + vehID + " travel time: " + travelTime + " s"); //Ausgabe Traveltime
//                                    return true;
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                            return false;
//                        });
//
//
//
//                } catch (IOException | InterruptedException e) {
//                    e.printStackTrace();
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            }


    }




        /*
TODO:
- Statistikberechnung von push auf pull seitens des GUIs, da sonst zu sehr performance belastend
- Daten als Stream in CSV / PDF schreiben (charts, metrics and timestamps + including filters for car colors, edges etc.)

    */

