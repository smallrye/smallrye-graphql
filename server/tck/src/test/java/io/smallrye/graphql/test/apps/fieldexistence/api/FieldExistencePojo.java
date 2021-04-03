package io.smallrye.graphql.test.apps.fieldexistence.api;

import org.eclipse.microprofile.graphql.Query;

public class FieldExistencePojo {

    public String publicField = "publicField";

    public static final String PUBLIC_STATIC_FINAL_FIELD = "PUBLIC_STATIC_FINAL_FIELD";

    public final String publicFinalField = "finalField";

    @Query
    public String queryMethod() {
        return "queryMethod";
    }

}
