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
