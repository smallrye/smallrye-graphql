package io.smallrye.graphql.execution.context;

import java.util.List;

public class BatchKeyContext {

    private List<Object> transformedArguments;
    private SmallRyeContext smallRyeContext;

    public BatchKeyContext() {

    }

    public BatchKeyContext(List<Object> transformedArguments, SmallRyeContext smallRyeContext) {
        this.transformedArguments = transformedArguments;
        this.smallRyeContext = smallRyeContext;
    }

    public List<Object> getTransformedArguments() {
        return transformedArguments;
    }

    public void setTransformedArguments(List<Object> transformedArguments) {
        this.transformedArguments = transformedArguments;
    }

    public SmallRyeContext getSmallRyeContext() {
        return smallRyeContext;
    }

    public void setSmallRyeContext(SmallRyeContext smallRyeContext) {
        this.smallRyeContext = smallRyeContext;
    }

}
