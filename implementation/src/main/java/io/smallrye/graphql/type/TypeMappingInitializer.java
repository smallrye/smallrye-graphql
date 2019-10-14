package io.smallrye.graphql.type;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ArrayType;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import io.smallrye.graphql.index.Annotations;

/**
 * Create a Map of all types
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@Dependent
public class TypeMappingInitializer {
    private static final Logger LOG = Logger.getLogger(TypeMappingInitializer.class.getName());

    @Inject
    private Index index;

    @Inject
    @Named("scalars")
    public Set<DotName> scalars;

    @Produces
    @Named("types")
    private Set<DotName> types = new TreeSet<>();

    public void init(@Priority(Integer.MAX_VALUE - 1) @Observes @Initialized(ApplicationScoped.class) Object init) {
        // Actual POJOS
        discoverType(Annotations.INPUTTYPE);
        discoverType(Annotations.TYPE);
        // Return type and input parameters
        discoverType(Annotations.QUERY);
        discoverType(Annotations.MUTATION);
    }

    private void discoverType(DotName annotationToScan) {
        LOG.warn("Finding all " + annotationToScan + "...");
        List<AnnotationInstance> annotations = this.index.getAnnotations(annotationToScan);

        //LOG.warn(annotations);
        for (AnnotationInstance annotation : annotations) {
            switch (annotation.target().kind()) {
                case CLASS:
                    // This is when we annotate with @Type related annotations. TODO: Add JsonB ?
                    ClassInfo classInfo = annotation.target().asClass();
                    discoveredType(classInfo);
                    break;
                case METHOD:
                    // This is return types on Queries and Mutations
                    MethodInfo methodInfo = annotation.target().asMethod();
                    handleReturnTypeOrArgumentType(methodInfo.returnType());
                    break;
            }
        }
    }

    private void discoveredType(DotName dotName) {
        ClassInfo classInfo = index.getClassByName(dotName); // TODO: If not in the index, index right here ?
        discoveredType(classInfo);
    }

    private void discoveredType(ClassInfo classInfo) {
        LOG.error("name = " + classInfo);
        types.add(classInfo.name());

        // TODO: Look at fields ? public only 

        List<MethodInfo> methods = classInfo.methods();
        for (MethodInfo method : methods) {
            String methodName = method.name();

            if ((methodName.length() > 3 && methodName.startsWith("get"))
                    || (methodName.length() > 2 && methodName.startsWith("is"))) {
                // Getter
                handleReturnTypeOrArgumentType(method.returnType());
            } else if (methodName.length() > 3 && methodName.startsWith("set")) {
                // Setter
                List<Type> inputTypes = method.parameters();
                for (Type inputType : inputTypes) {
                    handleReturnTypeOrArgumentType(inputType);
                }
            }

        }

    }

    // TODO: Add negative test. Eg, no return on getter and not params on setter,
    private void handleReturnTypeOrArgumentType(Type returnType) {
        DotName returnTypeName = returnType.name();
        if (weDoNotKnowAboutThis(returnTypeName)) {
            // TODO: Generics and Enum ?
            switch (returnType.kind()) {
                case VOID:
                    LOG.warn("Ignoring void return"); // TODO: Throw an exception ?
                    break;
                case ARRAY:
                    ArrayType arrayType = returnType.asArrayType();
                    discoveredType(arrayType.component().name());
                    break;
                case PARAMETERIZED_TYPE:
                    ParameterizedType parameterizedType = returnType.asParameterizedType();
                    List<Type> collectionType = parameterizedType.arguments();
                    for (Type t : collectionType) {
                        // Check if we already know about this type
                        if (!scalars.contains(t.name()) && !types.contains(t.name())) {
                            discoveredType(t.name());
                        }
                    }
                    break;
                default:
                    discoveredType(returnTypeName);
                    break;
            }
        }
    }

    private boolean weDoNotKnowAboutThis(DotName d) {
        return !scalars.contains(d) && !types.contains(d);
    }

}
