package com.chatroom.common;

import java.io.Serializable;

/**
 * 消息传输协议类
 * <p>
 * 该类定义了客户端与服务器之间通信的数据格式。
 * 所有的数据交互（登录、注册、聊天）均通过传输该类的对象来实现。
 * </p>
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    // 消息类型常量定义
    /** 消息类型：登录请求 */
    public static final String LOGIN = "LOGIN";
    /** 消息类型：登录成功响应 */
    public static final String LOGIN_SUCCESS = "SUCCESS";
    /** 消息类型：登录失败响应 */
    public static final String LOGIN_FAIL = "FAIL";
    /** 消息类型：普通聊天消息 */
    public static final String CHAT = "CHAT";
    /** 消息类型：注册请求 */
    public static final String REGISTER = "REGISTER";
    /** 消息类型：注册成功响应 */
    public static final String REGISTER_SUCCESS = "REG_OK";
    /** 消息类型：注册失败响应 */
    public static final String REGISTER_FAIL = "REG_FAIL";

    /** 消息类型 */
    private String type;
    /** 发送者昵称 */
    private String sender;
    /** 消息内容 (登录/注册时为组合字符串) */
    private String content;
    /** 接收者昵称 (群聊时默认为 ALL) */
    private String receiver;

    /**
     * 无参构造方法
     */
    public Message() {}

    /**
     * 全参构造方法
     *
     * @param type 消息类型
     * @param sender 发送者
     * @param content 内容
     * @param receiver 接收者
     */
    public Message(String type, String sender, String content, String receiver) {
        this.type = type;
        this.sender = sender;
        this.content = content;
        this.receiver = receiver;
    }

    // Getter 和 Setter 方法
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getSender() {
        return sender;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public String getReceiver() {
        return receiver;
    }
    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }
}