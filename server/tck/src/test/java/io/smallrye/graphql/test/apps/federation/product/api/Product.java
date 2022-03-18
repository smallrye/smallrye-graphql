package io.smallrye.graphql.test.apps.federation.product.api;

import org.eclipse.microprofile.graphql.Id;

import io.smallrye.graphql.api.federation.Key;

@Key(fields = "id")
public class Product {
    static Product product(String id, String name) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        return product;
    }

    @Id
    private String id;
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
