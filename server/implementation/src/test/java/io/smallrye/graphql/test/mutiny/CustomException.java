package io.smallrye.graphql.test.mutiny;

import io.smallrye.graphql.api.ErrorCode;

@ErrorCode("custom-error")
public class CustomException extends RuntimeException {}