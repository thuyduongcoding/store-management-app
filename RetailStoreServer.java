import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class RetailStoreServer {
    public static void main(String[] args) {
        try {
            // Define the server port
            int port = 8000;
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            // Create context for /search endpoint
            server.createContext("/search", new SearchHandler());

            // Create context for /products endpoint
            server.createContext("/products", new ProductHandler());

            // Create context for /users endpoint
            server.createContext("/users", new UserHandler());

            // Create context for /orders endpoint
            server.createContext("/orders", new OrderHandler());

            // Optionally, create context for other endpoints like /register, /sales-report, etc.

            // Set executor to handle multiple requests concurrently
            server.setExecutor(Executors.newFixedThreadPool(10));

            // Start the server
            server.start();
            System.out.println("Server started on port " + port);

            // Add shutdown hook for graceful shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down server...");
                server.stop(1);
                DataStore.closeMongoConnection(); // Close MongoDB connection if necessary
                System.out.println("Server stopped.");
            }));

        } catch (IOException e) {
            System.out.println("Failed to create HTTP server on port " + 8000);
            e.printStackTrace();
        }
    }
}
