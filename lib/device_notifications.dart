import 'dart:async';
import 'dart:io' show Platform;
import 'dart:typed_data';

import 'package:flutter/services.dart';

class DeviceNotification {
  static const int FLAG_GROUP_SUMMARY = 0x00000200;

  int id;
  String key;
  int priority;
  int when;
  int number;
  int visibility;
  int flags;
  String category;
  String appName;
  Uint8List appIcon;
  String title;
  String text;
  Uint8List icon;
  Map extras;
  String channelId;
  String group;

  DeviceNotification(int id, String key, Map n) {
    this.id = id;
    this.key = key;
    extras = n['extras'];
    category = n['category'];
    priority = n['priority'];
    when = n['when'];
    number = n['number'];
    visibility = n['visibility'];
    flags = n['flags'];
    appName = n['appName'];
    appIcon = n['appIcon'];
    title = n['title'];
    text = n['text'];
    icon = n['icon'];
    channelId = n['channelId'];
    group = n['group'];
  }

  bool isGroupSummary() {
    return flags & FLAG_GROUP_SUMMARY != 0;
  }
}

class DeviceNotificationEvent {
  int id;
  String key;
  String action;
  DeviceNotification notification;

  DeviceNotificationEvent(Map e) {
    id = e['id'];
    key = e['key'];
    action = e['action'];
    var n = e['notification'];
    if (n != null) {
      notification = new DeviceNotification(id, key, n);
    }
  }
}

class DeviceNotificationStream {
  static const EventChannel _channel = const EventChannel('device_notifications');
  static Stream<DeviceNotificationEvent> _stream;

  /// Obtain a stream subscription to device notifications.
  StreamSubscription<DeviceNotificationEvent> listen(void onData(DeviceNotificationEvent event)) {
    if (!Platform.isAndroid) {
      throw Exception('Device notifications are only available on Android');
    }
    if (_stream == null) {
      _stream = _channel.receiveBroadcastStream().map((event) => DeviceNotificationEvent(event));
    }
    return _stream.listen(onData);
  }
}
