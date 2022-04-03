package io.smallrye.graphql.execution.context;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.json.JsonArray;
import javax.json.JsonObject;

import io.smallrye.graphql.api.Context;
import io.smallrye.graphql.schema.model.Field;

/**
 * Implements the Context from MicroProfile API.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @param <KEY> Key
 * @param <VAL> Value
 * @param <ARG> Argument
 * @param <SRC> Source
 */
public abstract class AbstractContext<KEY, VAL, ARG, SRC> implements Context<KEY, VAL, ARG, SRC> {
    private JsonObject request;
    private String executionId;
    private Field field;
    private String fieldName;
    private Map<String, ARG> arguments;
    private Map<KEY, VAL> metaDatas;
    private Map<KEY, VAL> localMetaDatas;
    private SRC source;
    private String path;
    private JsonArray selectedFields;
    private JsonArray selectedAndSourceFields;
    private String operationType;
    private List<String> requestedOperationTypes;
    private String parentTypeName;
    private String operationName;

    @Override
    public JsonObject getRequest() {
        return this.request;
    }

    public void setRequest(JsonObject request) {
        this.request = request;
    }

    @Override
    public String getExecutionId() {
        return this.executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    @Override
    public String getFieldName() {
        return this.fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    @Override
    public Map<String, ARG> getArguments() {
        return this.arguments;
    }

    public void setArguments(Map<String, ARG> arguments) {
        this.arguments = arguments;
    }

    @Override
    public Map<KEY, VAL> getMetaDatas() {
        return this.metaDatas;
    }

    public void setMetaDatas(Map<KEY, VAL> metaDatas) {
        this.metaDatas = metaDatas;
    }

    @Override
    public Map<KEY, VAL> getLocalMetaDatas() {
        return this.localMetaDatas;
    }

    public void setLocalMetaDatas(Map<KEY, VAL> localMetaDatas) {
        this.localMetaDatas = localMetaDatas;
    }

    @Override
    public SRC getSource() {
        return (SRC) this.source;
    }

    public void setSource(SRC source) {
        this.source = source;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public JsonArray getSelectedFields() {
        return this.selectedFields;
    }

    public void setSelectedFields(JsonArray selectedFields) {
        this.selectedFields = selectedFields;
    }

    @Override
    public JsonArray getSelectedAndSourceFields() {
        return selectedAndSourceFields;
    }

    public void setSelectedAndSourceFields(JsonArray selectedAndSourceFields) {
        this.selectedAndSourceFields = selectedAndSourceFields;
    }

    @Override
    public String getOperationType() {
        return this.operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    @Override
    public List<String> getRequestedOperationTypes() {
        return this.requestedOperationTypes;
    }

    public void setRequestedOperationTypes(List<String> requestedOperationTypes) {
        this.requestedOperationTypes = requestedOperationTypes;
    }

    @Override
    public Optional<String> getParentTypeName() {
        if (this.parentTypeName == null) {
            return Optional.empty();
        }
        return Optional.of(this.parentTypeName);
    }

    public void setParentTypeName(String parentTypeName) {
        this.parentTypeName = parentTypeName;
    }

    @Override
    public Optional<String> getOperationName() {
        if (this.operationName == null) {
            return Optional.empty();
        }
        return Optional.of(this.operationName);
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }
}
