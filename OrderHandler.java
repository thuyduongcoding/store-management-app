import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class OrderHandler implements HttpHandler {
    private DataStore dataStore;
    private Gson gson;

    public OrderHandler() {
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
        if (pathParts.length != 3) { // Expecting /orders/{id}
            sendResponse(exchange, 400, "Invalid URL format. Use /orders/{id}");
            return;
        }

        String orderId = pathParts[2];

        Order order = dataStore.findOrderById(orderId);
        if (order == null) {
            sendResponse(exchange, 404, "Order not found.");
            return;
        }

        String jsonResponse = gson.toJson(order);
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
