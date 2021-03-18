package io.smallrye.graphql.schema.model;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Here we keep all the scalars we know about
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class Scalars {
    private static final Map<String, Reference> scalarMap = new HashMap<>();
    private static final Map<String, Reference> scalarNameMap = new HashMap<>();
    private static final Map<String, Reference> formattedScalarMap = new HashMap<>();
    private static final String STRING = "String";
    private static final String BOOLEAN = "Boolean";
    private static final String INTEGER = "Int";
    private static final String FLOAT = "Float";
    private static final String BIGINTEGER = "BigInteger";
    private static final String BIGDECIMAL = "BigDecimal";
    private static final String DATE = "Date";
    private static final String TIME = "Time";
    private static final String DATETIME = "DateTime";
    private static final String ID = "ID";
    private static final String PERIOD = "Period";
    private static final String DURATION = "Duration";

    private Scalars() {
    }

    public static boolean isScalar(String className) {
        return scalarMap.containsKey(className);
    }

    public static Reference getScalar(String identifier) {
        if (scalarMap.containsKey(identifier)) {
            return scalarMap.get(identifier);
        } else {
            return scalarNameMap.get(identifier);
        }
    }

    public static Reference getFormattedScalar(String className) {
        return formattedScalarMap.get(className);
    }

    public static Reference getIDScalar(String className) {
        return new Reference(className, ID, ReferenceType.SCALAR);
    }

    static {
        // The main java type should go first.

        // Strings
        populateScalar(String.class.getName(), STRING, String.class.getName());
        populateScalar(char.class.getName(), STRING, String.class.getName());
        populateScalar(Character.class.getName(), STRING, String.class.getName());
        populateScalar(UUID.class.getName(), STRING, String.class.getName());
        populateScalar(URL.class.getName(), STRING, String.class.getName());
        populateScalar(URI.class.getName(), STRING, String.class.getName());
        populateScalar("javax.json.JsonObject", STRING, String.class.getName());
        populateScalar("javax.json.JsonArray", STRING, String.class.getName());

        // Boolean
        populateScalar(Boolean.class.getName(), BOOLEAN);
        populateScalar(boolean.class.getName(), BOOLEAN);
        populateScalar(AtomicBoolean.class.getName(), BOOLEAN);

        // Integer
        populateScalar(Integer.class.getName(), INTEGER, Integer.class.getName());
        populateScalar(int.class.getName(), INTEGER, Integer.class.getName());
        populateScalar(Short.class.getName(), INTEGER, Integer.class.getName());
        populateScalar(short.class.getName(), INTEGER, Integer.class.getName());
        populateScalar(Byte.class.getName(), INTEGER, Integer.class.getName());
        populateScalar(byte.class.getName(), INTEGER, Integer.class.getName());
        populateScalar(OptionalInt.class.getName(), INTEGER, Integer.class.getName());
        populateScalar(AtomicInteger.class.getName(), INTEGER, Integer.class.getName());

        // Float
        populateScalar(Float.class.getName(), FLOAT, Float.class.getName());
        populateScalar(float.class.getName(), FLOAT, Float.class.getName());
        populateScalar(Double.class.getName(), FLOAT, Float.class.getName());
        populateScalar(double.class.getName(), FLOAT, Float.class.getName());
        populateScalar(OptionalDouble.class.getName(), FLOAT, Float.class.getName());

        // BigInteger
        populateScalar(BigInteger.class.getName(), BIGINTEGER, BigInteger.class.getName());
        populateScalar(Long.class.getName(), BIGINTEGER, BigInteger.class.getName());
        populateScalar(long.class.getName(), BIGINTEGER, BigInteger.class.getName());

        // BigDecimal
        populateScalar(BigDecimal.class.getName(), BIGDECIMAL, BigDecimal.class.getName());
        populateScalar(OptionalLong.class.getName(), BIGINTEGER, BigInteger.class.getName());
        populateScalar(AtomicLong.class.getName(), BIGINTEGER, BigInteger.class.getName());

        // Date
        populateScalar(LocalDate.class.getName(), DATE, String.class.getName());
        populateScalar(Date.class.getName(), DATE, String.class.getName());

        // Time
        populateScalar(LocalTime.class.getName(), TIME, String.class.getName());
        populateScalar(Time.class.getName(), TIME, String.class.getName());
        populateScalar(OffsetTime.class.getName(), TIME, String.class.getName());

        // DateTime
        populateScalar(LocalDateTime.class.getName(), DATETIME, String.class.getName());
        populateScalar(java.util.Date.class.getName(), DATETIME, String.class.getName());
        populateScalar(Timestamp.class.getName(), DATETIME, String.class.getName());
        populateScalar(ZonedDateTime.class.getName(), DATETIME, String.class.getName());
        populateScalar(OffsetDateTime.class.getName(), DATETIME, String.class.getName());
        populateScalar(Instant.class.getName(), DATETIME, String.class.getName());

        // Duration
        populateScalar(Duration.class.getName(), DURATION, String.class.getName());
        // Period
        populateScalar(Period.class.getName(), PERIOD, String.class.getName());
    }

    private static void populateScalar(String className, String scalarName) {
        populateScalar(className, scalarName, className);
    }

    private static void populateScalar(String className, String scalarName, String externalClassName) {
        Reference reference = new Reference(className, scalarName, ReferenceType.SCALAR, externalClassName);
        scalarMap.put(className, reference);

        // looking up by name
        scalarNameMap.putIfAbsent(scalarName, reference);

        //Currently, each scalar is formatted as String
        formattedScalarMap.put(className, new Reference(className, STRING, ReferenceType.SCALAR, String.class.getName()));
    }

}
