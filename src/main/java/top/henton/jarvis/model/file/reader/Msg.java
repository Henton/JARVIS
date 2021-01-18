package top.henton.jarvis.model.file.reader;

public class Msg {

    private long timestamp;

    private String rid;

    private int msgCode;

    private int costTime;

    private byte from;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getRid() {
        return rid;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }

    public int getMsgCode() {
        return msgCode;
    }

    public void setMsgCode(int msgCode) {
        this.msgCode = msgCode;
    }

    public int getCostTime() {
        return costTime;
    }

    public void setCostTime(int costTime) {
        this.costTime = costTime;
    }

    public byte getFrom() {
        return from;
    }

    public void setFrom(byte from) {
        this.from = from;
    }

    public static Msg fromLineStr(String lineStr) {
        String[] params = lineStr.split(" ");
        Msg msg = new Msg();
        msg.setTimestamp(Long.parseLong(params[0]));
        msg.setRid(params[1]);
        msg.setMsgCode(Integer.parseInt(params[2]));
        msg.setCostTime(Integer.parseInt(params[3]));
        return msg;
    }
}
