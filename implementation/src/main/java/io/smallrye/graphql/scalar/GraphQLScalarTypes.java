package io.smallrye.graphql.scalar;

import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.time.DateTimeException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import graphql.Scalars;
import graphql.schema.GraphQLScalarType;
import io.smallrye.graphql.scalar.number.BigDecimalScalar;
import io.smallrye.graphql.scalar.number.BigIntegerScalar;
import io.smallrye.graphql.scalar.number.FloatScalar;
import io.smallrye.graphql.scalar.number.IntegerScalar;
import io.smallrye.graphql.scalar.time.DateScalar;
import io.smallrye.graphql.scalar.time.DateTimeScalar;
import io.smallrye.graphql.scalar.time.TimeScalar;
import io.smallrye.graphql.transformation.DateTransformer;
import io.smallrye.graphql.transformation.NumberTransformer;

/**
 * Here we keep all the graphql-java scalars
 * mapped by classname
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class GraphQLScalarTypes {

    private static final DateTransformer DATE_TRANSFORMER = DateTransformer.transformer();
    private static final NumberTransformer NUMBER_TRANSFORMER = NumberTransformer.transformer();

    private GraphQLScalarTypes() {
    }

    public static Map<String, GraphQLScalarType> getScalarMap() {
        return SCALAR_MAP;
    }

    public static boolean isScalarType(String className) {
        return SCALAR_MAP.containsKey(className);
    }

    public static Object stringToScalar(String input, String expectedClassName)
            throws ParseException, NumberFormatException, DateTimeException {
        // Boolean
        if (expectedClassName.equals(boolean.class.getName())) {
            return Boolean.parseBoolean(input);
        } else if (expectedClassName.equals(Boolean.class.getName())) {
            return Boolean.valueOf(input);

            // Character    
        } else if (expectedClassName.equals(char.class.getName())) {
            return input.charAt(0);
        } else if (expectedClassName.equals(Character.class.getName())) {
            return Character.valueOf(input.charAt(0));

            // Try date and number
        } else {
            Optional<Object> maybeDate = DATE_TRANSFORMER.stringToDateType(input, expectedClassName);
            if (maybeDate.isPresent()) {
                return maybeDate.get();
            }

            Optional<Object> maybeNumber = NUMBER_TRANSFORMER.stringToNumberType(input, expectedClassName);
            if (maybeNumber.isPresent()) {
                return maybeNumber.get();
            }

            // String (default)
            return input;
        }
    }

    // Scalar map we can just create now.
    private static final Map<String, GraphQLScalarType> SCALAR_MAP = new HashMap<>();
    private static final String ID = "ID";

    static {
        SCALAR_MAP.put(ID, Scalars.GraphQLID);

        SCALAR_MAP.put(Boolean.class.getName(), Scalars.GraphQLBoolean);
        SCALAR_MAP.put(boolean.class.getName(), Scalars.GraphQLBoolean);

        SCALAR_MAP.put(char.class.getName(), Scalars.GraphQLString);
        SCALAR_MAP.put(Character.class.getName(), Scalars.GraphQLString);

        SCALAR_MAP.put(String.class.getName(), Scalars.GraphQLString);
        SCALAR_MAP.put(UUID.class.getName(), Scalars.GraphQLString);
        SCALAR_MAP.put(URL.class.getName(), Scalars.GraphQLString);
        SCALAR_MAP.put(URI.class.getName(), Scalars.GraphQLString);

        mapType(new IntegerScalar()); // Integer, int, Short, short, Byte, byte
        mapType(new FloatScalar()); // Float, float, Double, double
        mapType(new BigIntegerScalar()); // BigInteger, Long, long
        mapType(new BigDecimalScalar()); // BigDecimal
        mapType(new DateScalar()); // LocalDate, java.sql.Date
        mapType(new TimeScalar()); // LocalTime, java.sql.Time, OffsetTime
        mapType(new DateTimeScalar()); // LocalDateTime, Date, java.sql.Timestamp, ZonedDateTime, OffsetDateTime
    }

    private static void mapType(AbstractScalar abstractScalar) {
        for (Class c : abstractScalar.getSupportedClasses()) {
            SCALAR_MAP.put(c.getName(), (GraphQLScalarType) abstractScalar);
        }
    }
}
