package com.wkallil;

import com.wkallil.persistence.migration.MigrationStrategy;
import com.wkallil.ui.MainMenu;

import java.sql.SQLException;

import static com.wkallil.persistence.config.ConnectionConfig.getConnection;


public class Main {
    public static void main(String[] args) throws SQLException {

        try(var connection = getConnection()) {
            new MigrationStrategy(connection).executeMigration();
        }
        new MainMenu().execute();
    }
}