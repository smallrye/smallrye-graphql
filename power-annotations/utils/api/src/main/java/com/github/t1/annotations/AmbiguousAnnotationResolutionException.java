package com.github.t1.annotations;

/**
 * @see Annotations#get(Class)
 */
@SuppressWarnings("unused")
public class AmbiguousAnnotationResolutionException extends RuntimeException {
    public AmbiguousAnnotationResolutionException() {
    }

    public AmbiguousAnnotationResolutionException(String message) {
        super(message);
    }

    public AmbiguousAnnotationResolutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public AmbiguousAnnotationResolutionException(Throwable cause) {
        super(cause);
    }

    public AmbiguousAnnotationResolutionException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
