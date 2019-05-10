package com.xdhsyy.xdhcall;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;

import java.util.ArrayList;
import java.util.List;

public class ContactsMsgUtils {
    /**
     * 获取所有通话记录
     * @param context
     * @return
     */
    public  List<CallLogInfo> getCallLog(Context context) {
        List<CallLogInfo> infos = new ArrayList<CallLogInfo>();
        ContentResolver cr = context.getContentResolver();
        Uri uri = CallLog.Calls.CONTENT_URI;
        String[] projection = new String[] { CallLog.Calls.NUMBER, CallLog.Calls.DATE,
                CallLog.Calls.TYPE,CallLog.Calls.DURATION};
        Cursor cursor = cr.query(uri, projection, null, null, "date desc");
        while (cursor.moveToNext()) {
            String number = cursor.getString(0);
            long date = cursor.getLong(1);
            int type = cursor.getInt(2);
            int duraition = cursor.getInt(3);
            infos.add(new CallLogInfo(number, date, type,duraition));
        }
        cursor.close();
        return infos;
    }

    /**
     * 获取通话记录根据条件
     * @param context
     * @param where "NUMBER = 10086"
     * @param order "DATE desc"
     * @return
     */
    public  List<CallLogInfo> getCallLogByWhere(Context context,String where,String order) {
        List<CallLogInfo> infos = new ArrayList<CallLogInfo>();
        ContentResolver cr = context.getContentResolver();
        Uri uri = CallLog.Calls.CONTENT_URI;
        String[] projection = new String[] { CallLog.Calls.NUMBER, CallLog.Calls.DATE,
                CallLog.Calls.TYPE,CallLog.Calls.DURATION};
        Cursor cursor = cr.query(uri, projection, where, null, order);
        while (cursor.moveToNext()) {
            String number = cursor.getString(0);
            long date = cursor.getLong(1);
            int type = cursor.getInt(2);
            int duraition = cursor.getInt(3);
            infos.add(new CallLogInfo(number, date, type,duraition));
        }
        cursor.close();
        return infos;
    }

}

