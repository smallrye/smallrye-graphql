package io.smallrye.graphql.test;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.graphql.Name;

/**
 * Some POJO
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class TestObject {

    private String id;
    private String name;
    @Name("amounts")
    private List<TestListObject> testListObjects = new ArrayList<>();

    private Number number;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TestListObject> getTestListObjects() {
        return testListObjects;
    }

    public void setTestListObjects(List<TestListObject> testListObjects) {
        this.testListObjects = testListObjects;
    }

    public void addTestListObject(TestListObject testListObject) {
        testListObjects.add(testListObject);
    }

    public Number getNumber() {
        return number;
    }

    public void setNumber(Number number) {
        this.number = number;
    }

    enum Number {
        ONE,
        TWO,
        THREE
    }
}
