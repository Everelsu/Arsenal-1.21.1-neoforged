package dev.doctor4t.arsenal.util;

/**
 * Embedded replacement for the single ratatouille TextUtils helper Arsenal uses.
 */
public final class TextUtils {
    private TextUtils() {}

    /** Converts an enum-style string ("blood_scythe") to a display name ("Blood Scythe"). */
    public static String formatValueString(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        StringBuilder builder = new StringBuilder();
        boolean startOfWord = true;
        for (char c : value.toCharArray()) {
            if (c == '_') {
                c = ' ';
            }
            if (Character.isWhitespace(c)) {
                startOfWord = true;
            } else if (startOfWord) {
                c = Character.toTitleCase(c);
                startOfWord = false;
            } else {
                c = Character.toLowerCase(c);
            }
            builder.append(c);
        }
        return builder.toString();
    }
}
