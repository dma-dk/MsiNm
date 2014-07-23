package dk.dma.msinm.web.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dk.dma.msinm.common.settings.Settings;
import dk.dma.msinm.common.settings.SettingsEntity;
import org.apache.commons.lang.StringUtils;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.*;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Rest API for accessing Settings
 */
@Path("/admin/settings")
@Stateless
@SecurityDomain("msinm-policy")
@RolesAllowed({ "admin" })
public class SettingsRestService {

    @Inject
    Logger log;

    @Inject
    Settings settings;

    /**
     * Returns all charts
     * @return returns all charts
     */
    @GET
    @Path("/all")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
    public List<SettingsVo> getSettings() {
        return settings
                .getAll()
                .stream()
                .map(SettingsVo::new)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/setting/{key}")
    @Produces("application/json")
    public SettingsVo getSetting(@PathParam("key") String key) throws Exception {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("Invalid empty key");
        }
        log.info("Getting setting " + key);
        SettingsVo setting = new SettingsVo();
        setting.setKey(key);
        setting.setValue(settings.get(key.trim()));
        return setting;
    }

    @PUT
    @Path("/setting")
    @Consumes("application/json")
    @Produces("application/json")
    public String updateSetting(SettingsVo settingVo) throws Exception {
        SettingsEntity setting = settingVo.toEntity();
        log.info("Updating setting " + setting);
        settings.updateSetting(setting);
        return "OK";
    }


    /**
     * VO for settings
     */
    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class SettingsVo implements Serializable {

        String key;
        String value;

        /**
         * Constructor
         */
        public SettingsVo() {
        }

        /**
         * Constructor
         */
        public SettingsVo(SettingsEntity entity) {
            key = entity.getKey();
            value = entity.getValue();
        }

        /**
         * Converts the VO to an entity
         */
        public SettingsEntity toEntity() {
            SettingsEntity entity = new SettingsEntity();
            entity.setKey(key);
            entity.setValue(value);
            return entity;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
