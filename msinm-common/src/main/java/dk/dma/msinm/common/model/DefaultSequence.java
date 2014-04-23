package dk.dma.msinm.common.model;

/**
 * A default sequence implementation
 */
public final class DefaultSequence implements Sequence {

    String name;
    long initialValue;

    public DefaultSequence(String name) {
        this(name, 0);
    }

    public DefaultSequence(String name, long initialValue) {
        this.name = name;
        this.initialValue = initialValue;
    }

    @Override
    public String getSequenceName() {
        return name;
    }

    @Override
    public long initialValue() {
        return initialValue;
    }
}
