package io.smallrye.graphql.execution;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.graphql.ConfigKey;

/**
 * Configuration for GraphQL
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class GraphQLConfig {

    private static final String SUPPOSED_EMPTY_STRING = "**empty**";
    private static final List<String> SUPPOSED_EMPTY_LIST = singletonList(SUPPOSED_EMPTY_STRING);

    @Inject
    @ConfigProperty(name = ConfigKey.EXCEPTION_BLACK_LIST, defaultValue = SUPPOSED_EMPTY_STRING)
    private List<String> blackList;

    @Inject
    @ConfigProperty(name = ConfigKey.EXCEPTION_WHITE_LIST, defaultValue = SUPPOSED_EMPTY_STRING)
    private List<String> whiteList;

    @Inject
    @ConfigProperty(name = ConfigKey.DEFAULT_ERROR_MESSAGE, defaultValue = "Server Error")
    private String defaultErrorMessage;

    @Inject
    @ConfigProperty(name = "mp.graphql.printDataFetcherException", defaultValue = "false")
    private boolean printDataFetcherException;

    @Inject
    @ConfigProperty(name = "mp.graphql.allowGet", defaultValue = "false")
    private boolean allowGet;

    @Inject
    @ConfigProperty(name = "smallrye.graphql.metrics.enabled", defaultValue = "false")
    private boolean metricsEnabled;

    public String getDefaultErrorMessage() {
        return defaultErrorMessage;
    }

    public void setDefaultErrorMessage(String defaultErrorMessage) {
        this.defaultErrorMessage = defaultErrorMessage;
    }

    public boolean isPrintDataFetcherException() {
        return printDataFetcherException;
    }

    public void setPrintDataFetcherException(boolean printDataFetcherException) {
        this.printDataFetcherException = printDataFetcherException;
    }

    public List<String> getBlackList() {
        if (SUPPOSED_EMPTY_LIST.equals(blackList)) {
            blackList = emptyList();
        }
        return blackList;
    }

    public void setBlackList(List<String> blackList) {
        this.blackList = blackList;
    }

    public List<String> getWhiteList() {
        if (SUPPOSED_EMPTY_LIST.equals(whiteList)) {
            whiteList = emptyList();
        }
        return whiteList;
    }

    public void setWhiteList(List<String> whiteList) {
        this.whiteList = whiteList;
    }

    public boolean isAllowGet() {
        return allowGet;
    }

    public void setAllowGet(boolean allowGet) {
        this.allowGet = allowGet;
    }

    public boolean isMetricsEnabled() {
        return metricsEnabled;
    }
}
