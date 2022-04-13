package io.smallrye.graphql.execution.datafetcher.helper;

import java.util.ArrayList;
import java.util.List;

import org.dataloader.BatchLoaderEnvironment;

import io.smallrye.graphql.execution.context.BatchKeyContext;
import io.smallrye.graphql.execution.context.SmallRyeContext;
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

    public <K> ArgumentsAndContext getArgumentsAndContext(List<K> keys, BatchLoaderEnvironment ble) {
        ArgumentsAndContext argumentsAndContext = new ArgumentsAndContext();
        List<Object> arguments = new ArrayList<>();
        arguments.add(keys);

        List<Object> keyContextsList = ble.getKeyContextsList();
        if (keyContextsList != null && !keyContextsList.isEmpty()) {
            BatchKeyContext batchKeyContext = (BatchKeyContext) keyContextsList.get(0);
            arguments.addAll(batchKeyContext.getTransformedArguments());
            argumentsAndContext.smallRyeContext = batchKeyContext.getSmallRyeContext();
        }

        argumentsAndContext.arguments = arguments.toArray();

        return argumentsAndContext;
    }

    public class ArgumentsAndContext {
        public Object[] arguments;
        public SmallRyeContext smallRyeContext;
    }
}
