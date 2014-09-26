package dk.dma.msinm.publish;

import dk.dma.msinm.common.settings.DefaultSetting;
import dk.dma.msinm.common.settings.Setting;
import dk.dma.msinm.common.settings.Settings;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Provides access to Twitter
 */
@Singleton
@Lock(LockType.READ)
public class TwitterProvider {

    public static final Setting API_KEY = new DefaultSetting("publishTwitterApiKey", "CqgrxkIiBA3sC35TmoZ5F5Oru");
    public static final Setting API_SECRET = new DefaultSetting("publishTwitterApiSecret", "xZXl9vsW3LCtX1Py6U2VqYUmyAK0GGYZ4RINFyXgNwV7PPcQip");
    public static final Setting ACCESS_TOKEN = new DefaultSetting("publishTwitterAccessToken", "2829892014-kqkkQLD88xhfakDlbxY0rUPdRA72Nw14e6KED0n");
    public static final Setting ACCESS_TOKEN_SECRET = new DefaultSetting("publishTwitterAccessTokenSecret", "9brE9Ed6qak2UqluvvVG1CAShqaeezEUv5pqdQ5QZQlAG");

    public static final int MAX_LENGTH = 140;

    @Inject
    Settings settings;

    TwitterFactory twitterFactory;

    /**
     * Instantiate a Twitter factory
     */
    @PostConstruct
    public void init() {
        //Instantiate a re-usable and thread-safe factory
        twitterFactory = new TwitterFactory();
    }

    /**
     * Get a new Twitter instance
     * @return a new Twitter instance
     */
    public Twitter getInstance() {
        //Instantiate a new Twitter instance
        Twitter twitter = twitterFactory.getInstance();

        //setup OAuth Consumer Credentials
        twitter.setOAuthConsumer(settings.get(API_KEY), settings.get(API_SECRET));

        //setup OAuth Access Token
        twitter.setOAuthAccessToken(new AccessToken(settings.get(ACCESS_TOKEN), settings.get(ACCESS_TOKEN_SECRET)));

        return twitter;
    }

}
