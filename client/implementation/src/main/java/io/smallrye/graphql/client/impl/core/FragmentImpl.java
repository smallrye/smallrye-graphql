package io.smallrye.graphql.client.impl.core;

import java.util.StringJoiner;
import java.util.stream.Collectors;

import io.smallrye.graphql.client.core.Buildable;
import io.smallrye.graphql.client.core.exceptions.BuildException;

public class FragmentImpl extends AbstractFragment {

    @Override
    public String build() throws BuildException {
        StringBuilder builder = new StringBuilder();
        builder.append("fragment ").append(getName()).append(" on ").append(getTargetType());
        builder.append(this.getDirectives()
                .stream()
                .map(Buildable::build)
                .collect(Collectors.joining()));

        // fields
        StringJoiner stringJoiner = new StringJoiner(" ", " {", "}");
        getFields().forEach(field -> stringJoiner.add(field.build()));
        builder.append(stringJoiner);
        return builder.toString();
    }

}
