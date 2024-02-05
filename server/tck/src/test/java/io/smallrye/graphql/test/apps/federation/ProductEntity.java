package io.smallrye.graphql.test.apps.federation;

import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Name;

import io.smallrye.graphql.api.federation.FieldSet;
import io.smallrye.graphql.api.federation.Key;

@Key(fields = @FieldSet("id"))
@Name("Product")
public class ProductEntity {
    static ProductEntity product(String id, String name) {
        ProductEntity product = new ProductEntity();
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
