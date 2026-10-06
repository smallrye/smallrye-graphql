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
import io.smallrye.graphql.execution.datafetcher.FieldDataFetcher;
import io.smallrye.graphql.execution.datafetcher.MultiDataFetcher;
import io.smallrye.graphql.execution.datafetcher.PlugableBatchableDataFetcher;
import io.smallrye.graphql.execution.datafetcher.PlugableDataFetcher;
import io.smallrye.graphql.execution.datafetcher.PublisherDataFetcher;
import io.smallrye.graphql.execution.datafetcher.UniDataFetcher;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.Type;
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

    public <T> DataFetcher<T> getDataFetcher(Operation operation, Type type) {
        return (DataFetcher<T>) get(operation, type);
    }

    public <T> PlugableDataFetcher<T> getFieldDataFetcher(Field field, Type type, Reference owner) {
        for (DataFetcherService dfe : dataFetcherServices) {
            PlugableDataFetcher df = dfe.getFieldDataFetcher(field, type, owner);
            if (df != null) {
                return (PlugableDataFetcher) df;
            }
        }
        return new FieldDataFetcher<>(field, type, owner);
    }

    public <K, T> BatchLoaderWithContext<K, T> getSourceBatchLoader(Operation operation, Type type) {
        return (BatchLoaderWithContext<K, T>) get(operation, type);
    }

    public Wrapper unwrap(Field field, boolean isBatch) {
        if (isFutureLike(field) && isBatch) {
            return field.getWrapper().getWrapper().getWrapper();
        } else if (isFutureLike(field)) {
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

    private <V> V get(Operation operation, Type type) {
        if (isCompletionStage(operation)) {
            return (V) getCompletionStageDataFetcher(operation, type);
        } else if (isMutinyUni(operation)) {
            return (V) getUniDataFetcher(operation, type);
        } else if (isMutinyMulti(operation)) {
            return (V) getMultiDataFetcher(operation, type);
        } else if (isPublisher(operation)) {
            return (V) getPublisherDataFetcher(operation, type);
        } else if (isFlowPublisher(operation)) {
            return (V) getPublisherDataFetcher(operation, type);
        } else if (isWrapped(operation)) {
            return (V) getOtherWrappedDataFetcher(operation, type);
        }
        return (V) getOtherFieldDataFetcher(operation, type);
    }

    private PlugableBatchableDataFetcher getCompletionStageDataFetcher(Operation operation, Type type) {

        for (DataFetcherService dfe : dataFetcherServices) {
            PlugableBatchableDataFetcher df = dfe.getCompletionStageDataFetcher(operation, type);
            if (df != null) {
                return df;
            }
        }

        return new CompletionStageDataFetcher(operation, type);
    }

    private PlugableBatchableDataFetcher getUniDataFetcher(Operation operation, Type type) {

        for (DataFetcherService dfe : dataFetcherServices) {
            PlugableBatchableDataFetcher df = dfe.getUniDataFetcher(operation, type);
            if (df != null) {
                return df;
            }
        }

        return new UniDataFetcher(operation, type);
    }

    private PlugableBatchableDataFetcher getPublisherDataFetcher(Operation operation, Type type) {

        for (DataFetcherService dfe : dataFetcherServices) {
            PlugableBatchableDataFetcher df = dfe.getPublisherDataFetcher(operation, type);
            if (df != null) {
                return df;
            }
        }

        return new PublisherDataFetcher(operation, type);
    }

    private PlugableBatchableDataFetcher getMultiDataFetcher(Operation operation, Type type) {

        for (DataFetcherService dfe : dataFetcherServices) {
            PlugableBatchableDataFetcher df = dfe.getMultiDataFetcher(operation, type);
            if (df != null) {
                return df;
            }
        }

        return new MultiDataFetcher(operation, type);
    }

    private PlugableBatchableDataFetcher getOtherWrappedDataFetcher(Operation operation, Type type) {

        for (DataFetcherService dfe : dataFetcherServices) {
            PlugableBatchableDataFetcher df = dfe.getOtherWrappedDataFetcher(operation, type);
            if (df != null) {
                return df;
            }
        }

        return getDefaultDataFetcher(operation, type);
    }

    private PlugableBatchableDataFetcher getOtherFieldDataFetcher(Operation operation, Type type) {

        for (DataFetcherService dfe : dataFetcherServices) {
            PlugableBatchableDataFetcher df = dfe.getOtherFieldDataFetcher(operation, type);
            if (df != null) {
                return df;
            }
        }

        for (DataFetcherService dfe : dataFetcherServices) {
            PlugableBatchableDataFetcher df = dfe.getDefaultDataFetcher(operation, type);
            if (df != null) {
                return df;
            }
        }

        return new DefaultDataFetcher(operation, type);
    }

    private PlugableBatchableDataFetcher getDefaultDataFetcher(Operation operation, Type type) {
        return getOtherFieldDataFetcher(operation, type);
    }

    private boolean isFutureLike(Field field) {
        return isCompletionStage(field) || isMutinyUni(field) || isMutinyMulti(field);
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

    private boolean isFlowPublisher(Field field) {
        if (field.hasWrapper()) {
            String wrapperClassName = field.getWrapper().getWrapperClassName();
            return wrapperClassName.equals("java.util.concurrent.Flow$Publisher");
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
