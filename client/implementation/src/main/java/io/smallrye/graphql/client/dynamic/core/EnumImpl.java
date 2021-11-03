package io.smallrye.graphql.client.dynamic.core;

import io.smallrye.graphql.client.core.exceptions.BuildException;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class EnumImpl extends AbstractEnum {

    // According to http://spec.graphql.org/June2018/#sec-Enum-Value
    private static final Pattern VALID_ENUM_NAME = Pattern.compile("[_A-Za-z][_0-9A-Za-z]*");
    private static final List<String> FORBIDDEN_ENUM_NAMES = Arrays.asList(
            "true", "false", "null");

    @Override
    public String build() {
        if (!VALID_ENUM_NAME.matcher(this.getValue()).matches()) {
            throw new BuildException("Enum value '" + this.getValue() + "' is not valid gql name");
        }
        if (FORBIDDEN_ENUM_NAMES.contains(this.getValue())) {
            throw new BuildException("Enum is a forbidden name '" + this.getValue() + "'");
        }
        return this.getValue();
    }

}
