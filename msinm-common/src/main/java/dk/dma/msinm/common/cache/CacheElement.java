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
package dk.dma.msinm.common.cache;

import java.io.Serializable;

/**
 * Can be used in JCache/Infinispan to wrap the stored element.
 * This way, you can cache a value of "null"
 */
public class CacheElement<T> implements Serializable {

    private static final long serialVersionUID = 1L;
    T element;

    /**
     * Constructor
     * @param element the element being wrapped
     */
    public CacheElement(T element) {
        this.element = element;
    }

    public T getElement() {
        return element;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return (element == null) ? 0 : element.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CacheElement that = (CacheElement) o;

        return !(element != null ? !element.equals(that.element) : that.element != null);

    }

    /**
     * Returns if the wrapped element is null or not
     * @return if the wrapped element is null or not
     */
    public boolean isNull() {
        return (element == null);
    }
}
