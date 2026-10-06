package io.smallrye.graphql.json;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.smallrye.graphql.api.CustomFloatScalar;
import io.smallrye.graphql.api.CustomIntScalar;
import io.smallrye.graphql.api.CustomStringScalar;
import io.smallrye.graphql.jackson.jsonb.JsonbCompatModule;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.InputType;
import io.smallrye.graphql.spi.ClassloadingService;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.KeyDeserializer;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.deser.KeyDeserializers;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

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
        return defaultMapperBuilder()
                .propertyNamingStrategy(new GraphQLNamingStrategy(customFieldNameMapping))
                .build();
    }

    private static ObjectMapper createDefaultObjectMapper() {
        return defaultMapperBuilder().build();
    }

    private static JsonMapper.Builder defaultMapperBuilder() {
        return JsonMapper.builder()
                .addModule(new JsonbCompatModule())
                .addModule(CUSTOM_SCALARS_MODULE)
                .addModule(createComplexKeyModule())
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .changeDefaultPropertyInclusion(v -> JsonInclude.Value.construct(JsonInclude.Include.ALWAYS,
                        JsonInclude.Include.ALWAYS));
    }

    private static SimpleModule createCustomScalarsModule() {
        SimpleModule module = new SimpleModule("CustomScalars");

        // CustomStringScalar serializer/deserializer
        module.addSerializer(CustomStringScalar.class, new ValueSerializer<CustomStringScalar>() {
            @Override
            public void serialize(CustomStringScalar value, JsonGenerator gen, SerializationContext serializers) {
                gen.writeString(value.stringValueForSerialization());
            }
        });
        module.addDeserializer(CustomStringScalar.class, new ValueDeserializer<CustomStringScalar>() {
            @Override
            public CustomStringScalar deserialize(JsonParser p, DeserializationContext ctxt) {
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
        module.addSerializer(CustomIntScalar.class, new ValueSerializer<CustomIntScalar>() {
            @Override
            public void serialize(CustomIntScalar value, JsonGenerator gen, SerializationContext serializers) {
                gen.writeNumber(value.intValueForSerialization());
            }
        });
        module.addDeserializer(CustomIntScalar.class, new ValueDeserializer<CustomIntScalar>() {
            @Override
            public CustomIntScalar deserialize(JsonParser p, DeserializationContext ctxt) {
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
        module.addSerializer(CustomFloatScalar.class, new ValueSerializer<CustomFloatScalar>() {
            @Override
            public void serialize(CustomFloatScalar value, JsonGenerator gen, SerializationContext serializers) {
                gen.writeNumber(value.floatValueForSerialization());
            }
        });
        module.addDeserializer(CustomFloatScalar.class, new ValueDeserializer<CustomFloatScalar>() {
            @Override
            public CustomFloatScalar deserialize(JsonParser p, DeserializationContext ctxt) {
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

    private static tools.jackson.databind.JacksonModule createComplexKeyModule() {
        return new tools.jackson.databind.JacksonModule() {
            @Override
            public String getModuleName() {
                return "ComplexMapKeys";
            }

            @Override
            public tools.jackson.core.Version version() {
                return tools.jackson.core.Version.unknownVersion();
            }

            @Override
            public void setupModule(SetupContext context) {
                context.addKeyDeserializers(new KeyDeserializers() {
                    @Override
                    public KeyDeserializer findKeyDeserializer(JavaType keyType, DeserializationConfig config,
                            BeanDescription.Supplier beanDescSupplier) {
                        Class<?> raw = keyType.getRawClass();
                        if (!isComplexKeyType(raw)) {
                            return null;
                        }
                        return new KeyDeserializer() {
                            @Override
                            public Object deserializeKey(String key, DeserializationContext ctxt) {
                                try (JsonParser parser = ctxt.tokenStreamFactory().createParser(key)) {
                                    return ctxt.readValue(parser, keyType);
                                }
                            }
                        };
                    }
                });
            }
        };
    }

    private static boolean isComplexKeyType(Class<?> type) {
        if (type.isPrimitive() || type.isEnum() || type.isArray()) {
            return false;
        }
        String name = type.getName();
        return !name.startsWith("java.") && !name.startsWith("javax.")
                && !name.startsWith("jakarta.") && !name.startsWith("com.fasterxml.")
                && !name.startsWith("tools.jackson.");
    }

}
