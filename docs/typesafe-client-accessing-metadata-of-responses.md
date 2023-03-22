# Accessing Metadata of GraphQL Responses with TypesafeResponse
The TypesafeResponse class provides a wrapper around the "data"
field of GraphQL responses, along with additional metadata such as errors,
extensions, and transport metadata. Here are the methods you can use to access each type of metadata:

### Transport metadata
use `getTransportMeta()` method to retrieve a map of transport metadata associated with the response.
> [NOTE]
> Note that the transport metadata referred to in the context of HTTP requests
> and responses is commonly found in the headers of the communication.
> Therefore, the getTransportMeta() method mentioned here can be used to
> retrieve a map of HTTP headers.
### Extensions
use `getExtensions()` method to retrieve a JsonObject of response extensions.
The method returns a JSON object that represents the response extensions.

### Data
use `get()` method to retrieve the data object. The method returns the data
object if it exists and there are no errors. If there are errors,
it throws a NoSuchElementException with the error message.

### Errors 
use `getErrors()` method to retrieve a list of GraphQL errors.
The method returns a list of errors if there are errors and no data object.
If there is a data object, it throws a NoSuchElementException with the error message.


> [NOTE]
> Note that the get() method assumes that the response has a data object,
> and will throw an exception if there are errors without a data object.
> If you want to check for errors before retrieving the data object,
> use the hasErrors() method, which returns true if there are errors and false otherwise.


You can use these methods to access and process the metadata of GraphQL
responses returned by the TypesafeResponse class.

Let's say you want to get as a client extensions from the GraphQL response.
First of all you will need to modify your `GraphQLClientApi` interface to have
your return type class (in this case `SuperHero`) wrapped with
`TypesafeResponse` class.
I.e. your `GraphQLClientApi` interface could look like this:

```java
import examples.typesafeclient.SuperHero;
import io.smallrye.graphql.client.typesafe.api.TypesafeResponse;

@GraphQLClientApi
public interface SuperHeroApi {
    // ...
    TypesafeResponse<SuperHero> findSuperHeroByName(String name);
    // ...
}
```
> [NOTE]
> Note that you can have `TypesafeResponse<T>` only as a result (return) type of your query.
> Unlike `ErrorOr` the class cannot wrap individual fields.
> If that is the case `IllegalArgumentException` will be thrown.   

And now to get `extensions` from the response your code will look like something like this:
```java
    @Inject
    SuperHeroApi client;

    TypesafeResponse<SuperHero> superheroResponse = client.findSuperHeroByName("Batman");
    // returns the "data" field
    SuperHero superhero = superhero.get();
    // returns the "extensions" field
    JsonObject extensions = superhero.getExtensions();
```
