package io.smallrye.graphql.index;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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

    public static final DotName LOCALDATE = DotName.createSimple(LocalDate.class.getName());
    public static final DotName LOCALDATETIME = DotName.createSimple(LocalDateTime.class.getName());
    public static final DotName LOCALTIME = DotName.createSimple(LocalTime.class.getName());
}
