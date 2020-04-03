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
import io.smallrye.graphql.schema.helper.NonNullHelper;
import io.smallrye.graphql.schema.model.Definition;
import io.smallrye.graphql.schema.model.DefinitionType;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.Parameter;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.Schema;
import io.smallrye.graphql.schema.type.EnumCreator;
import io.smallrye.graphql.schema.type.ObjectCreator;

/**
 * Creates a model from the Jandex index
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class GraphQLCreator {

    private final IndexView index;
    private final ObjectCreator inputCreator;
    private final ObjectCreator typeCreator;
    private final ObjectCreator interfaceCreator;
    private final EnumCreator enumCreator;

    public static Schema bootstrap(IndexView index) {
        GraphQLCreator graphQLBootstrap = new GraphQLCreator(index);
        return graphQLBootstrap.generateSchema();
    }

    private GraphQLCreator(IndexView index) {
        this.index = index;
        this.inputCreator = new ObjectCreator(DefinitionType.INPUT, index);
        this.typeCreator = new ObjectCreator(DefinitionType.TYPE, index);
        this.interfaceCreator = new ObjectCreator(DefinitionType.INTERFACE, index);
        this.enumCreator = new EnumCreator();
    }

    private Schema generateSchema() {

        // Get all the @GraphQLAPI annotations
        Collection<AnnotationInstance> graphQLApiAnnotations = this.index.getAnnotations(Annotations.GRAPHQL_API);

        Schema schema = new Schema();

        for (AnnotationInstance graphQLApiAnnotation : graphQLApiAnnotations) {
            ClassInfo apiClass = graphQLApiAnnotation.target().asClass();
            Definition query = new Definition(apiClass.name().toString(), "Query", "Query root");
            Definition mutation = new Definition(apiClass.name().toString(), "Mutation", "Mutation root");
            List<MethodInfo> methods = apiClass.methods();

            addFields(methods, query, mutation);

            if (query.hasFields()) {
                schema.addQuery(query);
            }
            if (mutation.hasFields()) {
                schema.addMutation(mutation);
            }
        }

        // Now create the actual schema model

        // Add the input types
        Map<String, Reference> inputMap = ObjectBag.getReferenceMap(DefinitionType.INPUT);
        for (Reference reference : inputMap.values()) {
            ClassInfo inputClass = index.getClassByName(DotName.createSimple(reference.getClassName()));
            schema.addInput(inputCreator.create(inputClass));
        }

        // Add the output types
        Map<String, Reference> typeMap = ObjectBag.getReferenceMap(DefinitionType.TYPE);
        for (Reference reference : typeMap.values()) {
            ClassInfo typeClass = index.getClassByName(DotName.createSimple(reference.getClassName()));
            schema.addType(typeCreator.create(typeClass));
        }

        // Add the interface types
        Map<String, Reference> interfaceMap = ObjectBag.getReferenceMap(DefinitionType.INTERFACE);
        for (Reference reference : interfaceMap.values()) {
            ClassInfo typeClass = index.getClassByName(DotName.createSimple(reference.getClassName()));
            schema.addInterface(interfaceCreator.create(typeClass));
        }

        // Add the interface types
        Map<String, Reference> enumMap = ObjectBag.getReferenceMap(DefinitionType.ENUM);
        for (Reference reference : enumMap.values()) {
            ClassInfo typeClass = index.getClassByName(DotName.createSimple(reference.getClassName()));
            schema.addEnum(enumCreator.create(typeClass));
        }

        // TODO: Let's see what still needs to be done. Still needed ?

        // Reset the maps
        ObjectBag.clear();

        return schema;
    }

    private void addFields(List<MethodInfo> methods, Definition query, Definition mutation) {
        for (MethodInfo method : methods) {
            Annotations annotationsForMethod = AnnotationsHelper.getAnnotationsForMethod(method);
            if (annotationsForMethod.containsOneOfTheseKeys(Annotations.QUERY)) {
                AnnotationInstance queryAnnotation = annotationsForMethod.getAnnotation(Annotations.QUERY);
                Field field = createField(method, queryAnnotation, annotationsForMethod);
                query.addField(field);
            }
            if (annotationsForMethod.containsOneOfTheseKeys(Annotations.MUTATION)) {
                AnnotationInstance queryAnnotation = annotationsForMethod.getAnnotation(Annotations.MUTATION);
                Field field = createField(method, queryAnnotation, annotationsForMethod);
                mutation.addField(field);
            }
        }
    }

    private Field createField(MethodInfo methodInfo, AnnotationInstance graphQLAnnotation,
            Annotations annotationsForMethod) {

        Field field = new Field();

        // Name
        String fieldName = NameHelper.getExecutionTypeName(graphQLAnnotation, annotationsForMethod);
        field.setName(fieldName);

        // Description
        Optional<String> maybeDescription = DescriptionHelper.getDescriptionForType(annotationsForMethod);
        field.setDescription(maybeDescription.orElse(null));

        // DefinitionType (output)
        validateReturnType(methodInfo, graphQLAnnotation);
        Type returnType = methodInfo.returnType();
        if (CreatorHelper.isParameterized(returnType)) {
            field.setCollection(true);
        }

        Reference returnTypeRef = CreatorHelper.getReference(index, DefinitionType.TYPE, returnType,
                annotationsForMethod);
        field.setReturnType(returnTypeRef);

        // NotNull
        if (NonNullHelper.markAsNonNull(returnType, annotationsForMethod)) {
            field.setMandatory(true);
        }

        // Arguments (input)
        List<Type> parameters = methodInfo.parameters();
        for (short i = 0; i < parameters.size(); i++) {
            // DefinitionType
            Parameter parameter = CreatorHelper.getParameter(index, parameters.get(i), methodInfo, i);
            field.addParameter(parameter);
        }

        return field;
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
