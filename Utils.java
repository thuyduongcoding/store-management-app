import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class Utils {
    /**
     * Parses query parameters from a URL query string.
     *
     * @param query the query string
     * @return a map of parameter names to values
     */
    public static Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) {
            return params;
        }

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            try {
                if (idx > 0 && pair.length() > idx + 1) {
                    String key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                    String value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                    params.put(key, value);
                } else if (idx > 0) {
                    String key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                    params.put(key, "");
                }
            } catch (UnsupportedEncodingException e) {
                // UTF-8 is supported, so this should not occur
                e.printStackTrace();
            }
        }
        return params;
    }

    /**
     * Escapes HTML special characters to prevent XSS.
     *
     * @param input the input string
     * @return the escaped string
     */
    public static String escapeHTML(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#x27;");
    }
}
