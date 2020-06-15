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

    private final List<String> hideList;
    private final List<String> showList;

    public ExceptionLists(Optional<List<String>> maybeHideList, Optional<List<String>> maybeShowList) {
        if (maybeHideList.isPresent()) {
            this.hideList = maybeHideList.get();
        } else {
            this.hideList = Collections.EMPTY_LIST;
        }
        if (maybeShowList.isPresent()) {
            this.showList = maybeShowList.get();
        } else {
            this.showList = Collections.EMPTY_LIST;
        }
    }

    boolean shouldHide(Throwable throwable) {
        return isListed(throwable, hideList);
    }

    boolean shouldShow(Throwable throwable) {
        return isListed(throwable, showList);
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
