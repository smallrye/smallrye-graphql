# Federation

To enable support for [GraphQL Federation](https://www.apollographql.com/docs/federation), simply set the `smallrye.graphql.federation.enabled` config key to `true`.

You can add the Federation directives by using the equivalent Java annotation, e.g. to extend a `Product` entity with a `price` field, you can write a class:

```java
package org.example.price;

import org.eclipse.microprofile.graphql.Id;

import io.smallrye.graphql.api.federation.Extends;
import io.smallrye.graphql.api.federation.Key;

@Extends @Key(fields = "id")
public class Product {
    @Id
    private String id;

    @Description("The price in cent")
    private Integer price;

    // getters and setters omitted
}
```

And a normal query method that takes the `key` fields as a parameter and returns the requested type:

```java
package org.example.price;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class Prices {
    @Query
    public Product product(@Id String id) {
        return ...;
    }
}
```

The GraphQL Schema then contains:

```graphql
type Product @extends @key(fields : ["id"]) {
    id: ID
    price: Int
}

union _Entity = Product

type Query {
    _entities(representations: [_Any!]!): [_Entity]!
    _service: _Service!
    product(id: ID): Product
}
```
