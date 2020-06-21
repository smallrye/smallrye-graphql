package io.smallrye.graphql.index.app;

import java.util.Objects;

import io.smallrye.graphql.api.Scalar;
import io.smallrye.graphql.api.ToScalar;

public class Person {
    String firstName;
    String lastName;
    @ToScalar(Scalar.String.class)
    Email email;

    @ToScalar(Scalar.String.class)
    Website website;

    @ToScalar(Scalar.String.class)
    Phone phone;

    private TwitterHandle twitterHandle;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public Website getWebsite() {
        return website;
    }

    public void setWebsite(Website website) {
        this.website = website;
    }

    public Phone getPhone() {
        return phone;
    }

    public void setPhone(Phone phone) {
        this.phone = phone;
    }

    public TwitterHandle getTwitterHandle() {
        return twitterHandle;
    }

    public void setTwitterHandle(TwitterHandle twitterHandle) {
        this.twitterHandle = twitterHandle;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Person other = (Person) obj;
        if (!Objects.equals(this.firstName, other.firstName)) {
            return false;
        }
        if (!Objects.equals(this.lastName, other.lastName)) {
            return false;
        }
        if (!Objects.equals(this.email, other.email)) {
            return false;
        }
        if (!Objects.equals(this.website, other.website)) {
            return false;
        }
        return true;
    }

}
