import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class CustomerFrame extends JFrame {
    private User user;
    private Customer customer;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextField phoneField;
    private DefaultListModel<String> paymentListModel;
    private JList<String> paymentList;

    private JButton updateProfileButton;
    private JButton addPaymentButton;
    private JButton removePaymentButton;

    // Purchase Tab Components
    private JPanel purchasePanel;
    private JTable productsTable;
    private JButton addToCartButton;
    private JButton placeOrderButton;
    private DefaultListModel<String> cartListModel;
    private JList<String> cartList;
    private JLabel totalLabel;

    // To keep track of cart items
    private List<CartItem> cartItems;

    public CustomerFrame(User user, Customer customer) {
        this.user = user;
        this.customer = customer;
        this.cartItems = new ArrayList<>();

        setTitle("Retail Store - Customer");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centers the window

        // Create Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();

        // Profile Tab
        JPanel profilePanel = createProfilePanel();
        tabbedPane.addTab("Profile", profilePanel);

        // Payment Methods Tab
        JPanel paymentPanel = createPaymentPanel();
        tabbedPane.addTab("Payment Methods", paymentPanel);

        // Order History Tab
        JPanel orderHistoryPanel = createOrderHistoryPanel();
        tabbedPane.addTab("Order History", orderHistoryPanel);

        // Purchase Tab
        purchasePanel = createPurchasePanel();
        tabbedPane.addTab("Purchase", purchasePanel);

        add(tabbedPane);
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // First Name
        JLabel firstNameLabel = new JLabel("First Name:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10,10,10,10);
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(firstNameLabel, gbc);

        firstNameField = new JTextField(customer.getFirstName(), 20);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(firstNameField, gbc);

        // Last Name
        JLabel lastNameLabel = new JLabel("Last Name:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(lastNameLabel, gbc);

        lastNameField = new JTextField(customer.getLastName(), 20);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(lastNameField, gbc);

        // Email
        JLabel emailLabel = new JLabel("Email:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(emailLabel, gbc);

        emailField = new JTextField(customer.getEmail(), 20);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(emailField, gbc);

        // Phone Number
        JLabel phoneLabel = new JLabel("Phone Number:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(phoneLabel, gbc);

        phoneField = new JTextField(customer.getPhoneNumber(), 20);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(phoneField, gbc);

        // Update Profile Button
        updateProfileButton = new JButton("Update Profile");
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(updateProfileButton, gbc);

        // Add Action Listener
        updateProfileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleUpdateProfile();
            }
        });

        return panel;
    }

    private JPanel createPaymentPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Payment Methods List
        paymentListModel = new DefaultListModel<>();
        for (PaymentMethod pm : customer.getPaymentMethods()) {
            paymentListModel.addElement(pm.getType() + ": " + pm.getDetails());
        }
        paymentList = new JList<>(paymentListModel);
        JScrollPane scrollPane = new JScrollPane(paymentList);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Buttons Panel
        JPanel buttonsPanel = new JPanel();
        addPaymentButton = new JButton("Add Payment Method");
        removePaymentButton = new JButton("Remove Selected");
        buttonsPanel.add(addPaymentButton);
        buttonsPanel.add(removePaymentButton);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        // Add Action Listeners
        addPaymentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleAddPaymentMethod();
            }
        });

        removePaymentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRemovePaymentMethod();
            }
        });

        return panel;
    }

    private JPanel createOrderHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JTextArea orderHistoryArea = new JTextArea();
        orderHistoryArea.setEditable(false);

        List<Order> orders = DataStore.getOrdersForUser(user.getUserId());
        if (orders.isEmpty()) {
            orderHistoryArea.setText("You have no past orders.");
        } else {
            StringBuilder sb = new StringBuilder();
            for (Order o : orders) {
                Product p = DataStore.findProductById(o.getProductId());
                String productName = (p != null) ? p.getName() : "Unknown Product";
                sb.append("Order ID: ").append(o.getOrderId())
                  .append(", Product: ").append(productName)
                  .append(", Quantity: ").append(o.getQuantity())
                  .append(", Date: ").append(o.getOrderDate())
                  .append(", Status: ").append(o.getStatus())
                  .append("\n");
            }
            orderHistoryArea.setText(sb.toString());
        }

        JScrollPane scrollPane = new JScrollPane(orderHistoryArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Creates the Purchase Panel where customers can browse and purchase products.
     *
     * @return JPanel for Purchase
     */
    private JPanel createPurchasePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Top Panel for Product Search (Optional)
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel searchLabel = new JLabel("Search Products:");
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");

        topPanel.add(searchLabel);
        topPanel.add(searchField);
        topPanel.add(searchButton);

        panel.add(topPanel, BorderLayout.NORTH);

        // Center Panel for Products Table
        String[] columnNames = {"Product ID", "Name", "Description", "Price", "Stock"};
        productsTable = new JTable(); // We'll set the model later
        JScrollPane tableScrollPane = new JScrollPane(productsTable);
        panel.add(tableScrollPane, BorderLayout.CENTER);

        // Right Panel for Cart
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(250, 0));

        JLabel cartLabel = new JLabel("Your Cart:");
        cartListModel = new DefaultListModel<>();
        cartList = new JList<>(cartListModel);
        JScrollPane cartScrollPane = new JScrollPane(cartList);

        rightPanel.add(cartLabel, BorderLayout.NORTH);
        rightPanel.add(cartScrollPane, BorderLayout.CENTER);

        // Bottom Panel for Cart Actions
        JPanel cartBottomPanel = new JPanel(new BorderLayout());

        totalLabel = new JLabel("Total: $0.00");
        placeOrderButton = new JButton("Place Order");

        cartBottomPanel.add(totalLabel, BorderLayout.WEST);
        cartBottomPanel.add(placeOrderButton, BorderLayout.EAST);

        rightPanel.add(cartBottomPanel, BorderLayout.SOUTH);

        panel.add(rightPanel, BorderLayout.EAST);

        // Bottom Panel for Add to Cart Button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addToCartButton = new JButton("Add to Cart");
        bottomPanel.add(addToCartButton);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        // Load all products initially
        loadProducts("");

        // Add Action Listeners
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String keyword = searchField.getText().trim();
                loadProducts(keyword);
            }
        });

        addToCartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addSelectedProductToCart();
            }
        });

        placeOrderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placeOrder();
            }
        });

        return panel;
    }

    /**
     * Loads products into the products table based on the search keyword.
     *
     * @param keyword the search keyword
     */
    private void loadProducts(String keyword) {
        List<Product> products = DataStore.searchProductsByName(keyword);
        String[] columnNames = {"Product ID", "Name", "Description", "Price", "Stock"};
        Object[][] data = new Object[products.size()][5];
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            data[i][0] = p.getProductId();
            data[i][1] = p.getName();
            data[i][2] = p.getDescription();
            data[i][3] = p.getPrice();
            data[i][4] = p.getStock();
        }
        productsTable.setModel(new javax.swing.table.DefaultTableModel(data, columnNames) {
            // Make cells non-editable
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
    }

    /**
     * Adds the selected product from the table to the cart.
     */
    private void addSelectedProductToCart() {
        int selectedRow = productsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to add to the cart.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Retrieve product details from the table
        int productId = (int) productsTable.getValueAt(selectedRow, 0);
        String name = (String) productsTable.getValueAt(selectedRow, 1);
        String description = (String) productsTable.getValueAt(selectedRow, 2);
        double price = (double) productsTable.getValueAt(selectedRow, 3);
        int stock = (int) productsTable.getValueAt(selectedRow, 4);

        // Prompt user for quantity
        String quantityStr = JOptionPane.showInputDialog(this, "Enter quantity for " + name + ":", "Quantity", JOptionPane.PLAIN_MESSAGE);
        if (quantityStr == null) {
            // User cancelled
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than zero.", "Invalid Quantity", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (quantity > stock) {
                JOptionPane.showMessageDialog(this, "Requested quantity exceeds available stock.", "Insufficient Stock", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid integer for quantity.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Add to cart
        String cartItem = "ID: " + productId + " | " + name + " | Qty: " + quantity + " | $" + price;
        cartListModel.addElement(cartItem);
        cartItems.add(new CartItem(productId, name, quantity, price));
        updateTotal();
    }

    /**
     * Updates the total price label based on cart contents.
     */
    private void updateTotal() {
        double total = 0.0;
        for (CartItem item : cartItems) {
            total += item.getPrice() * item.getQuantity();
        }
        totalLabel.setText(String.format("Total: $%.2f", total));
    }

    /**
     * Places the order based on cart contents.
     */
    private void placeOrder() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Your cart is empty.", "Empty Cart", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Confirm order
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to place this order?", "Confirm Order", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        boolean allOrdersPlaced = true;
        for (CartItem item : cartItems) {
            boolean orderPlaced = DataStore.placeOrder(user.getUserId(), item.getProductId(), item.getQuantity());
            if (!orderPlaced) {
                allOrdersPlaced = false;
            }
        }

        if (allOrdersPlaced) {
            JOptionPane.showMessageDialog(this, "All orders placed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Some orders could not be placed. Please check your cart.", "Partial Success", JOptionPane.WARNING_MESSAGE);
        }

        // Clear cart
        cartListModel.clear();
        cartItems.clear();
        updateTotal();
        loadProducts(""); // Refresh product list to reflect updated stock
        refreshOrderHistory(); // Update order history tab
    }

    /**
     * Refreshes the Order History panel to reflect new orders.
     */
    private void refreshOrderHistory() {
        // Assuming you have a reference to the Order History tab or can recreate it
        // For simplicity, you can refresh the entire tabbed pane
        JTabbedPane tabbedPane = (JTabbedPane) this.getContentPane().getComponent(0);
        int orderHistoryIndex = 2; // Assuming it's the third tab
        JPanel newOrderHistoryPanel = createOrderHistoryPanel();
        tabbedPane.setComponentAt(orderHistoryIndex, newOrderHistoryPanel);
    }

    private void handleUpdateProfile() {
        String firstName = firstNameField.getText().trim();
        String lastName  = lastNameField.getText().trim();
        String email     = emailField.getText().trim();
        String phone     = phoneField.getText().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setEmail(email);
        customer.setPhoneNumber(phone);

        boolean success = DataStore.updateCustomerProfile(customer);
        if (success) {
            JOptionPane.showMessageDialog(this, "Profile updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update profile.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleAddPaymentMethod() {
        JTextField typeField = new JTextField();
        JTextField detailsField = new JTextField();

        Object[] message = {
            "Payment Method Type (e.g., Credit Card, PayPal):", typeField,
            "Payment Details:", detailsField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Add Payment Method", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String type = typeField.getText().trim();
            String details = detailsField.getText().trim();

            if (type.isEmpty() || details.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            PaymentMethod newPayment = new PaymentMethod(type, details);
            customer.getPaymentMethods().add(newPayment);
            paymentListModel.addElement(newPayment.getType() + ": " + newPayment.getDetails());

            // Update in MongoDB
            boolean success = DataStore.updateCustomerPaymentMethods(user.getUserId(), customer.getPaymentMethods());
            if (success) {
                JOptionPane.showMessageDialog(this, "Payment method added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add payment method.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleRemovePaymentMethod() {
        int selectedIndex = paymentList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Please select a payment method to remove.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selectedValue = paymentListModel.getElementAt(selectedIndex);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove: " + selectedValue + "?", "Confirm Removal", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            // Remove from list model and customer object
            paymentListModel.remove(selectedIndex);
            customer.getPaymentMethods().remove(selectedIndex);

            // Update in MongoDB
            boolean success = DataStore.updateCustomerPaymentMethods(user.getUserId(), customer.getPaymentMethods());
            if (success) {
                JOptionPane.showMessageDialog(this, "Payment method removed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to remove payment method.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Inner class to represent items in the cart
    private class CartItem {
        private int productId;
        private String productName;
        private int quantity;
        private double price;

        public CartItem(int productId, String productName, int quantity, double price) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.price = price;
        }

        public int getProductId() { return productId; }
        public String getProductName() { return productName; }
        public int getQuantity() { return quantity; }
        public double getPrice() { return price; }
    }
}
