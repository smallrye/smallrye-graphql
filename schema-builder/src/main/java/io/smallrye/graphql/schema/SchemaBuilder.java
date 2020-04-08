package io.smallrye.graphql.schema;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

import io.smallrye.graphql.schema.helper.AnnotationsHelper;
import io.smallrye.graphql.schema.helper.CreatorHelper;
import io.smallrye.graphql.schema.helper.DescriptionHelper;
import io.smallrye.graphql.schema.helper.NameHelper;
import io.smallrye.graphql.schema.model.Complex;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.Method;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.ReferenceType;
import io.smallrye.graphql.schema.model.Schema;
import io.smallrye.graphql.schema.type.ComplexCreator;
import io.smallrye.graphql.schema.type.Creator;
import io.smallrye.graphql.schema.type.EnumCreator;

/**
 * Creates a model from the Jandex index
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class SchemaBuilder {

    private final IndexView index;
    private final ComplexCreator inputCreator;
    private final ComplexCreator typeCreator;
    private final ComplexCreator interfaceCreator;
    private final EnumCreator enumCreator;

    public static Schema build(IndexView index) {
        SchemaBuilder graphQLBootstrap = new SchemaBuilder(index);
        return graphQLBootstrap.generateSchema();
    }

    private SchemaBuilder(IndexView index) {
        this.index = index;
        this.inputCreator = new ComplexCreator(ReferenceType.INPUT, index);
        this.typeCreator = new ComplexCreator(ReferenceType.TYPE, index);
        this.interfaceCreator = new ComplexCreator(ReferenceType.INTERFACE, index);
        this.enumCreator = new EnumCreator();
    }

    private Schema generateSchema() {

        // Get all the @GraphQLAPI annotations
        Collection<AnnotationInstance> graphQLApiAnnotations = this.index.getAnnotations(Annotations.GRAPHQL_API);

        Schema schema = new Schema();

        for (AnnotationInstance graphQLApiAnnotation : graphQLApiAnnotations) {
            ClassInfo apiClass = graphQLApiAnnotation.target().asClass();
            Complex query = new Complex(apiClass.name().toString(), apiClass.name().withoutPackagePrefix() + " Query",
                    "Queries defined in " + apiClass.name().withoutPackagePrefix());
            Complex mutation = new Complex(apiClass.name().toString(),
                    apiClass.name().withoutPackagePrefix() + " Mutation",
                    "Mutations defined in " + apiClass.name().withoutPackagePrefix());
            List<MethodInfo> methods = apiClass.methods();

            addMethods(methods, query, mutation);

            if (query.hasMethods()) {
                schema.addQuery(query);
            }
            if (mutation.hasMethods()) {
                schema.addMutation(mutation);
            }
        }

        // The above queries and mutations reference some models (input / type / interfaces / enum), let's create those
        addTypesToSchema(schema);

        // We might have missed something
        addOutstandingTypesToSchema(schema);

        // Reset the maps. 
        ObjectBag.clear();

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
        for (Reference reference : ObjectBag.values(referenceType)) {
            ClassInfo classInfo = index.getClassByName(DotName.createSimple(reference.getClassName()));
            consumer.accept((T) creator.create(classInfo));
        }
    }

    private <T> boolean findOutstandingAndAddToSchema(ReferenceType referenceType, Creator creator,
            Predicate<String> contains, Consumer<T> consumer) {

        boolean allDone = true;
        // Let's see what still needs to be done.
        for (Reference reference : ObjectBag.values(referenceType)) {
            ClassInfo classInfo = index.getClassByName(DotName.createSimple(reference.getClassName()));
            if (!contains.test(reference.getName())) {
                consumer.accept((T) creator.create(classInfo));
                allDone = false;
            }
        }

        return allDone;
    }

    private void addMethods(List<MethodInfo> methodInfoList, Complex query, Complex mutation) {
        for (MethodInfo methodInfo : methodInfoList) {
            Annotations annotationsForMethod = AnnotationsHelper.getAnnotationsForMethod(methodInfo);
            if (annotationsForMethod.containsOneOfTheseKeys(Annotations.QUERY)) {
                AnnotationInstance queryAnnotation = annotationsForMethod.getAnnotation(Annotations.QUERY);
                Method method = createMethod(methodInfo, queryAnnotation, annotationsForMethod);
                query.addMethod(method);
            }
            if (annotationsForMethod.containsOneOfTheseKeys(Annotations.MUTATION)) {
                AnnotationInstance queryAnnotation = annotationsForMethod.getAnnotation(Annotations.MUTATION);
                Method method = createMethod(methodInfo, queryAnnotation, annotationsForMethod);
                mutation.addMethod(method);
            }
        }
    }

    private Method createMethod(MethodInfo methodInfo, AnnotationInstance graphQLAnnotation,
            Annotations annotationsForMethod) {

        // Name
        String fieldName = NameHelper.getExecutionTypeName(graphQLAnnotation, annotationsForMethod);

        // Description
        Optional<String> maybeDescription = DescriptionHelper.getDescriptionForType(annotationsForMethod);

        Method method = new Method(fieldName, maybeDescription.orElse(null));

        // Type (output)
        validateReturnType(methodInfo, graphQLAnnotation);
        method.setReturn(
                CreatorHelper.getReturnField(index, ReferenceType.TYPE, methodInfo.returnType(), annotationsForMethod));

        // Arguments (input)
        List<Type> parameters = methodInfo.parameters();
        for (short i = 0; i < parameters.size(); i++) {
            // ReferenceType
            Field parameter = CreatorHelper.getParameter(index, ReferenceType.INPUT, parameters.get(i), methodInfo, i);
            method.addParameter(parameter);
        }

        return method;
    }

    private void validateReturnType(MethodInfo methodInfo, AnnotationInstance graphQLAnnotation) {
        Type returnType = methodInfo.returnType();
        if (returnType.kind().equals(Type.Kind.VOID)) {
            throw new CreationException(
                    "Can not have a void return for [" + graphQLAnnotation.name().withoutPackagePrefix()
                            + "] on method [" + methodInfo.name() + "]");
        }
    }
}
