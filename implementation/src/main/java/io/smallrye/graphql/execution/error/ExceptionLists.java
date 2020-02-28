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
package io.smallrye.graphql.execution.error;

import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;

/**
 * Class that hold the exceptions to the exceptions
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ExceptionLists {
    private static final Logger LOG = Logger.getLogger(ExceptionLists.class.getName());

    private final List<String> blackList;
    private final List<String> whiteList;
    private final List<Class> blackListClasses;
    private final List<Class> whiteListClasses;

    public ExceptionLists(List<String> blackList, List<String> whiteList) {
        this.blackList = blackList;
        this.whiteList = whiteList;
        this.blackListClasses = populateClassInstances(blackList);
        this.whiteListClasses = populateClassInstances(whiteList);
    }

    boolean isBlacklisted(Throwable throwable) {
        return isListed(throwable, blackList, blackListClasses);
    }

    boolean isWhitelisted(Throwable throwable) {
        return isListed(throwable, whiteList, whiteListClasses);
    }

    private boolean isListed(Throwable throwable, List<String> classNames, List<Class> classes) {
        if (classNames == null) {
            return false;
        }

        // Check that specific class
        String name = throwable.getClass().getName();
        if (classNames.contains(name)) {
            return true;
        }

        // Check transitive
        for (Class c : classes) {
            if (c.isAssignableFrom(throwable.getClass())) {
                return true;
            }
        }

        return false;
    }

    private List<Class> populateClassInstances(List<String> classNames) {
        List<Class> classes = new ArrayList<>();
        if (classNames != null) {
            for (String className : classNames) {
                try {
                    classes.add(Class.forName(className));
                } catch (ClassNotFoundException ex) {
                    LOG.warn("Could not create instance of exception class [" + className
                            + "]. Can not do transitive black/whitelist check for this class");
                }
            }
        }
        return classes;
    }

}
