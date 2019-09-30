import 'package:flutter/material.dart';
import 'dart:async';

import 'package:device_notifications/device_notifications.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  DeviceNotificationStream _stream;
  StreamSubscription<DeviceNotificationEvent> _subscription;
  List<DeviceNotification> _notifications = new List();

  Map<String, IconData> categoryIcons = {
    'email': Icons.email,
    'msg': Icons.message,
    'reminder': Icons.event_available
  };

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    _stream = new DeviceNotificationStream();
    _subscription = _stream.listen(onDeviceNotificationEvent);
  }

  void onDeviceNotificationEvent(DeviceNotificationEvent event) {
    print('Device notification event ${event.action}, key: ${event.key}');
    setState(() {
      _notifications.removeWhere((n) => event.key == n.key);
      if (event.action != 'removed') {
        if (!event.notification.isGroupSummary()) {
          _notifications.add(event.notification);
        }
      }
    });
  }

  Widget buildNotificationCard(BuildContext context, int index) {
    var notification = _notifications[index];
    return Card(
      elevation: 4.0,
      child: InkWell(
        onTap: () {
          Navigator.push(context, MaterialPageRoute(builder: (context) => NotificationDetailsRoute(notification)));
        },
        child: ListTile(
          leading: Image.memory(notification.appIcon),
          //leading: Icon(categoryIcons[card.category] ?? Icons.do_not_disturb),
          title: Text(notification.title ?? '(No title)'),
          subtitle: Text(notification.text ?? '(No text)', overflow: TextOverflow.ellipsis),
        )
      )
    );
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      theme: ThemeData(
        brightness: Brightness.light,
      ),
      darkTheme: ThemeData(
        brightness: Brightness.dark,
      ),
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Device Notifications Example App'),
        ),
        body: Container(
          child: ListView.builder(
            scrollDirection: Axis.vertical,
            itemCount: _notifications.length,
            itemBuilder: (BuildContext context, int index) => buildNotificationCard(context, index)
          ),
        )
      ),
    );
  }
}

class NotificationDetailsRoute extends StatelessWidget {

  final DeviceNotification _notification;
  NotificationDetailsRoute(DeviceNotification notification) : _notification = notification;

  @override
  Widget build(BuildContext context) {
    var icon;
    if (_notification.icon != null) {
      icon = Image.memory(_notification.icon);
    }
    return Scaffold(
      appBar: AppBar(
        title: Text('Notification Details'),
      ),
      body: ListView(shrinkWrap: true, children: <Widget> [
        ListTile(
          title: GridView.count(
            shrinkWrap: true,
            crossAxisCount: 2,
            children: <Widget>[
              Card(
                //elevation: 8.0,
                child: Padding(
                  padding: EdgeInsets.all(8.0),
                  child: GridTile(
                    child: Center(
                      child: Image.memory(_notification.appIcon)
                    ),
                    footer: Text('appIcon', textAlign: TextAlign.center)
                  )
                )
              ),
              Card(
                //elevation: 8.0,
                child: Padding(
                  padding: EdgeInsets.all(8.0),
                  child: GridTile(
                    child: Center(
                      child: icon ?? Text('(No icon)')
                    ),
                    footer: Text('icon', textAlign: TextAlign.center)
                  )
                )
              ),
            ]
          )
        ),
          // ListTile(
          //   leading: Image.memory(_notification.appIcon),
          //   subtitle: Text('appIcon')
          // ),
          // ListTile(
          //   leading: icon,
          //   title: Text('${icon.width} x ${icon.height}'),
          //   subtitle: Text('icon')
          // ),
          //Divider(),
          ListTile(
            title: Text(_notification.id.toString()),
            subtitle: Text('id')
          ),
          ListTile(
            title: Text(_notification.key),
            subtitle: Text('key')
          ),
          ListTile(
            title: Text(_notification.number.toString()),
            subtitle: Text('number')
          ),
          ListTile(
            title: Text(_notification.priority.toString()),
            subtitle: Text('priority')
          ),
          ListTile(
            title: Text(_notification.category ?? '(No category)'),
            subtitle: Text('category')
          ),
          ListTile(
            title: Text(_notification.flags.toRadixString(16)),
            subtitle: Text('flags')
          ),
          ListTile(
            title: Text(_notification.appName ?? '(No appName)'),
            subtitle: Text('appName')
          ),
          ListTile(
            title: Text(_notification.title ?? '(No title)'),
            subtitle: Text('title')
          ),
          ListTile(
            title: Text(_notification.text ?? '(No text)'),
            subtitle: Text('text')
          ),
          ListTile(
            title: Text(_notification.channelId ?? '(No channelId)'),
            subtitle: Text('channelId')
          ),
          ListTile(
            title: Text(_notification.group ?? '(No group)'),
            subtitle: Text('group')
          ),
          ListTile(
            title: Text(_notification.visibility.toString()),
            subtitle: Text('visibilty')
          ),
          ListTile(
            title: Text(DateTime.fromMillisecondsSinceEpoch(_notification.when).toString()),
            subtitle: Text('when')
          ),
        ]
      )
    );
  }
}
