package no.rustelefonen.hap.notifications;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Fredrik on 24.02.2016.
 *
 * Source: Source: https://gist.github.com/BrandonSmith/6679223
 */
public class AchievementReceiver extends BroadcastReceiver{
    public static String NOTIFICATION_ID = "notification-id";
    public static String NOTIFICATION = "notification";

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        int id = intent.getIntExtra(NOTIFICATION_ID, 0);
        Notification notification = intent.getParcelableExtra(NOTIFICATION);
        notificationManager.notify(id, notification);
        EventBus.getDefault().post(new NotificationReceiveListener());

        new AchievementScheduler(context).scheduleNext();
    }

    public static class NotificationReceiveListener{}
}
