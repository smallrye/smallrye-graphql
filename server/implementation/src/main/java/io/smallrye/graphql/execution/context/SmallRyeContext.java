package io.smallrye.graphql.execution.context;

import static io.smallrye.graphql.SmallRyeGraphQLServerMessages.msg;

import graphql.ExecutionInput;
import graphql.language.Document;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.execution.QueryCache;

/**
 * This extends the Context further and implement it from the graphql-java context
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @param <KEY> Key
 * @param <VAL> Value
 * @param <ARG> Argument
 * @param <SRC> Source
 */
public class SmallRyeContext<KEY, VAL, ARG, SRC> extends AbstractContext<KEY, VAL, ARG, SRC> {

    private DataFetchingEnvironment dataFetchingEnvironment;
    private ExecutionInput executionInput;
    private QueryCache queryCache;
    private DocumentSupplier documentSupplier;

    public SmallRyeContext() {
        super();
    }

    public DataFetchingEnvironment getDataFetchingEnvironment() {
        return dataFetchingEnvironment;
    }

    public void setDataFetchingEnvironment(DataFetchingEnvironment dataFetchingEnvironment) {
        this.dataFetchingEnvironment = dataFetchingEnvironment;
    }

    public ExecutionInput getExecutionInput() {
        return executionInput;
    }

    public void setExecutionInput(ExecutionInput executionInput) {
        this.executionInput = executionInput;
    }

    public QueryCache getQueryCache() {
        return queryCache;
    }

    public void setQueryCache(QueryCache queryCache) {
        this.queryCache = queryCache;
    }

    public DocumentSupplier getDocumentSupplier() {
        return documentSupplier;
    }

    public void setDocumentSupplier(DocumentSupplier documentSupplier) {
        this.documentSupplier = documentSupplier;
    }

    @Override
    public <T> T unwrap(Class<T> wrappedType) {
        // We only support DataFetchingEnvironment, ExecutionInput and Document at this point
        if (wrappedType.equals(DataFetchingEnvironment.class)) {
            return (T) getDataFetchingEnvironment();
        } else if (wrappedType.equals(ExecutionInput.class)) {
            return (T) getExecutionInput();
        } else if (wrappedType.equals(Document.class)) {
            if (getExecutionInput() != null && getQueryCache() != null) {
                DocumentSupplier documentSupplier = new DocumentSupplier(executionInput, queryCache);
                return (T) documentSupplier.get();
            }
            return null;
        }
        throw msg.unsupportedWrappedClass(wrappedType.getName());
    }
}
