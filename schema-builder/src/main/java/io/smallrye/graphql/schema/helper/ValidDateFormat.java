package io.smallrye.graphql.schema.helper;

import org.jboss.jandex.DotName;

import io.smallrye.graphql.schema.Classes;

/**
 * Check is a Date type format is a valod ISO format
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ValidDateFormat {

    public static final String ISO_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String ISO_OFFSET_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ssZ";
    public static final String ISO_ZONED_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ssZ'['VV']'";
    public static final String ISO_DATE = "yyyy-MM-dd";
    public static final String ISO_TIME = "HH:mm:ss";
    public static final String ISO_OFFSET_TIME = "HH:mm:ssZ";

    public static boolean isValid(DotName typeName, String format) {
        if (typeName.equals(Classes.LOCALDATE)) {
            return isValidDate(format);
        } else if (typeName.equals(Classes.LOCALTIME)) {
            return isValidTime(format);
        } else if (typeName.equals(Classes.LOCALDATETIME)) {
            return isValidDateTime(format);
        }
        // TODO: Add support for other types ?
        return false;
    }

    /**
     * a Valid ISO DateTime has:
     * 
     * a valid date
     * The letter 'T'. Parsing is case insensitive.
     * a valid time
     * 
     * @param the format in question
     * @return true if it is
     */
    private static boolean isValidDateTime(String format) {
        if (format.contains("T")) {
            String[] split = format.split("T");
            return isValidDate(split[0]) && isValidTime(split[1]);
        } else if (format.contains("t")) {
            String[] split = format.split("t");
            return isValidDate(split[0]) && isValidTime(split[1]);
        }
        return false;
    }

    /**
     * a Valid ISO Date has:
     * 
     * Four digits or more for the year. Years in the range 0000 to 9999 will be pre-padded by zero to ensure four digits. Years
     * outside that range will have a prefixed positive or negative symbol.
     * A dash
     * Two digits for the month-of-year. This is pre-padded by zero to ensure two digits.
     * A dash
     * Two digits for the day-of-month. This is pre-padded by zero to ensure two digits.
     * 
     * @param the format in question
     * @return true if it is
     */
    private static boolean isValidDate(String format) {
        return ISO_DATE.equals(format);
    }

    /**
     * a Valid ISO Time has:
     * 
     * Two digits for the hour-of-day. This is pre-padded by zero to ensure two digits.
     * A colon
     * Two digits for the minute-of-hour. This is pre-padded by zero to ensure two digits.
     * If the second-of-minute is not available then the format is complete.
     * A colon
     * Two digits for the second-of-minute. This is pre-padded by zero to ensure two digits.
     * If the nano-of-second is zero or not available then the format is complete.
     * A decimal point
     * One to nine digits for the nano-of-second. As many digits will be output as required.
     * 
     * @param the format in question
     * @return true if it is
     */
    private static boolean isValidTime(String format) {
        String[] sections = format.split(":");

        if (sections.length == 2) { // HH:mm
            if (sections[0].equals("HH") && sections[1].equals("mm")) {
                return true;
            }
        } else if (sections.length == 3) {// HH:mm:ss
            if (sections[0].equals("HH") && sections[1].equals("mm") && sections[2].equals("ss")) {
                return true;
            } else if (sections[0].equals("HH") && sections[1].equals("mm") && sections[2].contains(".")) { // HH:mm:ss.S...S
                String[] secondsSection = sections[2].split(".");
                if (secondsSection.length == 2) {
                    if (secondsSection[0].equals("ss") && isValidNanoSecond(secondsSection[1])) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // One to nine digits for the nano-of-second. As many digits will be output as required. 
    private static boolean isValidNanoSecond(String input) {
        if (input != null && !input.isEmpty() && input.length() > 0 && input.length() < 10) {
            char[] chars = input.toCharArray();
            for (char c : chars) {
                if (c != S)
                    return false;
            }
            return true;
        }
        return false;
    }

    private static final char S = "S".charAt(0);
}
