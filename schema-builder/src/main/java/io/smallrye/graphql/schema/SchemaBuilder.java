package io.smallrye.graphql.schema;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;

import io.smallrye.graphql.schema.creator.OperationCreator;
import io.smallrye.graphql.schema.creator.ReferenceCreator;
import io.smallrye.graphql.schema.creator.type.Creator;
import io.smallrye.graphql.schema.creator.type.EnumCreator;
import io.smallrye.graphql.schema.creator.type.InputTypeCreator;
import io.smallrye.graphql.schema.creator.type.InterfaceCreator;
import io.smallrye.graphql.schema.creator.type.TypeCreator;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.ReferenceType;
import io.smallrye.graphql.schema.model.Schema;

/**
 * This builds schema model using Jandex.
 * 
 * It starts scanning all queries and mutation, building the operations for those.
 * The operation reference some types (via Reference) that should be created and added to the schema.
 * 
 * The creation of these type them self create more references to types (via Reference) that should be created and added to the
 * scheme.
 * 
 * It does above recursively until there is not more things to create.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class SchemaBuilder {

    private final InputTypeCreator inputCreator = new InputTypeCreator();
    private final TypeCreator typeCreator = new TypeCreator();
    private final InterfaceCreator interfaceCreator = new InterfaceCreator();
    private final EnumCreator enumCreator = new EnumCreator();

    /**
     * This builds the Schema from Jandex
     * 
     * @param index the Jandex index
     * @return the Schema
     */
    public static Schema build(IndexView index) {
        ScanningContext.register(index);
        SchemaBuilder graphQLBootstrap = new SchemaBuilder();
        return graphQLBootstrap.generateSchema();
    }

    private SchemaBuilder() {
    }

    private Schema generateSchema() {

        // Get all the @GraphQLAPI annotations
        Collection<AnnotationInstance> graphQLApiAnnotations = ScanningContext.getIndex()
                .getAnnotations(Annotations.GRAPHQL_API);

        final Schema schema = new Schema();

        for (AnnotationInstance graphQLApiAnnotation : graphQLApiAnnotations) {
            ClassInfo apiClass = graphQLApiAnnotation.target().asClass();
            List<MethodInfo> methods = apiClass.methods();
            addOperations(schema, methods);
        }

        // The above queries and mutations reference some models (input / type / interfaces / enum), let's create those
        addTypesToSchema(schema);

        // We might have missed something
        addOutstandingTypesToSchema(schema);

        // Reset the maps. 
        ReferenceCreator.clear();

        return schema;
    }

    private void addTypesToSchema(Schema schema) {
        // Add the input types
        createAndAddToSchema(ReferenceType.INPUT, inputCreator, schema::addInput);

        // Add the output types
        createAndAddToSchema(ReferenceType.TYPE, typeCreator, schema::addType);

        // Add the interface types
        createAndAddToSchema(ReferenceType.INTERFACE, interfaceCreator, schema::addInterface);

        // Add the enum types
        createAndAddToSchema(ReferenceType.ENUM, enumCreator, schema::addEnum);
    }

    private void addOutstandingTypesToSchema(Schema schema) {
        boolean allDone = true;
        // See if there is any inputs we missed
        if (!findOutstandingAndAddToSchema(ReferenceType.INPUT, inputCreator, schema::containsInput, schema::addInput)) {
            allDone = false;
        }

        // See if there is any types we missed
        if (!findOutstandingAndAddToSchema(ReferenceType.TYPE, typeCreator, schema::containsType, schema::addType)) {
            allDone = false;
        }

        // See if there is any interfaces we missed
        if (!findOutstandingAndAddToSchema(ReferenceType.INTERFACE, interfaceCreator, schema::containsInterface,
                schema::addInterface)) {
            allDone = false;
        }

        // See if there is any enums we missed
        if (!findOutstandingAndAddToSchema(ReferenceType.ENUM, enumCreator, schema::containsEnum,
                schema::addEnum)) {
            allDone = false;
        }

        // If we missed something, that something might have created types we do not know about yet, so continue until we have everything
        if (!allDone) {
            addOutstandingTypesToSchema(schema);
        }

    }

    private <T> void createAndAddToSchema(ReferenceType referenceType, Creator creator, Consumer<T> consumer) {
        for (Reference reference : ReferenceCreator.values(referenceType)) {
            ClassInfo classInfo = ScanningContext.getIndex().getClassByName(DotName.createSimple(reference.getClassName()));
            consumer.accept((T) creator.create(classInfo));
        }
    }

    private <T> boolean findOutstandingAndAddToSchema(ReferenceType referenceType, Creator creator,
            Predicate<String> contains, Consumer<T> consumer) {

        boolean allDone = true;
        // Let's see what still needs to be done.
        for (Reference reference : ReferenceCreator.values(referenceType)) {
            ClassInfo classInfo = ScanningContext.getIndex().getClassByName(DotName.createSimple(reference.getClassName()));
            if (!contains.test(reference.getName())) {
                consumer.accept((T) creator.create(classInfo));
                allDone = false;
            }
        }

        return allDone;
    }

    /**
     * This inspect all method, looking for Query and Mutation annotations,
     * to create those Operations.
     * 
     * @param schema the schema to add the operation to.
     * @param methodInfoList the java methods.
     */
    private void addOperations(Schema schema, List<MethodInfo> methodInfoList) {
        for (MethodInfo methodInfo : methodInfoList) {
            Annotations annotationsForMethod = Annotations.getAnnotationsForMethod(methodInfo);
            if (annotationsForMethod.containsOneOfTheseAnnotations(Annotations.QUERY)) {
                Operation query = OperationCreator.createOperation(methodInfo, OperationType.Query);
                schema.addQuery(query);
            } else if (annotationsForMethod.containsOneOfTheseAnnotations(Annotations.MUTATION)) {
                Operation mutation = OperationCreator.createOperation(methodInfo, OperationType.Mutation);
                schema.addMutation(mutation);
            }
        }
    }
}
