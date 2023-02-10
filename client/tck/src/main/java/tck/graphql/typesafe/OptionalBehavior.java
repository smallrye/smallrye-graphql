package tck.graphql.typesafe;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;

class OptionalBehavior {
    private final TypesafeGraphQLClientFixture fixture = TypesafeGraphQLClientFixture.load();

    @GraphQLClientApi
    interface OptionalStringApi {
        Optional<String> greeting();
    }

    @Test
    void shouldCallNullOptionalStringQuery() {
        fixture.returnsData("'greeting':null");
        OptionalStringApi api = fixture.build(OptionalStringApi.class);

        Optional<String> greeting = api.greeting();

        then(fixture.query()).isEqualTo("query greeting { greeting }");
        then(greeting).isEmpty();
    }

    @Test
    void shouldCallOptionalStringQuery() {
        fixture.returnsData("'greeting':'hi'");
        OptionalStringApi api = fixture.build(OptionalStringApi.class);

        Optional<String> greeting = api.greeting();

        then(fixture.query()).isEqualTo("query greeting { greeting }");
        then(greeting).contains("hi");
    }

    @GraphQLClientApi
    interface OptionalGreetingApi {
        Optional<Greeting> greeting();
    }

    private static class Greeting {
        String text;
        int code;

        @SuppressWarnings("unused")
        Greeting() {
        }

        Greeting(String text, int code) {
            this.text = text;
            this.code = code;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Greeting greeting = (Greeting) o;
            return code == greeting.code && text.equals(greeting.text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(text, code);
        }
    }

    @Test
    void shouldCallOptionalGreetingQuery() {
        fixture.returnsData("'greeting':{'text':'hi','code':5}");
        OptionalGreetingApi api = fixture.build(OptionalGreetingApi.class);

        Optional<Greeting> greeting = api.greeting();

        then(fixture.query()).isEqualTo("query greeting { greeting {text code} }");
        then(greeting).contains(new Greeting("hi", 5));
    }

    @Test
    void shouldCallNullOptionalGreetingQuery() {
        fixture.returnsData("'greeting':null");
        OptionalGreetingApi api = fixture.build(OptionalGreetingApi.class);

        Optional<Greeting> greeting = api.greeting();

        then(fixture.query()).isEqualTo("query greeting { greeting {text code} }");
        then(greeting).isEmpty();
    }

    @GraphQLClientApi
    interface OptionalGreetingListApi {
        Optional<List<Greeting>> greeting();
    }

    @Test
    void shouldCallOptionalGreetingListQuery() {
        fixture.returnsData("'greeting':[{'text':'hi','code':5},{'text':'ho','code':7}]");
        OptionalGreetingListApi api = fixture.build(OptionalGreetingListApi.class);

        Optional<List<Greeting>> greeting = api.greeting();

        then(fixture.query()).isEqualTo("query greeting { greeting {text code} }");
        assert greeting.isPresent();
        then(greeting.get()).contains(new Greeting("hi", 5), new Greeting("ho", 7));
    }

    @Test
    void shouldCallEmptyOptionalGreetingListQuery() {
        fixture.returnsData("'greeting':[]");
        OptionalGreetingListApi api = fixture.build(OptionalGreetingListApi.class);

        Optional<List<Greeting>> greeting = api.greeting();

        then(fixture.query()).isEqualTo("query greeting { greeting {text code} }");
        assert greeting.isPresent();
        then(greeting.get()).isEmpty();
    }

    @GraphQLClientApi
    interface ListOfOptionalGreetingApi {
        List<Optional<Greeting>> greetings();
    }

    @Test
    void shouldCallListOfOptionalGreetingsQuery() {
        fixture.returnsData("'greetings':[{'text':'hi','code':5},{'text':'ho','code':7}]");
        ListOfOptionalGreetingApi api = fixture.build(ListOfOptionalGreetingApi.class);

        List<Optional<Greeting>> greetings = api.greetings();

        then(fixture.query()).isEqualTo("query greetings { greetings {text code} }");
        then(greetings).containsExactly(
                Optional.of(new Greeting("hi", 5)),
                Optional.of(new Greeting("ho", 7)));
    }

    @Test
    void shouldCallEmptyListOfOptionalGreetingsQuery() {
        fixture.returnsData("'greetings':[]");
        ListOfOptionalGreetingApi api = fixture.build(ListOfOptionalGreetingApi.class);

        List<Optional<Greeting>> greetings = api.greetings();

        then(fixture.query()).isEqualTo("query greetings { greetings {text code} }");
        then(greetings).isEmpty();
    }

    @GraphQLClientApi
    interface OptionalOptionalStringApi {
        Optional<Optional<String>> greeting();
    }

    @Test
    void shouldCallOptionalOptionalStringQuery() {
        fixture.returnsData("'greeting':'hi'");
        OptionalOptionalStringApi api = fixture.build(OptionalOptionalStringApi.class);

        Optional<Optional<String>> greeting = api.greeting();

        then(fixture.query()).isEqualTo("query greeting { greeting }");
        then(greeting).contains(Optional.of("hi"));
    }

    @Test
    void optionalIntOutputTest() {
        fixture.returnsData("'allAnimalLegs':[{'name':'Lion', 'numberOfLegs':4}," +
                " {'name': 'Centipedes', 'numberOfLegs':70 }, {'name': 'Snake', 'numberOfLegs':0}]");
        AnimalApi api = fixture.build(AnimalApi.class);

        List<Animal> listOfAnimalLegs = api.allAnimalLegs();
        then(fixture.query()).isEqualTo("query allAnimalLegs { allAnimalLegs {name numberOfLegs" +
                " numberOfTeeth price} }");
        then(listOfAnimalLegs).containsExactly(
                new Animal("Lion", OptionalInt.of(4)),
                new Animal("Centipedes", OptionalInt.of(70)),
                new Animal("Snake", OptionalInt.of(0)));
    }

    @Test
    void optionalLongOutputTest() {
        fixture.returnsData("'allAnimalTeeth':[{'name':'Lion', 'numberOfTeeth':30}," +
                " {'name': 'Centipedes', 'numberOfTeeth':0 }, {'name': 'Snake', 'numberOfTeeth':100}]");
        AnimalApi api = fixture.build(AnimalApi.class);
        List<Animal> listOfAnimalTeeth = api.allAnimalTeeth();
        then(fixture.query()).isEqualTo("query allAnimalTeeth { allAnimalTeeth {name numberOfLegs" +
                " numberOfTeeth price} }");
        then(listOfAnimalTeeth).containsExactly(
                new Animal("Lion", OptionalLong.of(30)),
                new Animal("Centipedes", OptionalLong.of(0)),
                new Animal("Snake", OptionalLong.of(100)));
    }

    @Test
    void optionalDoubleOutputTest() {
        fixture.returnsData("'allAnimalPrice':[{'name':'Lion', 'price':355655.74}," +
                " {'name': 'Centipedes', 'price':241.62 }, {'name': 'Snake', 'price':1648.28}]");
        AnimalApi api = fixture.build(AnimalApi.class);
        List<Animal> listOfAnimalPrice = api.allAnimalPrice();
        then(fixture.query()).isEqualTo("query allAnimalPrice { allAnimalPrice {name numberOfLegs" +
                " numberOfTeeth price} }");
        then(listOfAnimalPrice).containsExactly(
                new Animal("Lion", OptionalDouble.of(355655.74)),
                new Animal("Centipedes", OptionalDouble.of(241.62)),
                new Animal("Snake", OptionalDouble.of(1648.28)));
    }

    @Test
    void optionalIntParameterQuery() {
        fixture.returnsData("'animalsByLegs':[" +
                "{'name':'Snake', 'numberOfLegs':0, 'numberOfTeeth':100, 'price':1648.28}]");
        AnimalApi api = fixture.build(AnimalApi.class);
        List<Animal> animals = api.getAnimalsByLegs(OptionalInt.of(0));
        then(fixture.query()).isEqualTo("query animalsByLegs($numberOfLegs: Int) { " +
                "animalsByLegs(numberOfLegs: $numberOfLegs) {name numberOfLegs numberOfTeeth price} }");
        then(fixture.variables()).isEqualTo("{'numberOfLegs':0}");
        then(animals).containsExactly(
                new Animal("Snake",
                        OptionalInt.of(0),
                        OptionalLong.of(100),
                        OptionalDouble.of(1648.28)));
    }

    @Test
    void optionalIntMutationTest() {
        fixture.returnsData("'newAnimal':{'name':'Pig', 'numberOfLegs':4}");
        AnimalApi api = fixture.build(AnimalApi.class);
        Animal animal = api.newAnimal(new Animal("Pig", OptionalInt.of(4)));
        then(fixture.query()).isEqualTo("mutation newAnimal($animal: AnimalInput) " +
                "{ newAnimal(animal: $animal) {name numberOfLegs numberOfTeeth price} }");
        then(fixture.variables()).isEqualTo("{'animal':{'name':'Pig','numberOfLegs':" +
                "4}}");
        then(animal).isEqualTo(new Animal("Pig", OptionalInt.of(4)));
    }

    @Test
    void optionalLongParameterQuery() {
        fixture.returnsData("'animalsByTeeth':[" +
                "{'name':'Lion', 'numberOfLegs':4, 'numberOfTeeth':30, 'price':330705.0}, " +
                "{'name':'Tiger', 'numberOfLegs':4, 'numberOfTeeth':30, 'price':165352.5}, " +
                "{'name':'Rhino', 'numberOfLegs':4, 'numberOfTeeth':30, 'price':2215390.3}]");
        AnimalApi api = fixture.build(AnimalApi.class);
        List<Animal> animals = api.getAnimalsByTeeth(OptionalLong.of(30));
        then(fixture.query()).isEqualTo("query animalsByTeeth($numberOfTeeth: BigInteger) { " +
                "animalsByTeeth(numberOfTeeth: $numberOfTeeth) {name numberOfLegs numberOfTeeth price} }");
        then(fixture.variables()).isEqualTo("{'numberOfTeeth':30}");
        then(animals).containsExactly(
                new Animal("Lion",
                        OptionalInt.of(4),
                        OptionalLong.of(30),
                        OptionalDouble.of(330705.0)),
                new Animal("Tiger",
                        OptionalInt.of(4),
                        OptionalLong.of(30),
                        OptionalDouble.of(165352.5)),
                new Animal("Rhino",
                        OptionalInt.of(4),
                        OptionalLong.of(30),
                        OptionalDouble.of(2215390.3)));
    }

    @Test
    void optionalLongMutationTest() {
        fixture.returnsData("'newAnimal':{'name':'Bat', 'numberOfTeeth':20}");
        AnimalApi api = fixture.build(AnimalApi.class);
        Animal animal = api.newAnimal(new Animal("Bat", OptionalLong.of(20)));
        then(fixture.query()).isEqualTo("mutation newAnimal($animal: AnimalInput) " +
                "{ newAnimal(animal: $animal) {name numberOfLegs numberOfTeeth price} }");
        then(fixture.variables()).isEqualTo("{'animal':{'name':'Bat','numberOfTeeth':" +
                "20}}");
        then(animal).isEqualTo(new Animal("Bat", OptionalLong.of(20)));
    }

    @Test
    void optionalDoubleParameterQuery() {
        fixture.returnsData("'animalsByPrice':[{'name':'Elephant'," +
                " 'numberOfLegs':4, 'numberOfTeeth':26, 'price':2215390.3}," +
                " {'name':'Rhino', 'numberOfLegs':4, 'numberOfTeeth':30, 'price':2215390.3}]");
        AnimalApi api = fixture.build(AnimalApi.class);
        List<Animal> animals = api.getAnimalsByPrice(OptionalDouble.of(2215390.3));
        then(fixture.query()).isEqualTo("query animalsByPrice($price: Float) { " +
                "animalsByPrice(price: $price) {name numberOfLegs numberOfTeeth price} }");
        then(fixture.variables()).isEqualTo("{'price':2215390.3}");
        then(animals).containsExactly(
                new Animal("Elephant",
                        OptionalInt.of(4),
                        OptionalLong.of(26),
                        OptionalDouble.of(2215390.3)),
                new Animal("Rhino",
                        OptionalInt.of(4),
                        OptionalLong.of(30),
                        OptionalDouble.of(2215390.3)));
    }

    @Test
    void optionalDoubleMutationTest() {
        fixture.returnsData("'newAnimal':{'name':'Labrador', 'price':6610.50}");
        AnimalApi api = fixture.build(AnimalApi.class);
        Animal animal = api.newAnimal(new Animal("Labrador", OptionalDouble.of(6610.50)));
        then(fixture.query()).isEqualTo("mutation newAnimal($animal: AnimalInput) " +
                "{ newAnimal(animal: $animal) {name numberOfLegs numberOfTeeth price} }");
        then(fixture.variables()).isEqualTo("{'animal':{'name':'Labrador','price':" +
                "6610.5}}");
        then(animal).isEqualTo(new Animal("Labrador", OptionalDouble.of(6610.50)));
    }

    static class Car {
        private List<OptionalLong> boo;

        public Car() {
        }

        public Car(List<OptionalLong> boo) {
            this.boo = boo;
        }

        public List<OptionalLong> getYearOfRelease() {
            return boo;
        }

        public void setBoo(List<OptionalLong> boo) {
            this.boo = boo;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Car car = (Car) o;
            return Objects.equals(boo, car.boo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(boo);
        }
    }

    @GraphQLClientApi
    interface CarApi {
        Car getRandomCar();
    }

    @Test
    void optionalNumberNull() {
        fixture.returnsData("'randomCar':{'boo':[null, 3, null]}");
        CarApi api = fixture.build(CarApi.class);
        Car car = api.getRandomCar();
        then(fixture.query()).isEqualTo("query randomCar { randomCar {boo} }");
        then(car).isEqualTo(new Car(List.of(
                OptionalLong.empty(),
                OptionalLong.of(3),
                OptionalLong.empty())));
    }
}
