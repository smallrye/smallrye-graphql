package io.smallrye.graphql.client.impl.core;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import io.smallrye.graphql.client.core.Enum;
import io.smallrye.graphql.client.core.exceptions.BuildException;

public abstract class AbstractEnum implements Enum {

    // According to http://spec.graphql.org/June2018/#sec-Enum-Value
    protected static final Pattern VALID_ENUM_NAME = Pattern.compile("[_A-Za-z][_0-9A-Za-z]*");
    protected static final List<String> FORBIDDEN_ENUM_NAMES = Arrays.asList(
            "true", "false", "null");

    private String value;

    public AbstractEnum() {
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        validateValue(value);
        this.value = value;
    }

    protected void validateValue(String value) {
        if (!VALID_ENUM_NAME.matcher(value).matches()) {
            throw new BuildException("Enum value '" + this.getValue() + "' is not valid gql name");
        }
        if (FORBIDDEN_ENUM_NAMES.contains(value)) {
            throw new BuildException("Enum is a forbidden name '" + this.getValue() + "'");
        }
    }
}
