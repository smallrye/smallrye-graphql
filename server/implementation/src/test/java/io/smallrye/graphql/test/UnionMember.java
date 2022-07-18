package io.smallrye.graphql.test;

public class UnionMember implements TestUnion {

  String name;

  public UnionMember(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
