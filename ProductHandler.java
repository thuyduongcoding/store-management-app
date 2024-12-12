import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class ProductHandler implements HttpHandler {
    private DataStore dataStore;
    private Gson gson;

    public ProductHandler() {
        this.dataStore = new DataStore();
        this.gson = new Gson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        if (!method.equalsIgnoreCase("GET")) {
            sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");
        if (pathParts.length != 3) { // Expecting /products/{id}
            sendResponse(exchange, 400, "Invalid URL format. Use /products/{id}");
            return;
        }

        String idStr = pathParts[2];
        int productId;
        try {
            productId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "Invalid product ID.");
            return;
        }

        Product product = dataStore.findProductById(productId);
        if (product == null) {
            sendResponse(exchange, 404, "Product not found.");
            return;
        }

        String jsonResponse = gson.toJson(product);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        sendResponse(exchange, 200, jsonResponse);
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
