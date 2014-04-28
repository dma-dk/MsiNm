package dk.dma.msinm.common.settings;

import dk.dma.msinm.common.config.MsiNm;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.persistence.EntityManager;

/**
 * Abstract base class for all settings.
 * <p>
 * Sub-classes should be annotated as stateless session beans or singletons.
 */
public abstract class BaseSettings {

    /**
     * The source of the setting can be database or system property
     */
    enum Source {
        DATABASE,
        SYSTEM_PROPERTY
    }

    @Inject
    private Logger log;

    @Inject
    @MsiNm
    protected EntityManager em;


}
