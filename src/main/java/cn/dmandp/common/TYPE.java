package cn.dmandp.common;

public interface TYPE {
    /**
     * 登录请求
     */
    byte LOGIN_REQ = 0;
    /**
     * 登录响应
     */
     byte LOGIN_RESP = 1;
    /**
     * 发送请求
     */
     byte SEND_REQ = 2;
    /**
     * 发送响应
     */
     byte SEND_RESP = 3;
    /**
     * 接收请求
     */
     byte RECEIVE_REQ = 4;
    /**
     * 接收响应
     */
     byte RECEIVE_RESP = 5;
    /**
     * 注册请求
     */
     byte REGISTER_REQ = 6;
    /**
     * 注册响应
     */
     byte REGISTER_RESP = 7;
    /**
     * 加入请求
     */
     byte JOIN_REQ = 8;
    /**
     * 加入响应
     */
    byte JOIN_RESP = 9;
    /**
     * 好友信息请求
     */
    byte FRIENDS_REQ = 10;
    /**
     * 好友信息响应
     */
     byte FRIENDS_RESP = 11;
    /**
     * 心跳
     */
     byte HEART = 12;
    /**
     * user info update req
     */
    @SuppressWarnings("unused")
    byte USERINFO_REQ = 13;
    /**
     * user info update resp
     */
     byte USERINFO_RESP = 14;
    /**
     * user photo get req
     */
     byte USERPHOTO_GET_REQ = 15;
    /**
     * user photo get resp
     */
     byte USERPHOTO_GET_RESP = 16;
    /**
     * user photo set req
     */
     byte USERPHOTO_SET_REQ = 17;
    /**
     * user photo set resp
     */
     byte USERPHOTO_SET_RESP = 18;
    /**
     * favorite req
     */
     byte FAVORITE_REQ = 19;
    /**
     * favorite resp
     */
     byte FAVORITE_RESP = 20;
}

