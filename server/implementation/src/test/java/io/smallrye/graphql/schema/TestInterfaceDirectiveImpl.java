package io.smallrye.graphql.schema;

@IntArrayTestDirective(value = { 1, 2, 3 })
public class TestInterfaceDirectiveImpl implements TestInterfaceDirective {
    @Override
    public String getTestValue() {
        return "Test value";
    }
}
