package core;

import java.util.ArrayList;
import java.util.List;

public class StringOperations {
    
    /**
     * Determines whether a given position in a line of text is currently inside a string literal,
     * accounting for escaped quotes.
     *
     * <p>This method scans the input line from the beginning up to (but not including) the specified position.
     * It toggles the {@code inString} flag each time it encounters a non-escaped double quote character.
     * This helps determine whether the character at the given position falls within a string.</p>
     *
     * @param line The input string to analyze.
     * @param position The index position in the string to check.
     * @return {@code true} if the position is within a string literal, {@code false} otherwise.
     */
    public static boolean isInString(String line, int position) {
        boolean inString = false;
        for (int i = 0; i < position; i++) {
            if (line.charAt(i) == '"' && (i == 0 || line.charAt(i-1) != '\\')) {
                inString = !inString;
            }
        }
        return inString;
    }

    /**
     * Determines whether the specified position in a line of text is currently inside a JSON array.
     *
     * <p>This method traverses the line up to (but not including) the given position, tracking array nesting depth.
     * It increments depth when encountering a non-escaped '[' character outside of a string, and decrements it for ']'.
     * If the resulting depth is greater than 0 at the given position, the position is considered to be inside an array.</p>
     *
     * @param line The input string to analyze.
     * @param position The index position in the string to check.
     * @return {@code true} if the position is within a JSON array, {@code false} otherwise.
     * @see #isInString(String, int)
     */
    public static boolean isInArray(String line, int position) {
        int depth = 0;
        for (int i = 0; i < position; i++) {
            if (line.charAt(i) == '[' && !isInString(line, i-1))
                depth++;
            else if (line.charAt(i) == ']' && !isInString(line, i-1))
                depth--;
        }
        return depth == 0 ? false : true;
    }
    
    /**
     * Checks if the given line contains at least one unquoted occurrence of the target character.
     *
     * <p>This method ignores characters that appear inside string literals (delimited by double quotes)
     * and only considers unescaped characters outside of strings.</p>
     *
     * @param line The string to search.
     * @param target The character to look for.
     * @return {@code true} if the character appears outside of a string, {@code false} otherwise.
     * @see #countUnquotedChar(String, char)
     * @see #isInString(String, int)
     */
    public static boolean containsUnquotedChar(String line, char target) {
        return countUnquotedChar(line, target) > 0;
    }

    /**
     * Counts how many times a target character appears outside of string literals in the given line.
     *
     * <p>This method scans the string and increments a counter for each instance of the target character
     * that is not within a quoted string. Escaped quotes are correctly ignored.</p>
     *
     * @param line The string to analyze.
     * @param target The character to count.
     * @return The number of unquoted occurrences of the target character.
     * @see #isInString(String, int)
     */
    public static int countUnquotedChar(String line, char target) {
        int count = 0;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == target && !isInString(line, i))
                count++;
        }
        return count;
    }

    /**
     * Splits the input string around occurrences of the given separator string,
     * ignoring separators that appear inside quoted strings.
     *
     * <p>This method respects string literals delimited by double quotes, treating
     * escaped quotes correctly. Trims whitespace around each resulting substring.</p>
     *
     * @param string The input string to split.
     * @param separator The substring to split on, when not within quotes.
     * @return An array of trimmed substrings resulting from the split.
     * @see #isInString(String, int)
     */
    public static String[] splitByUnquotedString(String string, String separator) {
        List<String> parts = new ArrayList<>();
        int lastSplitIndex = 0;
        
        for (int i = 0; i <= string.length() - separator.length(); i++) {
            // Check if this position contains the separator
            if (string.startsWith(separator, i)) {
                // Verify the separator is not in quotes
                if (!isInString(string, i)) {
                    // Add the part before this separator
                    parts.add(string.substring(lastSplitIndex, i).trim());
                    lastSplitIndex = i + separator.length();
                    i += separator.length() - 1; // Skip the rest of the separator
                }
            }
        }
        
        // Add the remaining part after the last separator
        if (lastSplitIndex <= string.length()) {
            parts.add(string.substring(lastSplitIndex).trim());
        }
        
        return parts.toArray(new String[0]);
    }

    /**
     * Splits the input string around occurrences of the given separator string,
     * ignoring separators that appear inside quoted strings, and limits the number
     * of resulting substrings.
     *
     * <p>Trims whitespace around each substring. Stops splitting once {@code limit}
     * parts have been collected, placing the remainder into the last entry.</p>
     *
     * @param string The input string to split.
     * @param separator The substring to split on, when not within quotes.
     * @param limit The maximum number of substrings to return. Must be at least 1.
     * @return An array of trimmed substrings resulting from the split, with at most {@code limit} entries.
     * @see #isInString(String, int)
     */
    public static String[] splitByUnquotedString(String string, String separator, int limit) {
        List<String> parts = new ArrayList<>();
        int lastSplitIndex = 0;
        int count = 1; // always returns at least one string

        for (int i = 0; i <= string.length() - separator.length() && count < limit; i++) {
            // Check if this position contains the separator
            if (string.startsWith(separator, i)) {
                // Verify the separator is not in quotes
                if (!isInString(string, i)) {
                    // Add the part before this separator
                    parts.add(string.substring(lastSplitIndex, i).trim());
                    count++;
                    lastSplitIndex = i + separator.length();
                    i += separator.length() - 1; // Skip the rest of the separator
                }
            }
        }

        // Add the remaining part after the last separator
        if (lastSplitIndex <= string.length()) {
            parts.add(string.substring(lastSplitIndex).trim());
        }
        
        return parts.toArray(new String[0]);
    }

    /**
     * Splits the input string around occurrences of the given separator string,
     * ignoring separators that appear within array brackets <code>[...]</code>.
     *
     * <p>This method treats brackets as array delimiters and avoids splitting inside
     * them, even if the separator appears. Trims whitespace around each resulting
     * substring.</p>
     *
     * @param string The input string to split.
     * @param separator The substring to split on, when not within array brackets.
     * @return An array of trimmed substrings resulting from the split.
     * @see #isInArray(String, int)
     */
    public static String[] splitByStringNotInArray(String string, String separator) {
        List<String> parts = new ArrayList<>();
        int lastSplitIndex = 0;
        
        for (int i = 0; i <= string.length() - separator.length(); i++) {
            // Check if this position contains the separator
            if (string.startsWith(separator, i)) {
                // Verify the separator is not in brackets
                if (!isInArray(string, i)) {
                    // Add the part before this separator
                    parts.add(string.substring(lastSplitIndex, i).trim());
                    lastSplitIndex = i + separator.length();
                    i += separator.length() - 1; // Skip the rest of the separator
                }
            }
        }
        
        // Add the remaining part after the last separator
        if (lastSplitIndex <= string.length()) {
            parts.add(string.substring(lastSplitIndex).trim());
        }
        
        return parts.toArray(new String[0]);
    }

    /**
     * Replaces the last occurrence of a specified regular expression with a replacement string in the input text.
     * This method uses the {@link #replaceFirst(String, String, String)} method by reversing the string and performing
     * the replacement on the first occurrence in the reversed string.
     *
     * @param text The input string in which the replacement will be made.
     * @param regex The regular expression to match for the replacement.
     * @param replacement The string to replace the matched regex.
     * @return A new string with the last occurrence of the regex replaced by the replacement string.
     * @see String#replaceFirst(String, String)
     */
    public static String replaceLast(String text, String regex, String replacement) {
        String reversedText = new StringBuilder(text).reverse().toString();
        String reversedResult = reversedText.replaceFirst(regex, replacement);
        String result = new StringBuilder(reversedResult).reverse().toString();
        return result;
    }

    /**
     * Replaces the first occurrence of a specified regular expression with a replacement string in the input text.
     * This method reverses the input string, performs the replacement on the first occurrence of the regex in the reversed string, 
     * and then reverses the resulting string again to produce the final output.
     *
     * @param text The input string in which the replacement will be made.
     * @param regex The regular expression to match for the replacement.
     * @param replacement The string to replace the matched regex.
     * @return A new string with the first occurrence of the regex replaced by the replacement string.
     * @see String#replaceFirst(String, String)
     */
    public static String replaceFirst(String text, String regex, String replacement) {
        return text.replaceFirst(regex, replacement);
    }

    /**
     * Replaces all occurrences of a specified regular expression with a replacement string in the input text.
     * @param text The input string in which the replacements will be made.
     * @param regex The regular expression to match for the replacement.
     * @param replacement The string to replace the matched regex.
     * @return A new string with all occurrences of the regex replaced by the replacement string.
     * @see String#replaceAll(String, String)
     */
    public static String replaceAll(String text, String regex, String replacement) {
        return text.replaceAll(regex, replacement);
    }

    /**
     * Replaces the first {@code count} occurrences of the specified regular expression
     * with the replacement string in the input text.
     * 
     * @param text The input string in which the replacements will be made.
     * @param regex The regular expression to match for the replacement.
     * @param replacement The string to replace the matched regex.
     * @param count The number of occurrences to replace.
     * @return A new string with the first {@code count} occurrences of the regex replaced by the replacement string.
     * @see #replaceFirst(String, String, String)
     */
    public static String replaceFirstCount(String text, String regex, String replacement, int count) {
        StringBuilder result = new StringBuilder(text);
        int startPos = 0;  // Start position for the next search

        for (int i = 0; i < count; i++) {
            // Find the next occurrence of the regex starting from the current position
            int index = result.indexOf(regex, startPos);
            if (index == -1) {
                break; // No more matches found, stop the loop
            }
            
            // Replace the match with the replacement string
            result.replace(index, index + regex.length(), replacement);
            
            // Move the start position just after the current replacement
            startPos = index + replacement.length();
        }

        return result.toString();
    }


    /**
     * Replaces the last {@code count} occurrences of the specified regular expression
     * with the replacement string in the input text.
     * 
     * @param text The input string in which the replacements will be made.
     * @param regex The regular expression to match for the replacement.
     * @param replacement The string to replace the matched regex.
     * @param count The number of occurrences to replace.
     * @return A new string with the last {@code count} occurrences of the regex replaced by the replacement string.
     * @see #replaceLast(String, String, String)
     */
    public static String replaceLastCount(String text, String regex, String replacement, int count) {
        StringBuilder result = new StringBuilder(text);
        int startPos = text.length();  // Start from the end of the string
    
        for (int i = 0; i < count; i++) {
            // Find the last occurrence of the regex before the current position
            int index = result.lastIndexOf(regex, startPos - 1);
            if (index == -1) {
                break; // No more matches found, stop the loop
            }
    
            // Replace the match with the replacement string
            result.replace(index, index + regex.length(), replacement);
    
            // Move the start position just before the current replacement
            startPos = index;
        }
    
        return result.toString();
    }    
}