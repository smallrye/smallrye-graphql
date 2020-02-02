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
package io.smallrye.graphql.schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;

import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLOutputType;

/**
 * Here we keep all the objects we know about
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class ObjectBag {

    @Produces
    private final Map<DotName, GraphQLInputType> inputMap = new HashMap<>();
    @Produces
    private final Map<DotName, GraphQLOutputType> typeMap = new HashMap<>();
    @Produces
    private final Map<DotName, GraphQLEnumType> enumMap = new HashMap<>();
    @Produces
    private final Map<DotName, GraphQLInterfaceType> interfaceMap = new HashMap<>();

    // This is object we discovered, but could not create at that time, so we store them here, to create later.
    @Produces
    private final List<ClassInfo> typeTodoList = new ArrayList<>();
}
