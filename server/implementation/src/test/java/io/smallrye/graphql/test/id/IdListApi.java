package io.smallrye.graphql.test.id;

import java.util.List;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Query;

/**
 * API used to reproduce https://github.com/smallrye/smallrye-graphql/issues/2733:
 * a {@code @Id} argument wrapped in a {@code List} must be usable, not just printed as {@code [ID]} in the schema.
 */
@GraphQLApi
public class IdListApi {

    @Query
    public List<String> echoIds(@Id List<String> ids) {
        return ids;
    }

    @Query
    public String echoId(@Id String id) {
        return id;
    }
}
