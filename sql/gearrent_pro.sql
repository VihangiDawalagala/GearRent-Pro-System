-- ============================================================
--  GearRent Pro – Database Schema & Sample Data
-- ============================================================
CREATE DATABASE IF NOT EXISTS gearrent_pro CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE gearrent_pro;

-- ─── Branches ──────────────────────────────────────────────
CREATE TABLE branches (
    branch_id   INT AUTO_INCREMENT PRIMARY KEY,
    branch_code VARCHAR(10)  NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    address     VARCHAR(255),
    contact     VARCHAR(20),
    is_active   BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ─── Equipment Categories ───────────────────────────────────
CREATE TABLE equipment_categories (
    category_id           INT AUTO_INCREMENT PRIMARY KEY,
    name                  VARCHAR(100) NOT NULL,
    description           TEXT,
    base_price_factor     DECIMAL(5,2) DEFAULT 1.00,
    weekend_multiplier    DECIMAL(5,2) DEFAULT 1.00,
    default_late_fee      DECIMAL(10,2) DEFAULT 500.00,
    is_active             BOOLEAN DEFAULT TRUE
);

-- ─── Equipment ─────────────────────────────────────────────
CREATE TABLE equipment (
    equipment_id     VARCHAR(20) PRIMARY KEY,
    category_id      INT NOT NULL,
    brand            VARCHAR(100),
    model            VARCHAR(100),
    purchase_year    INT,
    daily_base_price DECIMAL(10,2) NOT NULL,
    security_deposit DECIMAL(10,2) NOT NULL,
    status           ENUM('Available','Reserved','Rented','Under Maintenance') DEFAULT 'Available',
    branch_id        INT NOT NULL,
    FOREIGN KEY (category_id) REFERENCES equipment_categories(category_id),
    FOREIGN KEY (branch_id)   REFERENCES branches(branch_id)
);

-- ─── Membership Configuration ──────────────────────────────
CREATE TABLE membership_configs (
    level               ENUM('Regular','Silver','Gold') PRIMARY KEY,
    discount_percentage DECIMAL(5,2) DEFAULT 0.00,
    deposit_limit       DECIMAL(12,2) DEFAULT 500000.00
);

-- ─── Customers ─────────────────────────────────────────────
CREATE TABLE customers (
    customer_id      INT AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(100) NOT NULL,
    nic_passport     VARCHAR(20)  NOT NULL UNIQUE,
    contact_no       VARCHAR(20),
    email            VARCHAR(100),
    address          VARCHAR(255),
    membership_level ENUM('Regular','Silver','Gold') DEFAULT 'Regular',
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ─── System Users ──────────────────────────────────────────
CREATE TABLE system_users (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(100),
    role          ENUM('Admin','Branch Manager','Staff') NOT NULL,
    branch_id     INT,
    is_active     BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (branch_id) REFERENCES branches(branch_id)
);

-- ─── Reservations ──────────────────────────────────────────
CREATE TABLE reservations (
    reservation_id INT AUTO_INCREMENT PRIMARY KEY,
    equipment_id   VARCHAR(20) NOT NULL,
    customer_id    INT NOT NULL,
    branch_id      INT NOT NULL,
    start_date     DATE NOT NULL,
    end_date       DATE NOT NULL,
    status         ENUM('Active','Converted','Cancelled') DEFAULT 'Active',
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (equipment_id) REFERENCES equipment(equipment_id),
    FOREIGN KEY (customer_id)  REFERENCES customers(customer_id),
    FOREIGN KEY (branch_id)    REFERENCES branches(branch_id)
);

-- ─── Rentals ───────────────────────────────────────────────
CREATE TABLE rentals (
    rental_id           INT AUTO_INCREMENT PRIMARY KEY,
    equipment_id        VARCHAR(20) NOT NULL,
    customer_id         INT NOT NULL,
    branch_id           INT NOT NULL,
    start_date          DATE NOT NULL,
    end_date            DATE NOT NULL,
    rental_amount       DECIMAL(10,2) DEFAULT 0,
    security_deposit    DECIMAL(10,2) DEFAULT 0,
    membership_discount DECIMAL(10,2) DEFAULT 0,
    long_rental_discount DECIMAL(10,2) DEFAULT 0,
    final_payable       DECIMAL(10,2) DEFAULT 0,
    payment_status      ENUM('Paid','Partially Paid','Unpaid') DEFAULT 'Unpaid',
    rental_status       ENUM('Active','Returned','Overdue','Cancelled') DEFAULT 'Active',
    reservation_id      INT,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (equipment_id)   REFERENCES equipment(equipment_id),
    FOREIGN KEY (customer_id)    REFERENCES customers(customer_id),
    FOREIGN KEY (branch_id)      REFERENCES branches(branch_id),
    FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id)
);

-- ─── Rental Returns ────────────────────────────────────────
CREATE TABLE rental_returns (
    return_id           INT AUTO_INCREMENT PRIMARY KEY,
    rental_id           INT NOT NULL,
    actual_return_date  DATE NOT NULL,
    is_damaged          BOOLEAN DEFAULT FALSE,
    damage_description  TEXT,
    damage_charge       DECIMAL(10,2) DEFAULT 0,
    late_fee            DECIMAL(10,2) DEFAULT 0,
    total_charges       DECIMAL(10,2) DEFAULT 0,
    deposit_used        DECIMAL(10,2) DEFAULT 0,
    refund_amount       DECIMAL(10,2) DEFAULT 0,
    additional_payment  DECIMAL(10,2) DEFAULT 0,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (rental_id) REFERENCES rentals(rental_id)
);

-- ─── Pricing Rules ─────────────────────────────────────────
CREATE TABLE pricing_rules (
    rule_id                 INT AUTO_INCREMENT PRIMARY KEY,
    long_rental_min_days    INT          DEFAULT 7,
    long_rental_discount_pct DECIMAL(5,2) DEFAULT 10.00,
    global_late_fee_per_day DECIMAL(10,2) DEFAULT 500.00
);

-- ============================================================
--  SAMPLE DATA
-- ============================================================

-- Branches
INSERT INTO branches (branch_code, name, address, contact) VALUES
('COL','Colombo Branch','123 Galle Road, Colombo 03','0112-345678'),
('PAN','Panadura Branch','45 Mahinda Mawatha, Panadura','038-2234567'),
('GAL','Galle Branch','78 Wakwella Road, Galle','091-2223344');

-- Categories
INSERT INTO equipment_categories (name, description, base_price_factor, weekend_multiplier, default_late_fee) VALUES
('Camera',  'DSLR and Mirrorless cameras',        1.00, 1.20, 1500.00),
('Lens',    'Camera lenses of all focal lengths',  0.80, 1.10, 1000.00),
('Drone',   'Professional aerial drones',           1.50, 1.30, 2500.00),
('Lighting','Studio and field lighting equipment', 0.90, 1.10,  800.00),
('Audio',   'Microphones and audio recorders',     0.85, 1.10,  700.00);

-- Membership config
INSERT INTO membership_configs VALUES
('Regular', 0.00, 500000.00),
('Silver',  5.00, 750000.00),
('Gold',   10.00,1000000.00);

-- Pricing rules
INSERT INTO pricing_rules (long_rental_min_days, long_rental_discount_pct, global_late_fee_per_day) VALUES (7, 10.00, 500.00);

-- Equipment – Colombo (branch_id=1)
INSERT INTO equipment VALUES
('EQ-COL-001',1,'Sony','A7 IV',2022,5000.00,25000.00,'Available',1),
('EQ-COL-002',1,'Canon','EOS R5',2021,5500.00,27000.00,'Available',1),
('EQ-COL-003',2,'Sony','FE 24-70mm f/2.8',2022,2500.00,15000.00,'Available',1),
('EQ-COL-004',3,'DJI','Mavic 3 Pro',2023,8000.00,40000.00,'Available',1),
('EQ-COL-005',4,'Godox','SL-60W LED',2021,1500.00,8000.00,'Available',1),
('EQ-COL-006',5,'Rode','VideoMic Pro+',2022,1200.00,6000.00,'Available',1),
('EQ-COL-007',1,'Nikon','Z6 II',2021,4500.00,22000.00,'Under Maintenance',1),
('EQ-COL-008',3,'DJI','Air 2S',2022,6000.00,30000.00,'Available',1);

-- Equipment – Panadura (branch_id=2)
INSERT INTO equipment VALUES
('EQ-PAN-001',1,'Sony','A7 III',2020,4000.00,20000.00,'Available',2),
('EQ-PAN-002',2,'Sigma','18-35mm f/1.8',2021,2000.00,12000.00,'Available',2),
('EQ-PAN-003',3,'DJI','Mini 3 Pro',2023,5000.00,25000.00,'Available',2),
('EQ-PAN-004',4,'Aputure','120D II',2022,2000.00,10000.00,'Available',2),
('EQ-PAN-005',5,'Zoom','H5 Recorder',2021,1000.00,5000.00,'Available',2),
('EQ-PAN-006',1,'Fujifilm','X-T4',2021,3500.00,18000.00,'Available',2),
('EQ-PAN-007',2,'Canon','EF 50mm f/1.4',2020,1500.00,8000.00,'Available',2);

-- Equipment – Galle (branch_id=3)
INSERT INTO equipment VALUES
('EQ-GAL-001',1,'Canon','EOS R6',2022,4800.00,24000.00,'Available',3),
('EQ-GAL-002',2,'Tamron','28-75mm f/2.8',2022,1800.00,10000.00,'Available',3),
('EQ-GAL-003',3,'DJI','Mavic 3',2022,7000.00,35000.00,'Available',3),
('EQ-GAL-004',4,'Godox','AD400Pro',2022,2500.00,12000.00,'Available',3),
('EQ-GAL-005',5,'Sony','UWP-D21',2021,1500.00,7500.00,'Available',3),
('EQ-GAL-006',1,'Sony','FX3',2023,7000.00,35000.00,'Available',3);

-- Customers
INSERT INTO customers (name,nic_passport,contact_no,email,address,membership_level) VALUES
('Amal Perera',    '199012345678','0771234567','amal@email.com',   '12 Main St, Colombo 05','Gold'),
('Nimal Silva',    '198756789012','0782345678','nimal@email.com',  '34 Lake Rd, Kandy',     'Silver'),
('Kamala Fernando','200034567890','0763456789','kamala@email.com', '56 Beach Ave, Galle',   'Regular'),
('Ruwan Jayantha', '199123456789','0714567890','ruwan@email.com',  '78 Hill St, Kandy',     'Silver'),
('Priya Wickrama', '198967890123','0755678901','priya@email.com',  '90 Park Rd, Colombo 07','Gold'),
('Saman Kumara',   '199345678901','0726789012','saman@email.com',  '23 River Rd, Galle',    'Regular'),
('Dilani Mendis',  '200156789012','0747890123','dilani@email.com', '45 Sea View, Panadura', 'Regular'),
('Gayan Rathnayake','198878901234','0768901234','gayan@email.com', '67 Temple Rd, Colombo', 'Silver'),
('Anoma Dissanayake','199289012345','0779012345','anoma@email.com','89 Garden Ln, Galle',  'Gold'),
('Lasith Malinga', '198890123456','0750123456','lasith@email.com', '12 Cricket Ave, Galle', 'Regular'),
('Sachini Perera', '200201234567','0761234567','sachini@email.com','34 Flower Rd, Colombo', 'Silver');

-- System Users (password = SHA-256 of 'admin123', 'manager123', 'staff123')
-- We'll store SHA-256 hex strings. Passwords: admin123 → hash, etc.
-- For simplicity, using MD5-style placeholders; real hashing done in app
INSERT INTO system_users (username,password_hash,full_name,role,branch_id) VALUES
('admin',    SHA2('admin123',256),   'System Administrator', 'Admin',          NULL),
('mgr_col',  SHA2('manager123',256), 'Colombo Manager',      'Branch Manager', 1),
('mgr_pan',  SHA2('manager123',256), 'Panadura Manager',     'Branch Manager', 2),
('mgr_gal',  SHA2('manager123',256), 'Galle Manager',        'Branch Manager', 3),
('staff_col',SHA2('staff123',256),   'Colombo Staff',        'Staff',          1),
('staff_pan',SHA2('staff123',256),   'Panadura Staff',       'Staff',          2),
('staff_gal',SHA2('staff123',256),   'Galle Staff',          'Staff',          3);

-- Sample Reservations
INSERT INTO reservations (equipment_id,customer_id,branch_id,start_date,end_date,status) VALUES
('EQ-COL-003', 1, 1, DATE_ADD(CURDATE(), INTERVAL 3 DAY), DATE_ADD(CURDATE(), INTERVAL 8 DAY),  'Active'),
('EQ-PAN-003', 4, 2, DATE_ADD(CURDATE(), INTERVAL 5 DAY), DATE_ADD(CURDATE(), INTERVAL 9 DAY),  'Active'),
('EQ-GAL-003', 9, 3, DATE_ADD(CURDATE(), INTERVAL 2 DAY), DATE_ADD(CURDATE(), INTERVAL 5 DAY),  'Active');

-- Mark reserved equipment
UPDATE equipment SET status='Reserved' WHERE equipment_id IN ('EQ-COL-003','EQ-PAN-003','EQ-GAL-003');

-- Sample Active Rentals
INSERT INTO rentals (equipment_id,customer_id,branch_id,start_date,end_date,rental_amount,security_deposit,membership_discount,long_rental_discount,final_payable,payment_status,rental_status) VALUES
('EQ-COL-001',2,1,DATE_SUB(CURDATE(),INTERVAL 3 DAY),DATE_ADD(CURDATE(),INTERVAL 4 DAY),35000.00,25000.00,1750.00,0.00,58250.00,'Paid','Active'),
('EQ-PAN-001',6,2,DATE_SUB(CURDATE(),INTERVAL 2 DAY),DATE_ADD(CURDATE(),INTERVAL 5 DAY),28000.00,20000.00,0.00,0.00,48000.00,'Unpaid','Active');

-- Overdue Rentals (end_date in past, not returned)
INSERT INTO rentals (equipment_id,customer_id,branch_id,start_date,end_date,rental_amount,security_deposit,membership_discount,long_rental_discount,final_payable,payment_status,rental_status) VALUES
('EQ-GAL-001',3,3,DATE_SUB(CURDATE(),INTERVAL 15 DAY),DATE_SUB(CURDATE(),INTERVAL 5 DAY),38400.00,24000.00,0.00,0.00,62400.00,'Paid','Overdue'),
('EQ-COL-004',5,1,DATE_SUB(CURDATE(),INTERVAL 10 DAY),DATE_SUB(CURDATE(),INTERVAL 2 DAY),64000.00,40000.00,6400.00,0.00,97600.00,'Paid','Overdue');

UPDATE equipment SET status='Rented' WHERE equipment_id IN ('EQ-COL-001','EQ-PAN-001','EQ-GAL-001','EQ-COL-004');

-- Sample Returned Rental with Damage
INSERT INTO rentals (equipment_id,customer_id,branch_id,start_date,end_date,rental_amount,security_deposit,membership_discount,long_rental_discount,final_payable,payment_status,rental_status) VALUES
('EQ-COL-002',8,1,DATE_SUB(CURDATE(),INTERVAL 20 DAY),DATE_SUB(CURDATE(),INTERVAL 13 DAY),38500.00,27000.00,1925.00,3850.00,59725.00,'Paid','Returned');

INSERT INTO rental_returns (rental_id,actual_return_date,is_damaged,damage_description,damage_charge,late_fee,total_charges,deposit_used,refund_amount,additional_payment) VALUES
(5, DATE_SUB(CURDATE(),INTERVAL 12 DAY), TRUE, 'Minor scratch on lens mount', 5000.00, 0.00, 5000.00, 5000.00, 22000.00, 0.00);
