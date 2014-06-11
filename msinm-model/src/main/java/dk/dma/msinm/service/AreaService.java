package dk.dma.msinm.service;

import dk.dma.msinm.common.service.BaseService;
import dk.dma.msinm.model.Area;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.slf4j.Logger;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;

/**
 * Business interface for accessing MSI-NM messages
 */
@Stateless
@SecurityDomain("msinm-policy")
@PermitAll
public class AreaService extends BaseService {

    @Inject
    private Logger log;

    /**
     * Returns the hierarchical list of root areas
     * @return the hierarchical list of root areas
     */
    public List<Area> getAreas() {
        return em.createNamedQuery("Area.findRootAreas", Area.class)
                .getResultList();
    }

}
