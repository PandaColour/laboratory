package com.qihoo.auto.component;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
public class DatabaseConnectionManager {
    private HikariDataSource dataSource;

    public void initialize(String host, int port, String database, 
                         String username, String password) {
        String jdbcUrl = String.format("jdbc:mysql://%s:%d/%s", host, port, database);
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
        config.setIdleTimeout(300000);
        config.setConnectionTimeout(20000);
        
        this.dataSource = new HikariDataSource(config);
    }

    public DataSource getDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("Database connection not initialized. Call initialize() first.");
        }
        return dataSource;
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }

    /**
     * 执行查询SQL，返回结果列表
     * @param sql SQL语句
     * @param params SQL参数
     * @return 结果列表，每行数据以Map形式存储
     */
    public List<Map<String, Object>> select(String sql, Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置参数
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            ResultSet rs = pstmt.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            List<Map<String, Object>> result = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i);
                    Object value = rs.getObject(i);
                    row.put(columnName, value);
                }
                result.add(row);
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Error executing select query: " + sql, e);
        }
    }

    /**
     * 执行更新SQL（INSERT, UPDATE, DELETE）
     * @param sql SQL语句
     * @param params SQL参数
     * @return 受影响的行数
     */
    public int update(String sql, Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置参数
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error executing update query: " + sql, e);
        }
    }
} 