package io.smallrye.graphql.bootstrap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.dataloader.BatchLoaderWithContext;

import graphql.schema.DataFetcher;
import io.smallrye.graphql.execution.datafetcher.CompletionStageDataFetcher;
import io.smallrye.graphql.execution.datafetcher.DefaultDataFetcher;
import io.smallrye.graphql.execution.datafetcher.MultiDataFetcher;
import io.smallrye.graphql.execution.datafetcher.PlugableDataFetcher;
import io.smallrye.graphql.execution.datafetcher.PublisherDataFetcher;
import io.smallrye.graphql.execution.datafetcher.UniDataFetcher;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.schema.model.Wrapper;
import io.smallrye.graphql.spi.DataFetcherService;

/**
 * Create the datafetchers for a certain operation
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class DataFetcherFactory {

    private List<DataFetcherService> dataFetcherServices = new ArrayList<>();

    public DataFetcherFactory() {
        Iterator<DataFetcherService> i = ServiceLoader.load(DataFetcherService.class).iterator();
        while (i.hasNext()) {
            dataFetcherServices.add(i.next());
        }

        Collections.sort(dataFetcherServices, new Comparator<DataFetcherService>() {
            @Override
            public int compare(DataFetcherService o1, DataFetcherService o2) {
                return o1.getPriority().compareTo(o2.getPriority());
            }
        });
    }

    public <T> DataFetcher<T> getDataFetcher(Operation operation) {
        return (DataFetcher<T>) get(operation);
    }

    public <K, T> BatchLoaderWithContext<K, T> getSourceBatchLoader(Operation operation) {
        return (BatchLoaderWithContext<K, T>) get(operation);
    }

    public Wrapper unwrap(Field field, boolean isBatch) {
        if (isAsync(field) && isBatch) {
            return field.getWrapper().getWrapper().getWrapper();
        } else if (isAsync(field)) {
            return field.getWrapper().getWrapper();
        } else if (isBatch) {
            return field.getWrapper().getWrapper();
        } else if (field.hasWrapper() && field.getWrapper().isCollectionOrArrayOrMap()) {
            return field.getWrapper();
        } else if (field.hasWrapper()) {
            // TODO: Move Generics logic here ?
        }
        return null;
    }

    private <V> V get(Operation operation) {
        if (isCompletionStage(operation)) {
            return (V) getCompletionStageDataFetcher(operation);
        } else if (isMutinyUni(operation)) {
            return (V) getUniDataFetcher(operation);
        } else if (isPublisher(operation)) {
            return (V) getPublisherDataFetcher(operation);
        } else if (isMutinyMulti(operation)) {
            return (V) getMultiDataFetcher(operation);
        } else if (isWrapped(operation)) {
            return (V) getOtherWrappedDataFetcher(operation);
        }
        return (V) getOtherFieldDataFetcher(operation);
    }

    public PlugableDataFetcher getCompletionStageDataFetcher(Operation operation) {

        for (DataFetcherService dfe : dataFetcherServices) {
            PlugableDataFetcher df = dfe.getCompletionStageDataFetcher(operation);
            if (df != null) {
                return df;
            }
        }

        return new CompletionStageDataFetcher(operation);
    }

    public PlugableDataFetcher getUniDataFetcher(Operation operation) {

        for (DataFetcherService dfe : dataFetcherServices) {
            PlugableDataFetcher df = dfe.getUniDataFetcher(operation);
            if (df != null) {
                return df;
            }
        }

        return new UniDataFetcher(operation);
    }

    public PlugableDataFetcher getPublisherDataFetcher(Operation operation) {

        for (DataFetcherService dfe : dataFetcherServices) {
            PlugableDataFetcher df = dfe.getPublisherDataFetcher(operation);
            if (df != null) {
                return df;
            }
        }

        return new PublisherDataFetcher(operation);
    }

    public PlugableDataFetcher getMultiDataFetcher(Operation operation) {

        for (DataFetcherService dfe : dataFetcherServices) {
            PlugableDataFetcher df = dfe.getMultiDataFetcher(operation);
            if (df != null) {
                return df;
            }
        }

        return new MultiDataFetcher(operation);
    }

    public PlugableDataFetcher getOtherWrappedDataFetcher(Operation operation) {

        for (DataFetcherService dfe : dataFetcherServices) {
            PlugableDataFetcher df = dfe.getOtherWrappedDataFetcher(operation);
            if (df != null) {
                return df;
            }
        }

        return getDefaultDataFetcher(operation);
    }

    public PlugableDataFetcher getOtherFieldDataFetcher(Operation operation) {

        for (DataFetcherService dfe : dataFetcherServices) {
            PlugableDataFetcher df = dfe.getOtherFieldDataFetcher(operation);
            if (df != null) {
                return df;
            }
        }

        for (DataFetcherService dfe : dataFetcherServices) {
            PlugableDataFetcher df = dfe.getDefaultDataFetcher(operation);
            if (df != null) {
                return df;
            }
        }

        return new DefaultDataFetcher(operation);
    }

    public PlugableDataFetcher getDefaultDataFetcher(Operation operation) {
        return getOtherFieldDataFetcher(operation);
    }

    private boolean isAsync(Field field) {
        return isCompletionStage(field) || isMutinyUni(field);
    }

    private boolean isWrapped(Field field) {
        return field.hasWrapper();
    }

    private boolean isCompletionStage(Field field) {
        if (field.hasWrapper()) {
            String wrapperClassName = field.getWrapper().getWrapperClassName();
            return wrapperClassName.equals(CompletableFuture.class.getName())
                    || wrapperClassName.equals(CompletionStage.class.getName());
        }
        return false;
    }

    private boolean isMutinyUni(Field field) {
        if (field.hasWrapper()) {
            String wrapperClassName = field.getWrapper().getWrapperClassName();
            return wrapperClassName.equals("io.smallrye.mutiny.Uni");
        }
        return false;
    }

    private boolean isPublisher(Field field) {
        if (field.hasWrapper()) {
            String wrapperClassName = field.getWrapper().getWrapperClassName();
            return wrapperClassName.equals("org.reactivestreams.Publisher");
        }
        return false;
    }

    private boolean isMutinyMulti(Field field) {
        if (field.hasWrapper()) {
            String wrapperClassName = field.getWrapper().getWrapperClassName();
            return wrapperClassName.equals("io.smallrye.mutiny.Multi");
        }
        return false;
    }
}
