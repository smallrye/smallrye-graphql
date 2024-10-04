# Namespacing on the server side

> [NOTE]
> Using approaches to form namespaces in the schema can be useful for large APIs. There are several ways to do this.
> However, read the documentation carefully, especially the limitations and possible problems.

> [NOTE] You can only use one of the annotations - `@Name` or `@Namespace` over the GraphQLApi classes.

## Using @Namespace annotation

The annotation accepts an array of strings containing the nesting of the namespace. 
This method allows you to create any nesting of namespaces.
You can use any nesting and also combine different levels.

```java
@GraphQLApi
@Namespace({"admin", "users"})
@Description("Admin users operations")
public class AdminUsersApi {
    @Query
    public List<User> findAll() {
        //
    }
}

@GraphQLApi
@Namespace({"admin"})
@Description("Admin operations")
public class AdminApi {
    @Query
    public List<Admin> findAll() {
        //
    }
}
```

Will generate schema
```
"Query root"
type Query {
  admin: AdminQuery
}

"Admin operations"
type AdminQuery {
  users: AdminUsersQuery
  findAll: [Admin]
}

"Admin users operations"
type AdminUsersQuery {
  findAll: [User]
}

type Admin {
  id: BigInteger
  ...
}

type User {
  id: BigInteger
  ...
}
```

And you can send GraphQL request like:
```
query {
  admin {
    users {
      findAll {
        id
      }
    }
  }
}
```

## Using @Name annotation (deprecated)
> [NOTE] This feature may be removed in the future.

It does the same thing as `@Namespace`, the only difference is that there can only be one nesting level.
```java
@GraphQLApi
@Name("users")
@Description("Users operations")
public class UserApi {
    @Query
    public List<User> findAll() {
        //
    }
}
```
As a result, you can get methods with the same names.
```
query {
  users {
    findAll {
      ....
    }
  }
}
```

## Problems
While dividing APIs into namespaces may seem convenient, several issues are important to be aware of.

#### Mutations
On the client side, be careful when working with mutations.
This violates the GraphQL specification since mutations in this form can be executed in parallel.
Read more here about namespaces [Namespacing by Separation of Concerns](https://www.apollographql.com/docs/technotes/TN0012-namespacing-by-separation-of-concern/).
This article describes how to work with namespaces, what problems you may encounter, and how to solve them.

What does GraphQL say about this - ["GraphQL" Nested Mutations](https://benjie.dev/graphql/nested-mutations)

In summary, you can use nested mutations, but with some overhead on the client side. Be careful with mutations.

#### Subscriptions
GraphQL does not guarantee subscription resolution within namespaces.
While you can define subscriptions in namespaces, the generated schema will be valid, but the subscription will not be resolved.
For example, if you try to run such a subscription request, you will get an error. This is the behavior of `graphql-java`.
```java
@GraphQLApi
@Namepace("resource")
public class ResourceApi {
    @Subscription
    public Multi<ResourceSubscription> resourceChange() {
        //
    }
}
```

```
subscription {
  resource {
    resourceChange {
      ....
    }
  }
}
```

There is currently no way around this problem.
You must move subscriptions into a separate `@GraphQLApi` class that does not declare any namespace.
> [NOTE] 
> Be very careful when designing API with namespace.
> And be aware of all the potential consequences when working with mutations and subscriptions.