package io.smallrye.graphql.bootstrap;

import java.util.List;

/**
 * Configuration for GraphQL
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public interface Config {

    default String getDefaultErrorMessage() {
        return "System error";
    }

    default boolean isPrintDataFetcherException() {
        return false;
    }

    default List<String> getBlackList() {
        return null;
    }

    default List<String> getWhiteList() {
        return null;
    }

    default boolean isAllowGet() {
        return false;
    }

    default boolean isMetricsEnabled() {
        return false;
    }
}