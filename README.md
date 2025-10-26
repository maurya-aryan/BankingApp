# Java Banking Application

A robust desktop banking application built with Java Swing and MySQL, providing essential banking operations through a user-friendly graphical interface.

## 🚀 Features

- **Secure Login System**: Multi-user support with account-based authentication
- **Account Management**: 
  - Create new bank accounts
  - View account details and balance
  - Secure password protection
- **Transaction Operations**:
  - Deposit money
  - Withdraw money (with minimum balance protection)
  - View transaction history
- **Real-time Balance Updates**
- **Transaction History**: View last 5 transactions with details
- **User-friendly Interface**: Clean and intuitive GUI with background theming
- **Data Persistence**: MySQL database integration for secure data storage

## 🔧 Technical Requirements

- Java Development Kit (JDK) 8 or higher
- MySQL Server 5.7 or higher
- MySQL Connector/J (JDBC driver)
- Any IDE supporting Java (Eclipse, IntelliJ IDEA, VS Code, etc.)

## 📋 Database Setup

1. Create a MySQL database named `banking_db`
2. Create the following tables:

```sql
CREATE TABLE users (
    account_number VARCHAR(10) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    password VARCHAR(50) NOT NULL,
    balance DECIMAL(10,2) NOT NULL
);

CREATE TABLE transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(10),
    type VARCHAR(20) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    balance DECIMAL(10,2) NOT NULL,
    date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_number) REFERENCES users(account_number)
);
```

## 🚀 Installation & Setup

1. Clone the repository:
```bash
git clone https://github.com/yourusername/banking-application.git
```

2. Update database credentials in `BankingApp.java`:
```java
private static final String URL = "jdbc:mysql://localhost:3306/banking_db";
private static final String USER = "your_username";
private static final String PASSWORD = "your_password";
```

3. Compile the application:
```bash
javac -cp ".;mysql-connector-java-x.x.xx.jar" BankingApp.java
```

4. Run the application:
```bash
java -cp ".;mysql-connector-java-x.x.xx.jar" BankingApp
```

## 💡 Usage

1. **Creating a New Account**:
   - Click on "New User Registration" tab
   - Fill in required details (name, phone, initial deposit, password)
   - Minimum initial deposit: ₹500
   - Password must be 4 digits

2. **Logging In**:
   - Use account number and password
   - Account number is provided upon successful registration

3. **Banking Operations**:
   - Check balance
   - Deposit money
   - Withdraw money (maintaining minimum ₹500 balance)
   - View transaction history
   - Secure logout

## 🔒 Security Features

- Password protection for all accounts
- Minimum balance maintenance
- Transaction logging
- Session management
- Secure database operations

## 🎯 Future Enhancements

- Fund transfer between accounts
- Mobile number verification
- Email notifications for transactions
- Profile picture support
- Enhanced transaction history with filtering
- Password recovery system

## 👥 Contributing

Feel free to fork this project and submit pull requests. You can also open issues for bugs or feature suggestions.

## 📝 License

This project is open source and available under the [MIT License](LICENSE).

## 📧 Contact

For any queries or support, please contact:
- Email: aryan5592ar@gmail.com  
- GitHub:maurya-aryan

---
⭐ Don't forget to star this repository if you found it helpful!
