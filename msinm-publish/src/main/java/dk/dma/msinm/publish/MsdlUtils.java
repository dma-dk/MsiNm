package dk.dma.msinm.publish;

import dk.dma.msinm.common.repo.RepoFileVo;
import dk.dma.msinm.model.SeriesIdType;
import dk.dma.msinm.model.SeriesIdentifier;
import dk.dma.msinm.vo.AreaVo;
import dk.dma.msinm.vo.CategoryVo;
import dk.dma.msinm.vo.ChartVo;
import dk.dma.msinm.vo.LocationVo;
import dk.dma.msinm.vo.MessageVo;
import dk.dma.msinm.vo.ReferenceVo;
import dma.msinm.MCArea;
import dma.msinm.MCAreaDesc;
import dma.msinm.MCAttachment;
import dma.msinm.MCCategory;
import dma.msinm.MCCategoryDesc;
import dma.msinm.MCChart;
import dma.msinm.MCLocation;
import dma.msinm.MCLocationDesc;
import dma.msinm.MCLocationType;
import dma.msinm.MCMessage;
import dma.msinm.MCMessageDesc;
import dma.msinm.MCPoint;
import dma.msinm.MCPointDesc;
import dma.msinm.MCReference;
import dma.msinm.MCReferenceType;
import dma.msinm.MCSeriesIdType;
import dma.msinm.MCSeriesIdentifier;
import dma.msinm.MCStatus;
import dma.msinm.MCType;
import net.maritimecloud.util.Timestamp;

/**
 * Utility methods for copying to and fro MSDL entities
 */
public class MsdlUtils {

    /**
     * Converts a message to a Maritime Cloud message
     * @param message the message to convert
     * @return the corresponding Maritime Cloud message
     */
    public static MCMessage convert(MessageVo message) {
        MCMessage msg = new MCMessage();

        msg.setId(message.getId());
        msg.setVersion(message.getVersion());
        msg.setCreated(Timestamp.create(message.getCreated().getTime()));
        msg.setUpdated(Timestamp.create(message.getUpdated().getTime()));

        switch (message.getType()) {
            case PERMANENT_NOTICE: msg.setType(MCType.PERMANENT_NOTICE); break;
            case TEMPORARY_NOTICE: msg.setType(MCType.TEMPORARY_NOTICE); break;
            case PRELIMINARY_NOTICE: msg.setType(MCType.PRELIMINARY_NOTICE); break;
            case MISCELLANEOUS_NOTICE: msg.setType(MCType.MISCELLANEOUS_NOTICE); break;
            case COSTAL_WARNING: msg.setType(MCType.COSTAL_WARNING); break;
            case SUBAREA_WARNING: msg.setType(MCType.SUBAREA_WARNING); break;
            case NAVAREA_WARNING: msg.setType(MCType.NAVAREA_WARNING); break;
        }

        switch (message.getStatus()) {
            case DRAFT: msg.setStatus(MCStatus.DRAFT); break;
            case PUBLISHED: msg.setStatus(MCStatus.PUBLISHED); break;
            case EXPIRED: msg.setStatus(MCStatus.EXPIRED); break;
            case CANCELLED: msg.setStatus(MCStatus.CANCELLED); break;
            case DELETED: msg.setStatus(MCStatus.DELETED); break;
        }

        msg.setSeriesIdentifier(convert(message.getSeriesIdentifier()));

        if (message.getArea() != null) {
            msg.setArea(convert(message.getArea()));
        }

        if (message.getCategories() != null) {
            message.getCategories().forEach(cat -> msg.addCategories(convert(cat)));
        }

        if (message.getLocations() != null) {
            message.getLocations().forEach(loc -> msg.addLocations(convert(loc)));
        }

        if (message.getCharts() != null) {
            message.getCharts().forEach(chart -> msg.addCharts(convert(chart)));
        }

        msg.setHorizontalDatum(message.getHorizontalDatum());

        msg.setValidFrom(message.getValidFrom() == null ? null : Timestamp.create(message.getValidFrom().getTime()));
        msg.setValidTo(message.getValidTo() == null ? null : Timestamp.create(message.getValidTo().getTime()));

        if (message.getReferences() != null) {
            message.getReferences().forEach(ref -> msg.addReferences(convert(ref)));
        }

        if (message.getLightsListNumbers() != null) {
            message.getLightsListNumbers().forEach(msg::addLightsListNumbers);
        }

        msg.setOriginalInformation(message.isOriginalInformation());

        if (message.getDescs() != null) {
            message.getDescs().forEach(desc -> msg.addDescs(convert(desc)));
        }

        if (message.getAttachments() != null) {
            message.getAttachments().forEach(att -> msg.addAttachments(convert(att)));
        }

        return msg;
    }


    /**
     * Converts a series identifier to a Maritime Cloud series identifier
     * @param seriesIdentifier the series identifier to convert
     * @return the corresponding Maritime Cloud series identifier
     */
    public static MCSeriesIdentifier convert(SeriesIdentifier seriesIdentifier) {
        MCSeriesIdentifier id = new MCSeriesIdentifier();
        id.setMainType(seriesIdentifier.getMainType() == SeriesIdType.MSI ? MCSeriesIdType.MSI : MCSeriesIdType.NM);
        id.setNumber(seriesIdentifier.getNumber());
        id.setAuthority(seriesIdentifier.getAuthority());
        id.setYear(seriesIdentifier.getYear());

        return id;
    }

    /**
     * Converts a location to a Maritime Cloud location
     * @param location the location to convert
     * @return the corresponding Maritime Cloud location
     */
    public static MCLocation convert(LocationVo location) {
        MCLocation loc = new MCLocation();

        switch (location.getType()) {
            case "POINT": loc.setType(MCLocationType.POINT); break;
            case "CIRCLE": loc.setType(MCLocationType.CIRCLE); break;
            case "POLYGON": loc.setType(MCLocationType.POLYGON); break;
            case "POLYLINE": loc.setType(MCLocationType.POLYLINE); break;
        }

        if (location.getRadius() != null) {
            loc.setRadius(location.getRadius());
        }

        if (location.getDescs() != null) {
            location.getDescs().forEach(desc -> {
                MCLocationDesc d = new MCLocationDesc();
                d.setLang(desc.getLang());
                d.setDescription(desc.getDescription());
                loc.addDescs(d);
            });
        }

        if (location.getPoints() != null) {
            location.getPoints().forEach(point -> {
                MCPoint pt = new MCPoint();
                pt.setLat(point.getLat());
                pt.setLon(point.getLon());
                pt.setIndex(point.getIndex());
                loc.addPoints(pt);
                if (point.getDescs() != null) {
                    point.getDescs().forEach(desc -> {
                        MCPointDesc d = new MCPointDesc();
                        d.setLang(desc.getLang());
                        d.setDescription(desc.getDescription());
                        pt.addDescs(d);
                    });
                }
            });
        }

        return loc;
    }

    /**
     * Converts an area to a Maritime Cloud area
     * @param area the area to convert
     * @return the corresponding Maritime Cloud area
     */
    public static MCArea convert(AreaVo area) {
        MCArea a = new MCArea();

        if (area.getParent() != null) {
            a.setParent(convert(area.getParent()));
        }

        if (area.getDescs() != null) {
            area.getDescs().forEach(desc -> {
                MCAreaDesc d = new MCAreaDesc();
                d.setLang(desc.getLang());
                d.setName(desc.getName());
                a.addDescs(d);
            });
        }

        return a;
    }

    /**
     * Converts a category to a Maritime Cloud category
     * @param category the category to convert
     * @return the corresponding Maritime Cloud category
     */
    public static MCCategory convert(CategoryVo category) {
        MCCategory c = new MCCategory();

        if (category.getParent() != null) {
            c.setParent(convert(category.getParent()));
        }

        if (category.getDescs() != null) {
            category.getDescs().forEach(desc -> {
                MCCategoryDesc d = new MCCategoryDesc();
                d.setLang(desc.getLang());
                d.setName(desc.getName());
                c.addDescs(d);
            });
        }

        return c;
    }

    /**
     * Converts a chart to a Maritime Cloud chart
     * @param chart the chart to convert
     * @return the corresponding Maritime Cloud chart
     */
    public static MCChart convert(ChartVo chart) {
        MCChart c = new MCChart();
        c.setChartNumber(chart.getChartNumber());
        c.setInternationalNumber(chart.getInternationalNumber());

        return c;
    }

    /**
     * Converts a reference to a Maritime Cloud chart
     * @param reference the reference to convert
     * @return the corresponding Maritime Cloud reference
     */
    public static MCReference convert(ReferenceVo reference) {
        MCReference ref = new MCReference();
        ref.setSeriesIdentifier(convert(reference.getSeriesIdentifier()));
        switch (reference.getType()) {
            case REFERENCE: ref.setType(MCReferenceType.REFERENCE); break;
            case REPETITION: ref.setType(MCReferenceType.REPETITION); break;
            case CANCELLATION: ref.setType(MCReferenceType.CANCELLATION); break;
            case UPDATE: ref.setType(MCReferenceType.UPDATE); break;
        }

        return ref;
    }

    /**
     * Converts a RepoFileVo to a Maritime Cloud attachment
     * @param attachment the RepoFileVo to convert
     * @return the corresponding Maritime Cloud attachment
     */
    public static MCAttachment convert(RepoFileVo attachment) {
        MCAttachment att = new MCAttachment();
        att.setName(attachment.getName());
        att.setPath("/rest/repo/file/" + attachment.getPath());
        att.setThumbnail("/rest/repo/thumb/" + attachment.getPath());
        return att;
    }

    /**
     * Converts a message descriptor to a Maritime Cloud message descriptor
     * @param desc the message descriptor to convert
     * @return the corresponding Maritime Cloud message descriptor
     */
    public static MCMessageDesc convert(MessageVo.MessageDescVo desc) {
        MCMessageDesc d = new MCMessageDesc();
        d.setTitle(desc.getTitle());
        d.setDescription(desc.getDescription());
        d.setOtherCategories(desc.getOtherCategories());
        d.setTime(desc.getTime());
        d.setVicinity(desc.getVicinity());
        d.setNote(desc.getNote());
        d.setPublication(desc.getPublication());
        d.setSource(desc.getSource());

        return d;
    }

}
