package dk.dma.msinm.test;

import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;

/**
 * Returns the entity manager that should be used for test purposes.
 * <p></p>
 * The entity manager is instantiated by the {@code MsiNmUnitTest} class.
 */
public class TestDatabaseConfiguration {

    protected static EntityManager entityManager;

    @Produces
    public EntityManager getEntityManager() {
        return entityManager;
    }
}
