-- Drop tables if they exist
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS users;

-- Create users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(10) NOT NULL
);

-- Create transactions table
CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    type VARCHAR(20) NOT NULL,
    description VARCHAR(500) NOT NULL,
    counterparty_email VARCHAR(255),
    transfer_group_id VARCHAR(36),
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_transaction_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Insert default ADMIN user
-- Password is 'Admin@123' hashed with BCrypt
INSERT INTO users (name, email, password, role)
VALUES ('Admin', 'admin@gmail.com', '$2a$10$Q7yqzNqY1.F6UoUvI8mR1OuB/jO6PZqZyY/uU2P9n7a40u10S1/pW', 'ADMIN');
