public class PaymentMethod {
    private String type; // e.g., "Credit Card", "PayPal"
    private String details; // e.g., card number (masked), PayPal email

    // Constructor
    public PaymentMethod(String type, String details) {
        this.type = type;
        this.details = details;
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
