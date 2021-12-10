package com.garciajuanjo.calculatorpan.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class AppDao {

    private final String DATABASE_NAME = "CalculatorPan";
    private final String SQLITE_URI = "jdbc:sqlite:" + DATABASE_NAME + ".db";


    private Connection connection;

    public void conect() throws SQLException {
        connection = DriverManager.getConnection(SQLITE_URI);
    }

    public void close() throws SQLException {
        connection.close();
    }

    public Connection getConnection() {
        return connection;
    }

}
