package io.smallrye.graphql.schema.helper;

import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

/**
 * Helping with method operations.
 * 
 * Use to get the correct name for a method (so remove the get/set/is)
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class MethodHelper {

    private MethodHelper() {
    }

    /**
     * Get the correct field name from a method
     * 
     * @param direction the direction
     * @param methodName the method name
     * @return the field name
     */
    public static String getPropertyName(Direction direction, String methodName) {
        if (direction.equals(Direction.IN)) {
            return toNameFromSetter(methodName);
        } else if (direction.equals(Direction.OUT)) {
            return toNameFromGetter(methodName);
        }
        return methodName;
    }

    /**
     * See if this is a getter or setter for a field property (depending on the direction)
     *
     * @param direction The direction
     * @param methodName the methodName
     * @return true if it is
     * @deprecated Use {@link #isPropertyMethod(Direction, MethodInfo)} instead
     */
    @Deprecated
    public static boolean isPropertyMethod(Direction direction, String methodName) {
        if (direction.equals(Direction.IN)) {
            return isSetterName(methodName);
        } else if (direction.equals(Direction.OUT)) {
            return isGetterName(methodName);
        }
        return false;
    }

    /**
     * See if this is a getter or setter for a field property (depending on the direction)
     *
     * @param direction The direction
     * @param method the method
     * @return true if it is
     */
    public static boolean isPropertyMethod(Direction direction, MethodInfo method) {
        if (direction.equals(Direction.IN)) {
            return isSetter(method);
        } else if (direction.equals(Direction.OUT)) {
            return isGetter(method);
        }
        return false;
    }

    /**
     * Checks whether a method is a setter, thus:
     * <ul>
     * <li>has no return type</li>
     * <li>has exactly one parameter</li>
     * <li>is appropriately named</li>
     * </ul>
     *
     * @param method the method
     * @return true if it is
     */
    private static boolean isSetter(MethodInfo method) {
        return method.returnType().kind() == Type.Kind.VOID
                && method.parameters().size() == 1
                && isSetterName(method.name());
    }

    /**
     * Checks whether a method is a getter, thus:
     * <ul>
     * <li>has an return type</li>
     * <li>has no parameter</li>
     * <li>is appropriately named</li>
     * </ul>
     *
     * @param method the method
     * @return true if it is
     */
    private static boolean isGetter(MethodInfo method) {
        return method.returnType().kind() != Type.Kind.VOID
                && method.parameters().isEmpty()
                && isGetterName(method.name());
    }

    private static String toNameFromSetter(String methodName) {
        if (methodName.startsWith(SET) && methodName.length() > 3 && hasCapitalAt(methodName, 3)) {
            methodName = removeAndLowerCase(methodName, 3);
        }
        return methodName;
    }

    private static String toNameFromGetter(String methodName) {
        if (methodName.startsWith(GET) && methodName.length() > 3 && hasCapitalAt(methodName, 3)) {
            methodName = removeAndLowerCase(methodName, 3);
        } else if (methodName.startsWith(IS) && methodName.length() > 2 && hasCapitalAt(methodName, 2)) {
            methodName = removeAndLowerCase(methodName, 2);
        }
        return methodName;
    }

    private static boolean isGetterName(String methodName) {
        return (methodName.length() > 3 && methodName.startsWith(GET) && hasCapitalAt(methodName, 3))
                || (methodName.length() > 2 && methodName.startsWith(IS) && hasCapitalAt(methodName, 2));
    }

    private static boolean isSetterName(String methodName) {
        return methodName.length() > 3 && methodName.startsWith(SET) && hasCapitalAt(methodName, 3);
    }

    private static String removeAndLowerCase(String original, int pre) {
        original = original.substring(pre);
        return original.substring(0, 1).toLowerCase() + original.substring(1);
    }

    private static boolean hasCapitalAt(String name, int pos) {
        String letter = new String(new char[] { name.charAt(pos) });
        return !letter.equals(letter.toLowerCase());
    }

    private static final String SET = "set";
    private static final String GET = "get";
    private static final String IS = "is";
}
