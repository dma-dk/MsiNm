package dk.dma.msinm.user.security.oauth;

import dk.dma.msinm.common.config.CdiHelper;
import org.slf4j.Logger;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.naming.NamingException;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manage the registered list of AOut providers
 */
@Singleton
@Startup
@Lock(LockType.READ)
public class OAuthProviders {

    @Inject
    Logger log;

    /**
     * Contains the list of registered providers mapped by the provider id
     */
    Map<String, Class<? extends AbstractOAuthProvider>> providers = new ConcurrentHashMap<>();

    /**
     * Should be called from the @PostConstruct method of a provider service
     *
     * @param providerService the provider service to register
     */
    public void registerProvider(AbstractOAuthProvider providerService) {
        Objects.requireNonNull(providerService);
        providers.put(providerService.getOAuthProviderId(),
                providerService.getClass());
        log.info("Registered OAuth provider " + providerService.getOAuthProviderId());
    }

    /**
     * Returns the provider bean for the given provider ID or null if not found.
     * @param providerId the provider ID
     * @return the instantiated provider service or null
     */
    public AbstractOAuthProvider getProvider(String providerId) {
        try {
            return instantiateProvider(providerId);
        } catch (NamingException e) {
            log.warn("Error instantiating provider " + providerId);
        }
        return null;
    }

    /**
     * Instantiates the provider bean for the given provider ID.
     * @param providerId the provider ID
     * @return the instantiated provider service
     */
    private AbstractOAuthProvider instantiateProvider(String providerId) throws NamingException {
        return CdiHelper.getBean(providers.get(providerId));
    }

}
