

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class CashierFrame extends JFrame {
    private User user;
    private JTextField searchField;
    private JButton searchButton;
    private JList<String> productList;
    private DefaultListModel<String> listModel;
    private JButton updateInventoryButton;
    private JButton placeOrderButton;

    public CashierFrame(User user) {
        this.user = user;
        setTitle("Retail Store - Cashier");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centers the window

        // Create panels
        JPanel panel = new JPanel(new BorderLayout());

        // Top Panel for Search
        JPanel topPanel = new JPanel();
        searchField = new JTextField(20);
        searchButton = new JButton("Search Products");
        topPanel.add(new JLabel("Search Products: "));
        topPanel.add(searchField);
        topPanel.add(searchButton);

        // Center Panel for Product List
        listModel = new DefaultListModel<>();
        productList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(productList);

        // Bottom Panel for Actions
        JPanel bottomPanel = new JPanel();
        updateInventoryButton = new JButton("Update Inventory");
        placeOrderButton = new JButton("Place Order for Customer");
        bottomPanel.add(updateInventoryButton);
        bottomPanel.add(placeOrderButton);

        // Add panels to main panel
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        // Add Action Listeners
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSearch();
            }
        });

        updateInventoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleUpdateInventory();
            }
        });

        placeOrderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handlePlaceOrder();
            }
        });

        add(panel);
    }

    private void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a search keyword.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Product> results = DataStore.searchProductsByName(keyword);
        listModel.clear();
        for (Product p : results) {
            listModel.addElement("ID: " + p.getProductId() + " | " + p.getName() + " | $" + p.getPrice() + " | Stock: " + p.getStock());
        }

        if (results.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No products found with keyword: " + keyword, "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleUpdateInventory() {
        int selectedIndex = productList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to update.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selectedValue = listModel.getElementAt(selectedIndex);
        // Extract product ID from the selected value
        String[] parts = selectedValue.split("\\|");
        if (parts.length < 1) {
            JOptionPane.showMessageDialog(this, "Invalid product selection.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String idPart = parts[0].trim(); // "ID: X"
        int productId;
        try {
            productId = Integer.parseInt(idPart.split(":")[1].trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid product ID.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String newStockStr = JOptionPane.showInputDialog(this, "Enter new stock quantity:", "Update Stock", JOptionPane.PLAIN_MESSAGE);
        if (newStockStr == null) { // User canceled
            return;
        }

        int newStock;
        try {
            newStock = Integer.parseInt(newStockStr);
            if (newStock < 0) {
                JOptionPane.showMessageDialog(this, "Stock cannot be negative.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid stock quantity.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Product product = DataStore.findProductById(productId);
        if (product != null) {
            product.setStock(newStock);
            boolean success = DataStore.updateProduct(product);
            if (success) {
                JOptionPane.showMessageDialog(this, "Product stock updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                handleSearch(); // Refresh the product list
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update product stock.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Product not found.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handlePlaceOrder() {
        int selectedIndex = productList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to order.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selectedValue = listModel.getElementAt(selectedIndex);
        // Extract product ID from the selected value
        String[] parts = selectedValue.split("\\|");
        if (parts.length < 1) {
            JOptionPane.showMessageDialog(this, "Invalid product selection.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String idPart = parts[0].trim(); // "ID: X"
        int productId;
        try {
            productId = Integer.parseInt(idPart.split(":")[1].trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid product ID.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Prompt for Customer Username
        String customerIdStr = JOptionPane.showInputDialog(this, "Enter Customer UserID:", "Customer", JOptionPane.PLAIN_MESSAGE);
        if (customerIdStr == null || customerIdStr.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Customer user_id cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int customerIdInt = Integer.parseInt(customerIdStr.trim());
        // Verify Customer Exists in MongoDB
        boolean isCustomer = DataStore.isUserIdInMongoDB(customerIdInt);
        if (!isCustomer) {
            JOptionPane.showMessageDialog(this, "Customer profile not found in MongoDB.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Prompt for Quantity
        String quantityStr = JOptionPane.showInputDialog(this, "Enter quantity:", "Order Quantity", JOptionPane.PLAIN_MESSAGE);
        if (quantityStr == null) { // User canceled
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be positive.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid quantity.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Place Order
        boolean order = DataStore.placeOrder(customerIdInt, productId, quantity);
        if (order) {
            JOptionPane.showMessageDialog(this, "Order placed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            handleSearch(); // Refresh the product list
        } else {
            JOptionPane.showMessageDialog(this, "Failed to place order. Check product availability.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
