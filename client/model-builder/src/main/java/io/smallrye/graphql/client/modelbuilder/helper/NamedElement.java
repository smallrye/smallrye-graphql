package io.smallrye.graphql.client.modelbuilder.helper;

import java.util.List;

/**
 * An interface for elements that have a name and may be associated with GraphQL directives.
 *
 * @author mskacelik
 */
public interface NamedElement {

    /**
     * Gets the name of the NamedElement, considering any {@link org.eclipse.microprofile.graphql.Name} annotation if present.
     *
     * @return The field name.
     */
    String getName();

    /**
     * Gets the raw (original) name of the NamedElement.
     *
     * @return The raw field name.
     */
    String getRawName();

    /**
     * Gets the location of directives associated with this NamedElement.
     *
     * @return The directive location
     */
    String getDirectiveLocation();

    /**
     * Checks if the NamedElement has associated directives.
     *
     * @return {@code true} if the NamedElement has directives, otherwise {@code false}.
     */
    boolean hasDirectives();

    /**
     * Gets the list of directives associated with the NamedElement.
     *
     * @return The list of {@link DirectiveInstance} objects.
     */
    List<DirectiveInstance> getDirectives();
}
