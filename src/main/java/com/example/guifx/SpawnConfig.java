package com.example.guifx;

import java.util.*;

/**
 * SpawnConfig defines the rules that determine which vehicles
 * are eligible for spawning.
 * This class is immutable. All provided collections are defensively copied.
 * Changes are impossible from the outside.
 */

public class SpawnConfig {
    private final List<TypeFilter> allowedTypes;
    private final List<VehicleColor> allowedColors;
    private final Random random;

    /**
     * Creates a new spawn configuration with the given constraints.
     *
     * @param types  the collection of allowed vehicle types
     * @param colors the collection of allowed vehicle colors
     */
    private SpawnConfig(Collection<TypeFilter> types, Collection<VehicleColor> colors) {
        //create unmodifiable copies
        this.allowedTypes = List.copyOf(types);
        this.allowedColors = List.copyOf(colors);
        this.random = new Random();
    }

    /**
     * Creates a spawn configuration with no restrictions.
     *
     * @return a configuration that allows all vehicle types and colors
     */
    public static SpawnConfig random() {
        return new SpawnConfig(TypeFilter.spawnableAll(), VehicleColor.spawnableAll());
    }

    /**
     * Creates a spawn configuration that restricts vehicle types
     * but allows all vehicle colors.
     *
     * @param types the allowed vehicle types
     * @return a configuration with restricted types
     */
    public static SpawnConfig restrictTypes(Collection<TypeFilter> types) {
        return new SpawnConfig(types,VehicleColor.spawnableAll());
    }

    /**
     * Creates a spawn configuration that restricts vehicle colors
     * but allows all vehicle types.
     *
     * @param colors the allowed vehicle colors
     * @return a configuration with restricted colors
     */
    public static SpawnConfig restrictColors(Collection<VehicleColor> colors) {
        return new SpawnConfig(TypeFilter.spawnableAll(), colors);
    }

    /**
     * Creates a spawn configuration that restricts both vehicle types and colors.
     *
     * @param types  the allowed vehicle types
     * @param colors the allowed vehicle colors
     * @return a configuration with restricted types and colors
     * @throws IllegalArgumentException if either collection is empty
     */
    public static SpawnConfig restrictAll(Collection<TypeFilter> types, Collection<VehicleColor> colors) {
        if (types.isEmpty() || colors.isEmpty()) {
            throw new IllegalArgumentException("Error: SpawnConfig can't be empty");
        }
        return new SpawnConfig(types, colors);
    }

    /**
     * Randomly selects one of the allowed vehicle types.
     *
     * @return a randomly chosen vehicle type
     */
    public TypeFilter pickType() {
        return allowedTypes.get(random.nextInt(allowedTypes.size()));
    }

    /**
     * Randomly selects one of the allowed vehicle colors.
     *
     * @return a randomly chosen vehicle color
     */
    public VehicleColor pickColor() {
        return allowedColors.get(random.nextInt(allowedColors.size()));
    }
}
