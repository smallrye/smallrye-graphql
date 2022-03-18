package io.smallrye.graphql.test.apps.federation.price.api;

import static java.util.Arrays.asList;

import java.util.List;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class Prices {
    private static final List<ProductWithPrice> PRICES = asList(
            ProductWithPrice.product("1", 100),
            ProductWithPrice.product("2", 400));

    @Query
    public ProductWithPrice product(@Id String id) {
        return PRICES.stream()
                .filter(productWithPrice -> productWithPrice.getId().equals(id))
                .findFirst().orElseThrow(() -> new RuntimeException("product not find"));
    }
}
