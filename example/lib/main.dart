import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:xdh_call/xdh_call.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  @override
  void initState() {
    super.initState();
    initPlatformState();
    XdhCall(_onEvent,_onError);
  }

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

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await XdhCall.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }
  _sendsm() async {
    if(await XdhCall.sendSms(phone_number: "13252033403",sms_content: "测试啦啦啦啦")){
      setState(() {
          _platformVersion = "发送成功";
        });
    }
  }
  
  _getUniqueId() async {
    String str = await XdhCall.getUniqueId();
    setState(() {
	  _platformVersion = str;
	});
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(children: <Widget>[
            Text('Running on: $_platformVersion\n'),
            FlatButton(onPressed: (){
              XdhCall.callphone("call",is_confirm:false,phone_number:'10086');
            },child: new Text("无需确认方式拨打电话"),),
            FlatButton(onPressed: (){
              XdhCall.callphone("call",is_confirm:true,phone_number:'10086');
            },child: new Text("需确认方式拨打电话"),),
            FlatButton(onPressed: (){
              XdhCall.endCall();
            },child: new Text("挂断电话"),),
            FlatButton(onPressed: (){
              _sendsm();
            },child: new Text("发送短信"),),
            FlatButton(onPressed: (){
                    _getUniqueId();
            },child: new Text("获取唯一设备id"),),

          ],)
        ),
      ),
    );
  }
}
