package io.smallrye.graphql.execution;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Class helper
 * 
 * TODO: This needs cleanup. Some stuff might not be used.
 * TODO: Merge with classes in model-builder and move to separate module
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class Classes {
    private static final Map<String, Class> loadedClasses = new HashMap<>();

    private Classes() {
    }

    public static Class<?> loadClass(String className) {
        try {
            if (isPrimitive(className)) {
                return getPrimativeClassType(className);
            } else {
                if (loadedClasses.containsKey(className)) {
                    return loadedClasses.get(className);
                } else {

                    return AccessController.doPrivileged((PrivilegedExceptionAction<Class<?>>) () -> {
                        ClassLoader loader = Thread.currentThread().getContextClassLoader();
                        if (loader != null) {
                            try {
                                return loadClass(className, loader);
                            } catch (ClassNotFoundException cnfe) {
                                // Let's try this class classloader.
                            }
                        }
                        return loadClass(className, Classes.class.getClassLoader());
                    });

                }
            }
        } catch (PrivilegedActionException | ClassNotFoundException pae) {
            throw new RuntimeException("Can not load class [" + className + "]", pae);
        }
    }

    public static Class<?> loadClass(String className, ClassLoader loader) throws ClassNotFoundException {
        Class<?> c = Class.forName(className, false, loader);
        loadedClasses.put(className, c);
        return c;
    }

    public static boolean isOptional(String className) {
        return className.equals(OPTIONAL);
    }

    public static boolean isUUID(String className) {
        return className.equals(UUID);
    }

    public static boolean isURL(String className) {
        return className.equals(URL);
    }

    public static boolean isURI(String className) {
        return className.equals(URI);
    }

    public static boolean isPrimitive(String primitiveName) {
        return PRIMITIVE_CLASSES.containsKey(primitiveName);
    }

    public static boolean isCollection(Object c) {
        if (c == null)
            return false;
        return Collection.class.isAssignableFrom(c.getClass());
    }

    private static Class getPrimativeClassType(String primitiveName) throws ClassNotFoundException {
        if (isPrimitive(primitiveName)) {
            return PRIMITIVE_CLASSES.get(primitiveName);
        }
        throw new ClassNotFoundException("Unknown primative type [" + primitiveName + "]");
    }

    private static final Map<String, Class> PRIMITIVE_CLASSES = new HashMap<>();
    private static final Map<String, Class> OBJECT_PRIMITIVE_MAPPING = new HashMap<>();

    public static final String ENUM = Enum.class.getName();
    public static final String OPTIONAL = Optional.class.getName();

    public static final String UUID = java.util.UUID.class.getName();
    public static final String URL = java.net.URL.class.getName();
    public static final String URI = java.net.URI.class.getName();

    public static final String LOCALDATE = LocalDate.class.getName();
    public static final String LOCALDATETIME = LocalDateTime.class.getName();
    public static final String LOCALTIME = LocalTime.class.getName();
    public static final String ZONEDDATETIME = ZonedDateTime.class.getName();
    public static final String OFFSETDATETIME = OffsetDateTime.class.getName();
    public static final String OFFSETTIME = OffsetTime.class.getName();

    public static final String UTIL_DATE = Date.class.getName();
    public static final String SQL_DATE = java.sql.Date.class.getName();
    public static final String SQL_TIMESTAMP = java.sql.Timestamp.class.getName();
    public static final String SQL_TIME = java.sql.Time.class.getName();

    public static final String BYTE = Byte.class.getName();
    public static final String BYTE_PRIMATIVE = byte.class.getName();

    public static final String SHORT = Short.class.getName();
    public static final String SHORT_PRIMATIVE = short.class.getName();

    public static final String INTEGER = Integer.class.getName();
    public static final String INTEGER_PRIMATIVE = int.class.getName();
    public static final String BIG_INTEGER = BigInteger.class.getName();

    public static final String DOUBLE = Double.class.getName();
    public static final String DOUBLE_PRIMATIVE = double.class.getName();
    public static final String BIG_DECIMAL = BigDecimal.class.getName();

    public static final String LONG = Long.class.getName();
    public static final String LONG_PRIMATIVE = long.class.getName();

    public static final String FLOAT = Float.class.getName();
    public static final String FLOAT_PRIMATIVE = float.class.getName();

    static {
        PRIMITIVE_CLASSES.put("boolean", boolean.class);
        PRIMITIVE_CLASSES.put("byte", byte.class);
        PRIMITIVE_CLASSES.put("char", char.class);
        PRIMITIVE_CLASSES.put("short", short.class);
        PRIMITIVE_CLASSES.put("int", int.class);
        PRIMITIVE_CLASSES.put("long", long.class);
        PRIMITIVE_CLASSES.put("float", float.class);
        PRIMITIVE_CLASSES.put("double", double.class);

        OBJECT_PRIMITIVE_MAPPING.put(Boolean.class.getName(), boolean.class);
        OBJECT_PRIMITIVE_MAPPING.put(Byte.class.getName(), byte.class);
        OBJECT_PRIMITIVE_MAPPING.put(Character.class.getName(), char.class);
        OBJECT_PRIMITIVE_MAPPING.put(Short.class.getName(), short.class);
        OBJECT_PRIMITIVE_MAPPING.put(Integer.class.getName(), int.class);
        OBJECT_PRIMITIVE_MAPPING.put(Long.class.getName(), long.class);
        OBJECT_PRIMITIVE_MAPPING.put(Float.class.getName(), float.class);
        OBJECT_PRIMITIVE_MAPPING.put(Double.class.getName(), double.class);
    }

    /**
     * Tests, if {@code boxedType} is the wrapper-type of {@code primitiveType}.
     * For example, {@code java.lang.Integer} is the wrapper for {@code int}.
     * 
     * @param primitiveType the classname of the primitive type
     * @param boxedType the classname of the boxed type
     * @return true, if {@code boxedType} is the wrapper-type of {@code primitiveType}
     */
    public static boolean isPrimitiveOf(final String primitiveType, final String boxedType) {
        if (OBJECT_PRIMITIVE_MAPPING.containsKey(boxedType)) {
            return OBJECT_PRIMITIVE_MAPPING.get(boxedType).getName().equals(primitiveType);
        }
        return false;
    }
}
