package io.smallrye.graphql.test.apps.defaultvalue.api;

import java.util.List;

import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

/**
 * Testing Default values
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@GraphQLApi
public class DefaultValueParrotAPI {

    @Query
    public String[] arrayDefault(@DefaultValue("[\"creature\",\"comfort\"]") String[] values) {
        return values;
    }

    @Query
    public List<String> listDefault(@DefaultValue("[\"electric\",\"blue\"]") List<String> values) {
        return values;
    }
}
