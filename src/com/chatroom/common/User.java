package com.chatroom.common;

import java.io.Serializable;

/**
 * 用户实体类 (Entity)
 * <p>
 * 该类用于映射数据库中的 users 表，封装了用户的基本信息。
 * 实现了 Serializable 接口，以支持对象在网络间的序列化传输。
 * </p>
 *
 * @author YourName
 * @version 1.0
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 用户唯一标识 ID */
    private int id;
    /** 用户账号 */
    private String username;
    /** 用户密码 */
    private String password;
    /** 用户昵称（显示名称） */
    private String nickname;
    /** 头像ID */
    private int avatar;

    /**
     * 无参构造方法
     */
    public User() {}

    /**
     * 全参构造方法
     *
     * @param id 用户ID
     * @param username 账号
     * @param password 密码
     * @param nickname 昵称
     * @param avatar 头像ID
     */
    public User(int id, String username, String password, String nickname, int avatar) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.avatar = avatar;
    }

    // --- Getter 和 Setter 方法 ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public int getAvatar() { return avatar; }
    public void setAvatar(int avatar) { this.avatar = avatar; }
}