package io.smallrye.graphql.schema;

@IntArrayTestDirective(value = { 1, 2, 3 })
public interface TestInterfaceDirective {

    String getTestValue();
}
