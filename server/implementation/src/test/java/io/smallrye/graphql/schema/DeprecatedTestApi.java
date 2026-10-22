package io.smallrye.graphql.schema;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class DeprecatedTestApi {

    @Query
    public ItemWithDeprecations item() {
        return new ItemWithDeprecations();
    }

    @Query
    @io.smallrye.graphql.api.Deprecated(reason = "Use `item`.")
    public ItemWithDeprecations oldItem() {
        return new ItemWithDeprecations();
    }

    @Query
    public ItemWithDeprecations itemWithArgument(
            @io.smallrye.graphql.api.Deprecated(reason = "Use no argument.") String arg) {
        return new ItemWithDeprecations();
    }

    @Query
    public int inputQuery(InputWithDeprecations input) {
        return 0;
    }

    @Query
    public int enumQuery(EnumWithDeprecations value) {
        return 0;
    }

    public static class ItemWithDeprecations {
        public String current;

        @io.smallrye.graphql.api.Deprecated(reason = "Use `current`.")
        public String withSmallRye;

        @java.lang.Deprecated
        public String withJavaLang;
    }

    public static class InputWithDeprecations {
        public String current;

        @io.smallrye.graphql.api.Deprecated(reason = "Use `current`.")
        public String withSmallRye;
    }

    public enum EnumWithDeprecations {
        CURRENT,
        @io.smallrye.graphql.api.Deprecated(reason = "Use `CURRENT`.")
        OLD
    }
}
