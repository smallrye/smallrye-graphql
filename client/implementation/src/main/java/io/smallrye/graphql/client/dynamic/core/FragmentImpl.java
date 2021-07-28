package io.smallrye.graphql.client.dynamic.core;

import io.smallrye.graphql.client.core.FieldOrFragment;
import io.smallrye.graphql.client.core.exceptions.BuildException;

public class FragmentImpl extends AbstractFragment {

    @Override
    public String build() throws BuildException {
        StringBuilder builder = new StringBuilder();
        builder.append("fragment  ").append(getName()).append(" on ").append(getTargetType()).append(" {");

        FieldOrFragment[] fields = this.getFields().toArray(new FieldOrFragment[0]);
        for (int i = 0; i < fields.length; i++) {
            FieldOrFragment field = fields[i];
            builder.append(field.build());
            if (i < fields.length - 1) {
                builder.append(" ");
            }
        }

        builder.append("}");
        return builder.toString();
    }

}
