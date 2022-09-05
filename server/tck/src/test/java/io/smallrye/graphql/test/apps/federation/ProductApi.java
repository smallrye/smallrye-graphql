package io.smallrye.graphql.test.apps.federation;

import static java.util.Arrays.asList;

import java.util.List;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class ProductApi {
    private static final List<ProductEntity> PRODUCTS = asList(
            ProductEntity.product("1", "Armchair"),
            ProductEntity.product("2", "Table"));

    @Query
    public ProductEntity product(@Id String id) {
        return PRODUCTS.stream()
                .filter(product -> product.getId().equals(id))
                .findFirst().orElseThrow(() -> new RuntimeException("product not find"));
    }
}
