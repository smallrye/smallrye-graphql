package io.smallrye.graphql.schema.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodParameterInfo;
import org.jboss.logging.Logger;

import io.smallrye.graphql.schema.Annotations;

/**
 * Finds all @Source fields.
 * Creates a map of fields that needs to be added to certain Objects due to @Source annotation
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class SourceFieldHelper {
    private static final Logger LOG = Logger.getLogger(SourceFieldHelper.class.getName());

    private SourceFieldHelper() {
    }

    public static Map<DotName, List<MethodParameterInfo>> getAllSourceAnnotations(IndexView index) {
        Map<DotName, List<MethodParameterInfo>> sourceFields = new HashMap<>();
        Collection<AnnotationInstance> sourceAnnotations = index.getAnnotations(Annotations.SOURCE);
        for (AnnotationInstance ai : sourceAnnotations) {
            AnnotationTarget target = ai.target();
            if (target.kind().equals(AnnotationTarget.Kind.METHOD_PARAMETER)) {
                MethodParameterInfo methodParameter = target.asMethodParameter();
                short position = methodParameter.position();
                DotName name = methodParameter.method().parameters().get(position).name();
                sourceFields.computeIfAbsent(name, k -> new ArrayList<>()).add(methodParameter);
            } else {
                LOG.warn("Ignoring " + ai.target() + " on kind " + ai.target().kind() + ". Only expecting @"
                        + Annotations.SOURCE.local() + " on Method parameters");
            }
        }
        return sourceFields;
    }

}
