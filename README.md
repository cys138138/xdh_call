# xdh_call

通话状态监听

## Getting Started

```xml
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.PROCESS_OUTGOING_CALLS" />

## Example

``` dart
// IMPORT PACKAGE
import 'package:xdh_call/xdh_call.dart';
//1.初始化
XdhCall(_onEvent,_onError);
//2.定义回调方法
  void _onEvent(Object event) {
    print("调用自定义收到回复");
    setState(() {
      _platformVersion = event;
    });
    print(event);
  }

  void _onError(Object error) {
    print("收到错误");
  }
 // 拨打电话返回 "{'type':'outgoning_call','phone':'10086'}"
 // 挂断 "{'type':'call_state_idle','phone':'10086'}"
 // 接听 "{'type':'call_state_offhook','phone':'10086'}"
 // 呼入响铃 "{'type':'call_state_ringing','phone':'10086'}"
```


##配合 call_log 你会有意想不到的组合
https://pub.dartlang.org/packages/call_log
This project is a starting point for a Flutter
[plug-in package](https://flutter.io/developing-packages/),
a specialized package that includes platform-specific implementation code for
Android and/or iOS.

For help getting started with Flutter, view our 
[online documentation](https://flutter.io/docs), which offers tutorials, 
samples, guidance on mobile development, and a full API reference.
