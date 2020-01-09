/*
 * Copyright 2019 Red Hat, Inc.
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

package io.smallrye.graphql.schema;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;

/**
 * Class helper
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public interface Classes {
    public static boolean isEnum(ClassInfo classInfo) {
        if (classInfo == null)
            return false;
        return classInfo.superName().equals(ENUM);
    }

    public static Class getPrimativeClassType(String primativeName) {
        switch (primativeName) {
            case "boolean":
                return boolean.class;
            case "byte":
                return byte.class;
            case "char":
                return char.class;
            case "short":
                return short.class;
            case "int":
                return int.class;
            case "long":
                return long.class;
            case "float":
                return float.class;
            case "double":
                return double.class;
            default:
                throw new RuntimeException("Unknown primative type [" + primativeName + "]");
        }
    }

    public static Class toPrimativeClassType(Class objectType) {
        if (objectType.equals(Boolean.class)) {
            return boolean.class;
        } else if (objectType.equals(Byte.class)) {
            return byte.class;
        } else if (objectType.equals(Character.class)) {
            return char.class;
        } else if (objectType.equals(Short.class)) {
            return short.class;
        } else if (objectType.equals(Integer.class)) {
            return int.class;
        } else if (objectType.equals(Long.class)) {
            return long.class;
        } else if (objectType.equals(Float.class)) {
            return float.class;
        } else if (objectType.equals(Double.class)) {
            return double.class;
        } else {
            return objectType;
        }
    }

    public static Object stringToPrimative(String primativeInput, Class type) {

        if (type.equals(boolean.class)) {
            return Boolean.valueOf(primativeInput).booleanValue();
        } else if (type.equals(byte.class)) {
            return Byte.valueOf(primativeInput).byteValue();
        } else if (type.equals(char.class)) {
            return Character.valueOf(primativeInput.charAt(0)).charValue();
        } else if (type.equals(short.class)) {
            return Short.valueOf(primativeInput).shortValue();
        } else if (type.equals(int.class)) {
            return Integer.valueOf(primativeInput).intValue();
        } else if (type.equals(long.class)) {
            return Long.valueOf(primativeInput).longValue();
        } else if (type.equals(float.class)) {
            return Float.valueOf(primativeInput).floatValue();
        } else if (type.equals(double.class)) {
            return Double.valueOf(primativeInput).doubleValue();
        } else {
            throw new RuntimeException("Can not create new primative type [" + type + "] from input [" + primativeInput + "]");
        }
    }

    public static final DotName ENUM = DotName.createSimple(Enum.class.getName());

    public static final DotName LOCALDATE = DotName.createSimple(LocalDate.class.getName());
    public static final DotName LOCALDATETIME = DotName.createSimple(LocalDateTime.class.getName());
    public static final DotName LOCALTIME = DotName.createSimple(LocalTime.class.getName());
    public static final DotName UTIL_DATE = DotName.createSimple(Date.class.getName());
    public static final DotName SQL_DATE = DotName.createSimple(java.sql.Date.class.getName());

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

}
