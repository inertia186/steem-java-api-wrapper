package eu.bittrade.libs.steem.api.wrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;

import eu.bittrade.libs.steem.api.wrapper.communication.CommunicationHandler;
import eu.bittrade.libs.steem.api.wrapper.communication.DiscussionSortType;
import eu.bittrade.libs.steem.api.wrapper.communication.RequestMethods;
import eu.bittrade.libs.steem.api.wrapper.communication.SteemApis;
import eu.bittrade.libs.steem.api.wrapper.communication.dto.GetDiscussionParametersDTO;
import eu.bittrade.libs.steem.api.wrapper.communication.dto.RequestWrapper;
import eu.bittrade.libs.steem.api.wrapper.configuration.SteemApiWrapperConfig;
import eu.bittrade.libs.steem.api.wrapper.exceptions.SteemConnectionException;
import eu.bittrade.libs.steem.api.wrapper.exceptions.SteemResponseError;
import eu.bittrade.libs.steem.api.wrapper.exceptions.SteemTimeoutException;
import eu.bittrade.libs.steem.api.wrapper.exceptions.SteemTransformationException;
import eu.bittrade.libs.steem.api.wrapper.models.AccountActivity;
import eu.bittrade.libs.steem.api.wrapper.models.ActiveVote;
import eu.bittrade.libs.steem.api.wrapper.models.Block;
import eu.bittrade.libs.steem.api.wrapper.models.BlockHeader;
import eu.bittrade.libs.steem.api.wrapper.models.ChainProperties;
import eu.bittrade.libs.steem.api.wrapper.models.Config;
import eu.bittrade.libs.steem.api.wrapper.models.Discussion;
import eu.bittrade.libs.steem.api.wrapper.models.FeedHistory;
import eu.bittrade.libs.steem.api.wrapper.models.GlobalProperties;
import eu.bittrade.libs.steem.api.wrapper.models.HardforkSchedule;
import eu.bittrade.libs.steem.api.wrapper.models.OrderBook;
import eu.bittrade.libs.steem.api.wrapper.models.Price;
import eu.bittrade.libs.steem.api.wrapper.models.TrendingTag;
import eu.bittrade.libs.steem.api.wrapper.models.UserOrder;
import eu.bittrade.libs.steem.api.wrapper.models.Version;
import eu.bittrade.libs.steem.api.wrapper.models.Vote;
import eu.bittrade.libs.steem.api.wrapper.models.WitnessSchedule;
import eu.bittrade.libs.steem.api.wrapper.util.SteemApiWrapperUtil;

/**
 * This class is a wrapper for the Steem web socket API.
 * 
 * @author http://steemit.com/@dez1337
 */
public class SteemApiWrapper {
    private static final Logger LOGGER = LogManager.getLogger(SteemApiWrapper.class);

    private CommunicationHandler communicationHandler;
    private SteemApiWrapperConfig steemApiWrapperConfig;

    /**
     * Initialize the Steem API Wrapper.
     * 
     * @param steemApiWrapperConfig
     *            A SteemApiWrapperConfig object that contains the required
     *            configuration.
     * @throws SteemTimeoutException
     *             If the server was not able to answer the request in the given
     *             time (@see SteemApiWrapperConfig)
     * @throws SteemConnectionException
     *             If there is a connection problem.
     * @throws SteemTransformationException
     *             If the API Wrapper is unable to transform the JSON response
     *             into a Java object.
     * @throws SteemResponseError
     *             If the Server returned an error object.
     */
    public SteemApiWrapper(SteemApiWrapperConfig steemApiWrapperConfig)
            throws SteemConnectionException, SteemTimeoutException, SteemTransformationException, SteemResponseError {
        this.communicationHandler = new CommunicationHandler(steemApiWrapperConfig);
        this.steemApiWrapperConfig = steemApiWrapperConfig;

        if (!("").equals(String.valueOf(steemApiWrapperConfig.getPassword()))
                && !("").equals(steemApiWrapperConfig.getUsername())) {

            LOGGER.info("Calling the login method with the prodvided credentials before checking the available apis.");
            if (login(steemApiWrapperConfig.getUsername(), String.valueOf(steemApiWrapperConfig.getPassword()))) {
                LOGGER.info("You have been logged in.");
            } else {
                LOGGER.error("Login failed. The following check of available apis will be done as a anonymous user.");
            }
        } else {
            LOGGER.info(
                    "No credentials have been provided. The following check of available apis will be done as a anonymous user.");
            login("", "");
        }

        // Check all known apis
        for (SteemApis steemApi : SteemApis.values()) {
            if (getApiByName(steemApi.toString().toLowerCase()) == null) {
                LOGGER.warn("The {} is not published by the configured node.", steemApi.toString());
            }
        }
    }

    /**
     * Get the current number of registered Steem accounts.
     * 
     * @return The number of accounts.
     * @throws SteemTimeoutException
     *             If the server was not able to answer the request in the given
     *             time (@see SteemApiWrapperConfig)
     * @throws SteemConnectionException
     *             If there is a connection problem.
     * @throws SteemTransformationException
     *             If the API Wrapper is unable to transform the JSON response
     *             into a Java object.
     * @throws SteemResponseError
     *             If the Server returned an error object.
     */
    public int getAccountCount()
            throws SteemTimeoutException, SteemConnectionException, SteemTransformationException, SteemResponseError {
        RequestWrapper requestObject = new RequestWrapper();
        requestObject.setApiMethod(RequestMethods.GET_ACCOUNT_COUNT);
        requestObject.setSteemApi(SteemApis.DATABASE_API);
        String[] parameters = {};
        requestObject.setAdditionalParameters(parameters);

        return communicationHandler.performRequest(requestObject, Integer.class).get(0);
    }

    /**
     * Get the latest activities of a specific account.
     * 
     * @param accountName
     *            The user name of the account.
     * @param from
     *            The starting point.
     * @param limit
     *            The maximum number of entries.
     * @return A map containing the activities. The key is the id of the
     *         activity.
     * @throws SteemTimeoutException
     *             If the server was not able to answer the request in the given
     *             time (@see SteemApiWrapperConfig)
     * @throws SteemConnectionException
     *             If there is a connection problem.
     * @throws SteemTransformationException
     *             If the API Wrapper is unable to transform the JSON response
     *             into a Java object.
     * @throws SteemResponseError
     *             If the Server returned an error object.
     */
    public Map<Integer, AccountActivity> getAccountHistory(String accountName, int from, int limit)
            throws SteemTimeoutException, SteemConnectionException, SteemTransformationException, SteemResponseError {
        RequestWrapper requestObject = new RequestWrapper();
        requestObject.setSteemApi(SteemApis.DATABASE_API);
        requestObject.setApiMethod(RequestMethods.GET_ACCOUNT_HISTORY);
        String[] parameters = { accountName, String.valueOf(from), String.valueOf(limit) };
        requestObject.setAdditionalParameters(parameters);

        Map<Integer, AccountActivity> accountActivities = new HashMap<>();

        // TODO There are still problems with the deserialization of the op()
        // object.
        for (Object[] accountActivity : communicationHandler.performRequest(requestObject, Object[].class)) {
            accountActivities.put((Integer) accountActivity[0], communicationHandler.getObjectMapper()
                    .convertValue(accountActivity[1], new TypeReference<AccountActivity>() {
                    }));
        }

        return accountActivities;
    }

    /**
     * Get a list of all votes done by a specific account.
     * 
     * @param accountName
     *            The user name of the account.
     * @return A List of votes done by the specified account.
     * @throws SteemTimeoutException
     *             If the server was not able to answer the request in the given
     *             time (@see SteemApiWrapperConfig)
     * @throws SteemConnectionException
     *             If there is a connection problem.
     * @throws SteemTransformationException
     *             If the API Wrapper is unable to transform the JSON response
     *             into a Java object.
     * @throws SteemResponseError
     *             If the Server returned an error object.
     */
    public List<Vote> getAccountVotes(String accountName)
            throws SteemTimeoutException, SteemConnectionException, SteemTransformationException, SteemResponseError {
        RequestWrapper requestObject = new RequestWrapper();
        requestObject.setSteemApi(SteemApis.DATABASE_API);
        requestObject.setApiMethod(RequestMethods.GET_ACCOUNT_VOTES);
        String[] parameters = { accountName };
        requestObject.setAdditionalParameters(parameters);

        return communicationHandler.performRequest(requestObject, Vote.class);
    }

    /**
     * Get the current number of active witnesses.
     * 
     * @return The number of witnesses.
     * @throws SteemTimeoutException
     *             If the server was not able to answer the request in the given
     *             time (@see SteemApiWrapperConfig)
     * @throws SteemConnectionException
     *             If there is a connection problem.
     * @throws SteemTransformationException
     *             If the API Wrapper is unable to transform the JSON response
     *             into a Java object.
     * @throws SteemResponseError
     *             If the Server returned an error object.
     */
    public int getWitnessCount()
            throws SteemTimeoutException, SteemConnectionException, SteemTransformationException, SteemResponseError {
        RequestWrapper requestObject = new RequestWrapper();
        requestObject.setApiMethod(RequestMethods.GET_WITNESS_COUNT);
        requestObject.setSteemApi(SteemApis.DATABASE_API);
        String[] parameters = {};
        requestObject.setAdditionalParameters(parameters);

        return communicationHandler.performRequest(requestObject, Integer.class).get(0);
    }

    /**
     * Get the current miner queue.
     * 
     * @return A list of account names that are in the mining queue.
     * @throws SteemTimeoutException
     *             If the server was not able to answer the request in the given
     *             time (@see SteemApiWrapperConfig)
     * @throws SteemConnectionException
     *             If there is a connection problem.
     * @throws SteemTransformationException
     *             If the API Wrapper is unable to transform the JSON response
     *             into a Java object.
     * @throws SteemResponseError
     *             If the Server returned an error object.
     */
    public String[] getMinerQueue()
            throws SteemTimeoutException, SteemConnectionException, SteemTransformationException, SteemResponseError {
        RequestWrapper requestObject = new RequestWrapper();
        requestObject.setApiMethod(RequestMethods.GET_MINER_QUEUE);
        requestObject.setSteemApi(SteemApis.DATABASE_API);
        String[] parameters = {};
        requestObject.setAdditionalParameters(parameters);

        return communicationHandler.performRequest(requestObject, String[].class).get(0);
    }

    /**
     * Get the configuration.
     * 
     * @return The steem configuration.
     * @throws SteemTimeoutException
     *             If the server was not able to answer the request in the given
     *             time (@see SteemApiWrapperConfig)
     * @throws SteemConnectionException
     *             If there is a connection problem.
     * @throws SteemTransformationException
     *             If the API Wrapper is unable to transform the JSON response
     *             into a Java object.
     * @throws SteemResponseError
     *             If the Server returned an error object.
     */
    public Config getConfig()
            throws SteemTimeoutException, SteemConnectionException, SteemTransformationException, SteemResponseError {
        RequestWrapper requestObject = new RequestWrapper();
        requestObject.setApiMethod(RequestMethods.GET_CONFIG);
        requestObject.setSteemApi(SteemApis.DATABASE_API);
        String[] parameters = {};
        requestObject.setAdditionalParameters(parameters);

        return communicationHandler.performRequest(requestObject, Config.class).get(0);
    }

    /**
     * Get the version information of the connected node.
     * 
     * @return The steem version that the connected node is running.
     * @throws SteemTimeoutException
     *             If the server was not able to answer the request in the given
     *             time (@see SteemApiWrapperConfig)
     * @throws SteemConnectionException
     *             If there is a connection problem.
     * @throws SteemTransformationException
     *             If the API Wrapper is unable to transform the JSON response
     *             into a Java object.
     * @throws SteemResponseError
     *             If the Server returned an error object.
     */
    public Version getVersion()
            throws SteemTimeoutException, SteemConnectionException, SteemTransformationException, SteemResponseError {
        RequestWrapper requestObject = new RequestWrapper();
        requestObject.setApiMethod(RequestMethods.GET_VERSION);
        requestObject.setSteemApi(SteemApis.LOGIN_API);
        String[] parameters = {};
        requestObject.setAdditionalParameters(parameters);

        return communicationHandler.performRequest(requestObject, Version.class).get(0);
    }

    /**
     * Login under the use of the credentials which are stored in the config
     * object.
     * 
     * <p>
     * <b>Notice:</b> The login method is only needed to access protected apis.
     * For some apis like the broadcast_api a call of this method with empty
     * strings can be enough to access them.
     * 
     * @return true if the login was successful. False otherwise.
     * @throws SteemTimeoutException
     *             If the server was not able to answer the request in the given
     *             time (@see SteemApiWrapperConfig)
     * @throws SteemConnectionException
     *             If there is a connection problem.
     * @throws SteemTransformationException
     *             If the API Wrapper is unable to transform the JSON response
     *             into a Java object.
     * @throws SteemResponseError
     *             If the Server returned an error object.
     */
    public Boolean login()
            throws SteemTimeoutException, SteemConnectionException, SteemTransformationException, SteemResponseError {
        return login(steemApiWrapperConfig.getUsername(), String.valueOf(steemApiWrapperConfig.getPassword()));
    }

    /**
     * Login under the use of the specified credentials.
     * 
     * <p>
     * <b>Notice:</b> The login method is only needed to access protected apis.
     * For some apis like the broadcast_api a call of this method with empty
     * strings can be enough to access them.
     * 
     * @param username
     *            The user name.
     * @param password
     *            The password.
     * @return true if the login was successful. False otherwise.
     * @throws SteemTimeoutException
     *             If the server was not able to answer the request in the given
     *             time (@see SteemApiWrapperConfig)
     * @throws SteemConnectionException
     *             If there is a connection problem.
     * @throws SteemTransformationException
     *             If the API Wrapper is unable to transform the JSON response
     *             into a Java object.
     * @throws SteemResponseError
     *             If the Server returned an error object.
     */
    public Boolean login(String username, String password)
            throws SteemTimeoutException, SteemConnectionException, SteemTransformationException, SteemResponseError {
        RequestWrapper requestObject = new RequestWrapper();
        requestObject.setApiMethod(RequestMethods.LOGIN);
        requestObject.setSteemApi(SteemApis.LOGIN_API);
        String[] parameters = { username, password };
        requestObject.setAdditionalParameters(parameters);

        return communicationHandler.performRequest(requestObject, Boolean.class).get(0);
    }

    /**
     * Returns the id of an api or null if no api with the given name could be
     * found.
     * 
     * @param apiName
     *            The name of the api.
     * @return The id for the given api name or null, if the api is not active
     *         or does not exist.
     * @throws SteemTimeoutException
     *             If the server was not able to answer the request in the given
     *             time (@see SteemApiWrapperConfig)
     * @throws SteemConnectionException
     *             If there is a connection problem.
     * @throws SteemTransformationException
     *             If the API Wrapper is unable to transform the JSON response
     *             into a Java object.
     * @throws SteemResponseError
     *             If the Server returned an error object.
     */
    public String getApiByName(String apiName)
            throws SteemTimeoutException, SteemConnectionException, SteemTransformationException, SteemResponseError {
        RequestWrapper requestObject = new RequestWrapper();
        requestObject.setApiMethod(RequestMethods.GET_API_BY_NAME);
        requestObject.setSteemApi(SteemApis.LOGIN_API);
        String[] parameters = { apiName };
        requestObject.setAdditionalParameters(parameters);

        List<String> response = communicationHandler.performRequest(requestObject, String.class);
        if (response != null && response.get(0) != null) {
            return response.get(0);
        }

        return null;
    }

    /**
     * Returns detailed values for tags that match the given conditions.
     * 
     * @param firstTag
     *            Start the list after this category. An empty String will
     *            result in starting from the top.
     * @param limit
     *            The number of results.
     * @return A list of tags.
     * @throws SteemTimeoutException
     *             If the server was not able to answer the request in the given
     *             time (@see SteemApiWrapperConfig)
     * @throws SteemConnectionException
     *             If there is a connection problem.
     * @throws SteemTransformationException
     *             If the API Wrapper is unable to transform the JSON response
     *             into a Java object.
     * @throws SteemResponseError
     *             If the Server returned an error object.
     */
    public List<TrendingTag> getTrendingTags(String firstTag, int limit)
            throws SteemTimeoutException, SteemConnectionException, SteemTransformationException, SteemResponseError {
        RequestWrapper requestObject = new RequestWrapper();
        requestObject.setApiMethod(RequestMethods.GET_TRENDING_TAGS);
        requestObject.setSteemApi(SteemApis.DATABASE_API);
        String[] parameters = { firstTag, String.valueOf(limit) };
        requestObject.setAdditionalParameters(parameters);

        return communicationHandler.performRequest(requestObject, TrendingTag.class);
    }

    /**
     * Get the hardfork version.
     * 
     * @return The hardfork version that the connected node is running on.
     * @throws SteemTimeoutException
     *             If the server was not able to answer the request in the given
     *             time (@see SteemApiWrapperConfig)
     * @throws SteemConnectionException
     *             If there is a connection problem.
     * @throws SteemTransformationException
     *             If the API Wrapper is unable to transform the JSON response
     *             into a Java object.
     * @throws SteemResponseError
     *             If the Server returned an error object.
     */
    public String getHardforkVersion()
            throws SteemTimeoutException, SteemConnectionException, SteemTransformationException, SteemResponseError {
        RequestWrapper requestObject = new RequestWrapper();
        requestObject.setApiMethod(RequestMethods.GET_HARDFORK_VERSION);
        requestObject.setSteemApi(SteemApis.DATABASE_API);
        String[] parameters = {};
        requestObject.setAdditionalParameters(parameters);

        return communicationHandler.performRequest(requestObject, String.class).get(0);
    }

    /**
     * Get the witness schedule.
     * 
     * @return The witness schedule.
     * @throws SteemTimeoutException
     *             If the server was not able to answer the request in the given
     *             time (@see SteemApiWrapperConfig)
     * @throws SteemConnectionException
     *             If there is a connection problem.
     * @throws SteemTransformationException
     *             If the API Wrapper is unable to transform the JSON response
     *             into a Java object.
     * @throws SteemResponseError
     *             If the Server returned an error object.
     */
    public WitnessSchedule getWitnessSchedule()
            throws SteemTimeoutException, SteemConnectionException, SteemTransformationException, SteemResponseError {
        RequestWrapper requestObject = new RequestWrapper();
        requestObject.setApiMethod(RequestMethods.GET_WITNESS_SCHEDULE);
        requestObject.setSteemApi(SteemApis.DATABASE_API);
        String[] parameters = {};
        requestObject.setAdditionalParameters(parameters);

        return communicationHandler.performRequest(requestObject, WitnessSchedule.class).get(0);
    }

    /**
     * Search for accounts.
     * 
     * @param pattern
     *            The lower case pattern you want to search for.
     * @param limit
     *            The maximum number of account names.
     * @return A list of matching account names.
     * @throws SteemTimeoutException
     *             If the server was not able to answer the request in the given
     *             time (@see SteemApiWrapperConfig)
     * @throws SteemConnectionException
     *             If there is a connection problem.
     * @throws SteemTransformationException
     *             If the API Wrapper is unable to transform the JSON response
     *             into a Java object.
     * @throws SteemResponseError
     *             If the Server returned an error object.
     */
    public List<String> lookupAccounts(String pattern, int limit)
            throws SteemTimeoutException, SteemConnectionException, SteemTransformationException, SteemResponseError {
        RequestWrapper requestObject = new RequestWrapper();
        requestObject.setApiMethod(RequestMethods.LOOKUP_ACCOUNTS);
        requestObject.setSteemApi(SteemApis.DATABASE_API);
        String[] parameters = { pattern, String.valueOf(limit) };
        requestObject.setAdditionalParameters(parameters);

        return communicationHandler.performRequest(requestObject, String.class);
    }

    /**
     * Search for witness accounts.
     * 
     * @param pattern
     *            The lower case pattern you want to search for.
     * @param limit
     *            The maximum number of account names.
     * @return A list of matching account names.
     * @throws SteemTimeoutException
     *             If the server was not able to answer the request in the given
     *             time (@see SteemApiWrapperConfig)
     * @throws SteemConnectionException
     *             If there is a connection problem.
     * @throws SteemTransformationException
     *             If the API Wrapper is unable to transform the JSON response
     *             into a Java object.
     * @throws SteemResponseError
     *             If the Server returned an error object.
     */
    public List<String> lookupWitnessAccounts(String pattern, int limit)
            throws SteemTimeoutException, SteemConnectionException, SteemTransformationException, SteemResponseError {
        RequestWrapper requestObject = new RequestWrapper();
        requestObject.setApiMethod(RequestMethods.LOOKUP_WITNESS_ACCOUNTS);
        requestObject.setSteemApi(SteemApis.DATABASE_API);
        String[] parameters = { pattern, String.valueOf(limit) };
        requestObject.setAdditionalParameters(parameters);

        return communicationHandler.performRequest(requestObject, String.class);
    }

    /**
     * Get the global properties.
     * 
     * @return The dynamic global properties.
     * @throws SteemTimeoutException
     *             If the server was not able to answer the request in the given
     *             time (@see SteemApiWrapperConfig)
     * @throws SteemConnectionException
     *             If there is a connection problem.
     * @throws SteemTransformationException
     *             If the API Wrapper is unable to transform the JSON response
     *             into a Java object.
     * @throws SteemResponseError
     *             If the Server returned an error object.
     */
    public GlobalProperties getDynamicGlobalProperties()
            throws SteemTimeoutException, SteemConnectionException, SteemTransformationException, SteemResponseError {
        RequestWrapper requestObject = new RequestWrapper();
        requestObject.setApiMethod(RequestMethods.GET_DYNAMIC_GLOBAL_PROPERTIES);
        requestObject.setSteemApi(SteemApis.DATABASE_API);
        String[] parameters = {};
        requestObject.setAdditionalParameters(parameters);

        return communicationHandler.performRequest(requestObject, GlobalProperties.class).get(0);
    }

    /**
     * Get the chain properties.
     * 
     * @return The chain properties.
     * @throws SteemTimeoutException
     *             If the server was not able to answer the request in the given
     *             time (@see SteemApiWrapperConfig)
     * @throws SteemConnectionException
     *             If there is a connection problem.
     * @throws SteemTransformationException
     *             If the API Wrapper is unable to transform the JSON response
     *             into a Java object.
     * @throws SteemResponseError
     *             If the Server returned an error object.
     */
    public ChainProperties getChainProperties()
            throws SteemTimeoutException, SteemConnectionException, SteemTransformationException, SteemResponseError {
        RequestWrapper requestObject = new RequestWrapper();
        requestObject.setApiMethod(RequestMethods.GET_CHAIN_PROPERTIES);
        requestObject.setSteemApi(SteemApis.DATABASE_API);
        String[] parameters = {};
        requestObject.setAdditionalParameters(parameters);

        return communicationHandler.performRequest(requestObject, ChainProperties.class).get(0);
    }

    /**
     * Get the current median price.
     * 
     * @return The current median history price.
     * @throws SteemTimeoutException
     *             If the server was not able to answer the request in the given
     *             time (@see SteemApiWrapperConfig)
     * @throws SteemConnectionException
     *             If there is a connection problem.
     * @throws SteemTransformationException
     *             If the API Wrapper is unable to transform the JSON response
     *             into a Java object.
     * @throws SteemResponseError
     *             If the Server returned an error object.
     */
    public Price getCurrentMedianHistoryPrice()
            throws SteemTimeoutException, SteemConnectionException, SteemTransformationException, SteemResponseError {
        RequestWrapper requestObject = new RequestWrapper();
        requestObject.setApiMethod(RequestMethods.GET_CURRENT_MEDIAN_HISTORY_PRICE);
        requestObject.setSteemApi(SteemApis.DATABASE_API);
        String[] parameters = {};
        requestObject.setAdditionalParameters(parameters);

        return communicationHandler.performRequest(requestObject, Price.class).get(0);
    }

    /**
     * Get the details of a specific post.
     * 
     * @param author
     *            The authors name.
     * @param permlink
     *            The permlink of the article.
     * @return The details of a specific post.
     * @throws SteemTimeoutException
     *             If the server was not able to answer the request in the given
     *             time (@see SteemApiWrapperConfig)
     * @throws SteemConnectionException
     *             If there is a connection problem.
     * @throws SteemTransformationException
     *             If the API Wrapper is unable to transform the JSON response
     *             into a Java object.
     * @throws SteemResponseError
     *             If the Server returned an error object.
     */
    public Discussion getContent(String author, String permlink)
            throws SteemTimeoutException, SteemConnectionException, SteemTransformationException, SteemResponseError {
        RequestWrapper requestObject = new RequestWrapper();
        requestObject.setApiMethod(RequestMethods.GET_CONTENT);
        requestObject.setSteemApi(SteemApis.DATABASE_API);
        String[] parameters = { author, permlink };
        requestObject.setAdditionalParameters(parameters);

        return communicationHandler.performRequest(requestObject, Discussion.class).get(0);
    }

    /**
     * Get the replies of a specific post.
     * 
     * @param author
     *            The authors name.
     * @param permlink
     *            The permlink of the article.
     * @return A list of discussions or null if the post has no replies.
     * @throws SteemTimeoutException
     *             If the server was not able to answer the request in the given
     *             time (@see SteemApiWrapperConfig)
     * @throws SteemConnectionException
     *             If there is a connection problem.
     * @throws SteemTransformationException
     *             If the API Wrapper is unable to transform the JSON response
     *             into a Java object.
     * @throws SteemResponseError
     *             If the Server returned an error object.
     */
    public List<Discussion> getContentReplies(String author, String permlink)
            throws SteemTimeoutException, SteemConnectionException, SteemTransformationException, SteemResponseError {
        RequestWrapper requestObject = new RequestWrapper();
        requestObject.setApiMethod(RequestMethods.GET_CONTENT_REPLIES);
        requestObject.setSteemApi(SteemApis.DATABASE_API);
        String[] parameters = { author, permlink };
        requestObject.setAdditionalParameters(parameters);

        return communicationHandler.performRequest(requestObject, Discussion.class);
    }

    /**
     * Get the active votes for a given post of a given author.
     * 
     * @param author
     *            The authors name.
     * @param permlink
     *            The permlink of the article.
     * @return A list of votes for a specific article.
     * @throws SteemTimeoutException
     *             If the server was not able to answer the request in the given
     *             time (@see SteemApiWrapperConfig)
     * @throws SteemConnectionException
     *             If there is a connection problem.
     * @throws SteemTransformationException
     *             If the API Wrapper is unable to transform the JSON response
     *             into a Java object.
     * @throws SteemResponseError
     *             If the Server returned an error object.
     */
    public List<ActiveVote> getActiveVotes(String author, String permlink)
            throws SteemTimeoutException, SteemConnectionException, SteemTransformationException, SteemResponseError {
        RequestWrapper requestObject = new RequestWrapper();
        requestObject.setApiMethod(RequestMethods.GET_ACTIVE_VOTES);
        requestObject.setSteemApi(SteemApis.DATABASE_API);
        String[] parameters = { author, permlink };
        requestObject.setAdditionalParameters(parameters);

        return communicationHandler.performRequest(requestObject, ActiveVote.class);
    }

    /**
     * Get active discussions for a specified tag.
     * 
     * @param tag
     *            Get discussions that are tagged with this tag.
     * @param limit
     *            The number of results.
     * @param sortBy
     *            The way how the results should be sorted by.
     * @return A list of discussions.
     * @throws SteemTimeoutException
     *             If the server was not able to answer the request in the given
     *             time (@see SteemApiWrapperConfig)
     * @throws SteemConnectionException
     *             If there is a connection problem.
     * @throws SteemTransformationException
     *             If the API Wrapper is unable to transform the JSON response
     *             into a Java object.
     * @throws SteemResponseError
     *             If the Server returned an error object.
     */
    public List<Discussion> getDiscussionsBy(String tag, int limit, DiscussionSortType sortBy)
            throws SteemTimeoutException, SteemConnectionException, SteemTransformationException, SteemResponseError {
        RequestWrapper requestObject = new RequestWrapper();

        requestObject.setApiMethod(SteemApiWrapperUtil.getEquivalentRequestMethod(sortBy));
        requestObject.setSteemApi(SteemApis.DATABASE_API);
        // This steem api is the most non standardized shit I've ever seen in my
        // life. Here goes the workaround:
        GetDiscussionParametersDTO getDiscussionParameterDTO = new GetDiscussionParametersDTO();
        getDiscussionParameterDTO.setTag(tag);
        getDiscussionParameterDTO.setLimit(String.valueOf(limit));
        Object[] parameters = { getDiscussionParameterDTO };
        requestObject.setAdditionalParameters(parameters);

        return communicationHandler.performRequest(requestObject, Discussion.class);
    }

    /**
     * Get only the header of a block instead of the complete one.
     * 
     * @param blockNumber
     *            The id of the block the header should be requested from.
     * @return The header of a block.
     * @throws SteemTimeoutException
     *             If the server was not able to answer the request in the given
     *             time (@see SteemApiWrapperConfig)
     * @throws SteemConnectionException
     *             If there is a connection problem.
     * @throws SteemTransformationException
     *             If the API Wrapper is unable to transform the JSON response
     *             into a Java object.
     * @throws SteemResponseError
     *             If the Server returned an error object.
     */
    public BlockHeader getBlockHeader(long blockNumber)
            throws SteemTimeoutException, SteemConnectionException, SteemTransformationException, SteemResponseError {
        RequestWrapper requestObject = new RequestWrapper();
        requestObject.setApiMethod(RequestMethods.GET_BLOCK_HEADER);
        requestObject.setSteemApi(SteemApis.LOGIN_API);
        String[] parameters = { String.valueOf(blockNumber) };
        requestObject.setAdditionalParameters(parameters);

        return communicationHandler.performRequest(requestObject, BlockHeader.class).get(0);
    }

    /**
     * Get a complete block by a given block number including all transactions
     * of this block.
     * 
     * @param blockNumber
     *            The id of the block the header should be requested from.
     * @return A complete block.
     * @throws SteemTimeoutException
     *             If the server was not able to answer the request in the given
     *             time (@see SteemApiWrapperConfig)
     * @throws SteemConnectionException
     *             If there is a connection problem.
     * @throws SteemTransformationException
     *             If the API Wrapper is unable to transform the JSON response
     *             into a Java object.
     * @throws SteemResponseError
     *             If the Server returned an error object.
     */
    public Block getBlock(long blockNumber)
            throws SteemTimeoutException, SteemConnectionException, SteemTransformationException, SteemResponseError {
        RequestWrapper requestObject = new RequestWrapper();
        requestObject.setApiMethod(RequestMethods.GET_BLOCK);
        requestObject.setSteemApi(SteemApis.LOGIN_API);
        String[] parameters = { String.valueOf(blockNumber) };
        requestObject.setAdditionalParameters(parameters);

        return communicationHandler.performRequest(requestObject, Block.class).get(0);
    }

    /**
     * TODO: Look up what this is used for and what it can return.
     * 
     * @return Unknown
     * @throws SteemTimeoutException
     *             If the server was not able to answer the request in the given
     *             time (@see SteemApiWrapperConfig)
     * @throws SteemConnectionException
     *             If there is a connection problem.
     * @throws SteemTransformationException
     *             If the API Wrapper is unable to transform the JSON response
     *             into a Java object.
     * @throws SteemResponseError
     *             If the Server returned an error object.
     */
    public Object[] getConversationRequests()
            throws SteemTimeoutException, SteemConnectionException, SteemTransformationException, SteemResponseError {
        RequestWrapper requestObject = new RequestWrapper();
        requestObject.setApiMethod(RequestMethods.GET_COVERSATION_REQUEST);
        requestObject.setSteemApi(SteemApis.LOGIN_API);
        String[] parameters = {};
        requestObject.setAdditionalParameters(parameters);

        return communicationHandler.performRequest(requestObject, Object[].class).get(0);
    }

    /**
     * Get the current price and a list of history prices combined in one
     * object.
     * 
     * @return The current price and a list of past prices.
     * @throws SteemTimeoutException
     *             If the server was not able to answer the request in the given
     *             time (@see SteemApiWrapperConfig)
     * @throws SteemConnectionException
     *             If there is a connection problem.
     * @throws SteemTransformationException
     *             If the API Wrapper is unable to transform the JSON response
     *             into a Java object.
     * @throws SteemResponseError
     *             If the Server returned an error object.
     */
    public FeedHistory getFeedHistory()
            throws SteemTimeoutException, SteemConnectionException, SteemTransformationException, SteemResponseError {
        RequestWrapper requestObject = new RequestWrapper();
        requestObject.setApiMethod(RequestMethods.GET_FEED_HISTORY);
        requestObject.setSteemApi(SteemApis.LOGIN_API);
        String[] parameters = {};
        requestObject.setAdditionalParameters(parameters);

        return communicationHandler.performRequest(requestObject, FeedHistory.class).get(0);
    }

    /**
     * TODO: Check what this method is supposed to do. In a fist test it seems
     * to return the time since the current version is active.
     * 
     * @return ???
     * @throws SteemTimeoutException
     *             If the server was not able to answer the request in the given
     *             time (@see SteemApiWrapperConfig)
     * @throws SteemConnectionException
     *             If there is a connection problem.
     * @throws SteemTransformationException
     *             If the API Wrapper is unable to transform the JSON response
     *             into a Java object.
     * @throws SteemResponseError
     *             If the Server returned an error object.
     */
    public HardforkSchedule getNextScheduledHarfork()
            throws SteemTimeoutException, SteemConnectionException, SteemTransformationException, SteemResponseError {
        RequestWrapper requestObject = new RequestWrapper();
        requestObject.setApiMethod(RequestMethods.GET_NEXT_SCHEDULED_HARDFORK);
        requestObject.setSteemApi(SteemApis.LOGIN_API);
        String[] parameters = {};
        requestObject.setAdditionalParameters(parameters);

        return communicationHandler.performRequest(requestObject, HardforkSchedule.class).get(0);
    }

    /**
     * Get all open orders of a specified account.
     * 
     * @param accountName
     *            The name of the account.
     * @return A list of open orders for this account.
     * @throws SteemTimeoutException
     *             If the server was not able to answer the request in the given
     *             time (@see SteemApiWrapperConfig)
     * @throws SteemConnectionException
     *             If there is a connection problem.
     * @throws SteemTransformationException
     *             If the API Wrapper is unable to transform the JSON response
     *             into a Java object.
     * @throws SteemResponseError
     *             If the Server returned an error object.
     */
    public List<UserOrder> getOpenOrders(String accountName)
            throws SteemTimeoutException, SteemConnectionException, SteemTransformationException, SteemResponseError {
        RequestWrapper requestObject = new RequestWrapper();
        requestObject.setApiMethod(RequestMethods.GET_OPEN_ORDERS);
        requestObject.setSteemApi(SteemApis.LOGIN_API);
        String[] parameters = { accountName };
        requestObject.setAdditionalParameters(parameters);

        return communicationHandler.performRequest(requestObject, UserOrder.class);
    }

    /**
     * Get a list of orders of the order book.
     * 
     * @param limit
     *            The maximum number of results for each category (asks / bids).
     * @return A list of current asks and bids.
     * @throws SteemTimeoutException
     *             If the server was not able to answer the request in the given
     *             time (@see SteemApiWrapperConfig)
     * @throws SteemConnectionException
     *             If there is a connection problem.
     * @throws SteemTransformationException
     *             If the API Wrapper is unable to transform the JSON response
     *             into a Java object.
     * @throws SteemResponseError
     *             If the Server returned an error object.
     */
    public OrderBook getOrderBook(int limit)
            throws SteemTimeoutException, SteemConnectionException, SteemTransformationException, SteemResponseError {
        RequestWrapper requestObject = new RequestWrapper();
        requestObject.setApiMethod(RequestMethods.GET_ORDER_BOOK);
        requestObject.setSteemApi(SteemApis.LOGIN_API);
        String[] parameters = { String.valueOf(limit) };
        requestObject.setAdditionalParameters(parameters);

        return communicationHandler.performRequest(requestObject, OrderBook.class).get(0);
    }

    /**
     * Get the account names of the active witnesses.
     * 
     * @return A list of account names of the active witnesses.
     * @throws SteemTimeoutException
     *             If the server was not able to answer the request in the given
     *             time (@see SteemApiWrapperConfig)
     * @throws SteemConnectionException
     *             If there is a connection problem.
     * @throws SteemTransformationException
     *             If the API Wrapper is unable to transform the JSON response
     *             into a Java object.
     * @throws SteemResponseError
     *             If the Server returned an error object.
     */
    public String[] getActiveWitnesses()
            throws SteemTimeoutException, SteemConnectionException, SteemTransformationException, SteemResponseError {
        RequestWrapper requestObject = new RequestWrapper();
        requestObject.setApiMethod(RequestMethods.GET_ACTIVE_WITNESSES);
        requestObject.setSteemApi(SteemApis.DATABASE_API);
        String[] parameters = {};
        requestObject.setAdditionalParameters(parameters);

        return communicationHandler.performRequest(requestObject, String[].class).get(0);
    }

    /**
     * Search for users under the use of their public key(s).
     * 
     * @param publicKeys
     *            An array containing one or more public keys.
     * @return A list of arrays containing the matching account names.
     * @throws SteemTimeoutException
     *             If the server was not able to answer the request in the given
     *             time (@see SteemApiWrapperConfig)
     * @throws SteemConnectionException
     *             If there is a connection problem.
     * @throws SteemTransformationException
     *             If the API Wrapper is unable to transform the JSON response
     *             into a Java object.
     * @throws SteemResponseError
     *             If the Server returned an error object.
     */
    public List<String[]> getKeyReferences(String[] publicKeys)
            throws SteemTimeoutException, SteemConnectionException, SteemTransformationException, SteemResponseError {
        RequestWrapper requestObject = new RequestWrapper();
        requestObject.setApiMethod(RequestMethods.GET_KEY_REFERENCES);
        requestObject.setSteemApi(SteemApis.ACCOUNT_BY_KEY_API);
        Object[] parameters = { publicKeys };
        requestObject.setAdditionalParameters(parameters);

        return communicationHandler.performRequest(requestObject, String[].class);
    }

    // TODO implement this!
    public Boolean broadcastTransactionSynchronous(String trx)
            throws SteemTimeoutException, SteemConnectionException, SteemTransformationException, SteemResponseError {
        RequestWrapper requestObject = new RequestWrapper();
        requestObject.setApiMethod(RequestMethods.BROADCAST_TRANSACTION_SYNCHRONOUS);
        requestObject.setSteemApi(SteemApis.NETWORK_BROADCAST_API);
        String[] parameters = { trx };
        requestObject.setAdditionalParameters(parameters);

        return null;
    }

    // TODO implement this!
    public Boolean broadcastTransaction(String trx)
            throws SteemTimeoutException, SteemConnectionException, SteemTransformationException, SteemResponseError {
        RequestWrapper requestObject = new RequestWrapper();
        requestObject.setApiMethod(RequestMethods.BROADCAST_TRANSACTION);
        requestObject.setSteemApi(SteemApis.NETWORK_BROADCAST_API);
        String[] parameters = { trx };
        requestObject.setAdditionalParameters(parameters);

        communicationHandler.performRequest(requestObject, Object[].class);
        return null;
    }
}
