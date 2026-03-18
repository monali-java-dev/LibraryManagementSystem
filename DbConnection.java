package com.library;

import java.sql.Connection;
import java.sql.DriverManager;

public class DbConnection {

    public static Connection getConnection() {

        Connection con = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/library_db",
                "root",
                "@123"
            );

        } catch (Exception e) {
            System.out.println(e);
        }

        return con;
    }
}