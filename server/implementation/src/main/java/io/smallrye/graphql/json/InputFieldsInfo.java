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
    private static final Map<String, Map<String, Field>> inputFieldMappingMap = new HashMap<>();
    private static final Map<String, List<String>> creatorParameters = new HashMap<>();

    private InputFieldsInfo() {
    }

    protected static void register(InputType inputType) {
        if (inputType.hasFields()) {
            final ArrayList<String> creatorParameters = new ArrayList<>();
            for (final Field creatorParameter : inputType.getCreatorParameters()) {
                creatorParameters.add(creatorParameter.getName());
            }
            InputFieldsInfo.creatorParameters.put(inputType.getClassName(), creatorParameters);

            Map<String, Field> fieldsThatNeedsTransformation = new HashMap<>();
            Map<String, Field> fieldsThatNeedsMapping = new HashMap<>();

            Collection<Field> fields = inputType.getFields().values();
            for (Field field : fields) {
                // See if there is a transformation
                if (field.hasTransformation()
                        && !field.getTransformation().isJsonB()) {
                    fieldsThatNeedsTransformation.put(field.getName(), field);
                }

                // See if there is a mapping
                if (field.hasMapping() || field.getReference().hasMapping()) {
                    fieldsThatNeedsMapping.putIfAbsent(field.getName(), field);
                }
            }

            if (!fieldsThatNeedsTransformation.isEmpty()) {
                inputFieldTransformationMap.put(inputType.getClassName(), fieldsThatNeedsTransformation);
            }

            if (!fieldsThatNeedsMapping.isEmpty()) {
                inputFieldMappingMap.put(inputType.getClassName(), fieldsThatNeedsMapping);
            }
        }
    }

    public static boolean hasTransformationFields(String className) {
        return inputFieldTransformationMap.containsKey(className);
    }

    public static boolean hasMappingFields(String className) {
        return inputFieldMappingMap.containsKey(className);
    }

    public static Map<String, Field> getTransformationFields(String className) {
        if (inputFieldTransformationMap.containsKey(className)) {
            return inputFieldTransformationMap.get(className);
        }
        return null;
    }

    public static Map<String, Field> getMappingFields(String className) {
        if (inputFieldMappingMap.containsKey(className)) {
            return inputFieldMappingMap.get(className);
        }
        return null;
    }

    public static List<String> getCreatorParameters(String className) {
        if (creatorParameters.containsKey(className)) {
            return creatorParameters.get(className);
        }
        return Collections.emptyList();
    }

}
