package io.smallrye.graphql.json;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;

import io.smallrye.graphql.scalar.custom.CustomStringScalar;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.InputType;

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
            // TODO bdupras I tried instantiating (de)serializers dynamically at schema-building
            //   for each of the custom scalar Class<?> instances (com.acme.BigDecimalString),
            //   so that we could get rid of the `implements CustomStringScalar` on the custom
            //   scalar class declarations. But Jsonb relies on the parameterized type info from
            //   the declaration (JsonbSerializer<SomeTypeHere>) in order to look up the correct
            //   (de)serializer at runtime.
            .withSerializers(CustomStringScalar.SERIALIZER)
            .withDeserializers(CustomStringScalar.DESERIALIZER));

    private static final Map<String, Jsonb> jsonMap = new HashMap<>();

    private JsonBCreator() {
    }

    protected static void register(InputType inputType) {
        if (inputType.hasFields()) {
            Map<String, String> namemapping = new HashMap<>();
            Collection<Field> fields = inputType.getFields().values();
            for (Field field : fields) {
                field.getReference().getClassName(); // class might not exist on classpath?
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
}
