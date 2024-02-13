package io.smallrye.graphql.schema.link;

import static io.smallrye.graphql.api.federation.link.Link.FEDERATION_SPEC_URL;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.api.federation.link.Import;
import io.smallrye.graphql.api.federation.link.Link;

@GraphQLApi
@Link(url = FEDERATION_SPEC_URL + "/v2.1", _import = { @Import(name = "@policy") })
public class Link2Api {
    @Query
    public String foo() {
        return "bar";
    }
}
