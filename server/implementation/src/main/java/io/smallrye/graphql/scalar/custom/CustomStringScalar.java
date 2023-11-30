package io.smallrye.graphql.scalar.custom;

import java.lang.reflect.Type;

import jakarta.json.bind.serializer.DeserializationContext;
import jakarta.json.bind.serializer.JsonbDeserializer;
import jakarta.json.bind.serializer.JsonbSerializer;
import jakarta.json.bind.serializer.SerializationContext;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonParser;

import io.smallrye.graphql.spi.ClassloadingService;

public abstract class CustomStringScalar {

    private final String stringValue;

    public CustomStringScalar(String stringValue) {
        this.stringValue = stringValue;
    }

    public CustomStringScalar() {
        stringValue = null;
    }

    public static JsonbSerializer serializer() {
        return (JsonbSerializer<CustomStringScalar>) (customStringScalar, jsonGenerator, serializationContext) -> jsonGenerator.write(customStringScalar.toString());
    }

    public static JsonbDeserializer deserializer() {
        return (JsonbDeserializer<CustomStringScalar>) (jsonParser, deserializationContext, type) -> {
            ClassloadingService classloadingService = ClassloadingService.get();
            try {
                return (CustomStringScalar) classloadingService.loadClass(type.getTypeName()).getConstructor(String.class)
                        .newInstance(jsonParser.getString());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

//    public String getStringValue() {
//        return stringValue;
//    }
}
