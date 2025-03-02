package com.wkallil.persistence.migration;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.AllArgsConstructor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import static com.wkallil.persistence.config.ConnectionConfig.getConnection;

@AllArgsConstructor
public class MigrationStrategy {

    private final Connection connection;

    public void executeMigration() {
        var originalOut = System.out;
        var originalErr = System.err;

        try (var fos = new FileOutputStream("liquibase.log")) {
            System.setOut(new java.io.PrintStream(fos));
            System.setErr(new java.io.PrintStream(fos));
            try (var connection = getConnection(); var jdbcConnection = new JdbcConnection(connection);) {

                var liquibase = new Liquibase(
                        "/db/changelog/db.changelog-master.yml",
                        new ClassLoaderResourceAccessor(),
                        jdbcConnection);
                liquibase.update();
            } catch (SQLException | LiquibaseException e) {
                e.printStackTrace();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }
    }

}
