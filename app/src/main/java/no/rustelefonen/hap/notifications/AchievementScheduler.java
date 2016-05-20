package no.rustelefonen.hap.notifications;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import no.rustelefonen.hap.R;
import no.rustelefonen.hap.entities.Achievement;
import no.rustelefonen.hap.entities.User;
import no.rustelefonen.hap.main.tabs.activity.MainActivity;
import no.rustelefonen.hap.menudrawer.NotificationSettingsActivity;
import no.rustelefonen.hap.persistence.DatabaseHelper;
import no.rustelefonen.hap.persistence.dao.AchievementDao;
import no.rustelefonen.hap.persistence.dao.UserDao;

import static no.rustelefonen.hap.entities.Achievement.Type.FINANCE;
import static no.rustelefonen.hap.entities.Achievement.Type.HEALTH;
import static no.rustelefonen.hap.entities.Achievement.Type.MILESTONE;
import static no.rustelefonen.hap.entities.Achievement.Type.MINOR_MILESTONE;
import static no.rustelefonen.hap.menudrawer.NotificationSettingsActivity.ECONOMIC_TROPHY_KEY;
import static no.rustelefonen.hap.menudrawer.NotificationSettingsActivity.GRAND_TROPHY_KEY;
import static no.rustelefonen.hap.menudrawer.NotificationSettingsActivity.HEALTH_TROPHY_KEY;
import static no.rustelefonen.hap.menudrawer.NotificationSettingsActivity.SMALL_TROPHY_KEY;

/**
 * Created by Fredrik on 24.02.2016.
 */
public class AchievementScheduler {
    public static final String GROUP_ID = "group_key_achievements";

    private Context context;
    private AlarmManager alarmManager;

    public AchievementScheduler(Context context) {
        this.context = context;
        alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    }

    public void scheduleNext(){
        try {
            DatabaseHelper dbHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
            AchievementDao achievementDao = new AchievementDao(dbHelper);
            UserDao userDao = new UserDao(dbHelper);

            User user = userDao.getFirst();
            if (user == null) return;
            Achievement next = achievementDao.fetchNextUpcomingOfTypes(getAchievementTypesToSchedule(), user);
            if (next == null
                    || Double.isInfinite(next.secondsToCompletion(user))
                    || Double.isNaN(next.secondsToCompletion(user))) return;

            long millisToAdd = (long) (next.secondsToCompletion(user) * 1000);
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis() + millisToAdd);

            schedule(next, cal.getTimeInMillis());
        }
        finally {
            OpenHelperManager.releaseHelper();
        }
    }

    public void cancelNext(){
        PendingIntent pendingIntent = makePendingIntentFrom(makeNotificationIntent());
        alarmManager.cancel(pendingIntent);
    }

    public void reScheduleNext(){
        cancelNext();
        scheduleNext();
    }

    private void schedule(Achievement nextUpcomingAchievement, long fireDate){
        Intent notificationIntent = makeNotificationIntent();
        notificationIntent.putExtra(AchievementReceiver.NOTIFICATION_ID, 18541651); // same id causes a new incoming to replace current
        notificationIntent.putExtra(AchievementReceiver.NOTIFICATION, makeNotificationOf(nextUpcomingAchievement));

        PendingIntent pendingIntent = makePendingIntentFrom(notificationIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) alarmManager.setExact(AlarmManager.RTC, fireDate, pendingIntent); //Battery use increase :/
        else alarmManager.set(AlarmManager.RTC, fireDate, pendingIntent);
    }

    private Intent makeNotificationIntent(){
        return new Intent(context, AchievementReceiver.class);
    }

    private PendingIntent makePendingIntentFrom(Intent intent){
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private Notification makeNotificationOf(Achievement achievement){
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(MainActivity.NAV_TO_ACHIEVEMENT_EXTRA, true);
        PendingIntent activity = PendingIntent.getActivity(context, 0, intent, 0);

        Notification notification = new NotificationCompat.Builder(context)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setContentTitle(getApplicationName())
                .setContentText(context.getString(R.string.achievement_notification_text))
                .setSmallIcon(achievement.getImageId(true))
                .setContentIntent(activity)
                .setGroup(GROUP_ID)
                .build();

        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        return notification;
    }

    private String getApplicationName(){
        int stringId = context.getApplicationInfo().labelRes;
        return context.getString(stringId);
    }

    private List<Achievement.Type> getAchievementTypesToSchedule() {
        SharedPreferences  preferences = context.getSharedPreferences(NotificationSettingsActivity.SETTING_PREFERENCES, Context.MODE_PRIVATE);
        List<Achievement.Type> achievementTypesToSchedule = new ArrayList<>();

        if(preferences.getBoolean(SMALL_TROPHY_KEY, true)){
            achievementTypesToSchedule.add(MINOR_MILESTONE);
        }
        if(preferences.getBoolean(GRAND_TROPHY_KEY, true)){
            achievementTypesToSchedule.add(MILESTONE);
        }
        if(preferences.getBoolean(ECONOMIC_TROPHY_KEY, true)){
            achievementTypesToSchedule.add(FINANCE);
        }
        if(preferences.getBoolean(HEALTH_TROPHY_KEY, true)){
            achievementTypesToSchedule.add(HEALTH);
        }

        return achievementTypesToSchedule;
    }
}
