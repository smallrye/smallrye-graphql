package io.smallrye.graphql.test.apps.mapping.api;

import java.util.Currency;

import javax.json.bind.annotation.JsonbTypeAdapter;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class MappingResource {

    @Mutation
    @Description("Add new Data")
    public Data createData(Data data) {
        data.id = (long) (Math.random() * 10000);
        return data;
    }

    @Query
    public Data getData() {
        Data d = new Data();
        d.id = 1L;
        d.name = "Foo";
        d.currency = Currency.getInstance("EUR");
        return d;
    }

    public static class Data {
        public Long id;

        public String name;

        //@ToScalar(value = Scalar.String.class, deserializeMethod = "getInstance")
        @JsonbTypeAdapter(CurrencyAdapter.class)
        public Currency currency;

        //@ToScalar(Scalar.String.class)
        @JsonbTypeAdapter(EmailAdapter.class)
        public Email email;

    }

}
