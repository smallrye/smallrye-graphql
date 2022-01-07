Client-side configuration properties
========================

Note: if you are using Quarkus, it is recommended to use the `quarkus.*` property counterparts instead. 
See [Quarkus Documentation](https://quarkus.io/guides/all-config#quarkus-smallrye-graphql-client_quarkus-smallrye-graphql-client-smallrye-graphql-client) for more info. 

| Property | Default value | Meaning |
| ------------ | ------------- | ------------ |
| `CLIENT_NAME/mp-graphql/url` | none  | Denotes URL to connect to |
| `CLIENT_NAME/mp-graphql/header/KEY` | none  | Adds a HTTP header named `KEY` to all HTTP requests performed by the client |
| `CLIENT_NAME/mp-graphql/subprotocols` | none  | Comma-separated list of websocket subprotocols supported by this client. We currently support `graphql-ws`, `graphql-transport-ws` and the dummy protocol used by server-side SmallRye GraphQL 1.4.x. To use the dummy protocol, leave this blank.
