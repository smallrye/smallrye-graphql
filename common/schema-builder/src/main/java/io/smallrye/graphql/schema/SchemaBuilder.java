package io.smallrye.graphql.schema;

import static io.smallrye.graphql.schema.Annotations.DIRECTIVE;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.logging.Logger;

import io.smallrye.graphql.schema.creator.ArgumentCreator;
import io.smallrye.graphql.schema.creator.FieldCreator;
import io.smallrye.graphql.schema.creator.OperationCreator;
import io.smallrye.graphql.schema.creator.ReferenceCreator;
import io.smallrye.graphql.schema.creator.type.Creator;
import io.smallrye.graphql.schema.creator.type.DirectiveTypeCreator;
import io.smallrye.graphql.schema.creator.type.EnumCreator;
import io.smallrye.graphql.schema.creator.type.InputTypeCreator;
import io.smallrye.graphql.schema.creator.type.InterfaceCreator;
import io.smallrye.graphql.schema.creator.type.TypeCreator;
import io.smallrye.graphql.schema.helper.GroupHelper;
import io.smallrye.graphql.schema.helper.TypeAutoNameStrategy;
import io.smallrye.graphql.schema.model.ErrorInfo;
import io.smallrye.graphql.schema.model.Group;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.schema.model.OperationType;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.ReferenceType;
import io.smallrye.graphql.schema.model.Schema;

/**
 * This builds schema model using Jandex.
 * <p>
 * It starts scanning all queries and mutation, building the operations for those.
 * The operation reference some types (via Reference) that should be created and added to the schema.
 * <p>
 * The creation of these type them self create more references to types (via Reference) that should be created and added to the
 * scheme.
 * <p>
 * It does above recursively until there is no more things to create.
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class SchemaBuilder {
    private static final Logger LOG = Logger.getLogger(SchemaBuilder.class.getName());

    private final InputTypeCreator inputTypeCreator;
    private final TypeCreator typeCreator;
    private final InterfaceCreator interfaceCreator;
    private final EnumCreator enumCreator;
    private final ReferenceCreator referenceCreator;
    private final OperationCreator operationCreator;
    private final DirectiveTypeCreator directiveTypeCreator;

    /**
     * This builds the Schema from Jandex
     *
     * @param index the Jandex index
     * @return the Schema
     */
    public static Schema build(IndexView index) {
        return build(index, TypeAutoNameStrategy.Default);
    }

    /**
     * This builds the Schema from Jandex
     *
     * @param index the Jandex index
     * @param autoNameStrategy the naming strategy
     * @return the Schema
     */
    public static Schema build(IndexView index, TypeAutoNameStrategy autoNameStrategy) {
        ScanningContext.register(index);
        return new SchemaBuilder(autoNameStrategy).generateSchema();
    }

    private SchemaBuilder(TypeAutoNameStrategy autoNameStrategy) {
        enumCreator = new EnumCreator(autoNameStrategy);
        referenceCreator = new ReferenceCreator(autoNameStrategy);
        FieldCreator fieldCreator = new FieldCreator(referenceCreator);
        ArgumentCreator argumentCreator = new ArgumentCreator(referenceCreator);

        inputTypeCreator = new InputTypeCreator(fieldCreator, autoNameStrategy);
        operationCreator = new OperationCreator(referenceCreator, argumentCreator);
        typeCreator = new TypeCreator(referenceCreator, fieldCreator, operationCreator, autoNameStrategy);
        interfaceCreator = new InterfaceCreator(referenceCreator, fieldCreator, autoNameStrategy);
        directiveTypeCreator = new DirectiveTypeCreator(referenceCreator, autoNameStrategy);
    }

    private Schema generateSchema() {

        // Get all the @GraphQLAPI annotations
        Collection<AnnotationInstance> graphQLApiAnnotations = ScanningContext.getIndex()
                .getAnnotations(Annotations.GRAPHQL_API);

        final Schema schema = new Schema();

        addDirectiveTypes(schema);
        typeCreator.setDirectiveTypes(schema.getDirectiveTypes());

        for (AnnotationInstance graphQLApiAnnotation : graphQLApiAnnotations) {
            ClassInfo apiClass = graphQLApiAnnotation.target().asClass();
            List<MethodInfo> methods = apiClass.methods();
            Optional<Group> group = GroupHelper.getGroup(graphQLApiAnnotation);
            addOperations(group, schema, methods);
        }

        // The above queries and mutations reference some models (input / type / interfaces / enum), let's create those
        addTypesToSchema(schema);

        // We might have missed something
        addOutstandingTypesToSchema(schema);

        // Add all annotated errors (Exceptions)
        addErrors(schema);

        // Reset the maps. 
        referenceCreator.clear();

        return schema;
    }

    private void addDirectiveTypes(Schema schema) {
        for (AnnotationInstance annotationInstance : ScanningContext.getIndex().getAnnotations(DIRECTIVE)) {
            ClassInfo classInfo = annotationInstance.target().asClass();
            schema.addDirectiveType(directiveTypeCreator.create(classInfo));
        }
    }

    private void addTypesToSchema(Schema schema) {
        // Add the input types
        createAndAddToSchema(ReferenceType.INPUT, inputTypeCreator, schema::addInput);

        // Add the output types
        createAndAddToSchema(ReferenceType.TYPE, typeCreator, schema::addType);

        // Add the interface types
        createAndAddToSchema(ReferenceType.INTERFACE, interfaceCreator, schema::addInterface);

        // Add the enum types
        createAndAddToSchema(ReferenceType.ENUM, enumCreator, schema::addEnum);
    }

    private void addOutstandingTypesToSchema(Schema schema) {
        boolean keepGoing = false;

        // See if there is any inputs we missed
        if (findOutstandingAndAddToSchema(ReferenceType.INPUT, inputTypeCreator, schema::containsInput, schema::addInput)) {
            keepGoing = true;
        }

        // See if there is any types we missed
        if (findOutstandingAndAddToSchema(ReferenceType.TYPE, typeCreator, schema::containsType, schema::addType)) {
            keepGoing = true;
        }

        // See if there is any interfaces we missed
        if (findOutstandingAndAddToSchema(ReferenceType.INTERFACE, interfaceCreator, schema::containsInterface,
                schema::addInterface)) {
            keepGoing = true;
        }

        // See if there is any enums we missed
        if (findOutstandingAndAddToSchema(ReferenceType.ENUM, enumCreator, schema::containsEnum,
                schema::addEnum)) {
            keepGoing = true;
        }

        // If we missed something, that something might have created types we do not know about yet, so continue until we have everything
        if (keepGoing) {
            addOutstandingTypesToSchema(schema);
        }
    }

    private void addErrors(Schema schema) {
        Collection<AnnotationInstance> errorAnnotations = ScanningContext.getIndex().getAnnotations(Annotations.ERROR_CODE);
        if (errorAnnotations != null && !errorAnnotations.isEmpty()) {
            for (AnnotationInstance errorAnnotation : errorAnnotations) {
                AnnotationTarget annotationTarget = errorAnnotation.target();
                if (annotationTarget.kind().equals(AnnotationTarget.Kind.CLASS)) {
                    ClassInfo exceptionClass = annotationTarget.asClass();
                    AnnotationValue value = errorAnnotation.value();
                    if (value != null && value.asString() != null && !value.asString().isEmpty()) {
                        schema.addError(new ErrorInfo(exceptionClass.name().toString(), value.asString()));
                    } else {
                        LOG.warn("Ignoring @ErrorCode on " + annotationTarget.toString() + " - Annotation value is not set");
                    }
                } else {
                    LOG.warn("Ignoring @ErrorCode on " + annotationTarget.toString() + " - Wrong target, only apply to CLASS ["
                            + annotationTarget.kind().toString() + "]");
                }
            }
        }
    }

    private <T> void createAndAddToSchema(ReferenceType referenceType, Creator<T> creator, Consumer<T> consumer) {
        Queue<Reference> queue = referenceCreator.values(referenceType);
        while (!queue.isEmpty()) {
            Reference reference = queue.poll();
            ClassInfo classInfo = ScanningContext.getIndex().getClassByName(DotName.createSimple(reference.getClassName()));
            consumer.accept(creator.create(classInfo, reference));
        }
    }

    private <T> boolean findOutstandingAndAddToSchema(ReferenceType referenceType, Creator<T> creator,
            Predicate<String> contains, Consumer<T> consumer) {

        boolean keepGoing = false;
        // Let's see what still needs to be done.
        Queue<Reference> values = referenceCreator.values(referenceType);
        while (!values.isEmpty()) {
            Reference reference = values.poll();
            ClassInfo classInfo = ScanningContext.getIndex().getClassByName(DotName.createSimple(reference.getClassName()));
            if (!contains.test(reference.getName())) {
                consumer.accept(creator.create(classInfo, reference));
                keepGoing = true;
            }
        }

        return keepGoing;
    }

    /**
     * This inspect all method, looking for Query and Mutation annotations,
     * to create those Operations.
     *
     * @param schema the schema to add the operation to.
     * @param methodInfoList the java methods.
     */
    private void addOperations(Optional<Group> group, Schema schema, List<MethodInfo> methodInfoList) {
        for (MethodInfo methodInfo : methodInfoList) {
            Annotations annotationsForMethod = Annotations.getAnnotationsForMethod(methodInfo);
            if (annotationsForMethod.containsOneOfTheseAnnotations(Annotations.QUERY)) {
                Operation query = operationCreator.createOperation(methodInfo, OperationType.QUERY, null);
                if (group.isPresent()) {
                    schema.addGroupedQuery(group.get(), query);
                } else {
                    schema.addQuery(query);
                }
            } else if (annotationsForMethod.containsOneOfTheseAnnotations(Annotations.MUTATION)) {
                Operation mutation = operationCreator.createOperation(methodInfo, OperationType.MUTATION, null);
                if (group.isPresent()) {
                    schema.addGroupedMutation(group.get(), mutation);
                } else {
                    schema.addMutation(mutation);
                }
            }
        }
    }
}
