package dk.dma.msinm.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dk.dma.msinm.common.vo.LocalizableVo;
import dk.dma.msinm.common.vo.LocalizedDescVo;
import dk.dma.msinm.model.Chart;
import dk.dma.msinm.model.Message;
import dk.dma.msinm.model.MessageDesc;
import dk.dma.msinm.model.MessageSeriesIdentifier;
import dk.dma.msinm.model.MessageStatus;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Value object for the {@code Area} model entity
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class MessageVo extends LocalizableVo<Message, MessageVo.MessageDescVo> {

    Integer id;
    MessageSeriesIdentifier seriesIdentifier;
    MessageStatus status;
    AreaVo area;
    List<CategoryVo> categories = new ArrayList<>();
    List<LocationVo> locations = new ArrayList<>();
    List<Chart> charts = new ArrayList<>();
    String horizontalDatum;
    Date validFrom;
    Date validTo;
    Date cancellationDate;
    Set<MessageSeriesIdentifier> cancellations = new HashSet<>();
    List<String> lightsListNumbers = new ArrayList<>();
    boolean originalInformation;


    /**
     * Constructor
     */
    public MessageVo() {
    }

    /**
     * Constructor
     * @param message the message
     */
    public MessageVo(Message message) {
        super(message);

        id = message.getId();
        seriesIdentifier = message.getSeriesIdentifier();
        status = message.getStatus();
        area = (message.getArea() == null) ? null : new AreaVo(message.getArea(), false);
        message.getCategories().forEach(cat -> categories.add(new CategoryVo(cat, false)));
        message.getLocations().forEach(loc -> locations.add(new LocationVo(loc)));
        charts.addAll(message.getCharts());
        horizontalDatum = message.getHorizontalDatum();
        validFrom = message.getValidFrom();
        validTo = message.getValidTo();
        cancellationDate = message.getCancellationDate();
        cancellations.addAll(message.getCancellations());
        lightsListNumbers.addAll(message.getLightsListNumbers());
        originalInformation = message.isOriginalInformation();
        message.getDescs().forEach(desc -> getDescs().add(new MessageDescVo(desc)));
    }

    /**
     * Constructor
     * This version is used for search results and only contains a subset of the message data
     * @param message the message
     * @param lang the language
     */
    public MessageVo(Message message, String lang) {
        super(message);

        id = message.getId();
        area = (message.getArea() == null) ? null : new AreaVo(message.getArea(), lang, false);
        message.getLocations().forEach(loc -> locations.add(new LocationVo(loc, lang)));
        validFrom = message.getValidFrom();
        validTo = message.getValidTo();
        message.getDescs().stream()
            .filter(desc -> lang == null || desc.getLang().equals(lang))
            .forEach(desc -> getDescs().add(new MessageDescVo(desc)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message toEntity() {
        // TODO: Update ORM references

        Message message = new Message();
        message.setId(id);
        message.setSeriesIdentifier(seriesIdentifier);
        message.setStatus(status);
        message.setArea((area == null) ? null : area.toEntity());
        categories.forEach(cat -> message.getCategories().add(cat.toEntity()));
        locations.stream()
                .filter(loc -> loc.getPoints().size() > 0)
                .forEach(loc -> message.getLocations().add(loc.toEntity()));
        message.getCharts().addAll(charts);
        message.setHorizontalDatum(horizontalDatum);
        message.setValidFrom(validFrom);
        message.setValidTo(validTo);
        message.setCancellationDate(cancellationDate);
        message.getCancellations().addAll(cancellations);
        message.getLightsListNumbers().addAll(lightsListNumbers);
        message.setOriginalInformation(originalInformation);
        getDescs().stream()
                .filter(MessageDescVo::isDefined)
                .forEach(desc -> message.getDescs().add(desc.toEntity(message)));
        return message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageDescVo createDesc(String lang) {
        MessageDescVo message = new MessageDescVo();
        message.setLang(lang);
        return message;
    }

    // ************ Getters and setters *************

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public MessageSeriesIdentifier getSeriesIdentifier() {
        return seriesIdentifier;
    }

    public void setSeriesIdentifier(MessageSeriesIdentifier seriesIdentifier) {
        this.seriesIdentifier = seriesIdentifier;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    public AreaVo getArea() {
        return area;
    }

    public void setArea(AreaVo area) {
        this.area = area;
    }

    public List<CategoryVo> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryVo> categories) {
        this.categories = categories;
    }

    public List<LocationVo> getLocations() {
        return locations;
    }

    public void setLocations(List<LocationVo> locations) {
        this.locations = locations;
    }

    public List<Chart> getCharts() {
        return charts;
    }

    public void setCharts(List<Chart> charts) {
        this.charts = charts;
    }

    public String getHorizontalDatum() {
        return horizontalDatum;
    }

    public void setHorizontalDatum(String horizontalDatum) {
        this.horizontalDatum = horizontalDatum;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    public Date getValidTo() {
        return validTo;
    }

    public void setValidTo(Date validTo) {
        this.validTo = validTo;
    }

    public Date getCancellationDate() {
        return cancellationDate;
    }

    public void setCancellationDate(Date cancellationDate) {
        this.cancellationDate = cancellationDate;
    }

    public Set<MessageSeriesIdentifier> getCancellations() {
        return cancellations;
    }

    public void setCancellations(Set<MessageSeriesIdentifier> cancellations) {
        this.cancellations = cancellations;
    }

    public List<String> getLightsListNumbers() {
        return lightsListNumbers;
    }

    public void setLightsListNumbers(List<String> lightsListNumbers) {
        this.lightsListNumbers = lightsListNumbers;
    }

    public boolean isOriginalInformation() {
        return originalInformation;
    }

    public void setOriginalInformation(boolean originalInformation) {
        this.originalInformation = originalInformation;
    }

    /**
     * The entity description VO
     */
    public static class MessageDescVo extends LocalizedDescVo<MessageDesc, MessageVo> {

        String title;
        String description;
        String otherCategories;
        String time;
        String vicinity;

        /**
         * Constructor
         */
        public MessageDescVo() {
            super();
        }

        /**
         * Constructor
         * @param desc the entity
         */
        public MessageDescVo(MessageDesc desc) {
            super(desc);
            this.title = desc.getTitle();
            this.description = desc.getDescription();
            this.otherCategories = desc.getOtherCategories();
            this.time = desc.getTime();
            this.vicinity = desc.getVicinity();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public MessageDesc toEntity() {
            MessageDesc desc = new MessageDesc();
            desc.setLang(getLang());
            desc.setTitle(title);
            desc.setDescription(description);
            desc.setOtherCategories(otherCategories);
            desc.setTime(time);
            desc.setVicinity(vicinity);
            return desc;
        }

        public MessageDesc toEntity(Message message) {
            MessageDesc desc = toEntity();
            desc.setEntity(message);
            return desc;
        }

        /**
         * Checks that this is a non-blank welldefined description entity
         * @return if this is welldefined
         */
        public boolean isDefined() {
            return StringUtils.isNotBlank(title) &&
                    StringUtils.isNotBlank(description) &&
                    StringUtils.isNotBlank(otherCategories) &&
                    StringUtils.isNotBlank(time) &&
                    StringUtils.isNotBlank(vicinity);
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getOtherCategories() {
            return otherCategories;
        }

        public void setOtherCategories(String otherCategories) {
            this.otherCategories = otherCategories;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getVicinity() {
            return vicinity;
        }

        public void setVicinity(String vicinity) {
            this.vicinity = vicinity;
        }
    }

}


