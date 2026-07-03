package io.smallrye.graphql.schema.helper;

import java.util.ArrayList;
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
import io.smallrye.graphql.schema.ScanningContext;

/**
 * Finds all @Target input fields.
 */
public class TargetOperationHelper {

    private final Map<DotName, List<MethodParameterInfo>> targetAnnotations = scanAllTargetAnnotations();

    public Map<DotName, List<MethodParameterInfo>> getTargetAnnotations() {
        return targetAnnotations;
    }

    private static Map<DotName, List<MethodParameterInfo>> scanAllTargetAnnotations() {
        Map<DotName, List<MethodParameterInfo>> targetFields = new HashMap<>();
        Collection<AnnotationInstance> targetAnnotations = ScanningContext.getIndex().getAnnotations(Annotations.TARGET);
        for (AnnotationInstance ai : targetAnnotations) {
            AnnotationTarget target = ai.target();
            if (target.kind().equals(AnnotationTarget.Kind.METHOD_PARAMETER)) {
                MethodParameterInfo methodParameter = target.asMethodParameter();
                Type targetType = methodParameter.method().parameterType(methodParameter.position());
                DotName name = getName(targetType);
                targetFields.computeIfAbsent(name, k -> new ArrayList<>()).add(methodParameter);
            } else {
                Logger.getLogger(TargetOperationHelper.class.getName()).warn("Ignoring " + ai.target() + " on kind "
                        + ai.target().kind() + ". Only expecting @" + Annotations.TARGET.local() + " on Method parameters");
            }
        }
        return targetFields;
    }

    private static DotName getName(Type targetType) {
        if (targetType.kind().equals(Type.Kind.PARAMETERIZED_TYPE)) {
            return targetType.asParameterizedType().name();
        }
        return targetType.name();
    }
}
