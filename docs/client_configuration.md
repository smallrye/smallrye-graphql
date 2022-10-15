Client-side configuration properties
========================

Note: if you are using Quarkus, it is recommended to use the `quarkus.*` property counterparts instead. 
See [Quarkus Documentation](https://quarkus.io/guides/all-config#quarkus-smallrye-graphql-client_quarkus-smallrye-graphql-client-smallrye-graphql-client) for more info. 

| Property | Default value | Meaning |
| ------------ | ------------- | ------------ |
| `CLIENT_NAME/mp-graphql/url` | none  | Denotes URL to connect to |
| `CLIENT_NAME/mp-graphql/header/KEY` | none  | Adds a HTTP header named `KEY` to all HTTP requests performed by the client |
| `CLIENT_NAME/mp-graphql/subprotocols` | `graphql-ws,graphql-transport-ws`  | Comma-separated list of websocket subprotocols supported by this client. We currently support `graphql-ws` and `graphql-transport-ws`. If multiple subprotocols are provided, choosing the actual subprotocol will be subject to negotiation with the server.
| `CLIENT_NAME/mp-graphql/keystore` | none  | Path to client's keystore (for example `file:/path/to/keystore` or `classpath:path/to/keystore`) |
| `CLIENT_NAME/mp-graphql/keystoreType` | `JKS` | Keystore type |
| `CLIENT_NAME/mp-graphql/keystorePassword` | none | Keystore password |
| `CLIENT_NAME/mp-graphql/truststore` | none  | Path to client's truststore (for example `file:/path/to/truststore` or `classpath:path/to/truststore`) |
| `CLIENT_NAME/mp-graphql/truststoreType` | `JKS` | Truststore type |
| `CLIENT_NAME/mp-graphql/truststorePassword` | none | Truststore password |
| `CLIENT_NAME/mp-graphql/proxyHost` | none  | Hostname of the proxy to use |
| `CLIENT_NAME/mp-graphql/proxyPort` | none  | Port of the proxy to use |
| `CLIENT_NAME/mp-graphql/proxyUsername` | none  | Username for the proxy to use |
| `CLIENT_NAME/mp-graphql/proxyPassword` | none  | Password for the proxy to use |
| `CLIENT_NAME/mp-graphql/maxRedirects` | 16  | Max number of redirects to follow. Set to 0 to disable redirects. |
| `CLIENT_NAME/mp-graphql/websocketInitializationTimeout` | none  |  Maximum time in milliseconds that will be allowed to wait for the server to acknowledge a websocket connection. |
| `CLIENT_NAME/mp-graphql/runSingleOperationsOverWebsocket` | `false`  |  If true, then queries and mutations will run over the websocket transport rather than pure HTTP. Off by default, because it has higher overhead. |
| `CLIENT_NAME/mp-graphql/initPayload/KEY` | none  | Adds a property named `KEY` to the connect_init message payload when negotiating a subscription. All values will be treated as string. For other types instatiate the API with the builder. |