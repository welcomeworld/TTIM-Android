package cn.dmandp.entity;

/**
 * @author 杨泽雄  Email mengxiangzuojia@163.com
 * @version 1.0
 * @date 3 Apr 2018 22:10:54
 */
public class TTIMPacket {
    private byte TYPE;
    private int bodylength;
    private byte[] body;

    public byte getTYPE() {
        return TYPE;
    }

    public void setTYPE(byte tYPE) {
        TYPE = tYPE;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public int getBodylength() {
        return bodylength;
    }

    public void setBodylength(int bodylength) {
        this.bodylength = bodylength;
    }
}
