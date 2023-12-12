package io.smallrye.graphql.scalar.custom;

import java.lang.reflect.Type;
import java.math.BigInteger;

import jakarta.json.JsonValue.ValueType;
import jakarta.json.bind.serializer.DeserializationContext;
import jakarta.json.bind.serializer.JsonbDeserializer;
import jakarta.json.bind.serializer.JsonbSerializer;
import jakarta.json.bind.serializer.SerializationContext;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonParser;

import io.smallrye.graphql.spi.ClassloadingService;

public interface CustomIntScalar {
    // Note: using lambdas for the SERIALIZER/DESERIALIZER instances doesn't work because it
    // hides the parameterized type from Jsonb.

    JsonbSerializer<CustomIntScalar> SERIALIZER = new JsonbSerializer<>() {
        @Override
        public void serialize(CustomIntScalar customIntScalar, JsonGenerator jsonGenerator,
                SerializationContext serializationContext) {
            jsonGenerator.write(customIntScalar.integerValue());
        }
    };

    JsonbDeserializer<CustomIntScalar> DESERIALIZER = new JsonbDeserializer<>() {
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

    BigInteger integerValue();
}
