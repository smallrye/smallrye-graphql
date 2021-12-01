# Reactive types usage in typesafe clients

## Reactive types with queries and mutations

Regardless of the type that a query or mutation returns, you can always wrap the return type in a `Uni` in your
`@GraphQLClientApi` interface. The `Uni` will be completed after the response is received and processed.

## Subscriptions

The return type of a subscription operation must always be wrapped in a `Multi`.
The communication with the server runs over a WebSocket.

### Important note about errors

When using the typesafe client for subscriptions, if it is expected that the server might return errors,
then it is highly recommended to wrap the return types into an `ErrorOr`
(for example, turn the API method `@Subscription Multi<Person> people` into `@Subscription Multi<ErrorOr<Person>> people`).
The reason is once there is an error returned from the service and the result can't be turned into a domain object due to that,
the `Multi` will receive a failure as a result. Because of the design of Mutiny, a `Multi` can't continue producing any items after
a failure. That means the subscription will be cancelled after the first error, even though the server might continue
sending more items. In such case, your application would have to detect the error and start a new subscription.
See [Error handling](typesafe-client-error-handling.md) for more details on how to use `ErrorOr`.

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

