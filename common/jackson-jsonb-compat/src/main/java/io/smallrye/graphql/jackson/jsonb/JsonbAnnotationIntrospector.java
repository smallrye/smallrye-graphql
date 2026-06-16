package io.smallrye.graphql.jackson.jsonb;

import java.util.ArrayList;
import java.util.List;

import jakarta.json.bind.annotation.JsonbCreator;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.json.bind.annotation.JsonbNillable;
import jakarta.json.bind.annotation.JsonbNumberFormat;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbSubtype;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.json.bind.annotation.JsonbTypeInfo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;

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
    public JsonInclude.Value findPropertyInclusion(Annotated a) {
        // Field-level: @JsonbProperty(nillable = true)
        JsonbProperty prop = a.getAnnotation(JsonbProperty.class);
        if (prop != null && prop.nillable()) {
            return JsonInclude.Value.construct(JsonInclude.Include.ALWAYS, JsonInclude.Include.ALWAYS);
        }
        // Class-level: @JsonbNillable
        if (a.hasAnnotation(JsonbNillable.class)) {
            return JsonInclude.Value.construct(JsonInclude.Include.ALWAYS, JsonInclude.Include.ALWAYS);
        }
        // Also check declaring class for field-level elements
        if (a instanceof AnnotatedMember) {
            Class<?> declaring = ((AnnotatedMember) a).getDeclaringClass();
            if (declaring.isAnnotationPresent(JsonbNillable.class)) {
                return JsonInclude.Value.construct(JsonInclude.Include.ALWAYS, JsonInclude.Include.ALWAYS);
            }
        }
        return null;
    }

    @Override
    public Object findSerializer(Annotated a) {
        JsonbDateFormat dateFormat = a.getAnnotation(JsonbDateFormat.class);
        if (dateFormat != null) {
            return new JsonbDateFormatSerializer(dateFormat.value(), dateFormat.locale());
        }
        JsonbNumberFormat numberFormat = a.getAnnotation(JsonbNumberFormat.class);
        if (numberFormat != null) {
            return new JsonbNumberFormatSerializer(numberFormat.value(), numberFormat.locale());
        }
        return null;
    }

    @Override
    public Object findDeserializer(Annotated a) {
        JsonbDateFormat dateFormat = a.getAnnotation(JsonbDateFormat.class);
        if (dateFormat != null) {
            Class<?> rawType = resolveRawType(a);
            return new JsonbDateFormatDeserializer(dateFormat.value(), dateFormat.locale(), rawType);
        }
        JsonbNumberFormat numberFormat = a.getAnnotation(JsonbNumberFormat.class);
        if (numberFormat != null) {
            Class<?> rawType = resolveRawType(a);
            return new JsonbNumberFormatDeserializer(numberFormat.value(), numberFormat.locale(), rawType);
        }
        return null;
    }

    private Class<?> resolveRawType(Annotated a) {
        if (a instanceof AnnotatedField) {
            return ((AnnotatedField) a).getRawType();
        }
        if (a instanceof AnnotatedMethod) {
            AnnotatedMethod method = (AnnotatedMethod) a;
            if (method.getParameterCount() > 0) {
                return method.getRawParameterType(0);
            }
            return method.getRawReturnType();
        }
        return Object.class;
    }

    @Override
    public TypeResolverBuilder<?> findTypeResolver(MapperConfig<?> config, AnnotatedClass ac, JavaType baseType) {
        JsonbTypeInfo typeInfo = ac.getAnnotation(JsonbTypeInfo.class);
        if (typeInfo != null) {
            StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
            builder.init(JsonTypeInfo.Id.NAME, null);
            builder.inclusion(JsonTypeInfo.As.PROPERTY);
            builder.typeProperty(typeInfo.key());
            return builder;
        }
        return null;
    }

    @Override
    public List<NamedType> findSubtypes(Annotated a) {
        JsonbTypeInfo typeInfo = a.getAnnotation(JsonbTypeInfo.class);
        if (typeInfo != null) {
            List<NamedType> subtypes = new ArrayList<>();
            for (JsonbSubtype subtype : typeInfo.value()) {
                subtypes.add(new NamedType(subtype.type(), subtype.alias()));
            }
            return subtypes;
        }
        return null;
    }

    @Override
    public JsonCreator.Mode findCreatorAnnotation(MapperConfig<?> config, Annotated a) {
        if (a.hasAnnotation(JsonbCreator.class)) {
            return JsonCreator.Mode.PROPERTIES;
        }
        return null;
    }
}
