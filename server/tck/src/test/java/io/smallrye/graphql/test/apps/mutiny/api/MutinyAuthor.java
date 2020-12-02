package io.smallrye.graphql.test.apps.mutiny.api;

import java.time.LocalDate;

public class MutinyAuthor {
    public String name;
    public String bornName;
    public LocalDate birthDate;
    public String birthPlace;

    public MutinyAuthor() {
    }

    public MutinyAuthor(String name, String bornName, LocalDate birthDate, String birthPlace) {
        this.name = name;
        this.bornName = bornName;
        this.birthDate = birthDate;
        this.birthPlace = birthPlace;
    }
}
