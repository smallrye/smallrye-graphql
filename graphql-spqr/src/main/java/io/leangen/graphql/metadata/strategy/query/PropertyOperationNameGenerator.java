package io.leangen.graphql.metadata.strategy.query;

import java.lang.reflect.Method;

import io.leangen.graphql.util.ClassUtils;
import io.leangen.graphql.util.Utils;

public class PropertyOperationNameGenerator extends AnnotatedOperationNameGenerator {

    @Override
    public String generateQueryName(OperationNameGeneratorParams<?> params) {
        String name = super.generateQueryName(params);
        if (Utils.isEmpty(name) && params.isMethod()) {
            return ClassUtils.getFieldNameFromGetter((Method) params.getElement());
        }
        return name;
    }
}
