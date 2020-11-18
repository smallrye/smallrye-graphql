package io.smallrye.graphql.execution.datafetcher.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dataloader.BatchLoaderEnvironment;

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

    public Object[] getArguments(List<Object> keys, BatchLoaderEnvironment ble) {

        List<Object> arguments = new ArrayList<>();
        arguments.add(keys);

        List<Object> keyContextsList = ble.getKeyContextsList();
        if (keyContextsList != null && !keyContextsList.isEmpty()) {
            Object[] otherArguments = (Object[]) keyContextsList.get(0);
            arguments.addAll(Arrays.asList(otherArguments));
        }

        return arguments.toArray();
    }
}
