# Vehicle Hover/Click (Selection) Logic

This document describes how the **vehicle info panel** is updated when the user **hovers** or **clicks** a vehicle on the map.

## Goal / Behavior

- **Hover** a vehicle:
  - Show its info live (updates continuously) **only when no vehicle is pinned**.
- **Click** a vehicle:
  - Pin that vehicle selection.
  - Show its info live until cleared.
- **Click empty space**:
  - Clear selection.
  - Show `No vehicle selected`.

## Files involved

- Rendering + mouse handlers: `src/main/java/com/example/guifx/CarLayer.java`
- UI labels + update logic: `src/main/java/com/example/guifx/GUI.java`
- Layout for labels: `src/main/resources/com/example/guifx/GUI-view.fxml` (bottom bar `fx:id="rightVehicleInfo"`)

## CarLayer responsibilities (`CarLayer.java`)

### State

- `pinnedVehicleId`: vehicle id selected by click
- `hoveredVehicleId`: vehicle id currently under mouse

### Callback

`CarLayer.VehicleClickListener` is injected from `GUI`:

- `onVehicleFocusChanged(String vehicleId, boolean isPinned)`
  - `vehicleId == null`: no vehicle (hover left the node OR selection cleared)
  - `isPinned == true`: click selection (pin)
  - `isPinned == false`: hover focus

### Event handlers per vehicle node

- `setOnMouseEntered`:
  - sets `hoveredVehicleId`
  - notifies GUI only if nothing is pinned
- `setOnMouseExited`:
  - clears `hoveredVehicleId`
  - notifies GUI to display `No vehicle selected` only if nothing is pinned
- `setOnMouseClicked`:
  - sets `pinnedVehicleId`
  - notifies GUI with `isPinned=true`

### Clearing selection

- `carLayerPane.addEventFilter(MOUSE_PRESSED, ...)`:
  - If click target is the pane itself (empty area), call `clearSelection()`.

## GUI responsibilities (`GUI.java`)

### State

- `pinnedVehicleInfoId`: currently pinned vehicle (selected by click)
- `hoveredVehicleInfoId`: last hovered vehicle (when not pinned)

### Updating the UI

- `updateVehicleInfoSidebar(vehicleId)` renders details into labels:
  - `vehicleInfoHeader`, `vehicleInfoId`, `vehicleInfoType`, `vehicleInfoSpeed`, ...

### Real-time refresh

Inside the main `AnimationTimer` loop (`startLoop()`), the GUI refreshes every frame:

- If `pinnedVehicleInfoId != null` → refresh pinned
- Else if `hoveredVehicleInfoId != null` → refresh hovered

This keeps speed/position/angle/lane updates live while the simulation runs.

## Notes / Known limitations

- The header text currently always uses `Selected: <id>`.
  - If you want UX distinction, update it to:
    - `Hover: <id>` for hover
    - `Selected: <id>` for pinned
- Clearing selection is handled both in `CarLayer` (pane filter) and also in `GUI` (mapContainer filter) to support clearing on empty-map clicks.

# Vehicle Hover/Click (Selection) Logic

This document describes how the **vehicle info panel** is updated when the user **hovers** or **clicks** a vehicle on the map.

## Goal / Behavior

- **Hover** a vehicle:
  - Show its info live (updates continuously) **only when no vehicle is pinned**.
- **Click** a vehicle:
  - Pin that vehicle selection.
  - Show its info live until cleared.
- **Click empty space**:
  - Clear selection.
  - Show `No vehicle selected`.

## Files involved

- Rendering + mouse handlers: `src/main/java/com/example/guifx/CarLayer.java`
- UI labels + update logic: `src/main/java/com/example/guifx/GUI.java`
- Layout for labels: `src/main/resources/com/example/guifx/GUI-view.fxml` (bottom bar `fx:id="rightVehicleInfo"`)

## CarLayer responsibilities (`CarLayer.java`)

### State

- `pinnedVehicleId`: vehicle id selected by click
- `hoveredVehicleId`: vehicle id currently under mouse

### Callback

`CarLayer.VehicleClickListener` is injected from `GUI`:

- `onVehicleFocusChanged(String vehicleId, boolean isPinned)`
  - `vehicleId == null`: no vehicle (hover left the node OR selection cleared)
  - `isPinned == true`: click selection (pin)
  - `isPinned == false`: hover focus

### Event handlers per vehicle node

- `setOnMouseEntered`:
  - sets `hoveredVehicleId`
  - notifies GUI only if nothing is pinned
- `setOnMouseExited`:
  - clears `hoveredVehicleId`
  - notifies GUI to display `No vehicle selected` only if nothing is pinned
- `setOnMouseClicked`:
  - sets `pinnedVehicleId`
  - notifies GUI with `isPinned=true`

### Clearing selection

- `carLayerPane.addEventFilter(MOUSE_PRESSED, ...)`:
  - If click target is the pane itself (empty area), call `clearSelection()`.

## GUI responsibilities (`GUI.java`)

### State

- `pinnedVehicleInfoId`: currently pinned vehicle (selected by click)
- `hoveredVehicleInfoId`: last hovered vehicle (when not pinned)

### Updating the UI

- `updateVehicleInfoSidebar(vehicleId)` renders details into labels:
  - `vehicleInfoHeader`, `vehicleInfoId`, `vehicleInfoType`, `vehicleInfoSpeed`, ...

### Real-time refresh

Inside the main `AnimationTimer` loop (`startLoop()`), the GUI refreshes every frame:

- If `pinnedVehicleInfoId != null` → refresh pinned
- Else if `hoveredVehicleInfoId != null` → refresh hovered

This keeps speed/position/angle/lane updates live while the simulation runs.

## Notes / Known limitations

- The header text currently always uses `Selected: <id>`.
  - If you want UX distinction, update it to:
    - `Hover: <id>` for hover
    - `Selected: <id>` for pinned
- Clearing selection is handled both in `CarLayer` (pane filter) and also in `GUI` (mapContainer filter) to support clearing on empty-map clicks.
