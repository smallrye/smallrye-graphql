package io.smallrye.graphql.test.apps.adapt.with.api;

import io.smallrye.graphql.api.Adapter;

/**
 * Using an adapter
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class AddressAdapter implements Adapter<Address, EmailAddress> {

    @Override
    public Address from(EmailAddress email) {
        Address a = new Address();
        a.addressType = AddressType.email;
        a.addLine(email.getValue());
        return a;
    }

    @Override
    public EmailAddress to(Address address) {
        if (address != null && address.addressType != null && address.addressType.equals(AddressType.email)) {
            return new EmailAddress(address.lines.get(0));
        }
        return null;
    }
}