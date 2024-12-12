

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Date;


public class ManagerFrame extends JFrame {
    private User user;
    private JButton addProductButton;
    private JButton updateProductButton;
    private JButton deleteProductButton;
    private JButton viewSalesReportButton;
    private JButton refillInventoryButton;

    public ManagerFrame(User user) {
        this.user = user;
        setTitle("Retail Store - Manager");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centers the window

        // Create panels
        JPanel panel = new JPanel(new GridLayout(5, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 100, 30, 100));

        // Add Buttons
        addProductButton = new JButton("Add New Product");
        updateProductButton = new JButton("Update Existing Product");
        deleteProductButton = new JButton("Delete Product");
        viewSalesReportButton = new JButton("View Sales Reports");
        refillInventoryButton = new JButton("Refill Inventory");

        panel.add(addProductButton);
        panel.add(updateProductButton);
        panel.add(deleteProductButton);
        panel.add(viewSalesReportButton);
        panel.add(refillInventoryButton);

        // Add Action Listeners
        addProductButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleAddProduct();
            }
        });

        updateProductButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleUpdateProduct();
            }
        });

        deleteProductButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleDeleteProduct();
            }
        });

        viewSalesReportButton.addActionListener(new ActionListener() { // Updated listener
            @Override
            public void actionPerformed(ActionEvent e) {
                handleViewSalesReport();
            }
        });

        refillInventoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRefillInventory();
            }
        });

        add(panel);
    }

    private void handleAddProduct() {
        JTextField nameField = new JTextField();
        JTextField descriptionField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField stockField = new JTextField();

        Object[] message = {
            "Product Name:", nameField,
            "Description:", descriptionField,
            "Price:", priceField,
            "Initial Stock:", stockField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Add New Product", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String description = descriptionField.getText().trim();
            String priceStr = priceField.getText().trim();
            String stockStr = stockField.getText().trim();

            if (name.isEmpty() || description.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double price;
            int stock;
            try {
                price = Double.parseDouble(priceStr);
                stock = Integer.parseInt(stockStr);
                if (price < 0 || stock < 0) {
                    JOptionPane.showMessageDialog(this, "Price and Stock cannot be negative.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid price or stock value.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Product newProduct = new Product(0, name, description, price, stock); // Assuming productId is auto-generated
            boolean success = DataStore.addProduct(newProduct);
            if (success) {
                JOptionPane.showMessageDialog(this, "Product added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add product.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleUpdateProduct() {
        String productIdStr = JOptionPane.showInputDialog(this, "Enter Product ID to Update:", "Update Product", JOptionPane.PLAIN_MESSAGE);
        if (productIdStr == null || productIdStr.trim().isEmpty()) {
            return;
        }

        int productId;
        try {
            productId = Integer.parseInt(productIdStr.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Product ID.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Product product = DataStore.findProductById(productId);
        if (product == null) {
            JOptionPane.showMessageDialog(this, "Product not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JTextField nameField = new JTextField(product.getName());
        JTextField descriptionField = new JTextField(product.getDescription());
        JTextField priceField = new JTextField(String.valueOf(product.getPrice()));
        JTextField stockField = new JTextField(String.valueOf(product.getStock()));

        Object[] message = {
            "Product Name:", nameField,
            "Description:", descriptionField,
            "Price:", priceField,
            "Stock:", stockField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Update Product", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String description = descriptionField.getText().trim();
            String priceStr = priceField.getText().trim();
            String stockStr = stockField.getText().trim();

            if (name.isEmpty() || description.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double price;
            int stock;
            try {
                price = Double.parseDouble(priceStr);
                stock = Integer.parseInt(stockStr);
                if (price < 0 || stock < 0) {
                    JOptionPane.showMessageDialog(this, "Price and Stock cannot be negative.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid price or stock value.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);
            product.setStock(stock);

            boolean success = DataStore.updateProduct(product);
            if (success) {
                JOptionPane.showMessageDialog(this, "Product updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update product.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDeleteProduct() {
        String productIdStr = JOptionPane.showInputDialog(this, "Enter Product ID to Delete:", "Delete Product", JOptionPane.PLAIN_MESSAGE);
        if (productIdStr == null || productIdStr.trim().isEmpty()) {
            return;
        }

        int productId;
        try {
            productId = Integer.parseInt(productIdStr.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Product ID.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete Product ID: " + productId + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = DataStore.deleteProduct(productId);
            if (success) {
                JOptionPane.showMessageDialog(this, "Product deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete product.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleViewSalesReport() {
        // Define date format
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false); // Strict parsing

        // Prompt for Start Date
        String startDateStr = JOptionPane.showInputDialog(this, "Enter Start Date (yyyy-MM-dd):", "Start Date", JOptionPane.PLAIN_MESSAGE);
        if (startDateStr == null || startDateStr.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Start Date cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Prompt for End Date
        String endDateStr = JOptionPane.showInputDialog(this, "Enter End Date (yyyy-MM-dd):", "End Date", JOptionPane.PLAIN_MESSAGE);
        if (endDateStr == null || endDateStr.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "End Date cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Parse Dates
        Date startDate;
        Date endDate;
        try {
            startDate = dateFormat.parse(startDateStr.trim());
            endDate = dateFormat.parse(endDateStr.trim());
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use yyyy-MM-dd.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate Date Range
        if (startDate.after(endDate)) {
            JOptionPane.showMessageDialog(this, "Start Date cannot be after End Date.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Retrieve Sales Report
        List<SalesData> salesReports = DataStore.generateSalesReport(startDateStr, endDateStr);
        if (salesReports.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No sales data found for the given period.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Display Sales Report in JTable
        displaySalesReport(salesReports, startDateStr, endDateStr);
    }

    private void displaySalesReport(List<SalesData> salesReports, String startDate, String endDate) {
        // Define column names
        String[] columnNames = {"Product Name", "Total Quantity Sold", "Total Sales Amount"};

        // Create table model
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        for (SalesData report : salesReports) {
            Object[] rowData = {
                    report.getProductName(),
                    report.getTotalQuantitySold(),
                    String.format("$%.2f", report.getTotalSalesAmount())
            };
            tableModel.addRow(rowData);
        }

        // Create JTable
        JTable table = new JTable(tableModel);
        table.setEnabled(false); // Make table read-only
        table.setFillsViewportHeight(true);

        // Create scroll pane
        JScrollPane scrollPane = new JScrollPane(table);

        // Create a panel to hold the table and title
        JPanel panel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Sales Report from " + startDate + " to " + endDate, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Display in a dialog
        JOptionPane.showMessageDialog(this, panel, "Sales Report", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleRefillInventory() {
        // Step 1: Fetch all products
        List<Product> products = DataStore.getAllProducts();
        if (products.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No products available in the inventory.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Step 2: Display products in a JTable within a scroll pane
        String[] columnNames = {"Product ID", "Name", "Description", "Price", "Current Stock"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        for (Product p : products) {
            Object[] rowData = {
                    p.getProductId(),
                    p.getName(),
                    p.getDescription(),
                    String.format("$%.2f", p.getPrice()),
                    p.getStock()
            };
            tableModel.addRow(rowData);
        }

        JTable table = new JTable(tableModel);
        table.setEnabled(false); // Make table read-only
        table.setFillsViewportHeight(true);
        table.setPreferredScrollableViewportSize(new Dimension(550, 200));

        JScrollPane scrollPane = new JScrollPane(table);

        // Step 3: Create a panel to hold the table and instructions
        JPanel panel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Current Inventory", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Step 4: Show the products to the manager
        JOptionPane.showMessageDialog(this, panel, "Inventory", JOptionPane.INFORMATION_MESSAGE);

        // Step 5: Prompt for Product ID
        String productIdStr = JOptionPane.showInputDialog(this, "Enter Product ID to Refill:", "Refill Inventory", JOptionPane.PLAIN_MESSAGE);
        if (productIdStr == null || productIdStr.trim().isEmpty()) {
            // User cancelled or entered empty input
            return;
        }

        int productId;
        try {
            productId = Integer.parseInt(productIdStr.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Product ID. Please enter a numeric value.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Step 6: Validate Product ID
        Product product = DataStore.findProductById(productId);
        if (product == null) {
            JOptionPane.showMessageDialog(this, "Product with ID " + productId + " does not exist.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Step 7: Prompt for Refill Quantity
        String quantityStr = JOptionPane.showInputDialog(this, "Enter Quantity to Refill for '" + product.getName() + "':", "Refill Quantity", JOptionPane.PLAIN_MESSAGE);
        if (quantityStr == null || quantityStr.trim().isEmpty()) {
            // User cancelled or entered empty input
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr.trim());
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be a positive integer.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid quantity. Please enter a numeric value.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Step 8: Create Refill Order
        boolean success = DataStore.createRefillOrder(productId, quantity);
        if (success) {
            JOptionPane.showMessageDialog(this, "Refill order created successfully for '" + product.getName() + "'.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to create refill order.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
