package dk.dma.msinm.common.repo;

import dk.dma.msinm.common.vo.JsonSerializable;

/**
 * Represents a file in the repository
 */
public class RepoFileVo implements JsonSerializable {

    String name;
    String path;
    boolean directory;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isDirectory() {
        return directory;
    }

    public void setDirectory(boolean directory) {
        this.directory = directory;
    }
}
