import 'dart:async';

import 'package:flutter/services.dart';

class XdhCall {
  static const MethodChannel _channel = const MethodChannel('xdh_call');
  static const EventChannel eventChannel = EventChannel('xdh_call_event');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
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
}
