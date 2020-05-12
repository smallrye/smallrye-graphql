package io.smallrye.graphql.schema;

import java.time.LocalDate;
import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.graphql.DateFormat;
import org.eclipse.microprofile.graphql.NonNull;

public class AsyncApi {

    CompletionStage<String> string() {
        return null;
    }

    public CompletionStage<@NonNull String> nonNullString() {
        return null;
    }

    @NonNull
    public CompletionStage<String> nonNullCompletionStage() {
        return null;
    }

    public CompletionStage<@DateFormat(value = "yyyy-MM-dd") LocalDate> formattedLocalDate() {
        return null;
    }

    @DateFormat(value = "yyyy-MM-dd")
    public CompletionStage<LocalDate> formattedCompletionStage() {
        return null;
    }

}
