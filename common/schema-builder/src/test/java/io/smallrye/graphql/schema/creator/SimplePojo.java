package io.smallrye.graphql.schema.creator;

/**
 * Used to Test FieldCreator.
 *
 * Just contains some fields and methods with different modifiers.
 */
public class SimplePojo {

    public transient String publicTransientField = "";

    public static String PUBLIC_STATIC_FIELD = "";

    public final String publicFinalField = "";

    public String publicField = "";

    private String privateField = "";

    public static String getStaticField() {
        return "";
    }

    public static void setStaticField(String s) {
    }

    public void setPrivateField(final String privateField) {
        this.privateField = privateField;
    }

    public String getPrivateField() {
        return privateField;
    }

    public String getField() {
        return publicTransientField;
    }

    public void setField(final String publicTransientField) {
        this.publicTransientField = publicTransientField;
    }
}
