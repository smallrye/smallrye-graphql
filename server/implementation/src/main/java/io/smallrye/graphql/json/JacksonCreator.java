package io.smallrye.graphql.json;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.smallrye.graphql.api.CustomFloatScalar;
import io.smallrye.graphql.api.CustomIntScalar;
import io.smallrye.graphql.api.CustomStringScalar;
import io.smallrye.graphql.jackson.jsonb.JsonbCompatModule;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.InputType;
import io.smallrye.graphql.spi.ClassloadingService;

/**
 * Here we create ObjectMapper instances for certain input objects.
 *
 * We only use Jackson on input, as output uses data fetchers per field.
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class JacksonCreator {

    private static final SimpleModule CUSTOM_SCALARS_MODULE = createCustomScalarsModule();
    private static final ObjectMapper OBJECT_MAPPER = createDefaultObjectMapper();

    private static final Map<String, ObjectMapper> mapperMap = new HashMap<>();

    private JacksonCreator() {
    }

    protected static void register(InputType inputType) {
        if (inputType.hasFields()) {
            Map<String, String> namemapping = new HashMap<>();
            Collection<Field> fields = inputType.getFields().values();
            for (Field field : fields) {
                // See if the graphql name and property name is different
                if (!field.getName().equals(field.getPropertyName())) {
                    namemapping.put(field.getPropertyName(), field.getName());
                }
            }

            // Seems like there are some name mapping needed
            if (!namemapping.isEmpty()) {
                ObjectMapper mapper = createObjectMapper(namemapping);
                mapperMap.put(inputType.getClassName(), mapper);
            }
        }
    }

    protected static void override(Map<String, ObjectMapper> overrides) {
        mapperMap.putAll(overrides);
    }

    public static ObjectMapper getObjectMapper(String className) {
        if (mapperMap.containsKey(className)) {
            return mapperMap.get(className);
        }
        return getObjectMapper(); // default vanilla
    }

    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    private static ObjectMapper createObjectMapper(Map<String, String> customFieldNameMapping) {
        ObjectMapper mapper = createDefaultObjectMapper();
        mapper.setPropertyNamingStrategy(new GraphQLNamingStrategy(customFieldNameMapping));
        return mapper;
    }

    private static ObjectMapper createDefaultObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JsonbCompatModule());
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(CUSTOM_SCALARS_MODULE);
        mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // Include null values in serialization (required by @JsonbCreator / Jackson creator methods)
        mapper.setDefaultPropertyInclusion(JsonInclude.Include.ALWAYS);
        return mapper;
    }

    private static SimpleModule createCustomScalarsModule() {
        SimpleModule module = new SimpleModule("CustomScalars");

        // CustomStringScalar serializer/deserializer
        module.addSerializer(CustomStringScalar.class, new JsonSerializer<CustomStringScalar>() {
            @Override
            public void serialize(CustomStringScalar value, JsonGenerator gen, SerializerProvider serializers)
                    throws IOException {
                gen.writeString(value.stringValueForSerialization());
            }
        });
        module.addDeserializer(CustomStringScalar.class, new JsonDeserializer<CustomStringScalar>() {
            @Override
            public CustomStringScalar deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                JsonNode node = p.readValueAsTree();
                if (node == null || node.isNull()) {
                    return null;
                }
                ClassloadingService classloadingService = ClassloadingService.get();
                try {
                    Class<?> targetType = classloadingService
                            .loadClass(ctxt.getContextualType() != null
                                    ? ctxt.getContextualType().getRawClass().getName()
                                    : CustomStringScalar.class.getName());
                    return (CustomStringScalar) targetType
                            .getConstructor(String.class)
                            .newInstance(node.asText());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // CustomIntScalar serializer/deserializer
        module.addSerializer(CustomIntScalar.class, new JsonSerializer<CustomIntScalar>() {
            @Override
            public void serialize(CustomIntScalar value, JsonGenerator gen, SerializerProvider serializers)
                    throws IOException {
                gen.writeNumber(value.intValueForSerialization());
            }
        });
        module.addDeserializer(CustomIntScalar.class, new JsonDeserializer<CustomIntScalar>() {
            @Override
            public CustomIntScalar deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                JsonNode node = p.readValueAsTree();
                if (node == null || node.isNull()) {
                    return null;
                }
                ClassloadingService classloadingService = ClassloadingService.get();
                try {
                    Class<?> targetType = classloadingService
                            .loadClass(ctxt.getContextualType() != null
                                    ? ctxt.getContextualType().getRawClass().getName()
                                    : CustomIntScalar.class.getName());
                    return (CustomIntScalar) targetType
                            .getConstructor(Integer.class)
                            .newInstance(node.intValue());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // CustomFloatScalar serializer/deserializer
        module.addSerializer(CustomFloatScalar.class, new JsonSerializer<CustomFloatScalar>() {
            @Override
            public void serialize(CustomFloatScalar value, JsonGenerator gen, SerializerProvider serializers)
                    throws IOException {
                gen.writeNumber(value.floatValueForSerialization());
            }
        });
        module.addDeserializer(CustomFloatScalar.class, new JsonDeserializer<CustomFloatScalar>() {
            @Override
            public CustomFloatScalar deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                JsonNode node = p.readValueAsTree();
                if (node == null || node.isNull()) {
                    return null;
                }
                ClassloadingService classloadingService = ClassloadingService.get();
                try {
                    Class<?> targetType = classloadingService
                            .loadClass(ctxt.getContextualType() != null
                                    ? ctxt.getContextualType().getRawClass().getName()
                                    : CustomFloatScalar.class.getName());
                    return (CustomFloatScalar) targetType
                            .getConstructor(BigDecimal.class)
                            .newInstance(node.decimalValue());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        return module;
    }
}
