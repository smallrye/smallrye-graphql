# Reactive types usage in typesafe clients

## Reactive types with queries and mutations

Regardless of the type that a query or mutation returns, you can always wrap the return type in a `Uni` in your
`@GraphQLClientApi` interface. The `Uni` will be completed after the response is received and processed.

## Subscriptions

The return type of a subscription operation must always be wrapped in a `Multi`.
The communication with the server runs over a WebSocket.

### Subscription example

Given a server-side definition like this:

```java
@Subscription
public Multi<Integer> countToFive() {
    return Multi.createFrom().range(0, 5);
}
```

A client to consume this subscription can look like this:
```java
client.countToFive
    .subscribe()
    .with( // onItem:
        i -> System.out.println("Received number" + i),
        // onFailure:
        (t) -> t.printStackTrace()
    );
```