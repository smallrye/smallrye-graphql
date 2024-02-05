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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import io.smallrye.graphql.api.federation.FieldSet;
import io.smallrye.graphql.api.federation.policy.PolicyItem;
import io.smallrye.graphql.api.federation.requiresscopes.ScopeItem;

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
    private static final String VOID = "Void";
    private static final String FIELD_SET = "FieldSet";
    private static final String POLICY = "Policy";
    private static final String SCOPE = "Scope";

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

    public static Reference getIntScalar() {
        return getScalar(INTEGER);
    }

    public static Reference getStringScalar() {
        return getScalar(STRING);
    }

    public static Reference getBigIntegerScalar() {
        return getScalar(BIGINTEGER);
    }

    public static Reference getBigDecimalScalar() {
        return getScalar(BIGDECIMAL);
    }

    public static Reference getFormattedScalar(String className) {
        return formattedScalarMap.get(className);
    }

    public static Reference getIDScalar(String className) {
        return new Reference.Builder()
                .className(className)
                .name(ID)
                .type(ReferenceType.SCALAR)
                .build();
    }

    public static void registerCustomScalarInSchema(
            String graphQlScalarName,
            String valueClassName) {
        populateScalar(valueClassName, graphQlScalarName, valueClassName);
    }

    // this is for the UUID from graphql-java-extended-scalars
    // if used, it will override the original UUID type that is mapped to a String in the schema
    public static void addUuid() {
        populateScalar(UUID.class.getName(), "UUID", String.class.getName());
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
        populateScalar("org.bson.types.ObjectId", STRING, String.class.getName());
        populateScalar("javax.json.JsonObject", STRING, String.class.getName());
        populateScalar("javax.json.JsonArray", STRING, String.class.getName());
        populateScalar("jakarta.json.JsonObject", STRING, String.class.getName());
        populateScalar("jakarta.json.JsonArray", STRING, String.class.getName());

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
        populateScalar(OptionalLong.class.getName(), BIGINTEGER, BigInteger.class.getName());
        populateScalar(AtomicLong.class.getName(), BIGINTEGER, BigInteger.class.getName());

        // BigDecimal
        populateScalar(BigDecimal.class.getName(), BIGDECIMAL, BigDecimal.class.getName());

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
        populateScalar(Calendar.class.getName(), DATETIME, String.class.getName());
        populateScalar(GregorianCalendar.class.getName(), DATETIME, String.class.getName());

        // Duration
        populateScalar(Duration.class.getName(), DURATION, String.class.getName());
        // Period
        populateScalar(Period.class.getName(), PERIOD, String.class.getName());

        // Void
        populateScalar(Void.class.getName(), VOID, Void.class.getName());
        populateScalar(void.class.getName(), VOID, Void.class.getName());

        // Federation
        if (Boolean.getBoolean("smallrye.graphql.federation.enabled")) {
            populateScalar(FieldSet.class.getName(), FIELD_SET, FieldSet.class.getName());
            populateScalar(PolicyItem.class.getName(), POLICY, PolicyItem.class.getName());
            populateScalar(ScopeItem.class.getName(), SCOPE, ScopeItem.class.getName());
        }
    }

    private static void populateScalar(String className, String scalarName) {
        populateScalar(className, scalarName, className);
    }

    private static void populateScalar(String className, String scalarName, String externalClassName) {
        Reference reference = new Reference.Builder()
                .className(className)
                .name(scalarName)
                .type(ReferenceType.SCALAR)
                .graphQLClassName(externalClassName)
                .build();
        scalarMap.put(className, reference);

        // looking up by name
        scalarNameMap.put(scalarName, reference);

        //Currently, each scalar is formatted as String
        formattedScalarMap.put(className, new Reference.Builder()
                .className(className)
                .name(STRING)
                .type(ReferenceType.SCALAR)
                .graphQLClassName(String.class.getName())
                .build());
    }

}
