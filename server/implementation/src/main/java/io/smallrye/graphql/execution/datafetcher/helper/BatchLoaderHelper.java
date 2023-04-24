package io.smallrye.graphql.execution.datafetcher.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataloader.BatchLoaderEnvironment;

import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.schema.model.Operation;

/**
 * Helping with BatchLoaders
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class BatchLoaderHelper {

    public static final String ARGUMENTS = "arguments";
    public static final String DATA_FETCHING_ENVIRONMENT = "dataFetchingEnvironment";

    public String getName(Operation operation) {
        return operation.getSourceFieldOn().getName() + "_" + operation.getName();
    }

    public <K> Map<String, Object> getBatchContext(List<K> keys, BatchLoaderEnvironment ble) {
        Map<String, Object> batchContext = new HashMap<>();
        List<Object> keyContextsList = ble.getKeyContextsList();
        List<Object> finalArguments = new ArrayList<>();
        finalArguments.add(keys);

        if (keyContextsList != null && !keyContextsList.isEmpty()) {
            Map<String, Object> keyContexts = (Map<String, Object>) keyContextsList.get(0);
            List<Object> otherarguments = (List<Object>) keyContexts.get(ARGUMENTS);
            finalArguments.addAll(otherarguments);
            batchContext.putAll(keyContexts);
            batchContext.put(ARGUMENTS, finalArguments);
        }

        return batchContext;
    }

    public Object[] getArguments(Map<String, Object> batchContext) {
        List<Object> arguments = (List<Object>) batchContext.get(ARGUMENTS);
        return arguments.toArray();
    }

    public DataFetchingEnvironment getDataFetchingEnvironment(Map<String, Object> batchContext) {
        return (DataFetchingEnvironment) batchContext.get(DATA_FETCHING_ENVIRONMENT);
    }
}
