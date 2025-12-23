package com.chatroom.client;

import com.chatroom.common.Message;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * 聊天室主界面类
 * <p>
 * 提供聊天消息的展示和发送功能。
 * 包含一个后台线程 (ClientThread) 用于实时接收服务器转发的消息。
 * </p>
 */
public class ChatFrame extends JFrame {
    /** 消息显示区域 (只读) */
    private JTextArea chatArea;
    /** 消息输入框 */
    private JTextField inputField;

    /** 通信套接字 */
    private Socket socket;
    /** 当前用户昵称 */
    private String myNickname;
    /** 对象输出流，用于发送消息 */
    private ObjectOutputStream oos;

    /**
     * 构造方法：初始化聊天界面
     *
     * @param nickname 当前登录用户的昵称
     * @param socket   已建立的连接
     * @param oos      已初始化的输出流
     * @param ois      已初始化的输入流
     */
    public ChatFrame(String nickname, Socket socket, ObjectOutputStream oos, ObjectInputStream ois) {
        this.myNickname = nickname;
        this.socket = socket;
        this.oos = oos;

        // --- 窗口基本设置 ---
        setTitle("聊天室 - 当前用户：" + nickname);
        setSize(600, 500);
        setLocationRelativeTo(null); // 居中显示
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- 中部：聊天记录显示区 ---
        chatArea = new JTextArea();
        chatArea.setEditable(false); // 禁止直接编辑聊天记录
        chatArea.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        // 添加滚动条
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        // --- 底部：消息发送区 ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        inputField.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        JButton sendBtn = new JButton("发送");

        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendBtn, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        // --- 事件绑定 ---
        // 按钮点击事件
        sendBtn.addActionListener(e -> sendMessage());
        // 输入框回车事件
        inputField.addActionListener(e -> sendMessage());

        // --- 启动消息接收线程 ---
        // 传入当前界面引用，以便线程更新 UI
        new ClientThread(socket, this, ois).start();

        // 显示窗口
        setVisible(true);
    }

    /**
     * 发送消息逻辑
     * 获取输入框内容，封装成 Message 对象并发送至服务器。
     */
    private void sendMessage() {
        String content = inputField.getText().trim();
        if (content.isEmpty()) return; // 空消息不发送

        try {
            // 封装消息对象
            Message msg = new Message();
            msg.setType(Message.CHAT);
            msg.setSender(myNickname);
            msg.setContent(content);

            // 发送给服务器
            oos.writeObject(msg);
            oos.flush();

            // 将自己发送的消息立即显示在本地界面
            appendMessage("我: " + content + "\n");

            // 清空输入框
            inputField.setText("");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "消息发送失败，请检查网络连接！");
        }
    }

    /**
     * 向聊天区域追加消息
     * 该方法通常由 ClientThread 线程调用以更新 UI。
     *
     * @param text 需要显示的文本内容
     */
    public void appendMessage(String text) {
        chatArea.append(text);
        // 自动滚动至文本末尾，保证最新消息可见
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
}