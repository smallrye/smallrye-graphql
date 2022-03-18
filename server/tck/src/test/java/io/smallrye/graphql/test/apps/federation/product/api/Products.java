package io.smallrye.graphql.test.apps.federation.product.api;

import static java.util.Arrays.asList;

import java.util.List;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class Products {
    private static final List<Product> PRODUCTS = asList(
            Product.product("1", "Armchair"),
            Product.product("2", "Table"));

    @Query
    public Product product(@Id String id) {
        return PRODUCTS.stream()
                .filter(product -> product.getId().equals(id))
                .findFirst().orElseThrow(() -> new RuntimeException("product not find"));
    }
}
