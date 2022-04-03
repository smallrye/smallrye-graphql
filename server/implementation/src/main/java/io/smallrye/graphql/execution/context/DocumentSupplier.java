package io.smallrye.graphql.execution.context;

import java.util.function.Supplier;

import graphql.ExecutionInput;
import graphql.ParseAndValidate;
import graphql.ParseAndValidateResult;
import graphql.execution.preparsed.PreparsedDocumentEntry;
import graphql.language.Document;
import io.smallrye.graphql.execution.QueryCache;

public class DocumentSupplier implements Supplier<Document> {
    private final ExecutionInput executionInput;
    private final QueryCache queryCache;

    public DocumentSupplier(ExecutionInput executionInput,
            QueryCache queryCache) {
        this.executionInput = executionInput;
        this.queryCache = queryCache;
    }

    @Override
    public Document get() {
        if (queryCache == null) {
            ParseAndValidateResult parse = ParseAndValidate.parse(executionInput);
            return parse.isFailure() ? null : parse.getDocument();
        } else {
            PreparsedDocumentEntry documentEntry = queryCache.getDocument(executionInput, ei -> {
                ParseAndValidateResult parse = ParseAndValidate.parse(ei);
                return parse.isFailure() ? new PreparsedDocumentEntry(parse.getErrors())
                        : new PreparsedDocumentEntry(parse.getDocument());
            });
            return documentEntry.hasErrors() ? null : documentEntry.getDocument();
        }
    }
}
