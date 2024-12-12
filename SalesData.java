public class SalesData {
    private int productId;
    private String productName;
    private int totalQuantitySold;
    private double totalSalesAmount;

    public SalesData(int productId, String productName, int totalQuantitySold, double totalSalesAmount) {
        this.productId = productId;
        this.productName = productName;
        this.totalQuantitySold = totalQuantitySold;
        this.totalSalesAmount = totalSalesAmount;
    }

    // Getters and Setters
    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getTotalQuantitySold() { return totalQuantitySold; }
    public double getTotalSalesAmount() { return totalSalesAmount; }

    public void setProductId(int productId) { this.productId = productId; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setTotalQuantitySold(int totalQuantitySold) { this.totalQuantitySold = totalQuantitySold; }
    public void setTotalSalesAmount(double totalSalesAmount) { this.totalSalesAmount = totalSalesAmount; }
}
