package io.smallrye.graphql;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import graphql.Scalars;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaPrinter;

/**
 * Creates the GraphQL Schema
 * TODO: Make schema available for injection
 * TODO: Check that class is annotated with GraphQLApi ?
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class GraphQLSchemaInitializer {
    private static final Logger LOG = Logger.getLogger(GraphQLSchemaInitializer.class.getName());

    @Inject
    private Index index;

    @Inject
    private Map<DotName, GraphQLScalarType> scalarMapping;

    @Produces
    private GraphQLSchema graphQLSchema;

    public void init(@Priority(Integer.MAX_VALUE) @Observes @Initialized(ApplicationScoped.class) Object init) {

        GraphQLObjectType allQueries = getAllQueries();
        GraphQLObjectType allMutations = getAllMutations();

        GraphQLSchema.Builder schemaBuilder = GraphQLSchema.newSchema();
        schemaBuilder.query(allQueries);
        schemaBuilder.mutation(allMutations);

        this.graphQLSchema = schemaBuilder.build();
        printGraphQLSchema();
    }

    private GraphQLObjectType getAllQueries() {
        return createGraphQLObjectType(QUERY_ANNOTATION, QUERY, "Query root"); // TODO: Make description configurable ?
    }

    private GraphQLObjectType getAllMutations() {
        return createGraphQLObjectType(MUTATION_ANNOTATION, MUTATION, "Mutation root"); // TODO: Make description configurable ?
    }

    // TODO: Complex type, Arguments, Variables, enums
    private GraphQLObjectType createGraphQLObjectType(DotName annotationToScan, String name, String description) {
        List<AnnotationInstance> queryAnnotations = this.index.getAnnotations(annotationToScan);

        GraphQLObjectType.Builder queryTypeBuilder = GraphQLObjectType.newObject().name(name).description(description);

        for (AnnotationInstance queryAnnotation : queryAnnotations) {
            switch (queryAnnotation.target().kind()) {

                case METHOD:

                    MethodInfo methodInfo = queryAnnotation.target().asMethod();

                    Type returnType = methodInfo.returnType();

                    GraphQLFieldDefinition.Builder fieldDefinitionBuilder = GraphQLFieldDefinition.newFieldDefinition();
                    // Name
                    fieldDefinitionBuilder = fieldDefinitionBuilder.name(getName(queryAnnotation));
                    // Description
                    Optional<String> maybeDescription = getDescription(methodInfo);
                    if (maybeDescription.isPresent()) {
                        fieldDefinitionBuilder = fieldDefinitionBuilder.description(maybeDescription.get());
                    }
                    // Type
                    fieldDefinitionBuilder = fieldDefinitionBuilder.type(toGraphQLScalarType(returnType));
                    // fieldDefinitionBuilder.withDirectives(directives) // TODO ?

                    queryTypeBuilder = queryTypeBuilder.field(fieldDefinitionBuilder.build());
                    break;
            }
        }

        return queryTypeBuilder.build();
    }

    private GraphQLScalarType toGraphQLScalarType(Type type) {
        // First check if the type is set with an annotation like @Id
        if (hasIdAnnotation(type)) {
            return Scalars.GraphQLID;
        }
        // Else get the type from the mapping
        DotName dotName = type.name();
        if (scalarMapping.containsKey(dotName)) {
            return scalarMapping.get(dotName);
        }
        // Or default to String
        LOG.warn("Could not find scalars for [" + dotName.toString() + "] - defaulting to String");
        return Scalars.GraphQLString;
    }

    private String getName(AnnotationInstance annotation) {
        if (annotation.value() == null || annotation.value().asString().isEmpty()) {
            return annotation.target().asMethod().name();
        } else {
            return annotation.value().asString();
        }
    }

    private Optional<String> getDescription(MethodInfo methodInfo) {
        // See if there is a @Description Annotation on the method
        AnnotationInstance descriptionAnnotation = methodInfo.annotation(DESCRIPTION_ANNOTATION);
        if (descriptionAnnotation != null) {
            AnnotationValue value = descriptionAnnotation.value();
            if (value != null) {
                return Optional.of(value.asString());
            }
        }
        return Optional.empty();
    }

    private boolean hasIdAnnotation(Type type) {
        // See if there is a @Id Annotation on return type
        List<AnnotationInstance> annotations = type.annotations();
        for (AnnotationInstance annotation : annotations) {
            if (annotation.name().equals(ID_ANNOTATION)) {
                return true;
            }
        }
        return false;
    }

    private void printGraphQLSchema() {
        SchemaPrinter schemaPrinter = new SchemaPrinter();//TODO: ? SchemaPrinter.Options.defaultOptions().includeSchemaDefintion(true));
        String schemaString = schemaPrinter.print(this.graphQLSchema);
        LOG.error(schemaString);
    }

    private static final DotName QUERY_ANNOTATION = DotName.createSimple(Query.class.getName());
    private static final DotName MUTATION_ANNOTATION = DotName.createSimple(Mutation.class.getName());

    private static final DotName ID_ANNOTATION = DotName.createSimple(Id.class.getName());
    private static final DotName DESCRIPTION_ANNOTATION = DotName.createSimple(Description.class.getName());

    private static final String QUERY = "Query";
    private static final String MUTATION = "Mutation";
}
