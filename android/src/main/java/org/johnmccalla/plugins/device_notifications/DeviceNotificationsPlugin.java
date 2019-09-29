package org.johnmccalla.plugins.device_notifications;

import java.util.HashMap;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.plugin.common.EventChannel.EventSink;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

/** DeviceNotificationsPlugin */
public class DeviceNotificationsPlugin implements EventChannel.StreamHandler {

    private static final String TAG = "DeviceNotificationsPlugin";

    private Context context;
    private Intent serviceIntent;
    private NotificationReceiver notificationReceiver;
    private EventSink eventSink;
    private HashMap<Integer, Object> activeNotifications;

    /** Plugin registration */
    public static void registerWith(Registrar registrar) {
        final EventChannel channel = new EventChannel(registrar.messenger(), "device_notifications");
        channel.setStreamHandler(new DeviceNotificationsPlugin(registrar.activeContext()));
    }

    /** Plugin constructor */
    private DeviceNotificationsPlugin(Context context) {
        this.context = context;
        serviceIntent = new Intent(context, DeviceNotificationsService.class);
        activeNotifications = new HashMap<>() ;
    }

    /** Called whenever the event channel is subscribed to in Flutter */
    @Override
    public void onListen(Object o, EventSink eventSink) {
        Log.d(TAG, "Event sink listening");
        this.eventSink = eventSink;
        notificationReceiver = new NotificationReceiver();
        context.registerReceiver(notificationReceiver, new IntentFilter(DeviceNotificationsService.INTENT));
        context.startService(serviceIntent);
    }

    /** Called whenever the event channel subscription is cancelled in Flutter */
    @Override
    public void onCancel(Object o) {
        Log.d(TAG, "Event sink cancelled");
        context.stopService(serviceIntent);
        context.unregisterReceiver(notificationReceiver);
        notificationReceiver = null;
        eventSink = null;
        activeNotifications.clear();
    }

    class NotificationReceiver extends BroadcastReceiver {

        private static final String TAG = "NotificationReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Received intent " + intent.toString());
            eventSink.success(intent.getSerializableExtra(DeviceNotificationsService.INTENT_EVENT));
        }
    }
}
