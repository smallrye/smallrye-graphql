package io.smallrye.graphql.schema;

import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;

/**
 * Class helper
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class Classes {

    private Classes() {
    }

    /**
     * Check if a certain type is Optional
     * 
     * @param type the type
     * @return true if it is
     */
    public static boolean isOptional(Type type) {
        Type.Kind kind = type.kind();
        return kind.equals(Type.Kind.PARAMETERIZED_TYPE) && type.name().equals(OPTIONAL);
    }

    /**
     * Check if a certain class is an interface
     * 
     * @param classInfo the class to check
     * @return true if it is
     */
    public static boolean isInterface(ClassInfo classInfo) {
        if (classInfo == null)
            return false;
        return Modifier.isInterface(classInfo.flags());
    }

    /**
     * Check if a certain class is an enum
     * 
     * @param classInfo the class to check
     * @return true if it is
     */
    public static boolean isEnum(ClassInfo classInfo) {
        if (classInfo == null)
            return false;
        return classInfo.superName().equals(ENUM);
    }

    /**
     * Check if this type is a Number (or collection of numbers)
     * 
     * @param type the type to check
     * @return true if it is
     */
    public static boolean isNumberLikeTypeOrCollectionThereOf(Type type) {
        return isTypeOrCollectionThereOf(type, BYTE, BYTE_PRIMATIVE, SHORT, SHORT_PRIMATIVE, INTEGER, INTEGER_PRIMATIVE,
                BIG_INTEGER, DOUBLE, DOUBLE_PRIMATIVE, BIG_DECIMAL, LONG, LONG_PRIMATIVE, FLOAT, FLOAT_PRIMATIVE);
    }

    /**
     * Check if this type is a Date (or collection of numbers)
     * 
     * @param type the type to check
     * @return true if it is
     */
    public static boolean isDateLikeTypeOrCollectionThereOf(Type type) {
        return isTypeOrCollectionThereOf(type, LOCALDATE, LOCALTIME, LOCALDATETIME, ZONEDDATETIME, OFFSETDATETIME, OFFSETTIME,
                UTIL_DATE, SQL_DATE, SQL_TIMESTAMP, SQL_TIME);
    }

    private static boolean isTypeOrCollectionThereOf(Type type, DotName... valid) {
        switch (type.kind()) {
            case PARAMETERIZED_TYPE:
                // Collections
                Type typeInCollection = type.asParameterizedType().arguments().get(0);
                return isTypeOrCollectionThereOf(typeInCollection, valid);
            case ARRAY:
                // Array
                Type typeInArray = type.asArrayType().component();
                return isTypeOrCollectionThereOf(typeInArray, valid);
            default:
                for (DotName dotName : valid) {
                    if (type.name().toString().equals(dotName.toString())) {
                        return true;
                    }
                }
                return false;
        }
    }

    public static boolean isAsyncType(Type type) {
        return type.name().equals(COMPLETABLE_FUTURE) || type.name().equals(COMPLETION_STAGE);
    }

    /**
     * Return true if type is java array, or it is Collection type which is handled as GraphQL array
     * 
     * @param type to check
     * @return if this is a collection or array
     */
    public static boolean isCollectionOrArray(Type type) {
        return type.kind().equals(Type.Kind.ARRAY) || isCollection(type);
    }

    /**
     * Return true if type is java Collection type which is handled as GraphQL array
     * 
     * @param type to check
     * @return if this is a collection
     */
    public static boolean isCollection(Type type) {
        if (type.kind().equals(Type.Kind.PARAMETERIZED_TYPE)) {

            ClassInfo clazz = ScanningContext.getIndex().getClassByName(type.name());
            if (clazz == null) {
                // use classloader instead of jandex to handle basic java classes/interfaces
                try {
                    Class<?> clazzLoaded = Classes.class.getClassLoader().loadClass(type.name().toString());
                    return Collection.class.isAssignableFrom(clazzLoaded);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Info not found in Jandex index nor classpath for class name:" + type.name());
                }
            }
            if (clazz.name().equals(COLLECTION)) {
                return true;
            }

            // we have to go recursively over all super-interfaces as Jandex provides only direct interfaces
            // implemented in the class itself
            for (Type intf : clazz.interfaceTypes()) {
                if (isCollection(intf))
                    return true;
            }
        }
        return false;
    }

    /**
     * Return true if given type is parametrized type unwrapped/handled by the runtime before the serialization
     * (Optional&lt;&gt;, CompletableFutur&lt;&gt;, CompletionStage&lt;&gt; etc)
     * 
     * @param type to be checked
     * @return true if type is unwrapped by the runtime
     */
    public static boolean isUnwrappedType(Type type) {
        return type.kind().equals(Type.Kind.PARAMETERIZED_TYPE) && (isOptional(type) // Optional<>
                || isAsyncType(type)) // CompletableFutur or CompletionStage
        ;
    }

    public static final DotName COLLECTION = DotName.createSimple(Collection.class.getName());

    public static final DotName ENUM = DotName.createSimple(Enum.class.getName());
    public static final DotName OPTIONAL = DotName.createSimple(Optional.class.getName());

    public static final DotName LOCALDATE = DotName.createSimple(LocalDate.class.getName());
    public static final DotName LOCALDATETIME = DotName.createSimple(LocalDateTime.class.getName());
    public static final DotName LOCALTIME = DotName.createSimple(LocalTime.class.getName());
    public static final DotName ZONEDDATETIME = DotName.createSimple(ZonedDateTime.class.getName());
    public static final DotName OFFSETDATETIME = DotName.createSimple(OffsetDateTime.class.getName());
    public static final DotName OFFSETTIME = DotName.createSimple(OffsetTime.class.getName());

    public static final DotName PERIOD = DotName.createSimple(Period.class.getName());
    public static final DotName DURATION = DotName.createSimple(Duration.class.getName());

    public static final DotName UTIL_DATE = DotName.createSimple(Date.class.getName());
    public static final DotName SQL_DATE = DotName.createSimple(java.sql.Date.class.getName());
    public static final DotName SQL_TIMESTAMP = DotName.createSimple(java.sql.Timestamp.class.getName());
    public static final DotName SQL_TIME = DotName.createSimple(java.sql.Time.class.getName());

    private static final DotName BYTE = DotName.createSimple(Byte.class.getName());
    private static final DotName BYTE_PRIMATIVE = DotName.createSimple(byte.class.getName());

    private static final DotName SHORT = DotName.createSimple(Short.class.getName());
    private static final DotName SHORT_PRIMATIVE = DotName.createSimple(short.class.getName());

    private static final DotName INTEGER = DotName.createSimple(Integer.class.getName());
    private static final DotName INTEGER_PRIMATIVE = DotName.createSimple(int.class.getName());
    private static final DotName BIG_INTEGER = DotName.createSimple(BigInteger.class.getName());

    private static final DotName DOUBLE = DotName.createSimple(Double.class.getName());
    private static final DotName DOUBLE_PRIMATIVE = DotName.createSimple(double.class.getName());
    private static final DotName BIG_DECIMAL = DotName.createSimple(BigDecimal.class.getName());

    private static final DotName LONG = DotName.createSimple(Long.class.getName());
    private static final DotName LONG_PRIMATIVE = DotName.createSimple(long.class.getName());

    private static final DotName FLOAT = DotName.createSimple(Float.class.getName());
    private static final DotName FLOAT_PRIMATIVE = DotName.createSimple(float.class.getName());

    public static final DotName COMPLETABLE_FUTURE = DotName.createSimple(CompletableFuture.class.getName());
    public static final DotName COMPLETION_STAGE = DotName.createSimple(CompletionStage.class.getName());

}
