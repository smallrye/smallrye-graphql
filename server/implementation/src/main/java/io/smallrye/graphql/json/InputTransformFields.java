package io.smallrye.graphql.json;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.InputType;

/**
 * Here we create a mapping of all field in a input type that needs transformation
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class InputTransformFields {

    private static final Map<String, Map<String, Field>> inputFieldMap = new HashMap<>();

    private InputTransformFields() {
    }

    protected static void register(InputType inputType) {
        if (inputType.hasFields()) {
            Map<String, Field> fieldsThatNeedsTransformation = new HashMap<>();
            Set<Field> fields = inputType.getFields();
            for (Field field : fields) {
                // See if there is a transformation
                if (field.hasTransformInfo()
                        && !field.getTransformInfo().isJsonB()) {
                    fieldsThatNeedsTransformation.put(field.getName(), field);
                }
            }

            // Seems like there are some name mapping needed
            if (!fieldsThatNeedsTransformation.isEmpty()) {
                inputFieldMap.put(inputType.getClassName(), fieldsThatNeedsTransformation);
            }
        }
    }

    public static boolean hasTransformationFields(String className) {
        return inputFieldMap.containsKey(className);
    }

    public static Map<String, Field> getTransformationFields(String className) {
        if (inputFieldMap.containsKey(className)) {
            return inputFieldMap.get(className);
        }
        return null;
    }
}
