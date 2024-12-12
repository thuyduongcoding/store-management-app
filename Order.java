import java.util.Date;

public class Order {
    private String orderId;
    private int userId;
    private int productId;
    private int quantity;
    private Date orderDate;
    private String status;

    // Constructor matching the required signature
    public Order(String orderId, int userId, int productId, int quantity, Date orderDate, String status) {
        this.orderId = orderId;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.orderDate = orderDate;
        this.status = status;
    }

    // Getters and Setters
    public String getOrderId() { return orderId; }
    public int getUserId() { return userId; }
    public int getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public Date getOrderDate() { return orderDate; }
    public String getStatus() { return status; }

    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setProductId(int productId) { this.productId = productId; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }
    public void setStatus(String status) { this.status = status; }
}
