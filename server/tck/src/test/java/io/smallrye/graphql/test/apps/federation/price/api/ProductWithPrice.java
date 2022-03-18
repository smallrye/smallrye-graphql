package io.smallrye.graphql.test.apps.federation.price.api;

import org.eclipse.microprofile.graphql.Id;

import io.smallrye.graphql.api.federation.Extends;
import io.smallrye.graphql.api.federation.Key;

/**
 * In a real federated service, this would also be a {@code Price}, but we can't have the same type twice within one service.
 */
@Key(fields = "id")
@Extends
public class ProductWithPrice {
    public static ProductWithPrice product(String id, int price) {
        ProductWithPrice product = new ProductWithPrice();
        product.setId(id);
        product.setPrice(price);
        return product;
    }

    @Id
    private String id;
    private int price;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
