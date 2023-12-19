package io.smallrye.graphql.json;

import static io.smallrye.graphql.json.JsonBCreator.CustomScalarSerializers.CUSTOM_FLOAT_DESERIALIZER;
import static io.smallrye.graphql.json.JsonBCreator.CustomScalarSerializers.CUSTOM_FLOAT_SERIALIZER;
import static io.smallrye.graphql.json.JsonBCreator.CustomScalarSerializers.CUSTOM_INT_DESERIALIZER;
import static io.smallrye.graphql.json.JsonBCreator.CustomScalarSerializers.CUSTOM_INT_SERIALIZER;
import static io.smallrye.graphql.json.JsonBCreator.CustomScalarSerializers.CUSTOM_STRING_DESERIALIZER;
import static io.smallrye.graphql.json.JsonBCreator.CustomScalarSerializers.CUSTOM_STRING_SERIALIZER;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jakarta.json.JsonValue.ValueType;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.serializer.DeserializationContext;
import jakarta.json.bind.serializer.JsonbDeserializer;
import jakarta.json.bind.serializer.JsonbSerializer;
import jakarta.json.bind.serializer.SerializationContext;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonParser;

import io.smallrye.graphql.scalar.custom.CustomFloatScalar;
import io.smallrye.graphql.scalar.custom.CustomIntScalar;
import io.smallrye.graphql.scalar.custom.CustomStringScalar;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.InputType;
import io.smallrye.graphql.spi.ClassloadingService;

/**
 * Here we create JsonB Objects for certain input object.
 *
 * We only use JsonB on input, as output use data fetchers per field.
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class JsonBCreator {
    private static final Jsonb JSONB = JsonbBuilder.create(new JsonbConfig()
            .withFormatting(true)
            .withNullValues(true) //null values are required by @JsonbCreator
            .withSerializers(CUSTOM_STRING_SERIALIZER,
                    CUSTOM_INT_SERIALIZER,
                    CUSTOM_FLOAT_SERIALIZER)
            .withDeserializers(CUSTOM_STRING_DESERIALIZER,
                    CUSTOM_INT_DESERIALIZER,
                    CUSTOM_FLOAT_DESERIALIZER));

    private static final Map<String, Jsonb> jsonMap = new HashMap<>();

    private JsonBCreator() {
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
                Jsonb jsonB = createJsonB(namemapping);
                jsonMap.put(inputType.getClassName(), jsonB);
            }
        }
    }

    protected static void override(Map<String, Jsonb> overrides) {
        jsonMap.putAll(overrides);
    }

    public static Jsonb getJsonB(String className) {
        if (jsonMap.containsKey(className)) {
            return jsonMap.get(className);
        }
        return getJsonB(); // default vanilla
    }

    public static Jsonb getJsonB() {
        return JSONB;
    }

    private static Jsonb createJsonB(Map<String, String> customFieldNameMapping) {

        JsonbConfig config = createDefaultConfig()
                .withPropertyNamingStrategy(new GraphQLNamingStrategy(customFieldNameMapping));

        return JsonbBuilder.create(config);
    }

    private static JsonbConfig createDefaultConfig() {
        return new JsonbConfig()
                .withNullValues(Boolean.TRUE)
                .withFormatting(Boolean.TRUE);
    }

    static class CustomScalarSerializers {
        // Note: using lambdas for the SERIALIZER/DESERIALIZER instances doesn't work because it
        // hides the parameterized type from Jsonb.

        /**
         * A serializer for CustomScalars based on GraphQL Strings, to inform JsonB how to serialize
         * a CustomStringScalar to a String value.
         */
        static JsonbSerializer<CustomStringScalar> CUSTOM_STRING_SERIALIZER = new JsonbSerializer<>() {
            @Override
            public void serialize(CustomStringScalar customStringScalar, JsonGenerator jsonGenerator,
                    SerializationContext serializationContext) {
                jsonGenerator.write(customStringScalar.stringValueForSerialization());
            }
        };

        /**
         * A deserializer for CustomScalars based on GraphQL Strings, to inform JsonB how to
         * deserialize to an instance of a CustomStringScalar.
         */
        static JsonbDeserializer<CustomStringScalar> CUSTOM_STRING_DESERIALIZER = new JsonbDeserializer<>() {
            @Override
            public CustomStringScalar deserialize(JsonParser jsonParser,
                    DeserializationContext deserializationContext, Type type) {
                ClassloadingService classloadingService = ClassloadingService.get();
                try {
                    if (jsonParser.getValue().getValueType() == ValueType.NULL) {
                        return null;
                    } else {
                        return (CustomStringScalar) classloadingService.loadClass(type.getTypeName())
                                .getConstructor(String.class)
                                .newInstance(jsonParser.getString());
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };

        /**
         * A serializer for CustomScalars based on a GraphQL Int, to inform JsonB how to serialize
         * a CustomStringScalar to a BigInteger value.
         */
        static JsonbSerializer<CustomIntScalar> CUSTOM_INT_SERIALIZER = new JsonbSerializer<>() {
            @Override
            public void serialize(CustomIntScalar customIntScalar, JsonGenerator jsonGenerator,
                    SerializationContext serializationContext) {
                jsonGenerator.write(customIntScalar.intValueForSerialization());
            }
        };

        /**
         * A deserializer for CustomScalars based on a GraphQL Int, to inform JsonB how to
         * deserialize to an instance of a CustomIntScalar.
         */
        static JsonbDeserializer<CustomIntScalar> CUSTOM_INT_DESERIALIZER = new JsonbDeserializer<>() {
            @Override
            public CustomIntScalar deserialize(JsonParser jsonParser,
                    DeserializationContext deserializationContext, Type type) {
                ClassloadingService classloadingService = ClassloadingService.get();
                try {
                    if (jsonParser.getValue().getValueType() == ValueType.NULL) {
                        return null;
                    } else {
                        return (CustomIntScalar) classloadingService.loadClass(type.getTypeName())
                                .getConstructor(Integer.class)
                                .newInstance(jsonParser.getInt());
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };

        /**
         * A serializer for CustomScalars based on a GraphQL Float, to inform JsonB how to serialize
         * a CustomStringScalar to a BigDecimal value.
         */
        static JsonbSerializer<CustomFloatScalar> CUSTOM_FLOAT_SERIALIZER = new JsonbSerializer<>() {
            @Override
            public void serialize(CustomFloatScalar customFloatScalar, JsonGenerator jsonGenerator,
                    SerializationContext serializationContext) {
                jsonGenerator.write(customFloatScalar.floatValueForSerialization());
            }
        };

        /**
         * A deserializer for CustomScalars based on a GraphQL Float, to inform JsonB how to
         * deserialize to an instance of a CustomFloatScalar.
         */
        static JsonbDeserializer<CustomFloatScalar> CUSTOM_FLOAT_DESERIALIZER = new JsonbDeserializer<>() {
            @Override
            public CustomFloatScalar deserialize(JsonParser jsonParser,
                    DeserializationContext deserializationContext, Type type) {
                ClassloadingService classloadingService = ClassloadingService.get();
                try {
                    if (jsonParser.getValue().getValueType() == ValueType.NULL) {
                        return null;
                    } else {
                        return (CustomFloatScalar) classloadingService.loadClass(type.getTypeName())
                                .getConstructor(BigDecimal.class)
                                .newInstance(jsonParser.getBigDecimal());
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
