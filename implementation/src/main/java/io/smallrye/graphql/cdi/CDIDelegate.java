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

package io.smallrye.graphql.cdi;

import java.util.ServiceLoader;

import javax.enterprise.inject.spi.CDI;

import io.smallrye.graphql.x.Classes;

public interface CDIDelegate {

    public static CDIDelegate delegate() {
        try {
            ServiceLoader<CDIDelegate> sl = ServiceLoader.load(CDIDelegate.class);
            return sl.iterator().next();
        } catch (Exception ex) {
            return new CDIDelegate() {

                @Override
                public Class<?> getClassFromCDI(Class<?> declaringClass) {
                    Object declaringObject = getInstanceFromCDI(declaringClass);
                    return declaringObject.getClass();
                }

                @Override
                public Object getInstanceFromCDI(Class<?> declaringClass) {
                    return CDI.current().select(declaringClass).get();
                }

                @Override
                public Object getInstanceFromCDI(String declaringClass) {
                    Class<?> loadedClass = Classes.loadClass(declaringClass);
                    return getInstanceFromCDI(loadedClass);
                }
            };
        }
    }

    Class<?> getClassFromCDI(Class<?> declaringClass);

    Object getInstanceFromCDI(Class<?> declaringClass);

    Object getInstanceFromCDI(String declaringClass);

}
