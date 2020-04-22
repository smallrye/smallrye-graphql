package io.smallrye.graphql.schema.creator.fieldnameapp;

import java.util.Date;
import java.util.List;

import javax.json.bind.annotation.JsonbProperty;

import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

public class SomeObjectAnnotatedGetters {

    private String name;
    private Integer query;
    private List<String> jsonbProperty;
    private Date fieldName;

    @Name("a")
    @Query("b")
    @JsonbProperty("c")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Query("d")
    @JsonbProperty("e")
    public Integer getQuery() {
        return query;
    }

    public void setQuery(Integer query) {
        this.query = query;
    }

    @JsonbProperty("f")
    public List<String> getJsonbProperty() {
        return jsonbProperty;
    }

    public void setJsonbProperty(List<String> jsonbProperty) {
        this.jsonbProperty = jsonbProperty;
    }

    public Date getFieldName() {
        return fieldName;
    }

    public void setFieldName(Date fieldName) {
        this.fieldName = fieldName;
    }
}