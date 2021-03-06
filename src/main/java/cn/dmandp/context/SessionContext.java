package cn.dmandp.context;

import android.app.Activity;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import cn.dmandp.entity.TTUser;

/**
 * @author 杨泽雄  Email mengxiangzuojia@163.com
 * @version 1.0
 * @date 18 Mar 2018 20:42:06
 */
public class SessionContext {
    public static Map<String, Activity> activities = new HashMap<>();
    private TTUser bindUser;
    private Integer uID;
    private Boolean login = false;
    private SocketChannel socketChannel;
    private Map<String, Object> Attributes;
    public String socketChannelErrorMessage;
    SessionContext(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
        Attributes = new HashMap<>();
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public void setSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public Object getAttribute(String key) {
        return Attributes.get(key);
    }

    public void setAttribute(String key, Object value) {
        Attributes.put(key, value);
    }

    public Integer getuID() {
        return uID;
    }

    public void setuID(Integer uID) {
        this.uID = uID;
    }

    public Boolean isLogin() {
        return login;
    }

    public void setLogin(Boolean login) {
        this.login = login;
    }

    public TTUser getBindUser() {
        return bindUser;
    }

    public void setBindUser(TTUser bindUser) {
        this.bindUser = bindUser;
    }
}
