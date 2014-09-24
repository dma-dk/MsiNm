/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.msinm.test;

import org.hibernate.jpa.AvailableSettings;
import org.junit.After;
import org.junit.AfterClass;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.*;

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

        props.put("hibernate.cache.use_second_level_cache", "false");
        props.put("hibernate.cache.use_query_cache", "false");

        entityManagerFactory = Persistence.createEntityManagerFactory(TEST_UNIT_NAME, props);
        entityManager = entityManagerFactory.createEntityManager();

        TestResources.entityManager = entityManager;

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
