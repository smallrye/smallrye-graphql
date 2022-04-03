package io.smallrye.graphql.cdi.producer;

//import io.smallrye.graphql.execution.context.SmallRyeContext;

/**
 * Class that serves as a proxy decorator for obtaining the current SmallRyeContext. It always calls
 * SmallRyeContext.getContext() to obtain the current instance, and delegates to it.
 * This is necessary because the SmallRyeContext is an immutable class, yet we, in some cases,
 * need to be able to inject different instances of it during the serving of one HTTP request.
 * This way, we make sure that an @Inject-ed SmallRyeContext is never cached and calls to it always check
 * if there is a new context instance assigned to the current thread.
 */
//@ApplicationScoped
public class SmallRyeContextAccessorProxy {//extends SmallRyeContext {

    //    public SmallRyeContextAccessorProxy() {
    //        // we extend SmallRyeContext, so to satisfy Java language requirements, we have to call its constructor,
    //        // even though we don't really need it
    //        super(null);
    //    }
    //
    //    @Override
    //    public SmallRyeContext withDataFromExecution(ExecutionInput executionInput) {
    //        return SmallRyeContext.getContext().withDataFromExecution(executionInput);
    //    }
    //
    //    @Override
    //    public SmallRyeContext withDataFromExecution(ExecutionInput executionInput, QueryCache queryCache) {
    //        return SmallRyeContext.getContext().withDataFromExecution(executionInput, queryCache);
    //    }
    //
    //    @Override
    //    public SmallRyeContext withDataFromFetcher(DataFetchingEnvironment dfe, Field field) {
    //        return SmallRyeContext.getContext().withDataFromFetcher(dfe, field);
    //    }
    //
    //    @Override
    //    public JsonObject getRequest() {
    //        return SmallRyeContext.getContext().getRequest();
    //    }
    //
    //    @Override
    //    public <T> T unwrap(Class<T> wrappedType) {
    //        return SmallRyeContext.getContext().unwrap(wrappedType);
    //    }
    //
    //    @Override
    //    public Boolean hasArgument(String name) {
    //        return SmallRyeContext.getContext().hasArgument(name);
    //    }
    //
    //    @Override
    //    public <T> T getArgument(String name) {
    //        return SmallRyeContext.getContext().getArgument(name);
    //    }
    //
    //    @Override
    //    public Map<String, Object> getArguments() {
    //        return SmallRyeContext.getContext().getArguments();
    //    }
    //
    //    @Override
    //    public String getPath() {
    //        return SmallRyeContext.getContext().getPath();
    //    }
    //
    //    @Override
    //    public String getExecutionId() {
    //        return SmallRyeContext.getContext().getExecutionId();
    //    }
    //
    //    @Override
    //    public String getFieldName() {
    //        return SmallRyeContext.getContext().getFieldName();
    //    }
    //
    //    @Override
    //    public <T> T getSource() {
    //        return SmallRyeContext.getContext().getSource();
    //    }
    //
    //    @Override
    //    public JsonArray getSelectedFields(boolean includeSourceFields) {
    //        return SmallRyeContext.getContext().getSelectedFields(includeSourceFields);
    //    }
    //
    //    @Override
    //    public String getOperationType() {
    //        return SmallRyeContext.getContext().getOperationType();
    //    }
    //
    //    @Override
    //    public List<String> getRequestedOperationTypes() {
    //        return SmallRyeContext.getContext().getRequestedOperationTypes();
    //    }
    //
    //    @Override
    //    public Optional<String> getParentTypeName() {
    //        return SmallRyeContext.getContext().getParentTypeName();
    //    }
    //
    //    @Override
    //    public String toString() {
    //        return SmallRyeContext.getContext().toString();
    //    }
    //
    //    @Override
    //    public String getQuery() {
    //        return SmallRyeContext.getContext().getQuery();
    //    }
    //
    //    @Override
    //    public Optional<String> getOperationName() {
    //        return SmallRyeContext.getContext().getOperationName();
    //    }
    //
    //    @Override
    //    public boolean hasOperationName() {
    //        return SmallRyeContext.getContext().hasOperationName();
    //    }
    //
    //    @Override
    //    public Optional<Map<String, Object>> getVariables() {
    //        return SmallRyeContext.getContext().getVariables();
    //    }
    //
    //    @Override
    //    public boolean hasVariables() {
    //        return SmallRyeContext.getContext().hasVariables();
    //    }
    //
    //    @Override
    //    public <T> T getArgumentOrDefault(String name, T defaultValue) {
    //        return SmallRyeContext.getContext().getArgumentOrDefault(name, defaultValue);
    //    }
    //
    //    @Override
    //    public boolean hasSource() {
    //        return SmallRyeContext.getContext().hasSource();
    //    }
    //
    //    @Override
    //    public JsonArray getSelectedFields() {
    //        return SmallRyeContext.getContext().getSelectedFields();
    //    }

}
