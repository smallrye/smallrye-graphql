package com.github.t1.powerannotations.scanner;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

import org.junit.jupiter.api.Test;

class IndexerConfigBehavior {
    @Test
    void shouldIgnoreUnknownResource() {
        IndexerConfig unknown = new IndexerConfig("unknown");

        then(unknown.excludes()).isEmpty();
    }

    @Test
    void shouldLoadEmptyResource() {
        IndexerConfig unknown = new IndexerConfig("META-INF/empty.properties");

        then(unknown.excludes()).isEmpty();
    }

    @Test
    void shouldLoadOtherPropertiesResource() {
        IndexerConfig unknown = new IndexerConfig("META-INF/other.properties");

        then(unknown.excludes()).isEmpty();
    }

    @Test
    void shouldLoadOneExcludeConfig() {
        IndexerConfig unknown = new IndexerConfig("META-INF/one-exclude.properties");

        then(unknown.excludes()).containsExactly(".*/foo/.*/bar-.*\\.jar");
    }

    @Test
    void shouldFailToLoadInvalidExcludeConfig() {
        Throwable throwable = catchThrowable(() -> new IndexerConfig("META-INF/invalid-exclude.properties"));

        then(throwable)
                .hasMessage("can't parse exclude config")
                .hasRootCauseMessage("expect `group:artifact` but found `invalid`");
    }

    @Test
    void shouldLoadMultiExcludeConfig() {
        IndexerConfig unknown = new IndexerConfig("META-INF/multi-exclude.properties");

        then(unknown.excludes()).containsExactly(
                ".*/foo/.*/bar-.*\\.jar",
                ".*/baz/.*/bee-.*\\.jar");
    }
}
