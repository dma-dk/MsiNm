package dk.dma.msinm.common.light;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a light model
 */
public class LightModel implements LightConstants {

    List<Light> lightGroups = new ArrayList<>();

    Integer elevation;  // Metres
    Integer period;     // Seconds
    Integer range;      // Nautical miles

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("[lightGroups={ ");
        str.append(lightGroups.stream().map(Light::toString).collect(Collectors.joining(", "))).append(" }");
        if (elevation != null) {
            str.append(", elevation=").append(elevation);
        }
        if (period != null) {
            str.append(", period=").append(period);
        }
        if (range != null) {
            str.append(", range=").append(range);
        }
        return str.append("]").toString();
    }

    /**
     * Creates a new light group
     * @return a new light group
     */
    public Light newLight() {
        Light l = new Light();
        lightGroups.add(l);
        return l;
    }

    /**
     * Returns the last light group added, and creates a new one, if no one has been added yet
     * @return the last light group added
     */
    public Light getLight() {
        if (lightGroups.isEmpty()) {
            return  newLight();
        }
        return lightGroups.get(lightGroups.size() - 1);
    }

    public List<Light> getLightGroups() {
        return lightGroups;
    }

    public Integer getElevation() {
        return elevation;
    }

    public Integer getPeriod() {
        return period;
    }

    public Integer getRange() {
        return range;
    }

    /**
     * Helper class that represents a single light group
     */
    public static class Light {
        Phase phase;
        List<Color> colors = new ArrayList<>();
        boolean grouped;
        String morseCode;
        List<Integer> groupSpec = new ArrayList<>();

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            StringBuilder str = new StringBuilder();
            str.append("[phase=").append(phase);
            if (colors.size() > 0) {
                str.append(", colors={ ");
                str.append(colors.stream().map(Object::toString).collect(Collectors.joining(", "))).append(" }");
            }
            str.append(", grouped=").append(grouped);
            if (groupSpec.size() > 0) {
                str.append(", groupSpec={ ");
                str.append(groupSpec.stream().map(Object::toString).collect(Collectors.joining(", "))).append(" }");
            }
            if (morseCode != null) {
                str.append(", morseCode=").append(morseCode);
            }
            return str.append("]").toString();
        }

        public Phase getPhase() {
            return phase;
        }

        public List<Color> getColors() {
            return colors;
        }

        public boolean isGrouped() {
            return grouped;
        }

        public boolean isComposite() {
            return isGrouped() && getGroupSpec().size() > 1;
        }

        public String getMorseCode() {
            return morseCode;
        }

        public List<Integer> getGroupSpec() {
            return groupSpec;
        }
    }
}
