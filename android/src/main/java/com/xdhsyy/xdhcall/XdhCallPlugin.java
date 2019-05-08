package com.xdhsyy.xdhcall;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.RemoteException;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;

import java.lang.reflect.Method;
import java.util.List;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import static android.content.Context.TELEPHONY_SERVICE;
import com.android.internal.telephony.ITelephony;

/**
 * MethodCallHandler实现MethodChannel的Flutter App调用Native APIs；
 * EventChannel.StreamHandler实现EventChannel的Native调用Flutter App。
 * https://www.jianshu.com/p/d9eeb15b3fa0
 */
public class XdhCallPlugin implements MethodCallHandler, EventChannel.StreamHandler {
    final Registrar mRegistrar;
    // 接收电池广播的BroadcastReceiver。
    private BroadcastReceiver chargingStateChangeReceiver;

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "xdh_call");
        XdhCallPlugin plugin = new XdhCallPlugin(registrar);
        channel.setMethodCallHandler(plugin);

        final EventChannel eventChannel = new EventChannel(registrar.messenger(), "xdh_call_event");
        eventChannel.setStreamHandler(plugin);
    }

    /**
     * 初始化
     * @param registrar
     */
    private XdhCallPlugin(Registrar registrar) {
        this.mRegistrar = registrar;
    }

    /**
     * flutter 调用原生时候的方法
     *
     * @param call
     * @param result
     */
    @Override
    public void onMethodCall(MethodCall call, Result result) {

        /**
         * 拨打电话
         */
        if (call.method.equals("call")) {
            String phone = call.argument("phone_number");
            boolean type = call.argument("is_confirm");
            System.out.println(type);
            //直接调用不用确认拨打
            if(type){
                callPhone(phone,result);
            }else{
                callNoComfirePhone(phone,result);
            }
            return;
        }
        else if (call.method.equals("endCall")) {
            endCall(result);
        }

        else if (call.method.equals("sendSms")) {
            String phone = call.argument("phone_number");
            String sms_content = call.argument("sms_content");
            sendSms(phone,sms_content,result);
        }
        else if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
        }
        else if (call.method.equals("getUniqueId")) {
            getUniqueId(result);
        }
        else {
            result.notImplemented();
        }
    }

    private void getUniqueId(Result result) {
        String uniqueId = DeviceUtils.getUniqueId(mRegistrar.activeContext());
        result.success(uniqueId);
    }

    private void sendSms(String phone_number,String sms_content, Result result) {
        SmsManager smsManager = SmsManager.getDefault();
        if(sms_content.length()>70) {
            List<String> contents = smsManager.divideMessage(sms_content);
            for(String sms:contents) {
                smsManager.sendTextMessage(phone_number,null,sms,null,null);
            }
        } else {
            smsManager.sendTextMessage(phone_number,null,sms_content,null,null);
        }
        result.success(true);
    }

    /**
     * 这个onListen是Flutter端开始监听这个channel时的回调，第二个参数 EventSink是用来传数据的载体。
     *
     * @param o
     * @param eventSink
     */
    @Override
    public void onListen(Object o, EventChannel.EventSink eventSink) {
        eventSink.success("原生监听成功");
        chargingStateChangeReceiver = createChargingStateChangeReceiver(eventSink);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
        filter.addAction(Intent.ACTION_ANSWER);
        //这里不知道怎么找到这个字符串，通过调试才找到这个事件名称
        filter.addAction("android.intent.action.PHONE_STATE");
        filter.setPriority(Integer.MAX_VALUE);
        mRegistrar.activity().registerReceiver(chargingStateChangeReceiver, filter);
    }

    /**
     * 客户端不在接受信息的处理
     *
     * @param o
     */
    @Override
    public void onCancel(Object o) {
        // 对面不再接收
        mRegistrar.activity().unregisterReceiver(chargingStateChangeReceiver);
        chargingStateChangeReceiver = null;
    }

    private BroadcastReceiver createChargingStateChangeReceiver(final EventChannel.EventSink events) {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                System.out.println("flutter action:" + intent.getAction());
                if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
                    //去电
                    System.out.println("拨打电话");
                    // events.error("UNAVAILABLE", "Charging status unavailable", null);
                    // 把电池状态发给Flutter
                    String extraIncomingNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                    events.success("{'type':'outgoning_call','phone':'" + extraIncomingNumber + "'}");
                } else {//来电(存在以下三种情况)
                    TelephonyManager tm = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
                    // 获取电话号码
                    String extraIncomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                    switch (tm.getCallState()) {
                        case TelephonyManager.CALL_STATE_IDLE:
                            System.out.println("挂断");
                            events.success("{'type':'call_state_idle','phone':'" + extraIncomingNumber + "'}");
                            break;
                        case TelephonyManager.CALL_STATE_OFFHOOK:
                            System.out.println("接听");
                            events.success("{'type':'call_state_offhook','phone':'" + extraIncomingNumber + "'}");
                            break;
                        case TelephonyManager.CALL_STATE_RINGING:
                            System.out.println("呼入响铃");
                            events.success("{'type':'call_state_ringing','phone':'" + extraIncomingNumber + "'}");
                            break;
                    }
                }
            }
        };
    }

    /**
     * 拨打电话（直接拨打电话）
     * @param phoneNum 电话号码
     */
    public void callNoComfirePhone(String phoneNum,Result result){
        Intent intent = new Intent(Intent.ACTION_CALL);
        Uri data = Uri.parse("tel:" + phoneNum);
        intent.setData(data);
        Activity activity = mRegistrar.activity();
        if (activity == null) {
            result.error("NO_ACTIVITY", "requires a foreground activity.", null);
            return;
        }
        activity.startActivity(intent);
    }

    /**
     * 拨打电话（跳转到拨号界面，用户手动点击拨打）
     *
     * @param phoneNum 电话号码
     */
    public void callPhone(String phoneNum,Result result) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        Uri data = Uri.parse("tel:" + phoneNum);
        intent.setData(data);

        Activity activity = mRegistrar.activity();
        if (activity == null) {
            result.error("NO_ACTIVITY", "requires a foreground activity.", null);
            return;
        }
        activity.startActivity(intent);
    }

    /**
     * 挂断电话
     * @return
     */
    public void endCall(Result result) {
        try {
            getITelephony(mRegistrar.activeContext()).endCall();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        result.success(true);
    }

    public static ITelephony getITelephony(Context context) {
        TelephonyManager mTelephonyManager = (TelephonyManager) context
                .getSystemService(TELEPHONY_SERVICE);
        Class c = TelephonyManager.class;
        Method getITelephonyMethod = null;
        try {
            getITelephonyMethod = c.getDeclaredMethod("getITelephony",
                    (Class[]) null); // 获取声明的方法
            getITelephonyMethod.setAccessible(true);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        try {
            ITelephony iTelephony = (ITelephony) getITelephonyMethod.invoke(
                    mTelephonyManager, (Object[]) null); // 获取实例
            return iTelephony;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}