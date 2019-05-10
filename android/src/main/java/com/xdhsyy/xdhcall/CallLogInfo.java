package com.xdhsyy.xdhcall;


public class CallLogInfo {
    public String number;
    public long date;
    public int type;
    public int duraition;
    public CallLogInfo(String number, long date, int type,int duraition) {
        super();
        this.number = number;
        this.date = date;
        this.type = type;
        this.duraition = duraition;
    }
    public CallLogInfo() {
        super();
    }
}