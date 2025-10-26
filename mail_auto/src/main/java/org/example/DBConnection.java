package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://rds-coco.ap-northeast-2.rds.amazonaws.com:3306/tms?useSSL=false";
    private static final String USER = "username";
    private static final String PASSWORD = "userpassword";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}