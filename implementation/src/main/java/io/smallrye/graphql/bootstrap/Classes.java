/*
 * Copyright 2020 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.smallrye.graphql.bootstrap;

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

import io.smallrye.graphql.bootstrap.schema.PrimitiveTypeNotFoundException;
import io.smallrye.graphql.bootstrap.schema.ScalarTypeNotFoundException;

/**
 * Class helper
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class Classes {
    private static final Map<String, Class> loadedClasses = new HashMap<>();

    private Classes() {
    }

    public static Class<?> loadClass(String className) {

        if (isPrimative(className)) {
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
                                Class<?> c = Class.forName(className, false, loader);
                                loadedClasses.put(className, c);
                                return c;
                            } catch (ClassNotFoundException cnfe) {
                                // Let's try this class classloader.
                            }
                        }
                        Class<?> c = Class.forName(className, false, Classes.class.getClassLoader());
                        loadedClasses.put(className, c);
                        return c;
                    });
                } catch (PrivilegedActionException pae) {
                    throw new ClassloaderException("Can not load class [" + className + "]", pae);
                }
            }
        }
    }

    public static boolean isOptional(Type type) {
        Type.Kind kind = type.kind();
        return kind.equals(Type.Kind.PARAMETERIZED_TYPE) && type.name().equals(OPTIONAL);
    }

    public static boolean isEnum(ClassInfo classInfo) {
        if (classInfo == null)
            return false;
        return classInfo.superName().equals(ENUM);
    }

    public static boolean isPrimative(String primativeName) {
        return PRIMATIVE_CLASSES.containsKey(primativeName);
    }

    public static Class getPrimativeClassType(String primativeName) {
        if (isPrimative(primativeName)) {
            return PRIMATIVE_CLASSES.get(primativeName);
        }
        throw new PrimitiveTypeNotFoundException("Unknown primative type [" + primativeName + "]");
    }

    public static Class toPrimativeClassType(Class objectType) {
        if (OBJECT_PRIMATIVE_MAPPING.containsKey(objectType)) {
            return OBJECT_PRIMATIVE_MAPPING.get(objectType);
        }
        return objectType;
    }

    public static Object stringToScalar(String input, Class type) {
        if (type.isPrimitive()) {
            return stringToPrimativeScalar(input, type);
        } else {
            return stringToObjectScalar(input, type);
        }
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
            throw new ScalarTypeNotFoundException(
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
            throw new ScalarTypeNotFoundException(
                    "Can not create new object scalar type [" + type + "] from input [" + input + "]");
        }
    }

    private static final Map<String, Class> PRIMATIVE_CLASSES = new HashMap<>();
    private static final Map<Class, Class> OBJECT_PRIMATIVE_MAPPING = new HashMap<>();

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

    public static final DotName BYTE = DotName.createSimple(Byte.class.getName());
    public static final DotName BYTE_PRIMATIVE = DotName.createSimple(byte.class.getName());

    public static final DotName SHORT = DotName.createSimple(Short.class.getName());
    public static final DotName SHORT_PRIMATIVE = DotName.createSimple(short.class.getName());

    public static final DotName INTEGER = DotName.createSimple(Integer.class.getName());
    public static final DotName INTEGER_PRIMATIVE = DotName.createSimple(int.class.getName());
    public static final DotName BIG_INTEGER = DotName.createSimple(BigInteger.class.getName());

    public static final DotName DOUBLE = DotName.createSimple(Double.class.getName());
    public static final DotName DOUBLE_PRIMATIVE = DotName.createSimple(double.class.getName());
    public static final DotName BIG_DECIMAL = DotName.createSimple(BigDecimal.class.getName());

    public static final DotName LONG = DotName.createSimple(Long.class.getName());
    public static final DotName LONG_PRIMATIVE = DotName.createSimple(long.class.getName());

    public static final DotName FLOAT = DotName.createSimple(Float.class.getName());
    public static final DotName FLOAT_PRIMATIVE = DotName.createSimple(float.class.getName());

    static {
        PRIMATIVE_CLASSES.put("boolean", boolean.class);
        PRIMATIVE_CLASSES.put("byte", byte.class);
        PRIMATIVE_CLASSES.put("char", char.class);
        PRIMATIVE_CLASSES.put("short", short.class);
        PRIMATIVE_CLASSES.put("int", int.class);
        PRIMATIVE_CLASSES.put("long", long.class);
        PRIMATIVE_CLASSES.put("float", float.class);
        PRIMATIVE_CLASSES.put("double", double.class);

        OBJECT_PRIMATIVE_MAPPING.put(Boolean.class, boolean.class);
        OBJECT_PRIMATIVE_MAPPING.put(Byte.class, byte.class);
        OBJECT_PRIMATIVE_MAPPING.put(Character.class, char.class);
        OBJECT_PRIMATIVE_MAPPING.put(Short.class, short.class);
        OBJECT_PRIMATIVE_MAPPING.put(Integer.class, int.class);
        OBJECT_PRIMATIVE_MAPPING.put(Long.class, long.class);
        OBJECT_PRIMATIVE_MAPPING.put(Float.class, float.class);
        OBJECT_PRIMATIVE_MAPPING.put(Double.class, double.class);
    }

}
