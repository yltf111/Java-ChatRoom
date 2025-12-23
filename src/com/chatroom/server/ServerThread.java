package com.chatroom.server;

import com.chatroom.common.Message;
import com.chatroom.utils.JDBCUtils;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

/**
 * 服务器端线程类
 * <p>
 * 该类继承自 Thread，每一个连接到服务器的客户端都会分配一个独立的 ServerThread 实例进行处理。
 * 主要负责监听客户端发送的消息，并根据消息类型（登录、聊天、注册）执行相应的业务逻辑。
 * </p>
 */
public class ServerThread extends Thread {
    /**
     * 与当前客户端建立连接的 Socket 对象
     */
    private Socket socket;

    /**
     * 在线用户列表
     * <p>
     * 使用静态 HashMap 存储所有在线用户的映射关系。
     * Key: 用户昵称
     * Value: 该用户对应的 ObjectOutputStream（用于向该用户发送消息）
     * </p>
     */
    private static HashMap<String, ObjectOutputStream> onlineUsers = new HashMap<>();

    /**
     * 构造方法
     *
     * @param socket 客户端连接的 Socket 实例
     */
    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    /**
     * 线程执行体
     * 持续监听客户端发送的消息对象，并进行分发处理。
     */
    @Override
    public void run() {
        try {
            // 初始化输入输出流，用于对象传输
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            while (true) {
                // 阻塞读取客户端发送的消息对象
                Message msg = (Message) ois.readObject();

                // 根据协议中的消息类型进行分发处理
                if (Message.LOGIN.equals(msg.getType())) {
                    handleLogin(msg, oos);
                } else if (Message.CHAT.equals(msg.getType())) {
                    handleChat(msg);
                } else if (Message.REGISTER.equals(msg.getType())) {
                    handleRegister(msg, oos);
                }
            }
        } catch (Exception e) {
            // 捕获异常通常意味着客户端断开连接或网络异常
            // 实际生产环境中应在此处处理用户下线逻辑（如从 onlineUsers 移除）
            System.out.println("客户端连接已断开...");
        }
    }

    /**
     * 处理用户登录请求
     *
     * @param msg 包含登录凭证的消息对象
     * @param oos 当前客户端的输出流，用于回传响应
     * @throws Exception 数据库连接或IO异常
     */
    private void handleLogin(Message msg, ObjectOutputStream oos) throws Exception {
        // 解析消息内容，格式约定为：账号|密码
        String[] parts = msg.getContent().split("\\|");
        String username = parts[0];
        String password = parts[1];

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Message response = new Message();

        try {
            // 获取数据库连接并执行查询
            conn = JDBCUtils.getConnection();
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                // 验证通过，构建成功响应
                response.setType(Message.LOGIN_SUCCESS);
                response.setContent("欢迎回来，" + rs.getString("nickname"));

                // 将当前用户加入在线列表，以便后续进行消息转发
                onlineUsers.put(rs.getString("nickname"), oos);
                System.out.println("用户上线：" + rs.getString("nickname"));
            } else {
                // 验证失败
                response.setType(Message.LOGIN_FAIL);
                response.setContent("账号或密码错误！");
            }

            // 向客户端发送响应结果
            oos.writeObject(response);
            oos.flush();
        } finally {
            // 释放数据库资源
            JDBCUtils.close(conn, pstmt, rs);
        }
    }

    /**
     * 处理群聊消息转发
     *
     * @param msg 包含聊天内容的完整消息对象
     * @throws Exception IO异常
     */
    private void handleChat(Message msg) throws Exception {
        // 遍历在线用户列表，实现消息广播
        for (String nickname : onlineUsers.keySet()) {
            // 排除发送者自身，只转发给其他用户
            if (!nickname.equals(msg.getSender())) {
                ObjectOutputStream clientOos = onlineUsers.get(nickname);
                clientOos.writeObject(msg);
                clientOos.flush();
            }
        }
        System.out.println("转发消息 -> 发送者: " + msg.getSender() + " 内容: " + msg.getContent());

        // 将聊天记录持久化到数据库
        saveChatLog(msg);
    }

    /**
     * 处理用户注册请求
     *
     * @param msg 包含注册信息的消息对象
     * @param oos 当前客户端的输出流
     * @throws Exception 数据库操作异常
     */
    private void handleRegister(Message msg, ObjectOutputStream oos) throws Exception {
        // 解析注册信息，格式约定为：账号|密码|昵称
        String[] parts = msg.getContent().split("\\|");
        String username = parts[0];
        String password = parts[1];
        String nickname = parts[2];
        // 随机分配默认头像ID (1-3)
        int avatar = 1;

        Connection conn = null;
        PreparedStatement pstmt = null;
        Message response = new Message();

        try {
            conn = JDBCUtils.getConnection();

            // 步骤1：检查账号是否已存在
            String checkSql = "SELECT * FROM users WHERE username = ?";
            pstmt = conn.prepareStatement(checkSql);
            pstmt.setString(1, username);

            if (pstmt.executeQuery().next()) {
                response.setType(Message.REGISTER_FAIL);
                response.setContent("账号已存在，请更换账号！");
            } else {
                // 步骤2：执行插入操作
                // 关闭上一个 PreparedStatement
                pstmt.close();

                String insertSql = "INSERT INTO users (username, password, nickname, avatar) VALUES (?, ?, ?, ?)";
                pstmt = conn.prepareStatement(insertSql);
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                pstmt.setString(3, nickname);
                pstmt.setInt(4, avatar);
                pstmt.executeUpdate();

                response.setType(Message.REGISTER_SUCCESS);
                response.setContent("注册成功！请使用新账号登录。");
                System.out.println("新用户注册成功：" + username);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setType(Message.REGISTER_FAIL);
            response.setContent("服务器内部错误：" + e.getMessage());
        } finally {
            JDBCUtils.close(conn, pstmt, null);
        }

        // 返回注册结果
        oos.writeObject(response);
        oos.flush();
    }

    /**
     * 将聊天记录持久化保存至数据库
     *
     * @param msg 需要保存的消息对象
     */
    private void saveChatLog(Message msg) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = JDBCUtils.getConnection();
            String sql = "INSERT INTO chat_logs (sender_name, content, receiver_name) VALUES (?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, msg.getSender());
            pstmt.setString(2, msg.getContent());
            // 若接收者为空，则默认为 "ALL" (群聊)
            pstmt.setString(3, msg.getReceiver() == null ? "ALL" : msg.getReceiver());

            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("聊天记录保存失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            JDBCUtils.close(conn, pstmt, null);
        }
    }
}