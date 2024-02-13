package io.smallrye.graphql.schema.link;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import io.smallrye.graphql.api.federation.Authenticated;
import io.smallrye.graphql.api.federation.External;
import io.smallrye.graphql.api.federation.FieldSet;
import io.smallrye.graphql.api.federation.Inaccessible;
import io.smallrye.graphql.api.federation.Requires;
import io.smallrye.graphql.api.federation.Tag;
import io.smallrye.graphql.api.federation.policy.Policy;
import io.smallrye.graphql.api.federation.policy.PolicyGroup;
import io.smallrye.graphql.api.federation.policy.PolicyItem;

@Authenticated
@Policy(policies = {
        @PolicyGroup({ @PolicyItem("policy1"), @PolicyItem("policy2") }),
        @PolicyGroup({ @PolicyItem("policy3") })
})
public class Book {
    public String isbn;
    @Inaccessible
    public String title;
    @Tag(name = "internal")
    @Requires(fields = @FieldSet("isbn"))
    public LocalDate published;
    @External
    public List<String> authors;

    public Book() {
    }

    public Book(String isbn, String title, LocalDate published, String... authors) {
        this.isbn = isbn;
        this.title = title;
        this.published = published;
        this.authors = Arrays.asList(authors);
    }

}
