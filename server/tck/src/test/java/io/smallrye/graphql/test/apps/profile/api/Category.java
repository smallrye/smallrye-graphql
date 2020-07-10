package io.smallrye.graphql.test.apps.profile.api;

import org.eclipse.microprofile.graphql.Ignore;

/**
 * Enum should omit normal methods
 * see issues #309
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public enum Category {
    AI_MACHINE_LEARNING("AI/Machine Learning"),
    @Ignore
    FIELD_SHOULD_BE_IGNORED("Please ignore me"),
    APPLICATION_RUNTIME("Application Runtime");

    private Category(String displayName) {
        this.displayName = displayName;
    }

    private final String displayName;

    public String getDisplayName() {
        return displayName;
    }
}
