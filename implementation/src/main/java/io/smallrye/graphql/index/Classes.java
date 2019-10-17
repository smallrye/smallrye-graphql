package io.smallrye.graphql.index;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;

/**
 * Class helper
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public interface Classes {
    public static boolean isEnum(ClassInfo classInfo) {
        if (classInfo == null)
            return false;
        return classInfo.superName().equals(ENUM);
    }

    public static final DotName ENUM = DotName.createSimple(Enum.class.getName());
}
