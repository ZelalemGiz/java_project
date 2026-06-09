DROP DATABASE IF EXISTS pharmacydb2;
CREATE DATABASE pharmacydb2;
USE pharmacydb2;

-- Pharmacists table
CREATE TABLE pharmacists (
    id INT AUTO_INCREMENT PRIMARY KEY,
    photo LONGBLOB NULL,
    idNumber VARCHAR(100) NOT NULL,
    firstName VARCHAR(100) NOT NULL,
    middleName VARCHAR(100) NULL,
    lastName VARCHAR(100) NOT NULL,
    gender VARCHAR(20) NULL,
    dateofbirth DATE NULL,
    nationality VARCHAR(100) NULL,
    education_level VARCHAR(100) NULL,
    salary DOUBLE NULL DEFAULT 0,
    phoneNumber VARCHAR(30) NULL,
    email VARCHAR(100) NULL,
    address VARCHAR(255) NULL,
    branch_name VARCHAR(100) NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'PHARMACIST',
    security_question VARCHAR(200) NULL,
    security_answer VARCHAR(200) NULL,
    employing_date DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Users table
CREATE TABLE users (
    id INT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    FOREIGN KEY (id) REFERENCES pharmacists(id) ON DELETE CASCADE
);

-- Medicines table
CREATE TABLE medicines (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50),
    cost_price DOUBLE NOT NULL,
    price DOUBLE NOT NULL,
    profit DOUBLE NOT NULL,
    stock INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    add_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    expiry_date DATE NOT NULL,
    branch_name VARCHAR(100) NULL
);

-- Suppliers table (needed for FK reference)
CREATE TABLE suppliers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    contact VARCHAR(50),
    address VARCHAR(200)
);

-- Sales table
CREATE TABLE sales (
    id INT AUTO_INCREMENT PRIMARY KEY,
    buyer_name VARCHAR(30) NOT NULL,
    buyer_phone VARCHAR(15) NOT NULL,
    medicine_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DOUBLE NOT NULL,
    discount DOUBLE NOT NULL,
    total_price DOUBLE NOT NULL,
    sale_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    total_amount DOUBLE NOT NULL,
    branch_name VARCHAR(100) NULL,
    branch_number VARCHAR(20) NULL,
    seller_id INT NULL,
    FOREIGN KEY (seller_id) REFERENCES pharmacists(id),
    FOREIGN KEY (medicine_id) REFERENCES medicines(id)
);

-- Sale_items table
CREATE TABLE sale_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sale_id INT,
    medicine_id INT,
    quantity INT NOT NULL,
    unit_price DOUBLE NOT NULL,
    discount DOUBLE NOT NULL,
    total_price DOUBLE NOT NULL,
    sale_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE CASCADE,
    FOREIGN KEY (medicine_id) REFERENCES medicines(id)
);

-- Purchase orders table
CREATE TABLE purchase_orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    supplier_id INT NOT NULL,
    medicine_id INT NOT NULL,
    quantity INT NOT NULL,
    cost_price DOUBLE NOT NULL,
    order_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'COMPLETED',
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
    FOREIGN KEY (medicine_id) REFERENCES medicines(id)
);

-- Medicine batches table
CREATE TABLE medicine_batches (
    id INT AUTO_INCREMENT PRIMARY KEY,
    medicine_id INT NOT NULL,
    batch_number VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    expiry_date DATE NOT NULL,
    date_received DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (medicine_id) REFERENCES medicines(id)
);

-- Companies table
CREATE TABLE companies (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    license_number VARCHAR(50),
    logo VARCHAR(255),
    email VARCHAR(100),
    phone VARCHAR(30),
    website VARCHAR(100)
);

INSERT INTO companies (name) VALUES ('My Pharmacy');

-- Branches table
CREATE TABLE IF NOT EXISTS branches (
    id INT AUTO_INCREMENT PRIMARY KEY,
    company_id INT NOT NULL,
    branch_name VARCHAR(100) NOT NULL,
    branch_number VARCHAR(20) NOT NULL,
    address VARCHAR(255),
    phone VARCHAR(30),
    manager VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (company_id) REFERENCES companies(id)
);

INSERT INTO branches (company_id, branch_name, branch_number) 
VALUES (1, 'Main Branch', 'BR-001');

-- Messages table
CREATE TABLE IF NOT EXISTS messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    sent_date DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Insert default admin
INSERT INTO pharmacists (idNumber, firstName, middleName, lastName, gender, 
dateofbirth, nationality, education_level, salary, phoneNumber, email, address, password, role) 
VALUES ('001', 'Zelalem', 'Gizachew', 'Nigus', 'Male', '2000-01-01', 'Ethiopian', 'Bachelor', 0, 'N/A', 'admin@pharmacy.com', 'N/A', '1234', 'ADMIN');

INSERT INTO users (id, username, password, role) 
VALUES (LAST_INSERT_ID(), 'admin', '1234', 'ADMIN');