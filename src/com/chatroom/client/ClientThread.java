package com.chatroom.client;

import com.chatroom.common.Message;
import javax.swing.*;
import java.io.ObjectInputStream;
import java.net.Socket;

/**
 * 客户端消息接收线程
 * <p>
 * 负责在后台持续监听服务器转发过来的消息，并将其更新到聊天界面上。
 * 解决了 Swing 界面在进行网络 IO 时可能出现的卡顿问题。
 * </p>
 */
public class ClientThread extends Thread {
    private Socket socket;
    private ChatFrame chatFrame;
    private ObjectInputStream ois;

    /**
     * 构造方法
     *
     * @param socket    通信套接字
     * @param chatFrame 聊天界面引用，用于回调显示消息
     * @param ois       对象输入流，复用 LoginFrame 中创建的流
     */
    public ClientThread(Socket socket, ChatFrame chatFrame, ObjectInputStream ois) {
        this.socket = socket;
        this.chatFrame = chatFrame;
        this.ois = ois;
    }

    /**
     * 线程执行体
     * 循环读取服务器发送的 Message 对象。
     */
    @Override
    public void run() {
        try {
            while (true) {
                // 阻塞读取服务器转发的消息
                Message msg = (Message) ois.readObject();

                // 格式化消息内容
                String text = msg.getSender() + ": " + msg.getContent() + "\n";

                // 回调 ChatFrame 的方法更新 UI
                chatFrame.appendMessage(text);
            }
        } catch (Exception e) {
            // 发生异常通常意味着服务器停止或网络断开
            System.out.println("与服务器断开连接...");
            JOptionPane.showMessageDialog(chatFrame, "服务器已断开连接，请重新登录！");
            // 退出程序
            System.exit(0);
        }
    }
}