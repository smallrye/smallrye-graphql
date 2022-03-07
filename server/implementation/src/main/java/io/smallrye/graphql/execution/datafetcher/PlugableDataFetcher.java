package io.smallrye.graphql.execution.datafetcher;

import org.dataloader.BatchLoaderWithContext;

import graphql.schema.DataFetcher;

/**
 * Allows DataFetchers to be plugged
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public interface PlugableDataFetcher<K, T> extends DataFetcher<T>, BatchLoaderWithContext<K, T> {

}
