package io.smallrye.graphql.bootstrap;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

class TypeFieldWrapper {
    private final GraphQLObjectType type;
    private final GraphQLFieldDefinition field;

    public TypeFieldWrapper(GraphQLObjectType type, GraphQLFieldDefinition field) {
        this.type = type;
        this.field = field;
    }

    public GraphQLObjectType getType() {
        return type;
    }

    public GraphQLFieldDefinition getField() {
        return field;
    }
}
