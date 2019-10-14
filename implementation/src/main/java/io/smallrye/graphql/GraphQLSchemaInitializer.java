package io.smallrye.graphql;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import graphql.Scalars;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaPrinter;
import io.smallrye.graphql.index.Annotations;

/**
 * Creates the GraphQL Schema
 * TODO: Make schema available for injection
 * TODO: Check that class is annotated with GraphQLApi ?
 * TODO: Collections ? List of ?
 * TODO: Cyclic references.
 * TODO: Default value ? Can that be on the schema ?
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

    // TODO: Move somewhere.
    private final Map<DotName, GraphQLObjectType> graphQLTypes = new HashMap<>();

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
        return createGraphQLObjectType(Annotations.QUERY, QUERY, "Query root"); // TODO: Make description configurable ?
    }

    private GraphQLObjectType getAllMutations() {
        return createGraphQLObjectType(Annotations.MUTATION, MUTATION, "Mutation root"); // TODO: Make description configurable ?
    }

    // TODO: Complex type, Arguments, Variables, enums
    private GraphQLObjectType createGraphQLObjectType(DotName annotationToScan, String name, String description) {
        List<AnnotationInstance> graphQLAnnotations = this.index.getAnnotations(annotationToScan);

        GraphQLObjectType.Builder queryTypeBuilder = GraphQLObjectType.newObject().name(name).description(description);

        for (AnnotationInstance graphQLAnnotation : graphQLAnnotations) {
            switch (graphQLAnnotation.target().kind()) {

                case METHOD:

                    MethodInfo methodInfo = graphQLAnnotation.target().asMethod();

                    Type returnType = methodInfo.returnType();

                    GraphQLFieldDefinition.Builder fieldDefinitionBuilder = GraphQLFieldDefinition.newFieldDefinition();
                    // Name
                    fieldDefinitionBuilder = fieldDefinitionBuilder.name(getNameFromAnnotation(graphQLAnnotation));
                    // Description
                    Optional<String> maybeDescription = getDescription(methodInfo);
                    if (maybeDescription.isPresent()) {
                        fieldDefinitionBuilder = fieldDefinitionBuilder.description(maybeDescription.get());
                    }
                    // Type
                    fieldDefinitionBuilder = fieldDefinitionBuilder.type(toGraphQLOutputType(returnType));
                    // fieldDefinitionBuilder.withDirectives(directives) // TODO ?

                    queryTypeBuilder = queryTypeBuilder.field(fieldDefinitionBuilder.build());
                    break;
            }
        }

        return queryTypeBuilder.build();
    }

    private GraphQLOutputType toGraphQLOutputType(Type type) {
        // First check if the type is set with an annotation like @Id 
        // TODO: What if this annotation is not on a scalar ??
        if (hasIdAnnotation(type)) {
            return Scalars.GraphQLID;
        }

        // Else get the type from the mapping
        DotName dotName = type.name();
        if (scalarMapping.containsKey(dotName)) {
            return scalarMapping.get(dotName);
        }
        // This is not a scalar. Let's see if we can create a Complex type.

        if (graphQLTypes.containsKey(dotName)) {
            return graphQLTypes.get(dotName);
        }

        if (type.kind().equals(Type.Kind.CLASS)) {
            ClassInfo clazz = index.getClassByName(type.name());
            if (clazz.hasNoArgsConstructor()) {
                GraphQLObjectType complexType = createComplexType(clazz);
                graphQLTypes.put(dotName, complexType);
                return complexType;
            }
        } else {
            // TODO: Enums ? Void ? 
        }

        // TODO: Throw exception ?? Or ignore with warning ?
        //LOG.warn("Could not map type [" + type + "] to any scalar and could not create a complex type - ignoring.");
        return Scalars.GraphQLString;
    }

    private GraphQLObjectType createComplexType(ClassInfo classInfo) {

        GraphQLObjectType.Builder complexTypeBuilder = GraphQLObjectType.newObject();

        // Name
        // TODO: Get name from annotation ? @Input @InputType ?
        //LOG.warn("[" + classInfo.name().local() + "]");
        complexTypeBuilder = complexTypeBuilder.name(classInfo.name().local());

        // Description
        Optional<String> maybeDescription = getDescription(classInfo);
        if (maybeDescription.isPresent()) {
            //LOG.warn("[" + maybeDescription.get() + "]");
            complexTypeBuilder = complexTypeBuilder.description(maybeDescription.get());
        }

        //        LOG.warn("\t simpleName [" + clazz.simpleName() + "]");
        //        LOG.warn("\t class annotations [" + clazz.classAnnotations() + "]");
        //        LOG.warn("\t annotations [" + clazz.annotations() + "]");
        //        LOG.warn("\t enclosing class [" + clazz.enclosingClass() + "]");
        //        LOG.warn("\t fields [" + clazz.fields() + "]");
        //        LOG.warn("\t noargs contructor ? [" + clazz.hasNoArgsConstructor() + "]");
        //        LOG.warn("\t typeParameters [" + clazz.typeParameters() + "]");
        //        LOG.warn("\t interface Names [" + clazz.interfaceNames() + "]");
        //        LOG.warn("\t interface Types [" + clazz.interfaceTypes() + "]");
        //        LOG.warn("\t methods [" + clazz.methods() + "]");
        //        LOG.warn("\t nesting Type [" + clazz.nestingType() + "]");
        //        LOG.warn("\t super Class Type [" + clazz.superClassType() + "]");
        //        LOG.warn("\t super Name [" + clazz.superName() + "]");
        //        LOG.warn("\t type parameters [" + clazz.typeParameters() + "]");

        List<FieldInfo> fields = classInfo.fields();
        for (FieldInfo field : fields) {

            GraphQLFieldDefinition.Builder fieldDefinitionBuilder = GraphQLFieldDefinition.newFieldDefinition();
            // Name (@JsonbProperty) TODO: What about our own annotation ?
            fieldDefinitionBuilder = fieldDefinitionBuilder.name(getNameFromField(field));
            // Description
            Optional<String> maybeFieldDescription = getDescription(field);
            if (maybeFieldDescription.isPresent()) {
                fieldDefinitionBuilder = fieldDefinitionBuilder.description(maybeFieldDescription.get());
            }
            // Type
            //fieldDefinitionBuilder = fieldDefinitionBuilder.type(toGraphQLOutputType(field.type()));
            fieldDefinitionBuilder = fieldDefinitionBuilder.type(Scalars.GraphQLString);
            //LOG.warn("\t field [" + field.name() + "(" + field.type() + ")]");

            //LOG.warn("\t output [" + toGraphQLOutputType(field.type()) + "]");
            //LOG.warn("\t annotations [" + field.annotations() + "]");

            complexTypeBuilder.field(fieldDefinitionBuilder);
        }

        return complexTypeBuilder.build();
    }

    private String getNameFromAnnotation(AnnotationInstance annotation) {
        if (annotation.value() == null || annotation.value().asString().isEmpty()) {
            return annotation.target().asMethod().name();
        } else {
            return annotation.value().asString();
        }
    }

    private String getNameFromField(FieldInfo fieldInfo) {
        Optional<AnnotationInstance> maybeJsonProperty = findAnnotationInstance(fieldInfo.annotations(),
                Annotations.JSONB_PROPERTY);
        if (maybeJsonProperty.isPresent()) {
            AnnotationInstance annotation = maybeJsonProperty.get();
            if (annotation != null && annotation.value() != null && !annotation.value().asString().isEmpty()) {
                return annotation.value().asString();
            }
        }
        return fieldInfo.name();
    }

    private Optional<String> getDescription(FieldInfo fieldInfo) {
        // See if there is a @Description Annotation on the field
        Optional<AnnotationInstance> maybeDescription = findAnnotationInstance(fieldInfo.annotations(),
                Annotations.DESCRIPTION);
        if (maybeDescription.isPresent()) {
            return getDescription(maybeDescription.get());
        }
        return Optional.empty();
    }

    private Optional<String> getDescription(ClassInfo classInfo) {
        // See if there is a @Description Annotation on the class
        Optional<AnnotationInstance> maybeDescription = findAnnotationInstance(classInfo.classAnnotations(),
                Annotations.DESCRIPTION);
        if (maybeDescription.isPresent()) {
            return getDescription(maybeDescription.get());
        }
        return Optional.empty();
    }

    private Optional<String> getDescription(MethodInfo methodInfo) {
        // See if there is a @Description Annotation on the method
        AnnotationInstance descriptionAnnotation = methodInfo.annotation(Annotations.DESCRIPTION);
        return getDescription(descriptionAnnotation);
    }

    private Optional<String> getDescription(AnnotationInstance descriptionAnnotation) {
        if (descriptionAnnotation != null) {
            AnnotationValue value = descriptionAnnotation.value();
            if (value != null) {
                return Optional.of(value.asString());
            }
        }
        return Optional.empty();
    }

    private Optional<AnnotationInstance> findAnnotationInstance(Collection<AnnotationInstance> annotations,
            DotName lookingFor) {
        if (annotations != null) {
            for (AnnotationInstance annotationInstance : annotations) {
                if (annotationInstance.name().equals(lookingFor))
                    return Optional.of(annotationInstance);
            }
        }
        return Optional.empty();
    }

    private boolean hasIdAnnotation(Type type) {
        // See if there is a @Id Annotation on return type
        List<AnnotationInstance> annotations = type.annotations();
        for (AnnotationInstance annotation : annotations) {
            if (annotation.name().equals(Annotations.ID)) {
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

    private static final String QUERY = "Query";
    private static final String MUTATION = "Mutation";
}
