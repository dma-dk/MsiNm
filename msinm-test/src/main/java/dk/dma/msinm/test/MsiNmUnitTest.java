package dk.dma.msinm.test;

import dk.dma.msinm.common.config.DatabaseConfiguration;
import org.hibernate.jpa.AvailableSettings;
import org.junit.After;
import org.junit.AfterClass;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for unit tests
 * <p/>
 * Sets up a H2 JPA entity manager
 */
public class MsiNmUnitTest {

    private static final String TEST_UNIT_NAME = "msi";
    protected static EntityManagerFactory entityManagerFactory;
    protected static EntityManager entityManager;

    /**
     * Should be called in sub-classes with the entity classes used in the test
     * @param entityClasses the entity classes
     */
    public static void prepareEntityManagerFactory(Class<?>... entityClasses) throws ClassNotFoundException {
        Class.forName("org.h2.Driver");

        Map<Object, Object> props = new HashMap<>();
        props.put(AvailableSettings.LOADED_CLASSES,
                Arrays.asList(entityClasses));

        entityManagerFactory = Persistence.createEntityManagerFactory(TEST_UNIT_NAME, props);
        entityManager = entityManagerFactory.createEntityManager();

        DatabaseConfiguration.EM = entityManager;

        entityManager.getTransaction().begin();
    }

    @After
    public void rollbackTransactionAndReleaseEntityManager() {

    }

    @AfterClass
    public static void releaseEntityManager() {
        if (entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().rollback();
        }

        if (entityManager.isOpen()) {
            entityManager.close();
        }
        entityManagerFactory.close();
    }
}
