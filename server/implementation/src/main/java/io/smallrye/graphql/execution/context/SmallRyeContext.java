package io.smallrye.graphql.execution.context;

import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;
import graphql.schema.SelectedField;
import io.smallrye.graphql.api.Context;

/**
 * Implements the Context from MicroProfile API.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class SmallRyeContext implements Context {
    private static final ThreadLocal<SmallRyeContext> current = new ThreadLocal<>();

    public static void register(JsonObject jsonInput) {
        SmallRyeContext registry = new SmallRyeContext(jsonInput);
        current.set(registry);
    }

    public static Context getContext() {
        return current.get();
    }

    public static void remove() {
        current.remove();
    }

    @Override
    public JsonObject getRequest() {
        return jsonObject;
    }

    @Override
    public DataFetchingEnvironment unwrap() {
        return this.dfe;
    }

    @Override
    public boolean hasArgument(String name) {
        return dfe.containsArgument(name);
    }

    @Override
    public <T> T getArgument(String name) {
        return dfe.getArgument(name);
    }

    @Override
    public Map<String, Object> getArguments() {
        return dfe.getArguments();
    }

    @Override
    public String getPath() {
        return dfe.getExecutionStepInfo().getPath().toString();
    }

    @Override
    public String getExecutionId() {
        return dfe.getExecutionId().toString();
    }

    @Override
    public <T> T getSource() {
        return dfe.getSource();
    }

    @Override
    public JsonArray getSelectedFields() {
        DataFetchingFieldSelectionSet selectionSet = dfe.getSelectionSet();
        List<SelectedField> fields = selectionSet.getFields();
        return toJsonArrayBuilder(fields).build();
    }

    private final JsonObject jsonObject;
    private DataFetchingEnvironment dfe;

    private SmallRyeContext(final JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public void setDataFetchingEnvironment(DataFetchingEnvironment dfe) {
        this.dfe = dfe;
    }

    private JsonArrayBuilder toJsonArrayBuilder(List<SelectedField> fields) {
        JsonArrayBuilder builder = jsonbuilder.createArrayBuilder();

        for (SelectedField field : fields) {
            if (!isFlattenScalar(field)) {
                if (isScalar(field)) {
                    builder = builder.add(field.getName());
                } else {
                    builder = builder.add(toJsonObjectBuilder(field));
                }
            }
        }

        return builder;
    }

    private JsonObjectBuilder toJsonObjectBuilder(SelectedField field) {
        JsonObjectBuilder builder = jsonbuilder.createObjectBuilder();
        builder = builder.add(field.getName(), toJsonArrayBuilder(field.getSelectionSet().getFields()));
        return builder;
    }

    private boolean isScalar(SelectedField field) {
        return isScalar(field.getFieldDefinition().getType());
    }

    private boolean isScalar(GraphQLType gqlt) {
        if (isNonNull(gqlt)) {
            GraphQLNonNull graphQLNonNull = (GraphQLNonNull) gqlt;
            return isScalar(graphQLNonNull.getWrappedType());
        } else if (isList(gqlt)) {
            GraphQLList graphQLList = (GraphQLList) gqlt;
            return isScalar(graphQLList.getWrappedType());
        }

        return GraphQLScalarType.class.isAssignableFrom(gqlt.getClass());
    }

    private boolean isNonNull(GraphQLType gqlt) {
        return GraphQLNonNull.class.isAssignableFrom(gqlt.getClass());
    }

    private boolean isList(GraphQLType gqlt) {
        return GraphQLList.class.isAssignableFrom(gqlt.getClass());
    }

    private boolean isFlattenScalar(SelectedField field) {
        return field.getQualifiedName().contains("/");
    }

    private JsonBuilderFactory jsonbuilder = Json.createBuilderFactory(null);
}
