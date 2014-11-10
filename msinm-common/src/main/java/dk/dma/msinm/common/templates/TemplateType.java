package dk.dma.msinm.common.templates;

/**
 * Defines the type of Freemarker template
 */
public enum TemplateType {
    PDF("pdf"),
    MAIL("mail"),
    Light("light"),
    MESSAGE("message");

    private String path;

    TemplateType(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
