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
package dk.dma.msinm.service;

import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.SpatialRelation;
import dk.dma.msinm.common.service.BaseService;
import dk.dma.msinm.model.AtoN;
import dk.dma.msinm.model.Location;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Interface for handling AtoNs
 *
 * TODO: Make it a singleton and cache the list of AtoNs
 * or create a Lucene index with AtoNs
 */
@Singleton
@Startup
@Lock(LockType.READ)
public class AtoNService extends BaseService {

    @Inject
    private Logger log;

    private List<AtoN> atons = new ArrayList<>();

    /** Reloads the list of AtoNs */
    @PostConstruct
    @Lock(LockType.WRITE)
    void loadAtoNs() {
        atons = getAll(AtoN.class);
    }

    /**
     * Returns the list of all AtoNs
     * @return the list of all AtoNs
     */
    public List<AtoN> getAllAtoNs() {
        return atons;
    }

    /**
     * Replaces the AtoN DB
     * @param atons the new AtoNs
     */
    @Lock(LockType.WRITE)
    public void replaceAtoNs(List<AtoN> atons) {

        // Delete old AtoNs
        int deleted = em.createNamedQuery("AtoN.deleteAll").executeUpdate();
        log.info("Deleted " + deleted + " AtoNs");
        em.flush();

        // Persist new list of AtoNs
        long t0 = System.currentTimeMillis();
        int x = 0;
        for (AtoN aton : atons) {
            em.persist(aton);

            if (x++ % 100 == 0) {
                em.flush();
            }
        }
        log.info("Persisted " + atons.size() + " AtoNs in " +
                (System.currentTimeMillis() - t0) + " ms");

        // Reload the AtoN list
        loadAtoNs();
    }

    /**
     * Computes the list of AtoNs within the given locations.<br>
     *
     * @return the AtoNs within the given locations
     */
    public List<AtoN> getWithinLocation(List<Location> locations) {
        try {
            List<Shape> bounds = locations.stream()
                    .map(Location::toWktOrNull)
                    .collect(Collectors.toList());

            return getAllAtoNs().stream()
                    .filter(a -> isContained(a, bounds))
                    .collect(Collectors.toList());

        } catch (RuntimeException e) {
            log.error("Error computing AtoNs wihting the location");
            return new ArrayList<>();
        }
    }

    /** Returns if the AtoN is contained in any of the given locations */
    boolean isContained(AtoN aton, List<Shape> locations) {
        Point pt = aton.toWkt();
        return locations.stream()
                .anyMatch(l -> l.relate(pt) == SpatialRelation.CONTAINS);
    }

}

