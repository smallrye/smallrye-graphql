package io.smallrye.graphql.test.namespace;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

@GraphQLApi
public class SourceNamespaceTestApi {
    public static class First {
    }

    public static class Second {
    }

    @Query("first")
    public First first() {
        return new First();
    }

    public Second second(@Source First first) {
        return new Second();
    }

    public SourceNamespaceModel getById(@Source Second second, String id) {
        return new SourceNamespaceModel(id, id);
    }
}
