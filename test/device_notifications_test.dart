import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:device_notifications/device_notifications.dart';

void main() {
  const MethodChannel channel = MethodChannel('device_notifications');

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await DeviceNotifications.platformVersion, '42');
  });
}
