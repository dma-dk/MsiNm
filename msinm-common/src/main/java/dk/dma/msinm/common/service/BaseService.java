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

    protected BaseService() {
    }

    protected BaseService(EntityManager entityManager) {

        this.em = entityManager;
    }

    public <E extends IEntity<?>> E getByPrimaryKey(Class<E> clazz, Object id) {
        try {
            return em.find(clazz, id);
        } catch (EntityNotFoundException e) {
            return null;
        }
    }

    public void remove(IEntity<?> entity) {

        em.remove(em.merge(entity));
    }

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

    public static <T> T getSingleOrNull(List<T> list) {
        return (list == null || list.size() == 0) ? null : list.get(0);
    }

    public <E extends IEntity<?>> List<E> getAll(Class<E> entityType) {
        em.clear();

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<E> cq = cb.createQuery(entityType);
        cq.from(entityType);
        return em.createQuery(cq).getResultList();
    }
}
