package io.smallrye.graphql.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.InputType;

/**
 * Here we create a mapping of all fields in a input type that needs transformation and mapping
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class InputFieldsInfo {

    private static final Map<String, Map<String, Field>> inputFieldTransformationMap = new HashMap<>();
    private static final Map<String, Map<String, Field>> inputFieldAdaptingToMap = new HashMap<>();
    private static final Map<String, Map<String, Field>> inputFieldAdaptingWithMap = new HashMap<>();
    private static final Map<String, List<Field>> creatorParameters = new HashMap<>();

    private InputFieldsInfo() {
    }

    protected static void register(InputType inputType) {
        if (inputType.hasFields()) {
            final ArrayList<Field> creatorParameters = new ArrayList<>();
            for (final Field creatorParameter : inputType.getCreatorParameters()) {
                creatorParameters.add(creatorParameter);
            }
            InputFieldsInfo.creatorParameters.put(inputType.getClassName(), creatorParameters);

            Map<String, Field> fieldsThatNeedsTransformation = new HashMap<>();
            Map<String, Field> fieldsThatNeedsAdaptingToScalar = new HashMap<>();
            Map<String, Field> fieldsThatNeedsAdaptingWith = new HashMap<>();

            Collection<Field> fields = inputType.getFields().values();
            for (Field field : fields) {
                // See if there is a transformation
                if (field.hasTransformation()
                        && !field.getTransformation().isJsonB()) {
                    fieldsThatNeedsTransformation.put(field.getName(), field);
                }
                // See if there is a adapter
                if (field.isAdaptingWith()
                        && !field.getAdaptWith().isJsonB()) {
                    fieldsThatNeedsAdaptingWith.put(field.getName(), field);
                    // See if there is a map (default adapter)
                } else if (field.hasWrapper() && field.getWrapper().isMap()) {
                    fieldsThatNeedsAdaptingWith.put(field.getName(), field);
                }

                // See if there is adapting to scalar
                if (field.isAdaptingTo() || field.getReference().isAdaptingTo()) {
                    fieldsThatNeedsAdaptingToScalar.putIfAbsent(field.getName(), field);
                }
            }

            if (!fieldsThatNeedsTransformation.isEmpty()) {
                inputFieldTransformationMap.put(inputType.getClassName(), fieldsThatNeedsTransformation);
            }

            if (!fieldsThatNeedsAdaptingToScalar.isEmpty()) {
                inputFieldAdaptingToMap.put(inputType.getClassName(), fieldsThatNeedsAdaptingToScalar);
            }

            if (!fieldsThatNeedsAdaptingWith.isEmpty()) {
                inputFieldAdaptingWithMap.put(inputType.getClassName(), fieldsThatNeedsAdaptingWith);
            }
        }
    }

    public static boolean hasTransformationFields(String className) {
        return inputFieldTransformationMap.containsKey(className);
    }

    public static Map<String, Field> getTransformationFields(String className) {
        if (inputFieldTransformationMap.containsKey(className)) {
            return inputFieldTransformationMap.get(className);
        }
        return null;
    }

    public static boolean hasAdaptToFields(String className) {
        return inputFieldAdaptingToMap.containsKey(className);
    }

    public static Map<String, Field> getAdaptToFields(String className) {
        if (inputFieldAdaptingToMap.containsKey(className)) {
            return inputFieldAdaptingToMap.get(className);
        }
        return null;
    }

    public static boolean hasAdaptWithFields(String className) {
        return inputFieldAdaptingWithMap.containsKey(className);
    }

    public static Map<String, Field> getAdaptWithFields(String className) {
        if (inputFieldAdaptingWithMap.containsKey(className)) {
            return inputFieldAdaptingWithMap.get(className);
        }
        return null;
    }

    public static List<Field> getCreatorParameters(String className) {
        if (creatorParameters.containsKey(className)) {
            return creatorParameters.get(className);
        }
        return Collections.emptyList();
    }

}
