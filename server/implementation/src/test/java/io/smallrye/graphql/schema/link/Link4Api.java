package io.smallrye.graphql.schema.link;

import static io.smallrye.graphql.api.federation.link.Link.FEDERATION_SPEC_LATEST_URL;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.api.federation.link.Import;
import io.smallrye.graphql.api.federation.link.Link;

@GraphQLApi
@Link(url = FEDERATION_SPEC_LATEST_URL, _import = { @Import(name = "@authenticated", as = "myAuthenticated") })
public class Link4Api {
    @Query
    public String foo() {
        return "bar";
    }
}
