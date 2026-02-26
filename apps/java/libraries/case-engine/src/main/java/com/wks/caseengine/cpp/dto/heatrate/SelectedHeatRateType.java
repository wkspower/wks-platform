package com.wks.caseengine.cpp.dto.heatrate;

/**
 * Enum representing the allowed values for SelectedHeatRate field
 * Tracks which heat rate source was selected by the user
 */
public enum SelectedHeatRateType {
    OEM("OEM"),
    PREVIOUS_YEAR("PREVIOUS_YEAR"),
    PROPOSED("PROPOSED"),
    OTHER("OTHER");

    private final String value;

    SelectedHeatRateType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Get enum from string value
     * @param value String value to convert
     * @return SelectedHeatRateType enum or null if invalid
     */
    public static SelectedHeatRateType fromValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        for (SelectedHeatRateType type : SelectedHeatRateType.values()) {
            if (type.value.equalsIgnoreCase(value.trim())) {
                return type;
            }
        }
        return null;
    }

    /**
     * Validate if a string is a valid SelectedHeatRateType
     * @param value String value to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValid(String value) {
        return fromValue(value) != null;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
