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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.Dependent;

import org.jboss.logging.Logger;

/**
 * Helping with collections
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@Dependent
public class CollectionHelper {
    private static final Logger LOG = Logger.getLogger(CollectionHelper.class.getName());

    public Collection getCorrectCollectionType(Class type) {

        if (type.equals(Collection.class) || type.equals(List.class) || type.getName().equals(ARRAYS_ARRAYLIST)) {
            return new ArrayList();
        } else if (type.equals(Set.class)) {
            return new HashSet();
        } else {
            try {
                return (Collection) type.newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
                LOG.error("Can not create new collection of [" + type.getName() + "]");
                return new ArrayList(); // default ?
            }
        }
    }

    private static final String ARRAYS_ARRAYLIST = "java.util.Arrays$ArrayList";
}
