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
 */
public class MsiNmUnitTest {

    private static final String TEST_UNIT_NAME = "msi";
    protected static EntityManagerFactory entityManagerFactory;
    protected static EntityManager entityManager;

    public static void prepareEntityManagerFactory(Class<?>... entityClasses) throws ClassNotFoundException {
        Class.forName("org.h2.Driver");

        Map<Object, Object> props = new HashMap<>();
        props.put("javax.persistence.provider", "org.hibernate.ejb.HibernatePersistence");
        props.put("javax.persistence.transactionType", "RESOURCE_LOCAL");
        props.put("hibernate.connection.username", "sa");
        props.put("hibernate.connection.password", "");
        props.put("hibernate.connection.driver_class", "org.h2.Driver");
        //props.put("hibernate.connection.url", "jdbc:h2:.");
        props.put("hibernate.connection.url", "jdbc:h2:mem:msi");
        props.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        props.put("hibernate.show_sql", "true");
        props.put("hibernate.format_sql", "true");

        props.put("javax.persistence.schema-generation.database.action", "drop-and-create");
        props.put("javax.persistence.schema-generation.create-source", "metadata");
        props.put("javax.persistence.schema-generation.drop-source", "metadata");

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
