# GearRent Pro - Multi-Branch Equipment Rental System

A full-featured JavaFX and MySQL desktop application for managing equipment rentals across multiple branches.

## Project Description

GearRent Pro manages all business activities of a professional equipment rental company that rents out cameras, drones, lighting kits, audio gear and more. It supports multiple branches, reservations, rentals, returns, damage tracking and reporting with role-based access control.

## Architecture

- Presentation Layer - JavaFX FXML + Controllers
- Service Layer - Business logic and validation
- DAO Layer - JDBC with PreparedStatements and Transactions
- Entity Layer - Plain Java classes
- Database - MySQL 8.x

## Prerequisites

- Java JDK 17 or later
- Maven 3.8+
- MySQL Server 8.0+
- JavaFX 21 (bundled via Maven)

## Database Setup

1. Start your MySQL server
2. Open Command Prompt and run: mysql -u root -p
3. Run the script: source C:/path/to/gearrent-pro/sql/gearrent_pro.sql
4. Edit src/main/resources/db.properties and update your password

## How to Run

cd gearrent-pro
mvn clean javafx:run

First run takes 3-5 minutes to download dependencies.

## Default Login Credentials


| Username  | Password   | Role           | Branch       |
|---------- |----------  |------          |--------      |
| admin     | admin123   | Admin          | All branches |
| mgr_col   | manager123 | Branch Manager | Colombo      |
| mgr_pan   | manager123 | Branch Manager | Panadura     |
| mgr_gal   | manager123 | Branch Manager | Galle        |
| staff_col | staff123   | Staff          | Colombo      |
| staff_pan | staff123   | Staff          | Panadura     |
| staff_gal | staff123   | Staff          | Galle        |
## Sample Data

- 3 branches: Colombo, Panadura, Galle
- 5 categories: Camera, Lens, Drone, Lighting, Audio
- 22 equipment items across all branches
- 11 customers with Regular, Silver and Gold membership
- 7 system users
- Active reservations, active rentals, overdue rentals and a completed return with damage

## Feature Modules

- Branches - Admin only
- Categories - Admin and Branch Manager
- Equipment - Admin, Manager and Staff
- Customers - All roles
- Membership and Pricing Config - Admin only
- Users - Admin only
- Reservations - All roles
- Rentals - All roles
- Returns - All roles
- Overdue View - All roles
- Reports - Admin and Branch Manager

## Business Rules

- Maximum rental duration is 30 days
- Equipment cannot be double-booked for overlapping dates
- Customer total active deposits cannot exceed configurable limit
- Late fee charged per day for overdue returns
- Security deposit offsets late fees and damage charges
- Remaining deposit refunded or extra amount collected

## Technologies Used

- Java 23
- JavaFX 21
- MySQL 8.0
- JDBC
- Maven
- SHA-256 password hashing