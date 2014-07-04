package dk.dma.msinm.common.time;

/**
 * An exception thrown by the time parsing system
 */
public class TimeException extends Exception {

    public TimeException(String message) {
        super(message);
    }

    public TimeException(String message, Exception ex) {
        super(message, ex);
    }
}
