package dk.dma.msinm.reporting;

import dk.dma.msinm.common.model.VersionedEntity;
import dk.dma.msinm.model.Area;
import dk.dma.msinm.model.Location;
import dk.dma.msinm.model.Message;
import dk.dma.msinm.user.User;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Defines a report, i.e. a user observation reported via the MSI-NM web site.
 */
@Entity
public class Report extends VersionedEntity<Integer> {

    @NotNull
    @Enumerated(EnumType.STRING)
    ReportStatus status;

    @Temporal(TemporalType.TIMESTAMP)
    Date dateProcessed;

    @OneToOne
    Message message;

    @ManyToOne(cascade = CascadeType.ALL)
    Area area;

    @OneToMany(cascade = CascadeType.ALL)
    List<Location> locations = new ArrayList<>();

    @Lob
    String description;

    String contact;

    @NotNull
    @OneToOne
    User user;


    // ****** Getters and setters

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

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
