import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class DataStore {
    // ----------------------------
    // MySQL Connection Details
    // ----------------------------
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/retail_store?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root"; // Replace with your MySQL username
    private static final String DB_PASSWORD = "pass"; // Replace with your MySQL password

    // ----------------------------
    // MongoDB Connection Details
    // ----------------------------
    private static final String MONGO_URI = "mongodb://localhost:27017";
    private static final String MONGO_DB_NAME = "retail_store_mongo";
    private static MongoClient mongoClient = null;
    private static MongoDatabase mongoDatabase = null;

    // ----------------------------
    // Static Block for MongoDB
    // ----------------------------
    static {
        try {
            mongoClient = new MongoClient(new MongoClientURI(MONGO_URI));
            mongoDatabase = mongoClient.getDatabase(MONGO_DB_NAME);
            System.out.println("MongoDB Connection Established!");
        } catch (Exception e) {
            System.out.println("Error initializing MongoDB connection");
            e.printStackTrace();
        }
    }

    // ----------------------------
    // MySQL Utility Methods
    // ----------------------------

    /**
     * Establishes and returns a connection to the MySQL database.
     *
     * @return Connection object
     * @throws SQLException if connection fails
     */
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
    }

    // ----------------------------
    // User Management (MySQL)
    // ----------------------------

    /**
     * Finds and returns a User by username from MySQL.
     *
     * @param username the username to search for
     * @return User object if found, else null
     */
    public static User findUserByUsername(String username) {
        String query = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt("user_id");
                String uname = rs.getString("username");
                String password = rs.getString("password"); // Plain-text password
                String role = rs.getString("role");
                return new User(userId, uname, password, role);
            }
        } catch (SQLException e) {
            System.out.println("Error finding user by username.");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Creates a new User in MySQL and a corresponding Customer in MongoDB.
     *
     * @param username the username
     * @param password the plain-text password
     * @param role     the role (e.g., "customer")
     * @param customer the Customer object with profile details
     * @return true if successful, else false
     */
    public static boolean createUserAndCustomer(String username, String password, String role, Customer customer) {
        String insertUserQuery = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertUserQuery, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, password); // Store plain-text password
            stmt.setString(3, role);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int userId = generatedKeys.getInt(1);
                    customer.setUserId(userId);
                    return createCustomer(customer);
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error creating user and customer.");
            e.printStackTrace();
            return false;
        }
    }

    // ----------------------------
    // Product Management (MySQL)
    // ----------------------------

    /**
     * Searches for products by name keyword.
     *
     * @param keyword the search keyword
     * @return list of matching Product objects
     */
    public static List<Product> searchProductsByName(String keyword) {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM products WHERE name LIKE ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int productId = rs.getInt("product_id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                double price = rs.getDouble("price");
                int stock = rs.getInt("stock");
                products.add(new Product(productId, name, description, price, stock));
            }
        } catch (SQLException e) {
            System.out.println("Error searching products by name.");
            e.printStackTrace();
        }
        return products;
    }

    /**
     * Finds and returns a Product by productId from MySQL.
     *
     * @param productId the product ID
     * @return Product object if found, else null
     */
    public static Product findProductById(int productId) {
        String query = "SELECT * FROM products WHERE product_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String name = rs.getString("name");
                String description = rs.getString("description");
                double price = rs.getDouble("price");
                int stock = rs.getInt("stock");
                return new Product(productId, name, description, price, stock);
            }
        } catch (SQLException e) {
            System.out.println("Error finding product by ID.");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Adds a new Product to MySQL.
     *
     * @param product the Product object to add
     * @return true if successful, else false
     */
    public static boolean addProduct(Product product) {
        String query = "INSERT INTO products (name, description, price, stock) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getDescription());
            stmt.setDouble(3, product.getPrice());
            stmt.setInt(4, product.getStock());
            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            System.out.println("Error adding product.");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Updates an existing Product in MySQL.
     *
     * @param product the Product object with updated details
     * @return true if successful, else false
     */
    public static boolean updateProduct(Product product) {
        String query = "UPDATE products SET name = ?, description = ?, price = ?, stock = ? WHERE product_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getDescription());
            stmt.setDouble(3, product.getPrice());
            stmt.setInt(4, product.getStock());
            stmt.setInt(5, product.getProductId());
            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            System.out.println("Error updating product.");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes a Product from MySQL.
     *
     * @param productId the product ID to delete
     * @return true if successful, else false
     */
    public static boolean deleteProduct(int productId) {
        String query = "DELETE FROM products WHERE product_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, productId);
            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            System.out.println("Error deleting product.");
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Retrieves all products from MySQL.
     *
     * @return list of all Product objects
     */
    public static List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM products";
        try (Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int productId = rs.getInt("product_id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                double price = rs.getDouble("price");
                int stock = rs.getInt("stock");
                products.add(new Product(productId, name, description, price, stock));
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving all products.");
            e.printStackTrace();
        }
        return products;
    }

        /**
     * Searches for products based on optional search parameters.
     *
     * @param productId        the product ID to search for (optional)
     * @param priceLessThan    the maximum price (optional)
     * @param priceGreaterThan the minimum price (optional)
     * @return List of matching Product objects
     */

    /**
     * Searches for products based on optional search parameters.
     *
     * @param productId        the product ID to search for (optional)
     * @param name             the product name keyword to search for (optional)
     * @param priceLessThan    the maximum price (optional)
     * @param priceGreaterThan the minimum price (optional)
     * @return List of matching Product objects
     */
    public List<Product> searchProducts(Integer productId, String name, Double priceLessThan, Double priceGreaterThan) {
        List<Product> products = new ArrayList<>();
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM products WHERE 1=1");
        List<Object> parameters = new ArrayList<>();

        // Dynamically build the query based on non-null parameters
        if (productId != null) {
            queryBuilder.append(" AND product_id = ?");
            parameters.add(productId);
        }

        if (name != null && !name.trim().isEmpty()) {
            queryBuilder.append(" AND name LIKE ?");
            parameters.add("%" + name.trim() + "%"); // Using wildcard for partial matches
        }

        if (priceLessThan != null) {
            queryBuilder.append(" AND price < ?");
            parameters.add(priceLessThan);
        }

        if (priceGreaterThan != null) {
            queryBuilder.append(" AND price > ?");
            parameters.add(priceGreaterThan);
        }

        String query = queryBuilder.toString();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Bind parameters to the prepared statement
            for (int i = 0; i < parameters.size(); i++) {
                Object param = parameters.get(i);
                if (param instanceof Integer) {
                    stmt.setInt(i + 1, (Integer) param);
                } else if (param instanceof Double) {
                    stmt.setDouble(i + 1, (Double) param);
                } else if (param instanceof String) {
                    stmt.setString(i + 1, (String) param);
                } else {
                    stmt.setObject(i + 1, param);
                }
            }

            // Execute the query
            ResultSet rs = stmt.executeQuery();

            // Map the result set to Product objects
            while (rs.next()) {
                int pid = rs.getInt("product_id");
                String pname = rs.getString("name");
                String description = rs.getString("description");
                double price = rs.getDouble("price");
                int stock = rs.getInt("stock");
                products.add(new Product(pid, pname, description, price, stock));
            }

        } catch (SQLException e) {
            System.out.println("Error searching products.");
            e.printStackTrace();
        }

        return products;
    }


    // ----------------------------
    // Customer Management (MongoDB)
    // ----------------------------

    /**
     * Creates a new Customer in MongoDB.
     *
     * @param customer the Customer object to create
     * @return true if successful, else false
     */
    public static boolean createCustomer(Customer customer) {
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection("customers");

        Document customerDoc = new Document("user_id", customer.getUserId())
                .append("first_name", customer.getFirstName())
                .append("last_name", customer.getLastName())
                .append("email", customer.getEmail())
                .append("phone_number", customer.getPhoneNumber())
                .append("payment_methods", convertPaymentMethods(customer.getPaymentMethods()));

        try {
            customersCollection.insertOne(customerDoc);
            System.out.println("Customer created successfully with ID: " + customerDoc.getObjectId("_id"));
            return true;
        } catch (Exception e) {
            System.out.println("Error creating customer.");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves a Customer from MongoDB by userId.
     *
     * @param userId the associated user ID in MySQL
     * @return Customer object if found, else null
     */
    public static Customer getCustomerByUserId(int userId) {
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection("customers");
        Document query = new Document("user_id", userId);
        Document doc = customersCollection.find(query).first();

        if (doc != null) {
            String firstName = doc.getString("first_name");
            String lastName = doc.getString("last_name");
            String email = doc.getString("email");
            String phoneNumber = doc.getString("phone_number");
            List<Document> paymentDocs = (List<Document>) doc.get("payment_methods");
            List<PaymentMethod> paymentMethods = convertDocumentsToPaymentMethods(paymentDocs);

            Customer customer = new Customer(userId, firstName, lastName, email, phoneNumber, paymentMethods);
            customer.setId(doc.getObjectId("_id"));
            return customer;
        }

        return null;
    }

    /**
     * Updates a Customer's profile information in MongoDB.
     *
     * @param customer the Customer object with updated profile
     * @return true if successful, else false
     */
    public static boolean updateCustomerProfile(Customer customer) {
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection("customers");
        Document query = new Document("user_id", customer.getUserId());

        Document updatedDoc = new Document("$set", new Document("first_name", customer.getFirstName())
                .append("last_name", customer.getLastName())
                .append("email", customer.getEmail())
                .append("phone_number", customer.getPhoneNumber()));

        try {
            customersCollection.updateOne(query, updatedDoc);
            System.out.println("Customer profile updated successfully.");
            return true;
        } catch (Exception e) {
            System.out.println("Error updating customer profile.");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates a Customer's payment methods in MongoDB.
     *
     * @param userId         the associated user ID in MySQL
     * @param paymentMethods the updated list of PaymentMethod objects
     * @return true if successful, else false
     */
    public static boolean updateCustomerPaymentMethods(int userId, List<PaymentMethod> paymentMethods) {
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection("customers");
        Document query = new Document("user_id", userId);

        Document updatedDoc = new Document("$set", new Document("payment_methods", convertPaymentMethods(paymentMethods)));

        try {
            customersCollection.updateOne(query, updatedDoc);
            System.out.println("Customer payment methods updated successfully.");
            return true;
        } catch (Exception e) {
            System.out.println("Error updating customer payment methods.");
            e.printStackTrace();
            return false;
        }
    }

    // ----------------------------
    // Order Management (MongoDB)
    // ----------------------------

        /**
     * Finds an order by its order ID in MongoDB.
     *
     * @param orderId the unique Order ID (e.g., "ORD-1630456789012")
     * @return Order object if found; otherwise, null
     */
    public Order findOrderById(String orderId) {
        MongoCollection<Document> ordersCollection = mongoDatabase.getCollection("orders");
        System.out.println(ordersCollection);
        // Create a filter to search for the specific order_id
        // Bson filter = Filters.eq("order_id", orderId);
        Document query = new Document("order_id", orderId);
        Document doc = ordersCollection.find(query).first();
        
        // Execute the query and retrieve the first matching document
        // Document doc = ordersCollection.find(filter).first();
        System.out.println(orderId);
        System.out.println(doc);
        if (doc != null) {
            String oid = doc.getString("order_id");
            System.out.print(oid);
            int userId = doc.getInteger("user_id", 0);
            int productId = doc.getInteger("product_id", 0);
            int quantity = doc.getInteger("quantity", 0);
            Date orderDate = doc.getDate("order_date");
            String status = doc.getString("status");

            return new Order(oid, userId, productId, quantity, orderDate, status);
        }
        
        // Order not found
        return null;
    }

     public static boolean placeOrder(int userId, int productId, int quantity) {
        // Step 1: Check stock availability
        Product product = findProductById(productId);
        if (product == null) {
            System.out.println("Product not found.");
            return false;
        }

        if (product.getStock() < quantity) {
            System.out.println("Insufficient stock for product: " + product.getName());
            return false;
        }

        // Step 2: Update stock in MySQL
        boolean stockUpdated = updateProductStock(productId, product.getStock() - quantity);
        if (!stockUpdated) {
            System.out.println("Failed to update product stock.");
            return false;
        }

        // Step 3: Create Order ID (e.g., ORD-10012)
        String orderId = generateOrderId();

        // Step 4: Create Order in MongoDB
        boolean orderCreated = createOrder(orderId, userId, productId, quantity);
        if (!orderCreated) {
            // If order creation fails, rollback stock update
            updateProductStock(productId, product.getStock());
            System.out.println("Failed to create order. Stock rollback.");
            return false;
        }

        System.out.println("Order placed successfully with Order ID: " + orderId);
        return true;
    }

    /**
     * Updates the stock of a product in MySQL.
     *
     * @param productId the product ID
     * @param newStock  the new stock level
     * @return true if successful, else false
     */
    private static boolean updateProductStock(int productId, int newStock) {
        String query = "UPDATE products SET stock = ? WHERE product_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, newStock);
            stmt.setInt(2, productId);
            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            System.out.println("Error updating product stock.");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Generates a unique Order ID.
     *
     * @return a unique Order ID string
     */
    private static String generateOrderId() {
        // Simple implementation using timestamp. For production, consider using UUID or a sequence.
        return "ORD-" + System.currentTimeMillis();
    }


    /**
     * Creates a new Order in MongoDB.
     *
     * @param orderId    the unique order ID
     * @param userId     the ID of the user placing the order
     * @param productId  the ID of the product being ordered
     * @param quantity   the quantity of the product
     * @return true if successful, else false
     */
    private static boolean createOrder(String orderId, int userId, int productId, int quantity) {
        MongoCollection<Document> ordersCollection = mongoDatabase.getCollection("orders");

        // Fetch product price from MySQL for total sales calculation
        Product product = findProductById(productId);
        if (product == null) {
            System.out.println("Product not found for Order creation.");
            return false;
        }

        Document orderDoc = new Document("order_id", orderId)
                .append("user_id", userId)
                .append("product_id", productId)
                .append("quantity", quantity)
                .append("order_date", new Date())
                .append("status", "Pending")
                .append("product_price", product.getPrice());

        try {
            ordersCollection.insertOne(orderDoc);
            ObjectId orderIdObj = orderDoc.getObjectId("_id");
            System.out.println("Order created successfully with ID: " + orderIdObj);
            return true;
        } catch (Exception e) {
            System.out.println("Error creating order.");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves all Orders for a specific user from MongoDB.
     *
     * @param userId the user ID
     * @return list of Order objects
     */
    public static List<Order> getOrdersForUser(int userId) {
        List<Order> userOrders = new ArrayList<>();
        MongoCollection<Document> ordersCollection = mongoDatabase.getCollection("orders");
        Document query = new Document("user_id", userId);
        for (Document doc : ordersCollection.find(query)) {
            ObjectId orderIdObj = doc.getObjectId("_id");
            String orderId = orderIdObj.toHexString();
            int productId = doc.getInteger("product_id", 0);
            int quantity = doc.getInteger("quantity", 0);
            Date orderDate = doc.getDate("order_date");
            String status = doc.getString("status");
            userOrders.add(new Order(orderId, userId, productId, quantity, orderDate, status));
        }
        return userOrders;
    }

    // ----------------------------
    // Refill Order Management (MongoDB)
    // ----------------------------

    /**
     * Creates a new Refill Order in MongoDB.
     *
     * @param productId the product ID to refill
     * @param quantity  the quantity to reorder
     * @return true if successful, else false
     */
    public static boolean createRefillOrder(int productId, int quantity) {
        MongoCollection<Document> refillOrdersCollection = mongoDatabase.getCollection("refill_orders");

        Document refillOrderDoc = new Document("product_id", productId)
                .append("quantity", quantity)
                .append("order_date", new Date())
                .append("status", "Pending");

        try {
            refillOrdersCollection.insertOne(refillOrderDoc);
            System.out.println("Refill order created with ID: " + refillOrderDoc.getObjectId("_id"));
            return true;
        } catch (Exception e) {
            System.out.println("Error creating refill order.");
            e.printStackTrace();
            return false;
        }
    }

    // ----------------------------
    // Sales Reporting (MongoDB)
    // ----------------------------

    /**
     * Generates a sales report for a given date range.
     *
     * @param startDateStr start date in "yyyy-MM-dd" format
     * @param endDateStr   end date in "yyyy-MM-dd" format
     * @return list of SalesData objects
     */
    public static List<SalesData> generateSalesReport(String startDateStr, String endDateStr) {
        MongoCollection<Document> ordersCollection = mongoDatabase.getCollection("orders");
        List<SalesData> salesReport = new ArrayList<>();
    
        // Parse dates
        Date startDate = parseDate(startDateStr);
        Date endDate = parseDate(endDateStr);
    
        if (startDate == null || endDate == null) {
            System.out.println("Invalid date format for sales report.");
            return salesReport;
        }
    
        // Aggregation pipeline
        List<Document> pipeline = List.of(
                new Document("$match", new Document("order_date", new Document("$gte", startDate).append("$lte", endDate))),
                new Document("$group", new Document("_id", "$product_id")
                        .append("total_quantity", new Document("$sum", "$quantity"))
                )
        );
    
        try {
            for (Document doc : ordersCollection.aggregate(pipeline)) {
                int productId = doc.getInteger("_id");
                int totalQuantity = doc.getInteger("total_quantity", 0);
    
                // Fetch product price from MySQL
                Product product = findProductById(productId);
                if (product == null) {
                    System.out.println("Product not found for ID: " + productId);
                    continue;
                }
    
                double totalSales = totalQuantity * product.getPrice();
                String productName = product.getName();
    
                salesReport.add(new SalesData(productId, productName, totalQuantity, totalSales));
            }
        } catch (Exception e) {
            System.out.println("Error generating sales report.");
            e.printStackTrace();
        }
    
        return salesReport;
    }
    

    /**
     * Helper method to parse a date string into a Date object.
     *
     * @param dateStr the date string in "yyyy-MM-dd" format
     * @return Date object if parsing is successful, else null
     */
    private static Date parseDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false); // Strict parsing
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            System.out.println("Date parsing error: " + e.getMessage());
            return null;
        }
    }

    // ----------------------------
    // Helper Methods
    // ----------------------------

    /**
     * Converts a list of PaymentMethod objects to a list of MongoDB Documents.
     *
     * @param paymentMethods list of PaymentMethod objects
     * @return list of Document objects
     */
    private static List<Document> convertPaymentMethods(List<PaymentMethod> paymentMethods) {
        List<Document> paymentDocs = new ArrayList<>();
        for (PaymentMethod pm : paymentMethods) {
            Document pmDoc = new Document("type", pm.getType())
                    .append("details", pm.getDetails());
            paymentDocs.add(pmDoc);
        }
        return paymentDocs;
    }

    /**
     * Converts a list of MongoDB Documents to a list of PaymentMethod objects.
     *
     * @param paymentDocs list of Document objects
     * @return list of PaymentMethod objects
     */
    private static List<PaymentMethod> convertDocumentsToPaymentMethods(List<Document> paymentDocs) {
        List<PaymentMethod> paymentMethods = new ArrayList<>();
        for (Document pmDoc : paymentDocs) {
            String type = pmDoc.getString("type");
            String details = pmDoc.getString("details");
            paymentMethods.add(new PaymentMethod(type, details));
        }
        return paymentMethods;
    }

    /**
     * Checks if a given user_id exists in the MongoDB 'customers' collection.
     *
     * @param userId the user ID to check
     * @return true if the user_id exists, else false
     */
    public static boolean isUserIdInMongoDB(int userId) {
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection("customers");
        Document query = new Document("user_id", userId);
        Document customerDoc = customersCollection.find(query).first();
        return customerDoc != null;
    }

    // ----------------------------
    // Closing Connections
    // ----------------------------

    /**
     * Closes the MongoDB connection.
     */
    public static void closeMongoConnection() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("MongoDB Connection Closed.");
        }
    }
}
