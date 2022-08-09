package io.smallrye.graphql.schema.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodParameterInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.Classes;
import io.smallrye.graphql.schema.ScanningContext;

/**
 * Finds all @Source fields.
 * Creates a map of fields that needs to be added to certain Objects due to @Source annotation
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class SourceOperationHelper {
    private final Logger LOG = Logger.getLogger(SourceOperationHelper.class.getName());

    private Map<DotName, List<MethodParameterInfo>> sourceAnnotations = scanAllSourceAnnotations(false, false,
            Type.Kind.CLASS,
            Type.Kind.PRIMITIVE);

    private Map<DotName, List<MethodParameterInfo>> sourceListAnnotations = scanAllSourceAnnotations(true, false,
            Type.Kind.ARRAY);

    public SourceOperationHelper() {
    }

    public Map<DotName, List<MethodParameterInfo>> getSourceAnnotations() {
        return sourceAnnotations;
    }

    public Map<DotName, List<MethodParameterInfo>> getSourceListAnnotations() {
        return sourceListAnnotations;
    }

    private Map<DotName, List<MethodParameterInfo>> scanAllSourceAnnotations(boolean includeCollections, boolean includeMap,
            Type.Kind... forReturnType) {
        Map<DotName, List<MethodParameterInfo>> sourceFields = new HashMap<>();
        Collection<AnnotationInstance> sourceAnnotations = ScanningContext.getIndex().getAnnotations(Annotations.SOURCE);
        for (AnnotationInstance ai : sourceAnnotations) {
            AnnotationTarget target = ai.target();
            if (target.kind().equals(AnnotationTarget.Kind.METHOD_PARAMETER)) {
                MethodParameterInfo methodParameter = target.asMethodParameter();
                short position = methodParameter.position();
                Type returnType = methodParameter.method().parameterType(position);
                if (forReturnType == null || Arrays.asList(forReturnType).contains(returnType.kind())) {
                    DotName name = getName(returnType);
                    sourceFields.computeIfAbsent(name, k -> new ArrayList<>()).add(methodParameter);
                } else if (returnType.kind().equals(Type.Kind.PARAMETERIZED_TYPE)) {
                    if (includeCollections && Classes.isCollection(returnType)) {
                        DotName name = getName(returnType);
                        sourceFields.computeIfAbsent(name, k -> new ArrayList<>()).add(methodParameter);
                    } else if (includeMap && Classes.isMap(returnType)) {
                        DotName name = getName(returnType);
                        sourceFields.computeIfAbsent(name, k -> new ArrayList<>()).add(methodParameter);
                    }
                }
            } else {
                LOG.warn("Ignoring " + ai.target() + " on kind " + ai.target().kind() + ". Only expecting @"
                        + Annotations.SOURCE.local() + " on Method parameters");
            }
        }
        return sourceFields;
    }

    private DotName getName(Type returnType) {
        if (returnType.kind().equals(Type.Kind.PARAMETERIZED_TYPE)) {
            Type typeInCollection = returnType.asParameterizedType().arguments().get(0);
            return getName(typeInCollection);
        } else if (returnType.kind().equals(Type.Kind.ARRAY)) {
            Type typeInArray = returnType.asArrayType().component();
            return getName(typeInArray);
        }
        return returnType.name();
    }

}
