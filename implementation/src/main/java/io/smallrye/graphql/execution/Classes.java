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

import io.smallrye.graphql.x.schema.PrimitiveTypeNotFoundException;
import io.smallrye.graphql.x.schema.ScalarTypeNotFoundException;

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

        if (isPrimitive(className)) {
            return getPrimativeClassType(className);
        } else {
            if (loadedClasses.containsKey(className)) {
                return loadedClasses.get(className);
            } else {
                try {
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
                } catch (PrivilegedActionException pae) {
                    throw new RuntimeException("Can not load class [" + className + "]", pae);
                }
            }
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

    //    public static boolean isInterface(ClassInfo classInfo) {
    //        return Modifier.isInterface(classInfo.flags());
    //    }
    //
    //    public static boolean isEnum(ClassInfo classInfo) {
    //        if (classInfo == null)
    //            return false;
    //        return classInfo.superName().equals(ENUM);
    //    }

    public static boolean isPrimitive(String primitiveName) {
        return PRIMITIVE_CLASSES.containsKey(primitiveName);
    }

    public static boolean isCollection(Object c) {
        if (c == null)
            return false;
        return Collection.class.isAssignableFrom(c.getClass());
    }

    public static Class getPrimativeClassType(String primitiveName) {
        if (isPrimitive(primitiveName)) {
            return PRIMITIVE_CLASSES.get(primitiveName);
        }
        throw new PrimitiveTypeNotFoundException("Unknown primative type [" + primitiveName + "]");
    }

    public static Class toPrimativeClassType(Object currentValue) {
        String currentValueClass = currentValue.getClass().toString();
        if (OBJECT_PRIMITIVE_MAPPING.containsKey(currentValueClass)) {
            return OBJECT_PRIMITIVE_MAPPING.get(currentValueClass);
        }
        return currentValue.getClass();
    }

    public static Object stringToScalar(String input, String expectedClassName) {
        if (isPrimitive(expectedClassName)) {
            return stringToPrimativeScalar(input, expectedClassName);
        } else {
            return stringToObjectScalar(input, expectedClassName);
        }
    }

    private static Object stringToPrimativeScalar(String input, String expectedClassName) {
        if (expectedClassName.equals(boolean.class.getName())) {
            return Boolean.parseBoolean(input);
        } else if (expectedClassName.equals(byte.class.getName())) {
            return Byte.parseByte(input);
        } else if (expectedClassName.equals(char.class.getName())) {
            return input.charAt(0);
        } else if (expectedClassName.equals(short.class.getName())) {
            return Short.parseShort(input);
        } else if (expectedClassName.equals(int.class.getName())) {
            return Integer.parseInt(input);
        } else if (expectedClassName.equals(long.class.getName())) {
            return Long.parseLong(input);
        } else if (expectedClassName.equals(float.class.getName())) {
            return Float.parseFloat(input);
        } else if (expectedClassName.equals(double.class.getName())) {
            return Double.parseDouble(input);
        } else {
            throw new ScalarTypeNotFoundException(
                    "Can not create new primative scalar type [" + expectedClassName + "] from input [" + input + "]");
        }
    }

    private static Object stringToObjectScalar(String input, String expectedClassName) {
        if (expectedClassName.equals(Boolean.class.getName())) {
            return Boolean.valueOf(input);
        } else if (expectedClassName.equals(Byte.class.getName())) {
            return Byte.valueOf(input);
        } else if (expectedClassName.equals(Character.class.getName())) {
            return Character.valueOf(input.charAt(0));
        } else if (expectedClassName.equals(Short.class.getName())) {
            return Short.valueOf(input);
        } else if (expectedClassName.equals(Integer.class.getName())) {
            return Integer.valueOf(input);
        } else if (expectedClassName.equals(Long.class.getName())) {
            return Long.valueOf(input);
        } else if (expectedClassName.equals(Float.class.getName())) {
            return Float.valueOf(input);
        } else if (expectedClassName.equals(Double.class.getName())) {
            return Double.valueOf(input);
        } else if (expectedClassName.equals(BigDecimal.class.getName())) {
            return new BigDecimal(input);
        } else if (expectedClassName.equals(BigInteger.class.getName())) {
            return new BigInteger(input);
        } else {// if (){
            throw new ScalarTypeNotFoundException(
                    "Can not create new object scalar type [" + expectedClassName + "] from input [" + input + "] that is ["
                            + input.getClass().getName() + "]");
        }
    }

    private static final Map<String, Class> PRIMITIVE_CLASSES = new HashMap<>();
    private static final Map<String, Class> OBJECT_PRIMITIVE_MAPPING = new HashMap<>();

    public static final String ENUM = Enum.class.getName();
    public static final String OPTIONAL = Optional.class.getName();

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

}
