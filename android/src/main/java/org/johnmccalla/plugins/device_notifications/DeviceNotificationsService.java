package org.johnmccalla.plugins.device_notifications;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import android.app.Notification;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Parcel;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

// TODO: need an interface to allow access to getting active notifications

public class DeviceNotificationsService extends NotificationListenerService {

    private static final String TAG = "DeviceNotificationsService";
    public static final String INTENT = "device_notification_event";
    // public static final String INTENT_ID = "id";
    // public static final String INTENT_ACTION = "action";
    // public static final String INTENT_NOTIFICATION = "notification";
    // public static final String ACTION_POSTED = "posted";
    // public static final String ACTION_REMOVED = "removed";
    public static final String INTENT_EVENT = "event";
    public static final String EVENT_ID = "id";
    public static final String EVENT_KEY = "key";
    public static final String EVENT_ACTION = "action";
    public static final String EVENT_NOTIFICATION = "notification";
    public static final String ACTION_POSTED = "posted";
    public static final String ACTION_REMOVED = "removed";
    public static final String ACTION_LISTED = "listed";

    private boolean connected = false;

    @Override
    public void onListenerConnected() {
        Log.d(TAG, "Listener connected");
        connected = true;
    }
    @Override
    public void onListenerDisconnected() {
        Log.d(TAG, "Listener disconnected");
        connected = false;
    }

    /**
     * Handles start commands by sending active notifications.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (connected) {
            StatusBarNotification[] notifications = getActiveNotifications();
            Log.d(TAG, "Start command received, " + notifications.length + " notifications are active");
            for (StatusBarNotification sbn : notifications) {
                Log.d(TAG, "Listed notification: " + sbn.getKey());
                sendNotification(ACTION_LISTED, sbn);
            }
        }
        else {
            Log.e(TAG, "Start command received while listener is not connected");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Handles posted notifications by sending them to the connected apps.
     */
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.d(TAG, "Posted notification: " + sbn.getKey());
        sendNotification(ACTION_POSTED, sbn);
    }

    /**
     * Handles posted notifications by sending them to the connected apps.
     */
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.d(TAG, "Removed notification " + sbn.getKey());
        Intent intent = new Intent(INTENT);
        HashMap<String, Object> event = new HashMap<>();
        event.put(EVENT_ID, sbn.getId());
        event.put(EVENT_KEY, sbn.getKey());
        event.put(EVENT_ACTION, ACTION_REMOVED);
        intent.putExtra(INTENT_EVENT, event);
        // intent.putExtra(INTENT_ID, sbn.getId());
        // intent.putExtra(INTENT_ACTION, ACTION_REMOVED);
        sendBroadcast(intent);
    }

    /**
     * Sends the notification by stuffing the notification into the intent
     * that will be broadcast and caught by the main app's activity. Nested
     * structures are not supported for now and will be "toString'ed".
     */
    private void sendNotification(String action, StatusBarNotification sbn) {
        final Intent intent = new Intent(INTENT);
        final HashMap<String, Object> event = new HashMap<>();
        event.put(EVENT_ID, sbn.getId());
        event.put(EVENT_KEY, sbn.getKey());
        event.put(EVENT_ACTION, action);

        Notification n = sbn.getNotification();
        HashMap<String, Object> notification = new HashMap<>();
        notification.put("channelId", n.getChannelId());
        notification.put("group", n.getGroup());
        notification.put("flags", n.flags);
        notification.put("category", n.category);
        notification.put("number", n.number);
        notification.put("priority", n.priority);
        notification.put("visibility", n.visibility);
        notification.put("when", n.when);

        if (n.extras != null) {
            final PackageManager pm = getApplicationContext().getPackageManager();
            final ApplicationInfo appInfo = (ApplicationInfo) n.extras.get("android.appInfo");
            if (appInfo != null) {
                if (appInfo.name != null) {
                    notification.put("appName", pm.getApplicationLabel(appInfo));
                }
                Drawable appIcon = pm.getApplicationIcon(appInfo);
                if (appIcon != null) {
                    if (appIcon instanceof AdaptiveIconDrawable) {
                        appIcon = ((AdaptiveIconDrawable)appIcon).getForeground();
                    }
                    notification.put("appIcon", serializeDrawable(appIcon));
                }
            }

            final Icon largeIcon = (Icon) n.extras.get("android.largeIcon");
            if (largeIcon != null) {
                notification.put("icon", serializeDrawable(largeIcon.loadDrawable(getApplicationContext())));
            }

            final Object title = n.extras.get("android.title");
            if (title != null) {
                notification.put("title", title.toString());
            }

            final Object text = n.extras.get("android.text");
            if (text != null) {
                notification.put("text", text.toString());
            }

            final HashMap<String, Object> extras = new HashMap<>();
            for (String key : n.extras.keySet()) {
                final Object value = n.extras.get(key);
                if (value != null) {
                    extras.put(key, value.toString());
                }
            }
            notification.put("extras", extras);
        }

        // TODO: deal with icons
        // Icon icon = notification.getLargeIcon();
        // if (icon == null) {
        //     icon = notification.getSmallIcon();
        // }
        // if (icon != null) {
        //     notificationMap.put("icon", icon.getType());
        // }

        event.put(EVENT_NOTIFICATION, notification);
        intent.putExtra(INTENT_EVENT, event);
        sendBroadcast(intent);

        // intent.putExtra(INTENT_ID, sbn.getId());
        // intent.putExtra(INTENT_ACTION, ACTION_POSTED);
        // intent.putExtra(INTENT_NOTIFICATION, notificationMap);
    }

    private byte[] serializeDrawable(Drawable d) {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        final Bitmap bitmap = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        d.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        d.draw(canvas);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
        return os.toByteArray();
    }
}