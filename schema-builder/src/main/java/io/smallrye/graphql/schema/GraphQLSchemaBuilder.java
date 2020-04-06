package io.smallrye.graphql.schema;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import io.smallrye.graphql.schema.type.EnumCreator;

/**
 * Creates a model from the Jandex index
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class GraphQLSchemaBuilder {

    private final IndexView index;
    private final ComplexCreator inputCreator;
    private final ComplexCreator typeCreator;
    private final ComplexCreator interfaceCreator;
    private final EnumCreator enumCreator;

    public static Schema build(IndexView index) {
        GraphQLSchemaBuilder graphQLBootstrap = new GraphQLSchemaBuilder(index);
        return graphQLBootstrap.generateSchema();
    }

    private GraphQLSchemaBuilder(IndexView index) {
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

        // Now create the actual schema model

        // Add the input types
        Map<String, Reference> inputMap = ObjectBag.getReferenceMap(ReferenceType.INPUT);
        for (Reference reference : inputMap.values()) {
            ClassInfo inputClass = index.getClassByName(DotName.createSimple(reference.getClassName()));
            schema.addInput(inputCreator.create(inputClass));
        }

        // Add the output types
        Map<String, Reference> typeMap = ObjectBag.getReferenceMap(ReferenceType.TYPE);
        for (Reference reference : typeMap.values()) {
            ClassInfo typeClass = index.getClassByName(DotName.createSimple(reference.getClassName()));
            schema.addType(typeCreator.create(typeClass));
        }

        // Add the interface types
        Map<String, Reference> interfaceMap = ObjectBag.getReferenceMap(ReferenceType.INTERFACE);
        for (Reference reference : interfaceMap.values()) {
            ClassInfo typeClass = index.getClassByName(DotName.createSimple(reference.getClassName()));
            schema.addInterface(interfaceCreator.create(typeClass));
        }

        // Add the interface types
        Map<String, Reference> enumMap = ObjectBag.getReferenceMap(ReferenceType.ENUM);
        for (Reference reference : enumMap.values()) {
            ClassInfo typeClass = index.getClassByName(DotName.createSimple(reference.getClassName()));
            schema.addEnum(enumCreator.create(typeClass));
        }

        // TODO: Let's see what still needs to be done. Still needed ?

        // Reset the maps
        ObjectBag.clear();

        return schema;
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

        Method method = new Method();

        // Name
        String fieldName = NameHelper.getExecutionTypeName(graphQLAnnotation, annotationsForMethod);
        method.setName(fieldName);

        // Description
        Optional<String> maybeDescription = DescriptionHelper.getDescriptionForType(annotationsForMethod);
        method.setDescription(maybeDescription.orElse(null));

        // Type (output)
        validateReturnType(methodInfo, graphQLAnnotation);
        method.setReturn(CreatorHelper.getReturnField(index, methodInfo.returnType(), annotationsForMethod));

        // Arguments (input)
        List<Type> parameters = methodInfo.parameters();
        for (short i = 0; i < parameters.size(); i++) {
            // ReferenceType
            Field parameter = CreatorHelper.getParameter(index, parameters.get(i), methodInfo, i);
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
