package io.smallrye.graphql.execution.datafetcher.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dataloader.BatchLoaderEnvironment;
import org.dataloader.DataLoader;

import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.execution.context.SmallRyeBatchLoaderContextProvider;
import io.smallrye.graphql.schema.model.Operation;

/**
 * Helping with BatchLoaders
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class BatchLoaderHelper {

    public String getName(Operation operation) {
        return operation.getSourceFieldOn().getName() + "_" + operation.getName();
    }

    public <K> Object[] getArguments(List<K> keys, BatchLoaderEnvironment ble) {

        List<Object> arguments = new ArrayList<>();
        arguments.add(keys);

        List<Object> keyContextsList = ble.getKeyContextsList();
        if (keyContextsList != null && !keyContextsList.isEmpty()) {
            Object[] otherArguments = (Object[]) keyContextsList.get(0);
            arguments.addAll(Arrays.asList(otherArguments));
        }

        return arguments.toArray();
    }

    public DataFetchingEnvironment getDataFetchingEnvironment(BatchLoaderEnvironment ble) {
        return ble.getContext();
    }

    public void setDataFetchingEnvironment(final DataLoader<Object, Object> dataLoader, final DataFetchingEnvironment dfe) {
        // FIXME: this is potentially brittle because it assumes that the batch loader will execute and
        //  consume the context before we call this again for a different operation, but I don't know
        //  how else to pass this context to the matching BatchLoaderEnvironment instance
        SmallRyeBatchLoaderContextProvider.getForDataLoader(dataLoader).set(dfe);
    }
}
