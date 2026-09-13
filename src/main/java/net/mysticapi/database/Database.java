package net.mysticapi.database;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public abstract class Database {

    protected HikariDataSource dataSource;

    public abstract void connect();

    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public boolean isConnected() {
        return dataSource != null && !dataSource.isClosed();
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public int executeUpdate(String sql, Object... params) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            applyParams(statement, params);
            return statement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
            return -1;
        }
    }

    public void executeQuery(String sql, ResultHandler handler, Object... params) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            applyParams(statement, params);
            try (ResultSet resultSet = statement.executeQuery()) {
                handler.handle(resultSet);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public CompletableFuture<Integer> executeUpdateAsync(String sql, Object... params) {
        return CompletableFuture.supplyAsync(() -> executeUpdate(sql, params));
    }

    public CompletableFuture<Void> executeQueryAsync(String sql, ResultHandler handler, Object... params) {
        return CompletableFuture.runAsync(() -> executeQuery(sql, handler, params));
    }

    private void applyParams(PreparedStatement statement, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
    }
}
