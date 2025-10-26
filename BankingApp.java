import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import javax.swing.*;

// ============================================
// MAIN APPLICATION CLASS
// ============================================
public class BankingApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginWindow();
        });
    }
}

// ============================================
// USER MODEL CLASS (POJO)
// ============================================
class User {
    private String accountNumber;
    private String name;
    private String phone;
    private String password;
    private double balance;

    public User() {}

    public User(String accountNumber, String name, String phone, String password, double balance) {
        this.accountNumber = accountNumber;
        this.name = name;
        this.phone = phone;
        this.password = password;
        this.balance = balance;
    }

    // Getters and Setters
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
}

// ============================================
// DATABASE HELPER CLASS
// ============================================
class DatabaseHelper {
    private static final String URL = "jdbc:mysql://localhost:3306/banking_db";
    private static final String USER = "root";
    private static final String PASSWORD = "vbnm0987"; // UPDATE THIS
    
    // Get database connection
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    
    // Create new user account
    public static String createUser(String name, String phone, String password, double initialDeposit) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            
            // Generate new account number
            String accountNumber = "10001"; // Default starting number
            String query = "SELECT MAX(CAST(account_number AS UNSIGNED)) as max_acc FROM users";
            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();
            
            if (rs.next() && rs.getString("max_acc") != null) {
                int maxAcc = Integer.parseInt(rs.getString("max_acc"));
                accountNumber = String.valueOf(maxAcc + 1);
            }
            rs.close();
            pstmt.close();
            
            // Insert new user
            query = "INSERT INTO users (account_number, name, phone, password, balance) VALUES (?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, accountNumber);
            pstmt.setString(2, name);
            pstmt.setString(3, phone);
            pstmt.setString(4, password);
            pstmt.setDouble(5, initialDeposit);
            pstmt.executeUpdate();
            pstmt.close();
            
            // Add initial transaction
            addTransaction(accountNumber, "ACCOUNT_CREATED", initialDeposit, initialDeposit);
            
            return accountNumber;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    // Validate login credentials
    public static boolean validateLogin(String accountNumber, String password) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            String query = "SELECT * FROM users WHERE account_number = ? AND password = ?";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, accountNumber);
            pstmt.setString(2, password);
            rs = pstmt.executeQuery();
            
            return rs.next();
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    // Get user details
    public static User getUserDetails(String accountNumber) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            String query = "SELECT * FROM users WHERE account_number = ?";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, accountNumber);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                User user = new User();
                user.setAccountNumber(rs.getString("account_number"));
                user.setName(rs.getString("name"));
                user.setPhone(rs.getString("phone"));
                user.setPassword(rs.getString("password"));
                user.setBalance(rs.getDouble("balance"));
                return user;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    // Update user balance
    public static boolean updateBalance(String accountNumber, double newBalance) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = getConnection();
            String query = "UPDATE users SET balance = ? WHERE account_number = ?";
            pstmt = conn.prepareStatement(query);
            pstmt.setDouble(1, newBalance);
            pstmt.setString(2, accountNumber);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    // Add transaction record
    public static boolean addTransaction(String accountNumber, String type, double amount, double balanceAfter) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = getConnection();
            String query = "INSERT INTO transactions (account_number, type, amount, balance) VALUES (?, ?, ?, ?)";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, accountNumber);
            pstmt.setString(2, type);
            pstmt.setDouble(3, amount);
            pstmt.setDouble(4, balanceAfter);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    // Get recent transactions
    public static String getTransactions(String accountNumber, int limit) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        StringBuilder transactions = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        
        try {
            conn = getConnection();
            String query = "SELECT * FROM transactions WHERE account_number = ? ORDER BY date DESC LIMIT ?";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, accountNumber);
            pstmt.setInt(2, limit);
            rs = pstmt.executeQuery();
            
            transactions.append(String.format("%-20s %-20s %-15s %-15s\n", "Date", "Type", "Amount", "Balance"));
            transactions.append("========================================================================\n");
            
            while (rs.next()) {
                String date = sdf.format(rs.getTimestamp("date"));
                String type = rs.getString("type");
                double amount = rs.getDouble("amount");
                double balance = rs.getDouble("balance");
                
                transactions.append(String.format("%-20s %-20s ₹%-14.2f ₹%-14.2f\n", 
                    date, type, amount, balance));
            }
            
            if (transactions.toString().split("\n").length <= 2) {
                return "No transactions yet.";
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error fetching transactions.";
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return transactions.toString();
    }
}

// ============================================
// BACKGROUND PANEL CLASS (for full-window background)
// ============================================
class BackgroundPanel extends JPanel {
    private Image backgroundImage;
    
    // Constructor that loads the background image
    public BackgroundPanel(String imagePath) {
        try {
            // Load the background image
            backgroundImage = new ImageIcon(imagePath).getImage();
            System.out.println("Background image loaded: " + imagePath);
        } catch (Exception e) {
            System.out.println("Background image not found: " + imagePath);
            System.out.println("Using default gradient background");
            backgroundImage = null;
        }
    }
    
    // Override paintComponent to draw the background image
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (backgroundImage != null) {
            // Draw image stretched to fill the entire panel (full window)
            // getWidth() and getHeight() automatically adjust to window size
            g.drawImage(backgroundImage, 0, 22, getWidth(), getHeight(), this);
        } else {
            // If no image, use a nice gradient background
            Graphics2D g2d = (Graphics2D) g;
            GradientPaint gradient = new GradientPaint(
                0, 0, new Color(41, 128, 185),  // Top color (blue)
                0, getHeight(), new Color(109, 213, 250)  // Bottom color (light blue)
            );
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}

// ============================================
// LOGIN WINDOW
// ============================================
class LoginWindow extends JFrame {
    private JTabbedPane tabbedPane;
    
    // Login components
    private JTextField loginAccountField;
    private JPasswordField loginPasswordField;
    private JButton loginButton;
    private JLabel loginStatusLabel;
    
    // Registration components
    private JTextField regNameField;
    private JTextField regPhoneField;
    private JTextField regDepositField;
    private JPasswordField regPasswordField;
    private JButton createAccountButton;
    private JLabel regStatusLabel;
    
    public LoginWindow() {
        setTitle("Online Banking Portal - Login");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initComponents();
        
        setVisible(true);
    }
    
    private void initComponents() {
        // Create background panel with image
        // Make sure "background.jpg" is in the same folder as BankingApp.java
        BackgroundPanel backgroundPanel = new BackgroundPanel("background.jpg");
        backgroundPanel.setLayout(new BorderLayout());
        
        // Set UIManager properties for transparent tabbed pane BEFORE creating it
        UIManager.put("TabbedPane.contentOpaque", Boolean.FALSE);
        UIManager.put("TabbedPane.opaque", Boolean.FALSE);
        UIManager.put("TabbedPane.tabsOpaque", Boolean.FALSE);
        
        tabbedPane = new JTabbedPane();
        
        // Make tabs completely transparent so background shows through
        tabbedPane.setOpaque(false);
        tabbedPane.setBackground(new Color(0, 0, 0, 0)); // Fully transparent
        
        // Create login panel
        JPanel loginPanel = createLoginPanel();
        loginPanel.setOpaque(false); // Make transparent
        tabbedPane.addTab("Existing User Login", loginPanel);
        
        // Create registration panel
        JPanel regPanel = createRegistrationPanel();
        regPanel.setOpaque(false); // Make transparent
        tabbedPane.addTab("New User Registration", regPanel);
        
        // Add tabs to background panel
        backgroundPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // Set background panel as the main content
        setContentPane(backgroundPanel);
    }
    
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false); // Transparent
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Title
        JLabel titleLabel = new JLabel("Login to Your Account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE); // White text for visibility
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        
        gbc.gridwidth = 1;
        
        // Account Number
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel accLabel = new JLabel("Account Number:");
        accLabel.setForeground(Color.WHITE);
        panel.add(accLabel, gbc);
        
        loginAccountField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(loginAccountField, gbc);
        
        // Password
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(Color.WHITE);
        panel.add(passLabel, gbc);
        
        loginPasswordField = new JPasswordField(20);
        gbc.gridx = 1;
        panel.add(loginPasswordField, gbc);
        
        // Login Button
        loginButton = new JButton("Login");
        loginButton.addActionListener(e -> handleLogin());
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(loginButton, gbc);
        
        // Status Label
        loginStatusLabel = new JLabel(" ");
        loginStatusLabel.setForeground(Color.YELLOW);
        loginStatusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        gbc.gridy = 4;
        panel.add(loginStatusLabel, gbc);
        
        return panel;
    }
    
    private JPanel createRegistrationPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false); // Transparent
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Title
        JLabel titleLabel = new JLabel("Create New Account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        
        gbc.gridwidth = 1;
        
        // Full Name
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel nameLabel = new JLabel("Full Name:");
        nameLabel.setForeground(Color.WHITE);
        panel.add(nameLabel, gbc);
        
        regNameField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(regNameField, gbc);
        
        // Phone Number
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel phoneLabel = new JLabel("Phone Number:");
        phoneLabel.setForeground(Color.WHITE);
        panel.add(phoneLabel, gbc);
        
        regPhoneField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(regPhoneField, gbc);
        
        // Initial Deposit
        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel depositLabel = new JLabel("Initial Deposit (Min ₹500):");
        depositLabel.setForeground(Color.WHITE);
        panel.add(depositLabel, gbc);
        
        regDepositField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(regDepositField, gbc);
        
        // Password
        gbc.gridx = 0;
        gbc.gridy = 4;
        JLabel passwordLabel = new JLabel("Create Password (4 digits):");
        passwordLabel.setForeground(Color.WHITE);
        panel.add(passwordLabel, gbc);
        
        regPasswordField = new JPasswordField(20);
        gbc.gridx = 1;
        panel.add(regPasswordField, gbc);
        
        // Create Account Button
        createAccountButton = new JButton("Create Account");
        createAccountButton.addActionListener(e -> handleRegistration());
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        panel.add(createAccountButton, gbc);
        
        // Status Label
        regStatusLabel = new JLabel(" ");
        regStatusLabel.setForeground(Color.YELLOW);
        regStatusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        gbc.gridy = 6;
        panel.add(regStatusLabel, gbc);
        
        return panel;
    }
    
    private void handleLogin() {
        String accountNumber = loginAccountField.getText().trim();
        String password = new String(loginPasswordField.getPassword());
        
        if (accountNumber.isEmpty() || password.isEmpty()) {
            loginStatusLabel.setText("Please fill all fields");
            return;
        }
        
        try {
            if (DatabaseHelper.validateLogin(accountNumber, password)) {
                User user = DatabaseHelper.getUserDetails(accountNumber);
                if (user != null) {
                    // Open banking window
                    new BankingWindow(user);
                    dispose(); // Close login window
                }
            } else {
                loginStatusLabel.setText("Invalid account number or password");
            }
        } catch (Exception e) {
            loginStatusLabel.setText("Database connection error");
            e.printStackTrace();
        }
    }
    
    private void handleRegistration() {
        String name = regNameField.getText().trim();
        String phone = regPhoneField.getText().trim();
        String depositStr = regDepositField.getText().trim();
        String password = new String(regPasswordField.getPassword());
        
        // Validation
        if (name.isEmpty() || phone.isEmpty() || depositStr.isEmpty() || password.isEmpty()) {
            regStatusLabel.setText("Please fill all fields");
            regStatusLabel.setForeground(Color.YELLOW);
            return;
        }
        
        if (phone.length() != 10) {
            regStatusLabel.setText("Phone number must be 10 digits");
            regStatusLabel.setForeground(Color.YELLOW);
            return;
        }
        
        if (password.length() != 4 || !password.matches("\\d+")) {
            regStatusLabel.setText("Password must be exactly 4 digits");
            regStatusLabel.setForeground(Color.YELLOW);
            return;
        }
        
        double deposit;
        try {
            deposit = Double.parseDouble(depositStr);
            if (deposit < 500) {
                regStatusLabel.setText("Minimum deposit is ₹500");
                regStatusLabel.setForeground(Color.YELLOW);
                return;
            }
        } catch (NumberFormatException e) {
            regStatusLabel.setText("Invalid deposit amount");
            regStatusLabel.setForeground(Color.YELLOW);
            return;
        }
        
        try {
            String accountNumber = DatabaseHelper.createUser(name, phone, password, deposit);
            if (accountNumber != null) {
                regStatusLabel.setText("Account created! Your Account Number: " + accountNumber);
                regStatusLabel.setForeground(Color.GREEN);
                
                // Clear fields
                regNameField.setText("");
                regPhoneField.setText("");
                regDepositField.setText("");
                regPasswordField.setText("");
                
                JOptionPane.showMessageDialog(this, 
                    "Account created successfully!\nYour Account Number: " + accountNumber + "\nPlease note it down.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                regStatusLabel.setText("Error creating account");
                regStatusLabel.setForeground(Color.YELLOW);
            }
        } catch (Exception e) {
            regStatusLabel.setText("Database connection error");
            regStatusLabel.setForeground(Color.YELLOW);
            e.printStackTrace();
        }
    }
}

// ============================================
// BANKING WINDOW
// ============================================
class BankingWindow extends JFrame {
    private User user;
    
    // Display labels
    private JLabel welcomeLabel;
    private JLabel accountLabel;
    private JLabel balanceLabel;
    
    // Operation buttons
    private JButton checkBalanceBtn;
    private JButton depositBtn;
    private JButton withdrawBtn;
    private JButton transactionsBtn;
    private JButton logoutBtn;
    
    // Action panel components
    private JPanel actionPanel;
    private JTextField amountField;
    private JButton submitBtn;
    private JButton cancelBtn;
    private String currentOperation = "";
    
    public BankingWindow(User user) {
        this.user = user;
        
        setTitle("Online Banking Portal - Dashboard");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initComponents();
        refreshBalance();
        
        setVisible(true);
    }
    
    private void initComponents() {
        // Create background panel with image
        BackgroundPanel backgroundPanel = new BackgroundPanel("background.jpg");
        backgroundPanel.setLayout(new BorderLayout(10, 10));
        
        // Top panel - Display information
        JPanel topPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        topPanel.setOpaque(false); // Transparent
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 60, 10, 20));
        
        welcomeLabel = new JLabel("Welcome, " + user.getName());
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        welcomeLabel.setForeground(Color.WHITE);
        
        accountLabel = new JLabel("Account No: " + user.getAccountNumber());
        accountLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        accountLabel.setForeground(Color.WHITE);
        
        balanceLabel = new JLabel("Current Balance: ₹" + String.format("%.2f", user.getBalance()));
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 18));
        balanceLabel.setForeground(Color.GREEN);
        
        topPanel.add(welcomeLabel);
        topPanel.add(accountLabel);
        topPanel.add(balanceLabel);
        
        backgroundPanel.add(topPanel, BorderLayout.NORTH);
        
        // Center panel - Operation buttons
        JPanel centerPanel = new JPanel(new GridLayout(5,1 ,10,10));
        centerPanel.setOpaque(false); // Transparent
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 100, 10, 100));
        
        // Create buttons without icons
        checkBalanceBtn = new JButton("Check Balance");
        checkBalanceBtn.addActionListener(e -> handleCheckBalance());
        
        depositBtn = new JButton("Deposit Money");
        depositBtn.addActionListener(e -> showActionPanel("DEPOSIT"));
        
        withdrawBtn = new JButton("Withdraw Money");
        withdrawBtn.addActionListener(e -> showActionPanel("WITHDRAW"));
        
        transactionsBtn = new JButton("View Transactions");
        transactionsBtn.addActionListener(e -> handleViewTransactions());
        
        logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> handleLogout());
        
        centerPanel.add(checkBalanceBtn);
        centerPanel.add(depositBtn);
        centerPanel.add(withdrawBtn);
        centerPanel.add(transactionsBtn);
        centerPanel.add(logoutBtn);
        
        backgroundPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel - Action panel (initially hidden)
        actionPanel = new JPanel(new FlowLayout());
        actionPanel.setOpaque(false); // Transparent
        actionPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        actionPanel.setVisible(false);
        
        JLabel amountLabel = new JLabel("Amount: ₹");
        amountLabel.setForeground(Color.BLUE);
        amountLabel.setFont(new Font("Arial", Font.BOLD, 14));
        actionPanel.add(amountLabel);
        
        amountField = new JTextField(15);
        actionPanel.add(amountField);
        
        submitBtn = new JButton("Submit");
        submitBtn.addActionListener(e -> handleSubmit());
        actionPanel.add(submitBtn);
        
        cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> hideActionPanel());
        actionPanel.add(cancelBtn);
        
        backgroundPanel.add(actionPanel, BorderLayout.SOUTH);
        
        // Set background panel as main content
        setContentPane(backgroundPanel);
    }
    
    private void refreshBalance() {
        User updatedUser = DatabaseHelper.getUserDetails(user.getAccountNumber());
        if (updatedUser != null) {
            user.setBalance(updatedUser.getBalance());
            balanceLabel.setText("Current Balance: ₹" + String.format("%.2f", user.getBalance()));
        }
    }
    
    private void handleCheckBalance() {
        refreshBalance();
        JOptionPane.showMessageDialog(this,
            "Account Number: " + user.getAccountNumber() + "\n" +
            "Account Holder: " + user.getName() + "\n" +
            "Current Balance: ₹" + String.format("%.2f", user.getBalance()),
            "Balance Inquiry",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showActionPanel(String operation) {
        currentOperation = operation;
        amountField.setText("");
        actionPanel.setVisible(true);
        amountField.requestFocus();
    }
    
    private void hideActionPanel() {
        actionPanel.setVisible(false);
        currentOperation = "";
        amountField.setText("");
    }
    
    private void handleSubmit() {
        String amountStr = amountField.getText().trim();
        
        if (amountStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an amount", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Amount must be positive", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid amount", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (currentOperation.equals("DEPOSIT")) {
            handleDeposit(amount);
        } else if (currentOperation.equals("WITHDRAW")) {
            handleWithdraw(amount);
        }
    }
    
    private void handleDeposit(double amount) {
        double newBalance = user.getBalance() + amount;
        
        if (DatabaseHelper.updateBalance(user.getAccountNumber(), newBalance)) {
            DatabaseHelper.addTransaction(user.getAccountNumber(), "DEPOSIT", amount, newBalance);
            refreshBalance();
            hideActionPanel();
            JOptionPane.showMessageDialog(this,
                "Deposit successful!\n" +
                "Amount deposited: ₹" + String.format("%.2f", amount) + "\n" +
                "New balance: ₹" + String.format("%.2f", newBalance),
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Deposit failed. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void handleWithdraw(double amount) {
        if (amount > user.getBalance()) {
            JOptionPane.showMessageDialog(this, "Insufficient balance!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        double newBalance = user.getBalance() - amount;
        
        if (newBalance < 500) {
            JOptionPane.showMessageDialog(this,
                "Insufficient balance. Maintain minimum ₹500\n" +
                "Maximum withdrawal allowed: ₹" + String.format("%.2f", user.getBalance() - 500),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (DatabaseHelper.updateBalance(user.getAccountNumber(), newBalance)) {
            DatabaseHelper.addTransaction(user.getAccountNumber(), "WITHDRAW", amount, newBalance);
            refreshBalance();
            hideActionPanel();
            JOptionPane.showMessageDialog(this,
                "Withdrawal successful!\n" +
                "Amount withdrawn: ₹" + String.format("%.2f", amount) + "\n" +
                "New balance: ₹" + String.format("%.2f", newBalance),
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Withdrawal failed. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void handleViewTransactions() {
        String transactions = DatabaseHelper.getTransactions(user.getAccountNumber(), 5);
        
        JTextArea textArea = new JTextArea(transactions);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 300));
        
        JOptionPane.showMessageDialog(this,
            scrollPane,
            "Transaction History (Last 5 Transactions)",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginWindow();
        }
    }
}