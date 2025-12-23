package com.chatroom.client;

import com.chatroom.common.Message;
import javax.swing.*;
import java.awt.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * 客户端登录界面类
 * <p>
 * 该类继承自 JFrame，提供图形化用户界面用于账号登录和注册。
 * 它是客户端程序的入口点，负责与服务器建立初始连接并进行身份验证。
 * </p>
 */
public class LoginFrame extends JFrame {
    /** 用户名输入框 */
    private JTextField userField;
    /** 密码输入框 */
    private JPasswordField passField;

    /**
     * 构造方法：初始化登录界面组件与布局
     */
    public LoginFrame() {
        setTitle("登录聊天室");
        setSize(300, 250);
        setLocationRelativeTo(null); // 设置窗口居中显示
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 1)); // 使用网格布局，分为3行

        // --- 第一行：账号输入区域 ---
        JPanel p1 = new JPanel();
        p1.add(new JLabel("账号:"));
        userField = new JTextField(15);
        userField.setText(""); // 默认置空
        p1.add(userField);
        add(p1);

        // --- 第二行：密码输入区域 ---
        JPanel p2 = new JPanel();
        p2.add(new JLabel("密码:"));
        passField = new JPasswordField(15);
        passField.setText("");
        p2.add(passField);
        add(p2);

        // --- 第三行：操作按钮区域 ---
        JPanel p3 = new JPanel();
        JButton loginBtn = new JButton("登录");
        JButton regBtn = new JButton("注册账号");
        p3.add(loginBtn);
        p3.add(regBtn);
        add(p3);

        // --- 注册事件监听器 ---
        // Lambda 表达式绑定按钮点击事件
        loginBtn.addActionListener(e -> doLogin());
        regBtn.addActionListener(e -> doRegister());

        // 显示窗口
        setVisible(true);
    }

    /**
     * 执行登录逻辑
     * <p>
     * 获取用户输入，建立 Socket 连接，发送登录请求消息，并处理服务器响应。
     * 若登录成功，则关闭当前窗口并打开聊天主界面。
     * </p>
     */
    private void doLogin() {
        String username = userField.getText().trim();
        String password = new String(passField.getPassword()).trim();

        // 简单的非空校验
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "账号或密码不能为空！");
            return;
        }

        try {
            // 1. 建立与服务器的 Socket 连接 (本地地址，端口8888)
            Socket socket = new Socket("127.0.0.1", 8888);

            // 2. 初始化对象传输流 (注意：必须先创建 Output 后创建 Input，防止死锁)
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            // 3. 封装并发送登录请求消息
            Message loginMsg = new Message();
            loginMsg.setType(Message.LOGIN);
            // 协议格式：账号|密码
            loginMsg.setContent(username + "|" + password);
            oos.writeObject(loginMsg);
            oos.flush();

            // 4. 阻塞等待服务器响应
            Message response = (Message) ois.readObject();

            // 5. 处理响应结果
            if (Message.LOGIN_SUCCESS.equals(response.getType())) {
                // 登录成功：解析昵称，跳转至聊天界面
                // 响应内容格式约定为："欢迎回来，[昵称]"
                String nickname = response.getContent().split("，")[1];

                JOptionPane.showMessageDialog(this, "登录成功！欢迎 " + nickname);

                // 销毁登录窗口
                this.dispose();

                // 打开主聊天窗口，并将建立好的连接传递过去以复用
                new ChatFrame(nickname, socket, oos, ois);
            } else {
                // 登录失败：提示错误信息并关闭连接
                JOptionPane.showMessageDialog(this, "登录失败：" + response.getContent());
                socket.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "连接服务器失败，请检查网络或服务器状态！");
        }
    }

    /**
     * 执行注册逻辑
     * <p>
     * 弹出对话框获取注册信息，建立临时连接发送注册请求。
     * </p>
     */
    private void doRegister() {
        // 使用 JOptionPane 获取用户输入
        String username = JOptionPane.showInputDialog(this, "请输入新账号 (英文):");
        if (username == null || username.trim().isEmpty()) return;

        String password = JOptionPane.showInputDialog(this, "请输入密码:");
        if (password == null || password.trim().isEmpty()) return;

        String nickname = JOptionPane.showInputDialog(this, "请输入昵称:");
        if (nickname == null || nickname.trim().isEmpty()) return;

        try {
            // 注册过程建立临时连接
            Socket socket = new Socket("127.0.0.1", 8888);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            // 封装注册请求消息
            Message regMsg = new Message();
            regMsg.setType(Message.REGISTER);
            // 协议格式：账号|密码|昵称
            regMsg.setContent(username + "|" + password + "|" + nickname);

            oos.writeObject(regMsg);
            oos.flush();

            // 等待服务器注册结果
            Message response = (Message) ois.readObject();

            if (Message.REGISTER_SUCCESS.equals(response.getType())) {
                JOptionPane.showMessageDialog(this, "注册成功！请使用新账号登录。");
                // 自动回填注册好的账号密码，提升体验
                userField.setText(username);
                passField.setText(password);
            } else {
                JOptionPane.showMessageDialog(this, "注册失败：" + response.getContent());
            }

            // 注册完成后断开该临时连接
            socket.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "服务器未启动或连接异常！");
        }
    }

    /**
     * 程序主入口
     */
    public static void main(String[] args) {
        // 建议在事件调度线程中启动 GUI，以保证线程安全
        SwingUtilities.invokeLater(() -> new LoginFrame());
    }
}