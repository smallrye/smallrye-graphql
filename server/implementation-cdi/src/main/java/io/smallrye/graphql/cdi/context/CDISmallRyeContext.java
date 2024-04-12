package io.smallrye.graphql.cdi.context;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.Specializes;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

import graphql.ExecutionInput;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.execution.QueryCache;
import io.smallrye.graphql.execution.context.DocumentSupplier;
import io.smallrye.graphql.execution.context.SmallRyeContext;
import io.smallrye.graphql.execution.context.SmallRyeContextManager;
import io.smallrye.graphql.schema.model.Field;

/**
 * Class that serves as a proxy decorator for obtaining the current SmallRyeContext. It always calls
 * SmallRyeContext.getContext() to obtain the current instance, and delegates to it.
 * This is necessary because the SmallRyeContext is an immutable class, yet we, in some cases,
 * need to be able to inject different instances of it during the serving of one HTTP request.
 * This way, we make sure that an @Inject-ed SmallRyeContext is never cached and calls to it always check
 * if there is a new context instance assigned to the current thread.
 */
@Specializes
@Priority(Integer.MAX_VALUE)
public class CDISmallRyeContext extends SmallRyeContext {

    public CDISmallRyeContext(String createdBy) {
        super(createdBy);
    }

    @Override
    public <T> T unwrap(Class<T> wrappedType) {
        return SmallRyeContextManager.getCurrentSmallRyeContext().unwrap(wrappedType);
    }

    @Override
    public void setDocumentSupplier(DocumentSupplier documentSupplier) {
        SmallRyeContextManager.getCurrentSmallRyeContext().setDocumentSupplier(documentSupplier);
    }

    @Override
    public DocumentSupplier getDocumentSupplier() {
        return SmallRyeContextManager.getCurrentSmallRyeContext().getDocumentSupplier();
    }

    @Override
    public void setQueryCache(QueryCache queryCache) {
        SmallRyeContextManager.getCurrentSmallRyeContext().setQueryCache(queryCache);
    }

    @Override
    public QueryCache getQueryCache() {
        return SmallRyeContextManager.getCurrentSmallRyeContext().getQueryCache();
    }

    @Override
    public void setExecutionInput(ExecutionInput executionInput) {
        SmallRyeContextManager.getCurrentSmallRyeContext().setExecutionInput(executionInput);
    }

    @Override
    public ExecutionInput getExecutionInput() {
        return SmallRyeContextManager.getCurrentSmallRyeContext().getExecutionInput();
    }

    @Override
    public void setDataFetchingEnvironment(DataFetchingEnvironment dataFetchingEnvironment) {
        SmallRyeContextManager.getCurrentSmallRyeContext().setDataFetchingEnvironment(dataFetchingEnvironment);
    }

    @Override
    public DataFetchingEnvironment getDataFetchingEnvironment() {
        return SmallRyeContextManager.getCurrentSmallRyeContext().getDataFetchingEnvironment();
    }

    @Override
    public void setParentTypeName(String parentTypeName) {
        SmallRyeContextManager.getCurrentSmallRyeContext().setParentTypeName(parentTypeName);
    }

    @Override
    public Optional<String> getParentTypeName() {
        return SmallRyeContextManager.getCurrentSmallRyeContext().getParentTypeName();
    }

    @Override
    public void setRequestedOperationTypes(List<String> requestedOperationTypes) {
        SmallRyeContextManager.getCurrentSmallRyeContext().setRequestedOperationTypes(requestedOperationTypes);
    }

    @Override
    public List<String> getRequestedOperationTypes() {
        return SmallRyeContextManager.getCurrentSmallRyeContext().getRequestedOperationTypes();
    }

    @Override
    public void setOperationType(String operationType) {
        SmallRyeContextManager.getCurrentSmallRyeContext().setOperationType(operationType);
    }

    @Override
    public String getOperationType() {
        return SmallRyeContextManager.getCurrentSmallRyeContext().getOperationType();
    }

    @Override
    public void setSelectedAndSourceFields(JsonArray selectedAndSourceFields) {
        SmallRyeContextManager.getCurrentSmallRyeContext().setSelectedAndSourceFields(selectedAndSourceFields);
    }

    @Override
    public JsonArray getSelectedAndSourceFields() {
        return SmallRyeContextManager.getCurrentSmallRyeContext().getSelectedAndSourceFields();
    }

    @Override
    public void setSelectedFields(JsonArray selectedFields) {
        SmallRyeContextManager.getCurrentSmallRyeContext().setSelectedFields(selectedFields);
    }

    @Override
    public JsonArray getSelectedFields() {
        return SmallRyeContextManager.getCurrentSmallRyeContext().getSelectedFields();
    }

    @Override
    public void setPath(String path) {
        SmallRyeContextManager.getCurrentSmallRyeContext().setPath(path);
    }

    @Override
    public String getPath() {
        return SmallRyeContextManager.getCurrentSmallRyeContext().getPath();
    }

    @Override
    public <S> void setSource(S source) {
        SmallRyeContextManager.getCurrentSmallRyeContext().setSource(source);
    }

    @Override
    public <S> S getSource() {
        return SmallRyeContextManager.getCurrentSmallRyeContext().getSource();
    }

    @Override
    public <A> void setArguments(Map<String, A> arguments) {
        SmallRyeContextManager.getCurrentSmallRyeContext().setArguments(arguments);
    }

    @Override
    public <A> Map<String, A> getArguments() {
        return SmallRyeContextManager.getCurrentSmallRyeContext().getArguments();
    }

    @Override
    public void setField(Field field) {
        SmallRyeContextManager.getCurrentSmallRyeContext().setField(field);
    }

    @Override
    public Field getField() {
        return SmallRyeContextManager.getCurrentSmallRyeContext().getField();
    }

    @Override
    public void setFieldName(String fieldName) {
        SmallRyeContextManager.getCurrentSmallRyeContext().setFieldName(fieldName);
    }

    @Override
    public String getFieldName() {
        return SmallRyeContextManager.getCurrentSmallRyeContext().getFieldName();
    }

    @Override
    public void setExecutionId(String executionId) {
        SmallRyeContextManager.getCurrentSmallRyeContext().setExecutionId(executionId);
    }

    @Override
    public String getExecutionId() {
        return SmallRyeContextManager.getCurrentSmallRyeContext().getExecutionId();
    }

    @Override
    public void setRequest(JsonObject request) {
        SmallRyeContextManager.getCurrentSmallRyeContext().setRequest(request);
    }

    @Override
    public JsonObject getRequest() {
        return SmallRyeContextManager.getCurrentSmallRyeContext().getRequest();
    }

    @Override
    public boolean hasSource() {
        return SmallRyeContextManager.getCurrentSmallRyeContext().hasSource();
    }

    @Override
    public <A> A getArgumentOrDefault(String name, A defaultValue) {
        return SmallRyeContextManager.getCurrentSmallRyeContext().getArgumentOrDefault(name, defaultValue);
    }

    @Override
    public <A> A getArgument(String name) {
        return SmallRyeContextManager.getCurrentSmallRyeContext().getArgument(name);
    }

    @Override
    public <A> Boolean hasArgument(String name) {
        return SmallRyeContextManager.getCurrentSmallRyeContext().hasArgument(name);
    }

    @Override
    public boolean hasVariables() {
        return SmallRyeContextManager.getCurrentSmallRyeContext().hasVariables();
    }

    @Override
    public Optional<Map<String, Object>> getVariables() {
        return SmallRyeContextManager.getCurrentSmallRyeContext().getVariables();
    }

    @Override
    public boolean hasOperationName() {
        return SmallRyeContextManager.getCurrentSmallRyeContext().hasOperationName();
    }

    @Override
    public Optional<String> getOperationName() {
        return SmallRyeContextManager.getCurrentSmallRyeContext().getOperationName();
    }

    public void setOperationName(String operationName) {
        SmallRyeContextManager.getCurrentSmallRyeContext().setOperationName(operationName);
    }

    @Override
    public String getQuery() {
        return SmallRyeContextManager.getCurrentSmallRyeContext().getQuery();
    }

    @Override
    public boolean hasRequest() {
        return SmallRyeContextManager.getCurrentSmallRyeContext().hasRequest();
    }

    @Override
    public String toString() {
        return SmallRyeContextManager.getCurrentSmallRyeContext().toString();
    }

    @Override
    public Map<String, Object> getAddedExtensions() {
        return SmallRyeContextManager.getCurrentSmallRyeContext().getAddedExtensions();
    }

    @Override
    public void addExtension(String key, Object value) {
        SmallRyeContextManager.getCurrentSmallRyeContext().addExtension(key, value);
    }
}
