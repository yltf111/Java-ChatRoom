package com.chatroom.server;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * 聊天室服务器端主程序
 * <p>
 * 负责启动 ServerSocket 监听指定端口，并循环接收客户端的连接请求。
 * 每当有新的客户端连接时，创建一个独立的线程 (ServerThread) 进行处理。
 * </p>
 */
public class ChatServer {
    /** 服务器监听端口 */
    private static final int PORT = 8888;

    public static void main(String[] args) {
        try {
            // 1. 启动服务器，绑定端口
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("服务器已启动，正在监听端口 " + PORT + "...");

            // 2. 循环监听客户端连接
            while (true) {
                // accept() 方法会阻塞，直到有客户端连接
                Socket socket = serverSocket.accept();
                System.out.println("客户端连接成功，IP地址: " + socket.getInetAddress());

                // 3. 为该客户端启动一个独立的线程进行服务
                new ServerThread(socket).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("服务器启动失败！");
        }
    }
}