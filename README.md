<<<<<<< HEAD
# GearRent-Pro-System
Multi-branch equipment rental system using Java, JDBC, and MySQL
=======
# ⚙ GearRent Pro – Multi-Branch Equipment Rental System

A full-featured JavaFX + MySQL desktop application for managing equipment rentals across multiple branches.

---

## 📋 Project Description

GearRent Pro manages all business activities of a professional equipment rental company — cameras, drones, lighting kits, audio gear and more. It supports multiple branches, reservations, rentals, returns, damage tracking and reporting with role-based access control.

---

## 🏗 Architecture

```
Presentation Layer  →  JavaFX FXML + Controllers
Service Layer       →  Business logic & validation
DAO Layer           →  JDBC with PreparedStatements + Transactions
Entity Layer        →  Plain Java classes (POJOs)
Database            →  MySQL 8.x
```

---

## 🖥 Prerequisites

| Tool | Version |
|------|---------|
| Java JDK | 17 or later |
| Maven | 3.8+ |
| MySQL Server | 8.0+ |
| JavaFX | 21 (bundled via Maven) |

---

## 🗄 Database Setup

1. Start your MySQL server.
2. Open a MySQL client (MySQL Workbench, DBeaver, or CLI).
3. Run the SQL script:
   ```sql
   source /path/to/gearrent-pro/sql/gearrent_pro.sql
   ```
   Or paste its contents into your client.

4. Edit `src/main/resources/db.properties` to match your MySQL credentials:
   ```properties
   db.url=jdbc:mysql://localhost:3306/gearrent_pro?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   db.username=root
   db.password=your_password_here
   ```

---

## ▶ Running the Application

### Option A – Maven (recommended)
```bash
cd gearrent-pro
mvn clean javafx:run
```

### Option B – Build fat JAR, then run
```bash
mvn clean package
java --module-path /path/to/javafx-sdk/lib \
     --add-modules javafx.controls,javafx.fxml \
     -jar target/gearrent-pro-1.0-SNAPSHOT.jar
```

---

## 🔐 Default Login Credentials

| Username | Password | Role | Branch |
|----------|----------|------|--------|
| `admin` | `admin123` | Admin | All branches |
| `mgr_col` | `manager123` | Branch Manager | Colombo |
| `mgr_pan` | `manager123` | Branch Manager | Panadura |
| `mgr_gal` | `manager123` | Branch Manager | Galle |
| `staff_col` | `staff123` | Staff | Colombo |
| `staff_pan` | `staff123` | Staff | Panadura |
| `staff_gal` | `staff123` | Staff | Galle |

---

## 🗂 Sample Data

The SQL script pre-loads:
- **3 branches**: Colombo, Panadura, Galle
- **5 categories**: Camera, Lens, Drone, Lighting, Audio
- **22 equipment items** distributed across branches
- **11 customers** with Regular / Silver / Gold membership
- **7 system users** (admin + managers + staff)
- **Active reservations**, **active rentals**, **overdue rentals**, and **a completed return with damage**

---

## 🧩 Feature Modules

| Module | Admin | Branch Manager | Staff |
|--------|-------|---------------|-------|
| Branches | ✅ Full CRUD | ❌ | ❌ |
| Categories | ✅ | ✅ | ❌ |
| Equipment | ✅ | ✅ | ✅ (view/filter) |
| Customers | ✅ | ✅ | ✅ |
| Membership Config | ✅ | ❌ | ❌ |
| Users | ✅ | ❌ | ❌ |
| Reservations | ✅ | ✅ | ✅ |
| Rentals | ✅ | ✅ | ✅ |
| Returns | ✅ | ✅ | ✅ |
| Overdue View | ✅ | ✅ | ✅ |
| Reports | ✅ | ✅ (own branch) | ❌ |

---

## 💰 Pricing Logic

```
finalDailyPrice = equipmentBasePrice × categoryFactor × weekendMultiplier (if Sat/Sun)
rentalAmount    = sum of finalDailyPrice for each day in period
longDiscount    = rentalAmount × discountPct  (if days ≥ minDays, default 7)
memberDiscount  = (rentalAmount − longDiscount) × membershipPct
finalPayable    = rentalAmount − longDiscount − memberDiscount + securityDeposit
```

---

## 🔒 Password Hashing

All passwords are stored as **SHA-256** hex hashes (via `java.security.MessageDigest`).

---

## 📁 Project Structure

```
gearrent-pro/
├── sql/gearrent_pro.sql
├── pom.xml
├── README.md
└── src/main/
    ├── java/
    │   ├── module-info.java
    │   └── com/gearrent/
    │       ├── Main.java
    │       ├── entity/          # 10 POJO classes
    │       ├── dao/             # 10 DAO classes + DBConnection
    │       ├── service/         # 10 service classes + ServiceFactory
    │       └── controller/      # 14 controllers + SessionManager + UIHelper
    └── resources/
        ├── db.properties
        └── com/gearrent/
            ├── fxml/            # 13 FXML layout files
            └── css/styles.css
```
>>>>>>> 0360ead (Initial commit)
