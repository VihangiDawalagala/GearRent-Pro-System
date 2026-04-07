module com.gearrent {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;

    opens com.gearrent to javafx.fxml;
    opens com.gearrent.controller to javafx.fxml;
    opens com.gearrent.entity to javafx.base;

    exports com.gearrent;
    exports com.gearrent.entity;
    exports com.gearrent.dao;
    exports com.gearrent.service;
    exports com.gearrent.controller;
}
