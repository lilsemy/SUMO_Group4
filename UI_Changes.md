# UI_Changes – JavaFX UI Refactoring Guide

This document describes **all UI-related changes** applied to the simulation project. It is intended to help team members **quickly understand the new layout, design rationale, and technical constraints**, especially regarding `fx:id` preservation to avoid breaking the Java controller logic.

---

## 1. Overall Objectives

### Main Goals

* Clean, professional UI that **does not overlap** on small screens.
* Controls grouped **vertically on the left** for easy interaction.
* Map placed **at the center**, always visible and unobstructed.
* Statistics, charts, and console combined into a **single horizontal bottom bar**.
* **Scroll support** for both sidebar and map.
* Simple **dark mode theme** (no gradients, no flashy colors).
* **Map container remains light** for clarity and contrast.

---

## 2. Changes in `GUI-view.fxml`

### 2.1. Global Layout Structure

The UI is divided into **three major regions**:

1. **Left Sidebar** – all control elements
2. **Center Map** – Live Simulation view
3. **Bottom Stats Bar** – statistics, charts, and console

```
+--------------------------------------------------+
| Sidebar |              Map                      |
|         |                                       |
|         |                                       |
+--------------------------------------------------+
|        Statistics | Charts | Console             |
+--------------------------------------------------+
```

---

### 2.2. Left Sidebar (Controls)

**Structure**:

* `ScrollPane`

  * `VBox`

**Contents**:

* Insert / Follow vehicle controls
* Get speed
* Filters
* Traffic light controls
* Load test and simulation options

**Design Rationale**:

* Large number of controls → vertical layout + scrolling
* Left placement allows quick access without covering the map

**Key Configuration**:

* `fitToWidth="true"`
* `hbarPolicy="NEVER"`
* `vbarPolicy="AS_NEEDED"`
* `AnchorPane.bottomAnchor="200.0"`

  * Reserves space for the bottom bar
  * Prevents overlap with statistics

---

### 2.3. Center Map (Live Simulation)

**Structure**:

* `ScrollPane`

  * `VBox`

    * `StackPane (fx:id="mapContainer")`

**Inside `mapContainer`**:

* `backgroundCanvas`
* `laneLayer`
* `trafficLightLayer`
* `carLayer`

**ScrollPane Configuration**:

* `fitToWidth="true"`
* `fitToHeight="true"`
* `pannable="true"`
* `AnchorPane.bottomAnchor="200.0"`

**Design Rationale**:

* Supports large maps via pan/scroll
* Ensures map is never covered by the bottom stats bar
* `StackPane` preserves correct rendering order of layers

---

### 2.4. Bottom Stats Bar

**Structure**:

* `HBox`

**Contents**:

* Statistics section
* Speed chart
* Travel time chart
* Console output

**Traffic Light Durations**:

* `Tl1Dur` and `Tl2Dur` are **moved here**
* Displayed alongside:

  * `vehicleCountLabel`
  * `avgSpeedLabel`

**Design Rationale**:

* Status information belongs at the bottom
* Frees vertical space for the map

---

### 2.5. fx:id Preservation (Critical)

**Do NOT rename the following fx:id values**, otherwise the Java controller bindings and updates will break.

**Map & Layers**

* `mapContainer`
* `backgroundCanvas`
* `laneLayer`
* `trafficLightLayer`
* `carLayer`

**Statistics & UI Labels**

* `vehicleCountLabel`
* `avgSpeedLabel`
* `Tl1Dur`
* `Tl2Dur`

**Charts & Console**

* `speedChart`
* `travelTimeChart`
* `consoleArea`

---

## 3. Changes in `simulation-style.css`

### 3.1. Theme Objectives

* Simple, neutral **dark mode**
* No gradients, no vivid colors
* Clean typography and spacing
* High readability
* Map remains visually clear

---

### 3.2. Global Dark Background

* `.root`, `.anchor-pane`

  * Background: `#121212`
* `.label`

  * Text color: light gray / off-white for readability

---

### 3.3. UI Cards and Containers

Applied to:

* Sidebar
* Map card wrapper
* Bottom bar

Style:

* Background: `#1b1b1b`
* Subtle light border
* Consistent border radius

Fixes previous issue where rounded corners exposed white background in stats/graph areas

---

### 3.4. Input Controls

Applied to:

* `.text-field`
* `.choice-box`
* `.text-area`
* `.control-field`

Style:

* Background: `#1f1f1f`
* Light text
* Focus state: subtle light border (no bright blue highlight)

---

### 3.5. Charts (Graphs)

* `.chart`, `.chart-plot-background`: dark background
* Grid lines: gray
* Axis labels and ticks: light gray

Ensures charts remain **readable in dark mode**

---

### 3.6. Map Container – Light Background (Exception)

As required: **map container is excluded from dark theme**

* `#mapContainer`: `#ffffff`
* `#backgroundCanvas`: `#f7f7f7`
* `#carLayer`, `#trafficLightLayer`, `#laneLayer`: transparent

This keeps the map clear and prevents visual fatigue.

---

## 4. Notes for the Team

* Do not change existing `fx:id` unless the controller is updated accordingly
* Add new controls inside the sidebar `VBox`
* Add new statistics or charts inside the bottom bar
* Preserve layer order inside `mapContainer`


# Stress Test Alarm – UI & Controller Changes

This document describes the **Stress Test Alarm feature** recently added to the JavaFX simulation project. It focuses on UI changes, controller logic, styling, and module configuration, and is intended as a standalone reference for the team.

---

## 1. Overview

### Purpose

* Provide **clear visual warning** when a Stress Test is running.
* Improve user awareness through **screen blinking** and **dynamic icon changes**.
* Keep changes **modular and non-breaking** to existing UI and controller logic.

---

## 2. Changes in `GUI-view.fxml`

### 2.1. Stress Test Button Icon

* Added `fx:id="stressAlarmIcon"` to the `ImageView` inside the **Stress Test** button.
* The Java controller can now dynamically update the icon at runtime.
* The **default icon remains `alarm.png`** when the application starts.

**Rationale**:

* Enables state-based visual feedback without altering button structure.
* Maintains compatibility with existing FXML and controller bindings.

---

## 3. Changes in `GUI.java`

### 3.1. Stress Test Alarm Mechanism

When a Stress Test is initiated with valid input:

* `startStressAlarmBlink(Duration.seconds(10))` is triggered.
* The application background **blinks dark red for 10 seconds**.
* The alarm automatically stops after the duration expires.

Implementation details:

* Blinking is handled by **adding/removing a CSS class** on the root node.
* No inline styles are used, keeping logic and presentation separated.

---

### 3.2. Alarm Icon State Switching

The Stress Test button icon reflects the alarm state:

* **Alarm active**:

  * Icon is switched to `alarm.gif`
* **Alarm ended** (after 10 seconds) **or invalid input**:

  * Icon is reset to `alarm.png`

This ensures immediate and intuitive feedback for the user.

---

### 3.3. Added Fields, Constants, and Utilities

The following elements were introduced in `GUI.java`:

* `@FXML ImageView stressAlarmIcon`
* `ALARM_PNG`
* `ALARM_GIF`

Helper method:

* `setStressAlarmIcon(boolean active)`

  * Centralizes icon switching logic
  * Avoids code duplication
  * Improves maintainability

---

## 4. Changes in `simulation-style.css`

### 4.1. Alarm Blink Style

* Added CSS class `.alarm-blink`
* Effect:

  * Temporarily changes the application background to **dark red** during the alarm

**Design Notes**:

* Uses CSS class toggling instead of hard-coded color changes
* Allows easy adjustment of warning intensity via CSS only

---

## 5. Notes for the Team

* Keep `stressAlarmIcon` fx:id unchanged unless controller logic is updated
* Alarm duration can be adjusted centrally via `startStressAlarmBlink(Duration)`
* Future warning types can reuse the same CSS-based blinking mechanism

---