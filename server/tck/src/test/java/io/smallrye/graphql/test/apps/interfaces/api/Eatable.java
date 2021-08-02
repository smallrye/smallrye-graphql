package io.smallrye.graphql.test.apps.interfaces.api;

import org.eclipse.microprofile.graphql.Type;

/**
 * Define something that is fit to be consumed as food
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@Type
public interface Eatable {

    public String getColor();

    public void setColor(String color);

    public String getName();

    public void setName(String name);

}
