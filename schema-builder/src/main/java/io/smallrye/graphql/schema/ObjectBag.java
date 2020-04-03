package io.smallrye.graphql.schema;

import java.util.HashMap;
import java.util.Map;

import org.jboss.logging.Logger;

import io.smallrye.graphql.schema.model.DefinitionType;
import io.smallrye.graphql.schema.model.Reference;

/**
 * Here we keep all the objects we know about
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ObjectBag {
    private static final Logger LOG = Logger.getLogger(ObjectBag.class.getName());

    // Some maps we populate during scanning
    private static final Map<String, Reference> inputReferenceMap = new HashMap<>();
    private static final Map<String, Reference> typeReferenceMap = new HashMap<>();
    private static final Map<String, Reference> enumReferenceMap = new HashMap<>();
    private static final Map<String, Reference> interfaceReferenceMap = new HashMap<>();

    private ObjectBag() {
    }

    public static Map<String, Reference> getReferenceMap(DefinitionType definitionType) {
        switch (definitionType) {
            case ENUM:
                return enumReferenceMap;
            case INPUT:
                return inputReferenceMap;
            case INTERFACE:
                return interfaceReferenceMap;
            case TYPE:
                return typeReferenceMap;
            default:
                return null;
        }
    }

    static void clear() {
        inputReferenceMap.clear();
        typeReferenceMap.clear();
        enumReferenceMap.clear();
        interfaceReferenceMap.clear();
    }
}
