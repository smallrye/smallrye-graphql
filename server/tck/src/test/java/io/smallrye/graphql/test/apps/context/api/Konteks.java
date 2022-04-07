package io.smallrye.graphql.test.apps.context.api;

import java.util.List;

public class Konteks {

    private String path;
    private String fieldName;
    private String operationType;
    private String selectedFields;
    private String selectedFieldIncludingSource;
    private List<String> requestedOperationTypes;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getSelectedFields() {
        return selectedFields;
    }

    public void setSelectedFields(String selectedFields) {
        this.selectedFields = selectedFields;
    }

    public String getSelectedFieldIncludingSource() {
        return selectedFieldIncludingSource;
    }

    public void setSelectedFieldIncludingSource(String selectedFieldIncludingSource) {
        this.selectedFieldIncludingSource = selectedFieldIncludingSource;
    }

    public List<String> getRequestedOperationTypes() {
        return requestedOperationTypes;
    }

    public void setRequestedOperationTypes(List<String> requestedOperationTypes) {
        this.requestedOperationTypes = requestedOperationTypes;
    }
}
