package com.example.guifx;

import java.util.*;

public class SpawnConfig {
    private final List<TypeFilter> allowedTypes;
    private final List<VehicleColor> allowedColors;
    //randomly generated number is needed to randomly pick attributes
    private final Random random;

    private SpawnConfig(Collection<TypeFilter> types, Collection<VehicleColor> colors) {
        this.allowedTypes = List.copyOf(types);
        this.allowedColors = List.copyOf(colors);
        this.random = new Random();
    }

    //selects ALL attributes for spawning. It then returns the whole config for use. It will be used later to pick randomly from the whole set of attributes
    public static SpawnConfig random() {
        return new SpawnConfig(EnumSet.allOf(TypeFilter.class), EnumSet.allOf(VehicleColor.class));
    }

    //restricts TYPE, but allows ALL colors. It then returns the whole config for use
    public static SpawnConfig restrictTypes(Collection<TypeFilter> types) {
        return new SpawnConfig(types,EnumSet.allOf(VehicleColor.class)
        );
    }

    //restricts COLOR, but allows all types, it then returns the whole config for use
    public static SpawnConfig restrictColors(Collection<VehicleColor> colors) {
        return new SpawnConfig(EnumSet.allOf(TypeFilter.class), colors);
    }

    //restricts both type AND color, it then returns the whole config for use
    public static SpawnConfig restrictAll(Collection<TypeFilter> types, Collection<VehicleColor> colors) {
        if (types.isEmpty() || colors.isEmpty()) {
            throw new IllegalArgumentException("Error: SpawnConfig can't be empty");
        }
        return new SpawnConfig(types, colors);
    }

    //randomly selects ONE type for spawning. returns a TYPE, not config. It will be coupled with the SpawnConfig random() method, to pick out attributes randomly
    public TypeFilter pickType() {
        return allowedTypes.get(random.nextInt(allowedTypes.size()));
    }
    //randomly selects ONE color for spawning. Returns a COLOR, not config. It will be coupled with the SpawnConfig random() method, to pick out attributes randomly
    public VehicleColor pickColor() {
        return allowedColors.get(random.nextInt(allowedColors.size()));
    }
}
