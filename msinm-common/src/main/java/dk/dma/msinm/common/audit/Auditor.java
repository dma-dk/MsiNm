package dk.dma.msinm.common.audit;

/**
 * An injectable Auditor bean that can be used to log audit entries
 * for the class the Auditor is injected into
 */
public interface Auditor {
    /**
     * Logs a new audit info message
     *
     * @param message the message
     * @param args optional list of arguments
     */
    public void info(String message, Object... args);

    /**
     * Logs a new audit error message
     *
     * @param message the message
     * @param args optional list of arguments
     */
    public void error(String message, Object... args);

    /**
     * Logs a new audit error message
     *
     * @param exception the exception
     * @param message the message
     * @param args optional list of arguments
     */
    public void error(Throwable exception, String message, Object... args);

}
