package io.smallrye.graphql.schema;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.smallrye.graphql.schema.model.DefinitionType;
import io.smallrye.graphql.schema.model.Reference;

/**
 * Here we keep all the scalars we know about
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class Scalars {
    private static final Map<String, Reference> scalarMap = new HashMap<>();
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

    private Scalars() {
    }

    public static boolean isScalar(String className) {
        return scalarMap.containsKey(className);
    }

    public static Reference getScalar(String className) {
        return scalarMap.get(className);
    }

    public static Reference getIDScalar() {
        return scalarMap.get(ID);
    }

    static {
        populateScalar(char.class.getName(), STRING);
        populateScalar(Character.class.getName(), STRING);
        populateScalar(String.class.getName(), STRING);
        populateScalar(UUID.class.getName(), STRING);

        populateScalar(URL.class.getName(), STRING);
        populateScalar(URI.class.getName(), STRING);

        populateScalar(Boolean.class.getName(), BOOLEAN);
        populateScalar(boolean.class.getName(), BOOLEAN);

        populateScalar(Integer.class.getName(), INTEGER);
        populateScalar(int.class.getName(), INTEGER);
        populateScalar(Short.class.getName(), INTEGER);
        populateScalar(short.class.getName(), INTEGER);
        populateScalar(Byte.class.getName(), INTEGER);
        populateScalar(byte.class.getName(), INTEGER);

        populateScalar(Float.class.getName(), FLOAT);
        populateScalar(float.class.getName(), FLOAT);
        populateScalar(Double.class.getName(), FLOAT);
        populateScalar(double.class.getName(), FLOAT);

        populateScalar(BigInteger.class.getName(), BIGINTEGER);
        populateScalar(Long.class.getName(), BIGINTEGER);
        populateScalar(long.class.getName(), BIGINTEGER);

        populateScalar(BigDecimal.class.getName(), BIGDECIMAL);

        populateScalar(LocalDate.class.getName(), DATE);
        populateScalar(Date.class.getName(), DATE);

        populateScalar(LocalTime.class.getName(), TIME);
        populateScalar(Time.class.getName(), TIME);
        populateScalar(OffsetTime.class.getName(), TIME);

        populateScalar(LocalDateTime.class.getName(), DATETIME);
        populateScalar(java.util.Date.class.getName(), DATETIME);
        populateScalar(Timestamp.class.getName(), DATETIME);
        populateScalar(ZonedDateTime.class.getName(), DATETIME);
        populateScalar(OffsetDateTime.class.getName(), DATETIME);

        populateScalar(ID, ID);
    }

    private static void populateScalar(String className, String scalarName) {
        scalarMap.put(className, new Reference(className, scalarName, DefinitionType.SCALAR));
    }

}
