package dk.dma.msinm.reporting;

import dk.dma.msinm.common.model.DataFilter;
import dk.dma.msinm.common.vo.BaseVo;
import dk.dma.msinm.model.Area;
import dk.dma.msinm.model.Message;
import dk.dma.msinm.vo.AreaVo;
import dk.dma.msinm.vo.LocationVo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Value object for the {@code Report} model entity
 */
public class ReportVo extends BaseVo<Report> {

    Integer id;
    ReportStatus status;
    Date dateProcessed;
    AreaVo area;
    String areaId;
    List<LocationVo> locations;
    String description;
    String contact;
    String repoPath;
    boolean sendEmail;

    /**
     * Constructor
     */
    public ReportVo() {
    }

    /**
     * Constructor
     * @param entity the report
     * @param dataFilter what type of data to include from the entity
     */
    public ReportVo(Report entity, DataFilter dataFilter) {
        super(entity);

        DataFilter compFilter = dataFilter.forComponent(Message.class);

        this.id = entity.getId();
        this.status = entity.getStatus();
        this.dateProcessed = entity.getDateProcessed();
        this.description = entity.getDescription();

        if (compFilter.include("details")) {
            this.area = (entity.getArea() == null) ? null : new AreaVo(entity.getArea(), compFilter);
            this.areaId = (entity.getArea() == null) ? null : String.valueOf(entity.getArea().getId());
            this.contact = entity.getContact();
            entity.getLocations()
                    .forEach(loc -> checkCreateLocations().add(new LocationVo(loc, compFilter)));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Report toEntity() {
        Report report = new Report();
        report.setId(id);
        report.setStatus(status);
        report.setDateProcessed(dateProcessed);
        report.setDescription(description);
        report.setContact(contact);
        Area reportArea = null;
        for (AreaVo areaVo = area; areaVo != null; areaVo = areaVo.getParent()) {
            Area childArea = reportArea;
            reportArea = areaVo.toEntity();
            if (childArea != null) {
                childArea.setParent(reportArea);
                reportArea.getChildren().add(childArea);
            } else {
                report.setArea(reportArea);
            }
        }
        if (locations != null) {
            locations.stream()
                    .filter(loc -> loc.getPoints().size() > 0)
                    .forEach(loc -> report.getLocations().add(loc.toEntity()));
        }

        return report;
    }

    /**
     * Returns or creates the list of locations
     * @return the list of locations
     */
    public List<LocationVo> checkCreateLocations() {
        if (locations == null) {
            locations = new ArrayList<>();
        }
        return locations;
    }

    // ************ Getters and setters *************


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public Date getDateProcessed() {
        return dateProcessed;
    }

    public void setDateProcessed(Date dateProcessed) {
        this.dateProcessed = dateProcessed;
    }

    public AreaVo getArea() {
        return area;
    }

    public void setArea(AreaVo area) {
        this.area = area;
    }

    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

    public List<LocationVo> getLocations() {
        return locations;
    }

    public void setLocations(List<LocationVo> locations) {
        this.locations = locations;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getRepoPath() {
        return repoPath;
    }

    public void setRepoPath(String repoPath) {
        this.repoPath = repoPath;
    }

    public boolean isSendEmail() {
        return sendEmail;
    }

    public void setSendEmail(boolean sendEmail) {
        this.sendEmail = sendEmail;
    }
}

