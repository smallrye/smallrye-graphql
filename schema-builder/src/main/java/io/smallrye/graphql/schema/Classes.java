package io.smallrye.graphql.schema;

import java.lang.reflect.Modifier;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;

/**
 * Class helper
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class Classes {
    private static final Map<String, Class> loadedClasses = new HashMap<>();

    private Classes() {
    }

    /**
     * Load a class via the classloader
     * 
     * @param className the class name
     * @return the instance of that class
     */
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
                    throw new SchemaBuilderException("Can not load class [" + className + "]", pae);
                }
            }
        }
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
     * Check if a class is a primitive
     * 
     * @param primitiveName the class name
     * @return true if it is
     */
    public static boolean isPrimitive(String primitiveName) {
        return PRIMITIVE_CLASSES.containsKey(primitiveName);
    }

    /**
     * Get the primitive class for a certain name
     * 
     * @param primitiveName the primitive class name
     * @return the class
     */
    public static Class getPrimativeClassType(String primitiveName) {
        if (isPrimitive(primitiveName)) {
            return PRIMITIVE_CLASSES.get(primitiveName);
        }
        throw new SchemaBuilderException("Unknown primative type [" + primitiveName + "]");
    }

    /**
     * Given the Object Type, return the primitive counterpart
     * 
     * @param objectType the Object type
     * @return the primitive type
     */
    public static Class toPrimativeClassType(Class objectType) {
        if (OBJECT_PRIMITIVE_MAPPING.containsKey(objectType)) {
            return OBJECT_PRIMITIVE_MAPPING.get(objectType);
        }
        return objectType;
    }

    /**
     * Given a String representation of a Scalar, get the correct type
     * 
     * @param input the input value as a String
     * @param type the type we are looking for
     * @return the input value in the correct type
     */
    public static Object stringToScalar(String input, Class type) {
        if (type.isPrimitive()) {
            return stringToPrimativeScalar(input, type);
        } else {
            return stringToObjectScalar(input, type);
        }
    }

    /**
     * Check if this type is a Number (or collection of numbers)
     * 
     * @param type the type to check
     * @return true if it is
     */
    public static boolean isNumberLikeTypeOrCollectionThereOf(Type type) {
        return isTypeOrCollectionThereOf(type,
                BYTE,
                BYTE_PRIMATIVE,
                SHORT,
                SHORT_PRIMATIVE,
                INTEGER,
                INTEGER_PRIMATIVE,
                BIG_INTEGER,
                DOUBLE,
                DOUBLE_PRIMATIVE,
                BIG_DECIMAL,
                LONG,
                LONG_PRIMATIVE,
                FLOAT,
                FLOAT_PRIMATIVE);
    }

    /**
     * Check if this type is a Date (or collection of numbers)
     * 
     * @param type the type to check
     * @return true if it is
     */
    public static boolean isDateLikeTypeOrCollectionThereOf(Type type) {
        return isTypeOrCollectionThereOf(type,
                LOCALDATE,
                LOCALTIME,
                LOCALDATETIME,
                ZONEDDATETIME,
                OFFSETDATETIME,
                OFFSETTIME,
                UTIL_DATE,
                SQL_DATE,
                SQL_TIMESTAMP,
                SQL_TIME);
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
                    if (type.name().equals(dotName)) {
                        return true;
                    }
                }
                return false;
        }
    }

    private static Class<?> loadClass(String className, ClassLoader loader) throws ClassNotFoundException {
        Class<?> c = Class.forName(className, false, loader);
        loadedClasses.put(className, c);
        return c;
    }

    private static Object stringToPrimativeScalar(String input, Class type) {
        if (type.equals(boolean.class)) {
            return Boolean.parseBoolean(input);
        } else if (type.equals(byte.class)) {
            return Byte.parseByte(input);
        } else if (type.equals(char.class)) {
            return input.charAt(0);
        } else if (type.equals(short.class)) {
            return Short.parseShort(input);
        } else if (type.equals(int.class)) {
            return Integer.parseInt(input);
        } else if (type.equals(long.class)) {
            return Long.parseLong(input);
        } else if (type.equals(float.class)) {
            return Float.parseFloat(input);
        } else if (type.equals(double.class)) {
            return Double.parseDouble(input);
        } else {
            throw new SchemaBuilderException(
                    "Can not create new primative scalar type [" + type + "] from input [" + input + "]");
        }
    }

    private static Object stringToObjectScalar(String input, Class type) {
        if (type.equals(Boolean.class)) {
            return Boolean.valueOf(input);
        } else if (type.equals(Byte.class)) {
            return Byte.valueOf(input);
        } else if (type.equals(Character.class)) {
            return Character.valueOf(input.charAt(0));
        } else if (type.equals(Short.class)) {
            return Short.valueOf(input);
        } else if (type.equals(Integer.class)) {
            return Integer.valueOf(input);
        } else if (type.equals(Long.class)) {
            return Long.valueOf(input);
        } else if (type.equals(Float.class)) {
            return Float.valueOf(input);
        } else if (type.equals(Double.class)) {
            return Double.valueOf(input);
        } else if (type.equals(BigDecimal.class)) {
            return new BigDecimal(input);
        } else if (type.equals(BigInteger.class)) {
            return new BigInteger(input);
        } else {
            throw new SchemaBuilderException(
                    "Can not create new object scalar type [" + type + "] from input [" + input + "]");
        }
    }

    public static final DotName ENUM = DotName.createSimple(Enum.class.getName());
    public static final DotName OPTIONAL = DotName.createSimple(Optional.class.getName());

    public static final DotName LOCALDATE = DotName.createSimple(LocalDate.class.getName());
    public static final DotName LOCALDATETIME = DotName.createSimple(LocalDateTime.class.getName());
    public static final DotName LOCALTIME = DotName.createSimple(LocalTime.class.getName());
    public static final DotName ZONEDDATETIME = DotName.createSimple(ZonedDateTime.class.getName());
    public static final DotName OFFSETDATETIME = DotName.createSimple(OffsetDateTime.class.getName());
    public static final DotName OFFSETTIME = DotName.createSimple(OffsetTime.class.getName());

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

    private static final Map<String, Class> PRIMITIVE_CLASSES = new HashMap<>();
    private static final Map<Class, Class> OBJECT_PRIMITIVE_MAPPING = new HashMap<>();

    static {
        PRIMITIVE_CLASSES.put("boolean", boolean.class);
        PRIMITIVE_CLASSES.put("byte", byte.class);
        PRIMITIVE_CLASSES.put("char", char.class);
        PRIMITIVE_CLASSES.put("short", short.class);
        PRIMITIVE_CLASSES.put("int", int.class);
        PRIMITIVE_CLASSES.put("long", long.class);
        PRIMITIVE_CLASSES.put("float", float.class);
        PRIMITIVE_CLASSES.put("double", double.class);

        OBJECT_PRIMITIVE_MAPPING.put(Boolean.class, boolean.class);
        OBJECT_PRIMITIVE_MAPPING.put(Byte.class, byte.class);
        OBJECT_PRIMITIVE_MAPPING.put(Character.class, char.class);
        OBJECT_PRIMITIVE_MAPPING.put(Short.class, short.class);
        OBJECT_PRIMITIVE_MAPPING.put(Integer.class, int.class);
        OBJECT_PRIMITIVE_MAPPING.put(Long.class, long.class);
        OBJECT_PRIMITIVE_MAPPING.put(Float.class, float.class);
        OBJECT_PRIMITIVE_MAPPING.put(Double.class, double.class);
    }

}
