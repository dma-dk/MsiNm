package dk.dma.msinm.legacy.msi.service;

/**
 * Defines the MSI legacy import type
 */
public enum LegacyMsiImportType {
    NONE,
    ACTIVE,
    ALL;

    public static LegacyMsiImportType get(String value) {
        if (ACTIVE.name().equalsIgnoreCase(value)) {
            return ACTIVE;
        } else if (ALL.name().equalsIgnoreCase(value)) {
            return ALL;
        }
        return NONE;
    }
}
