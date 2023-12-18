package io.smallrye.graphql.scalar.custom;

import java.lang.reflect.Type;

import jakarta.json.JsonValue.ValueType;
import jakarta.json.bind.serializer.DeserializationContext;
import jakarta.json.bind.serializer.JsonbDeserializer;
import jakarta.json.bind.serializer.JsonbSerializer;
import jakarta.json.bind.serializer.SerializationContext;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonParser;

import io.smallrye.graphql.spi.ClassloadingService;

/**
 * A base class for all CustomScalars that are based on GraphQL's String.
 */
public interface CustomStringScalar {
    // Note: using lambdas for the SERIALIZER/DESERIALIZER instances doesn't work because it
    // hides the parameterized type from Jsonb.

    /**
     * A serializer for CustomScalars based on GraphQL Strings, to inform JsonB how to serialize
     * a CustomStringScalar to a String value.
     */
    JsonbSerializer<CustomStringScalar> SERIALIZER = new JsonbSerializer<>() {
        @Override
        public void serialize(CustomStringScalar customStringScalar, JsonGenerator jsonGenerator,
                SerializationContext serializationContext) {
            jsonGenerator.write(customStringScalar.stringValueForSerialization());
        }
    };

    /**
     * A deserializer for CustomScalars based on GraphQL Strings, to inform JsonB how to deserialize
     * to an instance of a CustomStringScalar.
     */
    JsonbDeserializer<CustomStringScalar> DESERIALIZER = new JsonbDeserializer<>() {
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

    String stringValueForSerialization();
}
