package org.structured.api.quarkus.utils;

/**
 * The type String utils.
 */
public class StringUtils {


    /**
     * <pre>
     * Capitalizes a string
     * </pre>
     *
     * @param input the input
     * @return string
     */
    public static String capitalize(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1)
                    .toUpperCase() + input.substring(1);
    }

    /**
     * Quotes a string.
     * <p>
     * abcd into "abcd"
     *
     * @param input the input
     * @return the string
     */
    public static String quote(String input) {
        if (input == null) return null;
        return "\"" + input + "\"";
    }

    /**
     * Un-quotes a string.
     * <p>
     * "abcd" into abcd
     *
     * @param input the input
     * @return the string
     */
    public static String unQuote(String input) {
        if (input == null) return null;
        if (input.length() < 2) return input;
        return input.substring(1, input.length() - 1);
    }
}
