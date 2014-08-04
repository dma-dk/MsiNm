package dk.dma.msinm.common.repo;

/**
 * Defines the valid icon sizes
 */
public enum IconSize {
    SIZE_32(32),
    SIZE_64(64),
    SIZE_128(128);

    int size;
    IconSize(int size) {
        this.size = size;
    }
    public int getSize() { return size; }

    public static IconSize getIconSize(int size) {
        switch (size) {
            case 32:  return SIZE_32;
            case 128: return SIZE_128;
            default:  return SIZE_64;
        }
    }
}
