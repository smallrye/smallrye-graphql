package io.smallrye.graphql.tests.client.dynamic;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
@ApplicationScoped
public class DynamicClientApi {

    @Query
    public Dummy simple() {
        Dummy ret = new Dummy();
        ret.setInteger(30);
        ret.setString("asdf");
        return ret;
    }

    @Query
    public Dummy simple2() {
        Dummy ret = new Dummy();
        ret.setInteger(31);
        ret.setString("asdfgh");
        return ret;
    }

    @Query
    public Dummy queryWithArgument(@Name(value = "number") Integer number) {
        Dummy ret = new Dummy();
        ret.setInteger(number);
        return ret;
    }

    @Query
    public Dummy queryWithArgument2(@Name(value = "obj") DummyObject obj) {
        Dummy ret = new Dummy();
        ret.setDummyObject(obj);
        return ret;
    }

    @Query
    public Dummy queryWithArgument3(@Name(value = "obj") DummyEnum obj) {
        Dummy ret = new Dummy();
        ret.setInteger(obj.ordinal());
        return ret;
    }

    @Query
    public Dummy withRenamedField() {
        Dummy ret = new Dummy();
        ret.setRenamedField("foo");
        ret.setString("string");
        ret.setInteger(30);
        return ret;
    }

    @Query
    public List<Dummy> listWithRenamedField() {
        Dummy dummy1 = new Dummy();
        dummy1.setRenamedField("foo");
        Dummy dummy2 = new Dummy();
        dummy2.setRenamedField("foo2");

        ArrayList<Dummy> list = new ArrayList<>();
        list.add(dummy1);
        list.add(dummy2);
        return list;
    }

}
