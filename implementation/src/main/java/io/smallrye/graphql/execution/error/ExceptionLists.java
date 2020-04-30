package io.smallrye.graphql.execution.error;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Class that hold the exceptions to the exceptions
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ExceptionLists {

    private final List<String> blackList;
    private final List<String> whiteList;

    public ExceptionLists(Optional<List<String>> maybeBlackList, Optional<List<String>> maybeWhiteList) {
        if (maybeBlackList.isPresent()) {
            this.blackList = maybeBlackList.get();
        } else {
            this.blackList = Collections.EMPTY_LIST;
        }
        if (maybeWhiteList.isPresent()) {
            this.whiteList = maybeWhiteList.get();
        } else {
            this.whiteList = Collections.EMPTY_LIST;
        }
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
