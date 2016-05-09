package address.util;

/**
 * Helper functions for handling strings.
 */
public class StringUtil {
    public static boolean containsIgnoreCase(String source, String query) {
        return source.toLowerCase().contains(query.toLowerCase());
    }
}
