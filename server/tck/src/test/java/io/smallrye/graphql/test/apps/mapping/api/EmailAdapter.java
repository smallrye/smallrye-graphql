
package io.smallrye.graphql.test.apps.mapping.api;

import javax.json.bind.adapter.JsonbAdapter;

/**
 * Map a email to and from json
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class EmailAdapter implements JsonbAdapter<Email, String> {

    @Override
    public String adaptToJson(Email email) {
        return email.getValue();
    }

    @Override
    public Email adaptFromJson(String email) {
        return new Email(email);
    }
}