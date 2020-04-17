package io.smallrye.graphql.execution.error;

import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;

import io.smallrye.graphql.execution.Classes;

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
        if (classNames != null && !classNames.isEmpty()) {
            for (String className : classNames) {
                Class c = Classes.loadClass(className);
                if (c != null) {
                    classes.add(c);
                }
            }
        }
        return classes;
    }
}
