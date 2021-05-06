package io.smallrye.graphql.entry.http;

import java.util.Collections;
import java.util.Map;

/**
 * Represent a incoming query
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class QueryParameters {
    private String query;
    private String operationName;
    private Map<String, Object> variables = Collections.emptyMap();

    public QueryParameters() {
    }

    public QueryParameters(String query, String operationName) {
        this.query = query;
        this.operationName = operationName;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public void addVariable(String key, Object value) {
        this.variables.put(key, value);
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

}
