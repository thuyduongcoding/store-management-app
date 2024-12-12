import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

public class SearchHandler implements HttpHandler {
    private DataStore dataStore;

    public SearchHandler() {
        this.dataStore = new DataStore();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        URI requestURI = exchange.getRequestURI();
        Map<String, String> queryParams = Utils.parseQueryParams(requestURI.getQuery());

        Integer productId = null;
        String name = null;
        Double priceLessThan = null;
        Double priceGreaterThan = null;

        if (queryParams.containsKey("product_id")) {
            try {
                productId = Integer.parseInt(queryParams.get("product_id"));
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "Invalid product_id parameter.");
                return;
            }
        }

        if (queryParams.containsKey("name")) {
            name = queryParams.get("name").trim();
            if (name.isEmpty()) {
                sendResponse(exchange, 400, "Name parameter cannot be empty.");
                return;
            }
        }

        if (queryParams.containsKey("price_lt")) {
            try {
                priceLessThan = Double.parseDouble(queryParams.get("price_lt"));
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "Invalid price_lt parameter.");
                return;
            }
        }

        if (queryParams.containsKey("price_gt")) {
            try {
                priceGreaterThan = Double.parseDouble(queryParams.get("price_gt"));
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "Invalid price_gt parameter.");
                return;
            }
        }

        List<Product> products = dataStore.searchProducts(productId, name, priceLessThan, priceGreaterThan);

        String htmlResponse = generateHTML(products);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        sendResponse(exchange, 200, htmlResponse);
    }

    /**
     * Generates an HTML page listing the products.
     *
     * @param products the list of products
     * @return HTML string
     */
    private String generateHTML(List<Product> products) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><title>Product Search Results</title>");
        sb.append("<style>");
        sb.append("table {width: 80%; border-collapse: collapse; margin: 25px 0; font-size: 18px; text-align: left;}");
        sb.append("th, td {padding: 12px; border-bottom: 1px solid #ddd;}");
        sb.append("th {background-color: #f2f2f2;}");
        sb.append("</style></head><body>");
        sb.append("<h2>Product Search Results</h2>");

        if (products.isEmpty()) {
            sb.append("<p>No products found matching the search criteria.</p>");
        } else {
            sb.append("<table>");
            sb.append("<tr><th>Product ID</th><th>Name</th><th>Description</th><th>Price ($)</th><th>Stock</th></tr>");
            for (Product p : products) {
                sb.append("<tr>");
                sb.append("<td>").append(p.getProductId()).append("</td>");
                sb.append("<td>").append(Utils.escapeHTML(p.getName())).append("</td>");
                sb.append("<td>").append(Utils.escapeHTML(p.getDescription())).append("</td>");
                sb.append("<td>").append(String.format("%.2f", p.getPrice())).append("</td>");
                sb.append("<td>").append(p.getStock()).append("</td>");
                sb.append("</tr>");
            }
            sb.append("</table>");
        }

        sb.append("</body></html>");
        return sb.toString();
    }

    /**
     * Sends an HTTP response with the given status code and body.
     *
     * @param exchange   the HttpExchange object
     * @param statusCode the HTTP status code
     * @param response   the response body
     * @throws IOException
     */
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes("UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}
