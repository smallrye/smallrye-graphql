package io.smallrye.graphql.execution.error;

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

    public ExceptionLists(List<String> blackList, List<String> whiteList) {
        this.blackList = blackList;
        this.whiteList = whiteList;
    }

    boolean isBlacklisted(Throwable throwable) {
        return isListed(throwable, blackList);
    }

    boolean isWhitelisted(Throwable throwable) {
        return isListed(throwable, whiteList);
    }

    private boolean isListed(Throwable throwable, List<String> classNames) {
        if (classNames == null || classNames.isEmpty() || throwable == null) {
            return false;
        }

        return isListed(throwable.getClass(), classNames);
    }

    private boolean isListed(Class throwableClass, List<String> classNames) {
        if (classNames == null || classNames.isEmpty() || throwableClass == null
                || throwableClass.getName().equals(Object.class.getName())) {
            return false;
        }

        // Check that specific class
        if (classNames.contains(throwableClass.getName())) {
            return true;
        }

        // Check transitive
        return isListed(throwableClass.getSuperclass(), classNames);
    }

}
