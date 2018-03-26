package cn.dmandp.common;

public interface TYPE {
    /**
     * 登录请求
     */
    static final byte LOGIN_REQ = 0;
    /**
     * 登录响应
     */
    static final byte LOGIN_RESP = 1;
    /**
     * 发送请求
     */
    static final byte SEND_REQ = 2;
    /**
     * 发送响应
     */
    static final byte SEND_RESP = 3;
    /**
     * 接收请求
     */
    static final byte RECEIVE_REQ = 4;
    /**
     * 接收响应
     */
    static final byte RECEIVE_RESP = 5;
    /**
     * 注册请求
     */
    static final byte REGISTER_REQ = 6;
    /**
     * 注册响应
     */
    static final byte REGISTER_RESP = 7;
    /**
     * 加入请求
     */
    static final byte JOIN_REQ = 8;
    /**
     * 加入响应
     */
    static final byte JOIN_RESP = 9;
}

