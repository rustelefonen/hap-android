package no.rustelefonen.hap.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import no.rustelefonen.hap.R;
import no.rustelefonen.hap.entities.Achievement;
import no.rustelefonen.hap.main.tabs.activity.MainActivity;
import no.rustelefonen.hap.persistence.dao.AchievementDao;

/**
 * Created by simenfonnes on 16.06.2017.
 */

public class SurveyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        PendingIntent activity = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);

        Notification notification = new NotificationCompat.Builder(context)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setContentTitle("HAP")
                .setContentText("Ny undersøkelse!")
                .setContentIntent(activity)
                .setSmallIcon(R.drawable.milestone)
                .build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(12345, notification);
    }
}
