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
package dk.dma.msinm.common.service;

import dk.dma.msinm.common.model.IEntity;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

/**
 * DAO-like base class for services that work on work on {@linkplain dk.dma.msinm.common.model.IEntity}
 */
public abstract class BaseService {

    @Inject
    protected EntityManager em;

    /**
     * Constructor
     */
    protected BaseService() {
    }

    /**
     * Constructor
     *
     * @param entityManager the ent
     */
    protected BaseService(EntityManager entityManager) {
        this.em = entityManager;
    }

    /**
     * Returns the entity with the given id or {@code null} if none is found
     *
     * @param clazz the entity class
     * @param id the id of the entity
     * @return the entity with the given id or {@code null} if none is found
     */
    public <E extends IEntity<?>> E getByPrimaryKey(Class<E> clazz, Object id) {
        try {
            return em.find(clazz, id);
        } catch (EntityNotFoundException e) {
            return null;
        }
    }

    /**
     * Removes the given entity from the database
     *
     * @param entity the entity to remove
     */
    public void remove(IEntity<?> entity) {

        em.remove(em.merge(entity));
    }

    /**
     * Persists or updates the given entity
     *
     * @param entity the entity to persist or update
     * @return thed update entity
     */
    public <E extends IEntity<?>> E saveEntity(E entity) {
        if (entity.isPersisted()) {
            // Update existing
            entity = em.merge(entity);
        } else {
            // Save new
            em.persist(entity);
        }
        return entity;
    }

    /**
     * Returns the first element of the list, or {@code null} if the list is empty or {@code null}
     *
     * @param list the list
     * @return the first element
     */
    public static <T> T getSingleOrNull(List<T> list) {
        return (list == null || list.size() == 0) ? null : list.get(0);
    }

    /**
     * Returns all entities with the given class
     *
     * @param entityType the class
     * @return all entities with the given class
     */
    public <E extends IEntity<?>> List<E> getAll(Class<E> entityType) {
        em.clear();

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<E> cq = cb.createQuery(entityType);
        cq.from(entityType);
        return em.createQuery(cq).getResultList();
    }
}
