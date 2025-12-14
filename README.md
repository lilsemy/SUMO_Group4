
# Sumo Live Simulation - Project Group 4
This is the Source code of Java Project Group 4 on the Tuesdays Exercises. The following represent the progress till the first Milestone.

---
## How to run the Program:
1) Extract the .zip folder.
2) Inside the extracted folder you find two folders: ".idea" and "SUMO_Group4-main".
3) Open the "SUMO_Group4-main" Folder (the second one), in an IDE of your choice.
4) Run the "Launcher" Class.


### Dependencies:
- JDK 25 or higher
- Sumo Version 1.20 or higher
- "SUMO_HOME" should be set in PATH (on Windows normally done automatically by the installer. Linux instructions: https://sumo.dlr.de/docs/Basics/Basic_Computer_Skills.html)

---
## Further information

The .zip contains also the following files:
- Project Documentation (containing the declaration of authorship)
- Program folder (src, lib, target)

---

# SUMO Traffic Simulation – User Guide

Welcome to the **SUMO Traffic Simulation** application.  
This tool allows you to **visualize**, **control**, and **analyze** a traffic simulation in real time using an interactive map and live statistics dashboard.

---

## 1. Getting Started

When you launch the application:

- The simulation starts **automatically**
- The road network and traffic lights are rendered
- Live traffic statistics begin updating in real time

No manual setup is required after launch.

---

## 2. Map Interaction

You can navigate the simulation map using your mouse:

- **Pan**  
  Click and drag anywhere on the map to move around.

- **Zoom**  
  Use the mouse scroll wheel to zoom in and out.  
  The zoom operation is centered on your mouse pointer for precise navigation.

---

## 3. Control Panel

The control panel is located at the **top-left corner** of the window and provides direct interaction with the simulation.

| Button        | Description |
|--------------|-------------|
| **Insert Car** | Manually spawns a new vehicle into the simulation on a predefined route. |
| **Get Speed** | Retrieves the speed of the last spawned vehicle and displays it in the **Status** area (bottom-right). |
| **Change Phase** | Manually toggles the traffic light phases at the junctions. |
| **Stress Test** | Spawns a batch of **50 vehicles** to test system performance under heavy traffic conditions. |

---

## 4. Statistics & Dashboard

The dashboard on the **right side** of the interface displays real-time metrics:

- **Avg Speed**  
  Shows the global average speed of all vehicles currently in the simulation.

- **Vehicles**  
  Displays the total number of active vehicles.

- **Real-Time Chart**  
  A live graph visualizing the evolution of the average speed over time.

---

## Notes

- All statistics update continuously during the simulation
- The stress test is useful for evaluating performance and scalability
- User interactions immediately affect the running simulation

---






