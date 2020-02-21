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
package io.smallrye.graphql.execution.datafetchers;

/**
 * There was an issue with the reflection when fetching data.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ReflectionDataFetcherException extends RuntimeException {

    public ReflectionDataFetcherException() {
    }

    public ReflectionDataFetcherException(String string) {
        super(string);
    }

    public ReflectionDataFetcherException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    public ReflectionDataFetcherException(Throwable thrwbl) {
        super(thrwbl);
    }

    public ReflectionDataFetcherException(String string, Throwable thrwbl, boolean bln, boolean bln1) {
        super(string, thrwbl, bln, bln1);
    }

}
