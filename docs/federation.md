# Federation

Support for [GraphQL Federation](https://www.apollographql.com/docs/federation) is enabled by default. If you add one of the federation annotations, the corresponding directives will be declared to your schema and the additional Federation queries will be added automatically. You can also disable Federation completely by setting the `smallrye.graphql.federation.enabled` config key to `false`.

You can add the Federation directives by using the equivalent Java annotation, e.g. to extend a `Product` entity with a `price` field, you can write a class:

```java
package org.example.price;

import org.eclipse.microprofile.graphql.Id;

import io.smallrye.graphql.api.federation.Extends;
import io.smallrye.graphql.api.federation.Key;

@Extends @Key(fields = @FieldSet("id"))
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
        return ...
    }
}
```

The GraphQL Schema then contains:

```graphql
type Product @extends @key(fields : "id") {
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

If you can resolve, e.g., the product with different types of ids, you can add multiple `@Key` annotations.
## Federation Batch Resolver

For better performance, there is the option to use batch resolvers with federation. This is not activated on default. It can be activated by setting `smallrye.graphql.federation.batchResolving.enabled` config key to `true`.

It is not needed to provide batch resolvers for all entities, if there is no batch resolver, a non-batch resolver is used.

```java
package org.example.price;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class Prices {
    @Query
    public List<Product> product(@Id List<String> id) {
        return ...
    }
}
```

It is crucial that the sequence of argument list matches with the order of result list. Currently, the name of the Argument `id` must match with the property name in the type.

## Federation Reference Resolver

In federation you also may want extend external type by some fields, without publishing queries into schema. You can do it using @Resolver

```java
@Extends
@Key(fields = @FieldSet("upc"))
public final class Product {
    @External
    @NonNull
    private String upc;
    @External
    private Integer weight;
    @External
    private Integer price;
    private Boolean inStock;
    @Requires(fields = @FieldSet("price weight"))
    private Integer shippingPrice;
}

@GraphQLApi
public class Api {
    @Query // 0 query, that will be added into schema
    public Product findByUPC(String upc) {
        return new Product(upc , ...etc);
    }
    
    @Resolver // 1 You dont receive external fields price weight here, just key
    public Product resolveByUPC(String upc) {
        return new Product(upc , ...etc);
    }

    @Resolver // 2 The order of variables doesn't matter
    public Product resolveByUPCForShipping(int price, String upc, @Name("weight") int someWeight) {
        return new Product(upc , someWeight, price, (price * someWeight) /*calculate shippingPrice */, ...etc);
    }

    @Resolver // 3 
    public Product resolveByUPCForSource(int price, String upc) {
        return new Product(upc, price, ...etc);
    }

    @Requires(fields = @FieldSet("price"))
    public int anotherWeight(@Source Product product) {
        return product.price() * 2;
    }
}
```

Will be generated next schema
```
type Product @extends @key(fields : "upc") {
  anotherWeight: Int! @requires(fields : "price")
  inStock: Boolean
  price: Int @external
  shippingPrice: Int @requires(fields : "price weight")
  upc: String! @external
  weight: Int @external
}

type Query {
  _entities(representations: [_Any!]!): [_Entity]!
  _service: _Service!
}
```

These methods will only be available to the federation router, which send next request
```
// request 1
query {
  _entities(representations: [{ 
    "__typename": "Product", 
    "upc": "1" // just id key
  }]) {
    __typename
    ... on Product {
      inStock
    }
  }
}

// request 2
query {  
  _entities(representations: [{ 
    "__typename": "Product", 
    "upc": "1", // id key
    "price": 100, // shippingPrice requires this field
    "weight": 100  // shippingPrice requires this field
  }]) {
    __typename
    ... on Product {
      inStock
      shippingPrice
    }
  }
}

// request 3
query {
  _entities(representations: [{
    "__typename": "Product",
    "upc": "2",
    "price": 1299  // anotherWeight requires this field
  }
  ]) {
    __typename
    ... on Product {
      anotherWeight
    }
  }
}
```

Unfortunately, you will have to make separate methods with different `@External` parameters. 

It is not currently possible to combine them into one separate type.

You also can using @Query (if you want add queries into schema) or @Resolver (requests 0 and 1). 
And if it was request `_entities` - @Resolvers methods are checked first (they have higher priority). 
