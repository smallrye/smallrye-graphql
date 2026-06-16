package io.smallrye.graphql.jackson.jsonb;

import jakarta.json.bind.annotation.JsonbCreator;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;

public class JsonbAnnotationIntrospector extends NopAnnotationIntrospector {

    private static final long serialVersionUID = 1L;

    @Override
    public PropertyName findNameForSerialization(Annotated a) {
        JsonbProperty prop = a.getAnnotation(JsonbProperty.class);
        if (prop != null && !prop.value().isEmpty()) {
            return PropertyName.construct(prop.value());
        }
        return null;
    }

    @Override
    public PropertyName findNameForDeserialization(Annotated a) {
        JsonbProperty prop = a.getAnnotation(JsonbProperty.class);
        if (prop != null && !prop.value().isEmpty()) {
            return PropertyName.construct(prop.value());
        }
        return null;
    }

    @Override
    public boolean hasIgnoreMarker(AnnotatedMember m) {
        return m.hasAnnotation(JsonbTransient.class);
    }

    @Override
    public JsonCreator.Mode findCreatorAnnotation(MapperConfig<?> config, Annotated a) {
        if (a.hasAnnotation(JsonbCreator.class)) {
            return JsonCreator.Mode.PROPERTIES;
        }
        return null;
    }
}
