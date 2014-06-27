package dk.dma.msinm.common.model;

/**
 * Entitites implementing this interface should thus ensure that all realted
 * entities get preloaded when the {@code preload()} method gets called.
 */
public interface IPreloadable {

    /**
     * Pre-load all related entities
     */
    void preload();
}
