package cn.dmandp.netio;

/**
 * Created by 萌即正义 on 27/03/2018.
 */

public class Result {
    private byte resultType;
    private byte resultStatus;
    private Object resultBody;

    public byte getResultType() {
        return resultType;
    }

    public void setResultType(byte resultType) {
        this.resultType = resultType;
    }

    public byte getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(byte resultStatus) {
        this.resultStatus = resultStatus;
    }

    public Object getResultBody() {
        return resultBody;
    }

    public void setResultBody(Object resultBody) {
        this.resultBody = resultBody;
    }
}
