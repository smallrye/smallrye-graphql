package io.smallrye.graphql.execution.datafetcher;

import graphql.schema.DataFetcher;

/**
 * Allows DataFetchers to be plugged
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public interface PlugableDataFetcher<T> extends DataFetcher<T> {

}
