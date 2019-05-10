import 'dart:async';

import 'package:flutter/services.dart';

class XdhCall {
  static const MethodChannel _channel = const MethodChannel('xdh_call');
  static const EventChannel eventChannel = EventChannel('xdh_call_event');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  ///
  /// 拨打电话
  ///
  static Future<void> callphone(String str, {
    bool is_confirm,
    String phone_number,
  }) async {
    assert(phone_number != null);
    assert(is_confirm != null);
    await _channel.invokeMethod('call',<String, Object>{
      'is_confirm': is_confirm,
      'phone_number': phone_number,
    });

  }

  XdhCall(Function _onEvent,Function _onError){
    print("初始化监听事件开始");
    eventChannel.receiveBroadcastStream().listen(_onEvent, onError: _onError);
  }
//  void _onEvent(Object event) {
//    print("收到回复");
//    print(event);
//  }
//
//  void _onError(Object error) {
//    print("收到错误");
//  }

  ///
  /// 挂断电话
  ///
  static Future<bool> endCall() async {
    return await _channel.invokeMethod('endCall');
  }

  ///发送短信
  ///
  static Future<bool> sendSms({
    String sms_content,
    String phone_number,
  }) async {
    assert(phone_number != null);
    assert(sms_content != null);
    final bool result = await _channel.invokeMethod('sendSms',<String, Object>{
      'sms_content': sms_content,
      'phone_number': phone_number,
    });
    return result;
  }
  
  ///获取唯一设备码
  ///
  static Future<String> getUniqueId() async {
    final String result = await _channel.invokeMethod('getUniqueId');
    return result;
  }

  ///获取通话记录
  ///
  static Future<String> getCallLog() async {
    final String result = await _channel.invokeMethod('getCallLog');
    return result;
  }

  ///获取通话记录
  ///
  static Future<String> getCallLogByWhere(String whereStr,String orderStr) async {
    final String result = await _channel.invokeMethod('getCallLogByWhere',<String, Object>{
      'whereStr': whereStr,
      'orderStr': orderStr,
    });
    return result;
  }
}
