package io.smallrye.graphql.client.modelbuilder;

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

public class Scalars {
    private static final Map<String, String> scalarMap = new HashMap<>();
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

    private Scalars() {
    }

    public static boolean isScalar(String className) {
        return scalarMap.containsKey(className);
    }

    public static String getScalar(String identifier) {
        return scalarMap.get(identifier);
    }

    public static boolean isStringScalar(String className) {
        String potentionalString = scalarMap.get(className);

        return potentionalString != null && scalarMap.get(className).equals(STRING);
    }

    static {
        // The main java type should go first.

        // Strings
        populateScalar(String.class.getName(), STRING);
        populateScalar(char.class.getName(), STRING);
        populateScalar(Character.class.getName(), STRING);
        populateScalar(UUID.class.getName(), STRING);
        populateScalar(URL.class.getName(), STRING);
        populateScalar(URI.class.getName(), STRING);
        populateScalar("org.bson.types.ObjectId", STRING);
        populateScalar("javax.json.JsonObject", STRING);
        populateScalar("javax.json.JsonArray", STRING);
        populateScalar("jakarta.json.JsonObject", STRING);
        populateScalar("jakarta.json.JsonArray", STRING);

        // Boolean
        populateScalar(Boolean.class.getName(), BOOLEAN);
        populateScalar(boolean.class.getName(), BOOLEAN);
        populateScalar(AtomicBoolean.class.getName(), BOOLEAN);

        // Integer
        populateScalar(Integer.class.getName(), INTEGER);
        populateScalar(int.class.getName(), INTEGER);
        populateScalar(Short.class.getName(), INTEGER);
        populateScalar(short.class.getName(), INTEGER);
        populateScalar(Byte.class.getName(), INTEGER);
        populateScalar(byte.class.getName(), INTEGER);
        populateScalar(OptionalInt.class.getName(), INTEGER);
        populateScalar(AtomicInteger.class.getName(), INTEGER);

        // Float
        populateScalar(Float.class.getName(), FLOAT);
        populateScalar(float.class.getName(), FLOAT);
        populateScalar(Double.class.getName(), FLOAT);
        populateScalar(double.class.getName(), FLOAT);
        populateScalar(OptionalDouble.class.getName(), FLOAT);

        // BigInteger
        populateScalar(BigInteger.class.getName(), BIGINTEGER);
        populateScalar(Long.class.getName(), BIGINTEGER);
        populateScalar(long.class.getName(), BIGINTEGER);
        populateScalar(OptionalLong.class.getName(), BIGINTEGER);
        populateScalar(AtomicLong.class.getName(), BIGINTEGER);

        // BigDecimal
        populateScalar(BigDecimal.class.getName(), BIGDECIMAL);

        // Date
        populateScalar(LocalDate.class.getName(), DATE);
        populateScalar(Date.class.getName(), DATE);

        // Time
        populateScalar(LocalTime.class.getName(), TIME);
        populateScalar(Time.class.getName(), TIME);
        populateScalar(OffsetTime.class.getName(), TIME);

        // DateTime
        populateScalar(LocalDateTime.class.getName(), DATETIME);
        populateScalar(java.util.Date.class.getName(), DATETIME);
        populateScalar(Timestamp.class.getName(), DATETIME);
        populateScalar(ZonedDateTime.class.getName(), DATETIME);
        populateScalar(OffsetDateTime.class.getName(), DATETIME);
        populateScalar(Instant.class.getName(), DATETIME);
        populateScalar(Calendar.class.getName(), DATETIME);
        populateScalar(GregorianCalendar.class.getName(), DATETIME);

        // Duration
        populateScalar(Duration.class.getName(), DURATION);
        // Period
        populateScalar(Period.class.getName(), PERIOD);

        // Void
        populateScalar(Void.class.getName(), VOID);
        populateScalar(void.class.getName(), VOID);
    }

    private static void populateScalar(String className, String scalarName) {
        scalarMap.put(className, scalarName);
    }

}
