# Namespacing on the server side

## Before you continue reading
> [NOTE]
> Using approaches to form namespaces in the schema can be useful for large APIs. There are several ways to do this.
> However, read the documentation carefully, especially the limitations and possible problems.

## How use namespaces
There are 3 options how to use the name space - use the @Name annotation, @Source, or combine them.

### Using @Name annotation
The easiest way is that you can separate your API into namespace areas using the annotation @Name with @GraphQLApi.
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

@GraphQLApi
@Name("roles")
@Description("Roles operations")
public class RoleApi {
    @Query
    public List<Role> findAll() {
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
  roles {
    findAll {
      ....
    }
  }
}
```
When using annotation @Name, will be generated type - NameQuery, NameMutation and NameSubscription 
(Subscriptions placed in this type will not work. More details below).

### Using @Source annotation for deep nesting
You can use the @Source annotation to create deep nesting of namespaces.
```java
// create classes that represent namespaces
public class AdminQueryNamespace {
}

public class AdminMutationNamespace {
}

public class UserQueryNamespace {
}

public class UserMutationNamespace {
}

@GraphQLApi
public class UserApi {
    @Query("admin")
    public AdminQueryNamespace adminQueryNamespace() {
        return new AdminQueryNamespace();
    }

    public UserQueryNamespace userQueryNamespace(@Source AdminQueryNamespace namespace) {
        return new UserQueryNamespace();
    }

    public List<User> findAll(@Source UserQueryNamespace namespace) {
        // return users;
    }

    @Mutation("admin")
    public AdminMutationNamespace adminMutationNamespace() {
        return new AdminMutationNamespace();
    }

    public UserMutationNamespace userMutationNamespace(@Source AdminMutationNamespace namespace) {
        return new UserMutationNamespace();
    }

    public List<User> save(@Source UserMutationNamespace namespace, User user) {
        // save user
    }
}
```
As a result, you will be able to execute the following query.
```
query {
    admin {
        users {
            findAll {
             ....
            }
        }
    }
}

mutation {
    admin {
        users {
            save (user: ...) {
             ....
            }
        }
    }
}
```
### Using @Source and @Name annotations together for deep nesting

You can also simplify this example by using @Name.
```java
// create classes that represent namespaces
public class UserQueryNamespace {
}

public class UserMutationNamespace {
}

@GraphQLApi
@Name("admin")
@Description("Users operations")
public class UserApi {
    @Query("users")
    public UserQueryNamespace userQueryNamespace() {
        return new UserQueryNamespace();
    }

    public List<User> findAll(@Source UserQueryNamespace namespace) {
        // return users;
    }
    
    @Mutation("users")
    public UserMutationNamespace userMutationNamespace() {
        return new UserMutationNamespace();
    }

    public List<User> save(@Source UserMutationNamespace namespace, User user) {
        // save user
    }
}
```
## Problems
While dividing APIs into namespaces may seem convenient, it has several issues that are important to be aware of.

#### Mutations
Be careful when working with mutations on client.
This violates the Graphql specification, since mutations in this form can be executed in parallel.
Read more here about namespaces [Namespacing by Separation of Concerns](https://www.apollographql.com/docs/technotes/TN0012-namespacing-by-separation-of-concern/).
This article describes how you can work with namespaces, what problems you may encounter, and how to solve them.

What does Graphql say about this - ["GraphQL" Nested Mutations](https://benjie.dev/graphql/nested-mutations)

In summary, you can use nested mutations, but with some overhead on client. Be careful with mutations.

#### Subscriptions
Graphql does not allow creating subscriptions inside namespaces. 
Or rather, you can create them, generated schema will be valid, but the subscription will not be resolved.
As example, if you try to run such a subscription request, you will get an error. This is the behavior of `graphql-java`.

```java
@GraphQLApi
@Name("resource")
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
You must move subscriptions into a separate class that is not placed in a namespace.

> [NOTE] 
> Be very careful when designing API with namespace. 
> And take into account all the features of working with mutations and subscriptions.
