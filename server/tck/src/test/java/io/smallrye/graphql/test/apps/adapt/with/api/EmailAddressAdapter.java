package io.smallrye.graphql.test.apps.adapt.with.api;

import javax.json.bind.adapter.JsonbAdapter;

/**
 * Using an adapter
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class EmailAddressAdapter implements JsonbAdapter<EmailAddress, Address> {

    @Override
    public Address adaptToJson(EmailAddress email) {
        Address a = new Address();
        a.addressType = AddressType.email;
        a.addLine(email.getValue());
        return a;
    }

    @Override
    public EmailAddress adaptFromJson(Address address) {
        if (address.addressType.equals(AddressType.email)) {
            return new EmailAddress(address.lines.get(0));
        }
        return null;
    }
}