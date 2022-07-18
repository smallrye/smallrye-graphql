package io.smallrye.graphql.schema.model;

public final class UnionType extends Reference {

  private String description;

  public UnionType(String className, String name, String description) {
    super(className, name, ReferenceType.UNION);
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public String toString() {
    return "UnionType{" + "description=" + description + '}';
  }
}
