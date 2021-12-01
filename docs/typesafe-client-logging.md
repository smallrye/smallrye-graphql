Logging in typesafe clients
=======

The Client implementation logs all GraphQL requests and responses at
level `INFO` with the interface API as the logger name. It also logs the
keys of all headers added at level `DEBUG`; not the values, as they may
be security sensitive.