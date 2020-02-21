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
package io.smallrye.graphql.schema.helper;

import org.jboss.jandex.Type;

/**
 * When the date type used is not supported
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class InvalidDateTypeException extends RuntimeException {

    public InvalidDateTypeException(Type type) {
        this("Not a valid Date Type [" + type.name().toString() + "]");
    }

    public InvalidDateTypeException(String string) {
        super(string);
    }

    public InvalidDateTypeException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    public InvalidDateTypeException(Throwable thrwbl) {
        super(thrwbl);
    }

    public InvalidDateTypeException(String string, Throwable thrwbl, boolean bln, boolean bln1) {
        super(string, thrwbl, bln, bln1);
    }

}
