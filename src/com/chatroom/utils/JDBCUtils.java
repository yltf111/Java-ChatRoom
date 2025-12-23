package com.chatroom.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * 数据库操作工具类
 * <p>
 * 提供获取数据库连接和释放资源的通用方法。
 * 基于 JDBC 技术实现，使用静态代码块加载驱动。
 * </p>
 */
public class JDBCUtils {
    /** 数据库驱动类名 */
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    /** 数据库连接 URL (包含字符集和时区配置) */
    private static final String URL = "jdbc:mysql://localhost:3306/chat_room_db?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf-8";
    /** 数据库用户名 */
    private static final String USER = "root";
    /** 数据库密码  */
    private static final String PASSWORD = "root";

    // 静态代码块：类加载时注册驱动
    static {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("数据库驱动加载失败", e);
        }
    }

    /**
     * 获取数据库连接
     *
     * @return Connection 数据库连接对象
     * @throws Exception 连接获取失败异常
     */
    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * 释放数据库资源
     * <p>
     * 遵循先开后关的原则，依次关闭 ResultSet, Statement, Connection。
     * </p>
     *
     * @param conn 数据库连接对象
     * @param stmt SQL语句执行对象
     * @param rs   结果集对象
     */
    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}