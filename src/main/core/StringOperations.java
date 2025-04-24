import java.util.ArrayList;
import java.util.List;

public class StringOperations {
    
    public static boolean isInString(String line, int position) {
        boolean inString = false;
        for (int i = 0; i < position; i++) {
            if (line.charAt(i) == '"' && (i == 0 || line.charAt(i-1) != '\\')) {
                inString = !inString;
            }
        }
        return inString;
    }
    
    public static boolean containsUnquotedChar(String line, char target) {
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == target && !isInString(line, i)) {
                return true;
            }
        }
        return false;
    }

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

    public static class Markup {

        // Markup Tags have a start and end tag and provide a modification to whatever lies between those tags. Tags must start with % followed by a unique set of characters.
        private static final List<Markup> htmlMarkupTags = new ArrayList<Markup>() {{
            add (new Markup("%%", "%"));
            add (new Markup("%n", "<br>&#10;"));
            add (new Markup("%t", "&#9;"));
            add (new Markup("%'", "\""));
            add (new Markup("%i", "<i>", "%/i", "</i>"));
            add (new Markup("%k", "<span class=\"keyword\">", "%/k", "</span>"));
            add (new Markup("%c", "</p>\n\t<pre><code class=\"language-java\">", "%/c", "</code></pre>\n\t<p>"));
            add (new Markup("%b", "<b>", "%/b", "</b>"));
            add (new Markup("%l", "<a href=\"https://www.youtube.com/watch?v=dQw4w9WgXcQ\">", "%/l", "</a>"));
        }};

        private static final List<Markup> javaMarkupTags = new ArrayList<Markup>() {{
            add (new Markup("%%", "%"));
            add (new Markup("%n", "\n"));
            add (new Markup("%t", "\t"));
            add (new Markup("%'", "\""));
            add (new Markup("%w", " "));
        }};
        
        public String startTag;
        public String startReplacement;
        public String endTag;
        public String endReplacement;
        public Markup(String tag, String replacement) {
            this(tag, replacement, null, null);
        }
        public Markup(String startTag, String startReplacement, String endTag, String endReplacement) {
            this.startTag = startTag;
            this.startReplacement = startReplacement;
            this.endTag = endTag;
            this.endReplacement = endReplacement;
        }

        public static Markup findJavaMarkup(String tag) {
            for (Markup markup : javaMarkupTags) {
                if (markup.startTag.equals(tag)) return markup;
            }
            return null;
        }

        public static Markup findHTMLMarkup(String tag) {
            for (Markup markup : htmlMarkupTags) {
                if (markup.startTag.equals(tag)) return markup;
            }
            return null;
        }
    }

    public static String processJavaMarkup(String text, int startIndex) {
        StringBuilder result = new StringBuilder();

        for (int i = startIndex; i < text.length(); i++) {
            if (text.charAt(i) == '%') {
                if (i + 1 >= text.length()) {
                    result.append('%');
                    continue;
                }
                Markup markup = Markup.findJavaMarkup(text.substring(i, i + 2));
                if (markup == null) {
                    System.out.println("WARNING: Unrecognized markup " + text.substring(i, i + 2) + " in ..." + text.substring(Integer.max(0, i - 10), Integer.min(text.length(), i + 10)) + "...");
                    result.append(text.charAt(i));
                    continue;
                }

                result.append(markup.startReplacement);

                i += 1;
            }
            else result.append(text.charAt(i));
        }

        return result.toString();
    }

    public static String processHtmlMarkup(String text, int startIndex) {
        StringBuilder result = new StringBuilder();
    
        for (int i = startIndex; i < text.length(); i++) {
            if (text.charAt(i) == '%') {
                if (i + 1 >= text.length()) {
                    result.append('%');
                    continue;
                }
                
                // Handle end tags
                if (text.charAt(i + 1) == '/') {
                    return result.toString() + "§" + i; // Use § as delimiter for returning position
                }
                
                // Get markup tag
                Markup markup = Markup.findHTMLMarkup(text.substring(i, i + 2));
                if (markup == null) {
                    System.out.println("WARNING: Unrecognized markup " + text.substring(i, i + 2) + " in ..." + text.substring(Integer.max(0, i - 10), Integer.min(text.length(), i + 10)) + "...");
                    result.append(text.charAt(i));
                    continue;
                }
                
                // Handle single replacement tags
                if (markup.endReplacement == null) {
                    result.append(markup.startReplacement);
                    i++;
                    continue;
                }
                
                // Handle paired tags
                i += 2; // Skip the opening tag
                String processed = processHtmlMarkup(text, i);
                if (processed == null) {
                    System.out.println("WARNING: Unclosed tag at position " + i);
                    continue;
                }
                
                // Split position from content
                String[] parts = processed.split("§");
                String content = parts[0];
                i = Integer.parseInt(parts[1]); // Update position to end of nested content
                
                result.append(markup.startReplacement)
                    .append(content)
                    .append(markup.endReplacement);
                
                i += 2; // Skip the closing tag
            }
            else { // regular text
                result.append(text.charAt(i));
            }
        }
        
        return result.toString();
    }

    public static String replaceLast(String text, String regex, String replacement) {
        int index = text.lastIndexOf(regex);
        if (index == -1) {
            return text;
        }
        return text.substring(0, index) + replacement + text.substring(index + regex.length());
    }
}