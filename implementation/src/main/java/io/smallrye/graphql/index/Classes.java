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

package io.smallrye.graphql.index;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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

    public static final DotName ENUM = DotName.createSimple(Enum.class.getName());

    public static final DotName LOCALDATE = DotName.createSimple(LocalDate.class.getName());
    public static final DotName LOCALDATETIME = DotName.createSimple(LocalDateTime.class.getName());
    public static final DotName LOCALTIME = DotName.createSimple(LocalTime.class.getName());
}
