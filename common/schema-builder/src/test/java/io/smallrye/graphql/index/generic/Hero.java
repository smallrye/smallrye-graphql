package io.smallrye.graphql.index.generic;

import org.eclipse.microprofile.graphql.Input;

@Input("HeroInput")
public class Hero extends Character {
    public String name;
    public String surname;
    public Double height;
    public Integer mass;
    public Boolean darkSide;
}