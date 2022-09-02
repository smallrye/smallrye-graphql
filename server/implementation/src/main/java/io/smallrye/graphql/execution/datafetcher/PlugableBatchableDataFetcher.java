package io.smallrye.graphql.execution.datafetcher;

import org.dataloader.BatchLoaderWithContext;

/**
 * Allows DataFetchers to be plugged
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public interface PlugableBatchableDataFetcher<K, T> extends PlugableDataFetcher<T>, BatchLoaderWithContext<K, T> {

}
