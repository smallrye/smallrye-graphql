package tck.graphql.typesafe;

import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import org.eclipse.microprofile.graphql.Mutation;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;

@GraphQLClientApi
public interface AnimalApi {
    /* [{name, numberOfLegs}] */
    List<Animal> allAnimalLegs();

    /* [{name, numberOfTeeth}] */
    List<Animal> allAnimalTeeth();

    /* [{name, price}] */
    List<Animal> allAnimalPrice();

    /* [{name, numberOfLegs, numberOfTeeth, price}] */
    List<Animal> getAnimalsByLegs(OptionalInt numberOfLegs);

    /* [{name, numberOfLegs, numberOfTeeth, price}] */
    List<Animal> getAnimalsByTeeth(OptionalLong numberOfTeeth);

    /* [{name, numberOfLegs, numberOfTeeth, price}] */
    List<Animal> getAnimalsByPrice(OptionalDouble price);

    /* [{name, numberOfLegs, numberOfTeeth, price}] */
    @Mutation
    Animal newAnimal(Animal animal);
}
