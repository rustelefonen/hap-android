package no.rustelefonen.hap.main.tabs.home;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import no.rustelefonen.hap.R;
import no.rustelefonen.hap.customviews.LazyTextView;
import no.rustelefonen.hap.customviews.clock.ClockView;
import no.rustelefonen.hap.entities.Achievement;
import no.rustelefonen.hap.entities.User;
import no.rustelefonen.hap.main.tabs.achievements.AchievementTab;
import no.rustelefonen.hap.main.tabs.activity.MainActivity;
import no.rustelefonen.hap.notifications.AchievementReceiver;
import no.rustelefonen.hap.notifications.SurveyReceiver;
import no.rustelefonen.hap.persistence.dao.AchievementDao;
import no.rustelefonen.hap.persistence.dao.UserDao;
import no.rustelefonen.hap.tabs.misc.TabPage;

/**
 * Created by Fredrik on 19.01.2016.
 */
public class HomeTab extends TabPage {
    public static final int CALCULATOR_REQUEST = 2;

    @BindView(R.id.clock_label) LazyTextView clockLabel;
    @BindView(R.id.clock_view) ClockView clockView;
    @BindView(R.id.daily_subject_text) LazyTextView dailyTask;
    @BindView(R.id.money_saved_label) LazyTextView moneySavedLabel;
    @BindView(R.id.survey_text) LazyTextView surveyText;
    @BindView(R.id.survey_card) CardView surveyCard;
    @BindView(R.id.survey_button) LazyTextView surveyButton;

    private MainActivity mainActivity;
    private Handler handler;
    private Runnable timeUpdater;
    private DecimalFormat decimalFormat;
    private String[] tasks;
    private int currentTaskIndex = -1;
    private Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.home_tab, container, false);
        unbinder = ButterKnife.bind(this, v);
        mainActivity = (MainActivity) getActivity();
        handler = new Handler();
        initDecimalFormat();

        Resources res = getResources();
        tasks = res.getStringArray(R.array.daily_themes);

        return v;
    }

    private void initDecimalFormat() {
        decimalFormat = new DecimalFormat("###,##0.00");
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator(',');
        dfs.setGroupingSeparator(' ');
        decimalFormat.setDecimalFormatSymbols(dfs);
    }

    @Override
    public void onResume() {
        super.onResume();
        timeUpdater = new Runnable() {
            @Override
            public void run() {
                updateTimer();
                updateCalculatorLabel();
                updateDailyText();
                displaySurveyCard();
                handler.postDelayed(this, 1000);
            }
        };
        timeUpdater.run();
    }

    //Method calculates days hours and minutes from user.secondsSinceStarted()
    private void updateTimer() {
        User user = mainActivity.getUser();
        if (user == null || clockLabel == null || clockView == null) return;

        if (user.secondsSinceStarted() >= 0) clockLabel.updateText(R.string.program_started_clock_label);
        else clockLabel.updateText(R.string.program_not_started_clock_label);

        clockView.updateClock(Math.abs(user.secondsSinceStarted()));
    }

    private void updateCalculatorLabel() {
        User user = mainActivity.getUser();
        if (user == null || moneySavedLabel == null) return;

        if (user.getMoneySpentPerDayOnHash() <= 0) {
            moneySavedLabel.updateText(R.string.price_calculator_not_started);
        } else {
            String moneySaved = user.secondsSinceStarted() >= 0 ? decimalFormat.format(user.totalMoneySaved()) : "0";
            moneySavedLabel.updateText(String.format(getString(R.string.price_calculator_started), moneySaved));
        }
    }

    private void updateDailyText() {
        User user = mainActivity.getUser();
        if (user == null || dailyTask == null) return;

        int index = user.daysSinceStarted();
        if (index >= tasks.length) index = tasks.length - 1;
        else if (index < 0) index = 0;
        if(index == currentTaskIndex) return;
        currentTaskIndex = index;

        if (tasks.length > 0) dailyTask.updateText(tasks[index]);
        else dailyTask.updateText("Wow, ingen daglige temaer tilgjengelige");
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(timeUpdater);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.money_saved_label)
    public void startSavingsCalculator() {
        Intent intent = new Intent(getContext(), SavingsCalculatorActivity.class);
        intent.putExtra(SavingsCalculatorActivity.USER_INFO_EXTRA, mainActivity.getUser());
        startActivityForResult(intent, CALCULATOR_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == HomeTab.CALCULATOR_REQUEST && resultCode == Activity.RESULT_OK) {
            mainActivity.setUser((User) data.getExtras().getParcelable(SavingsCalculatorActivity.USER_INFO_EXTRA));
            EventBus.getDefault().post(new AchievementTab.RefreshAchievementListEvent());
        }
    }

    private void displaySurveyCard() {
        User user = mainActivity.getUser();
        if (user == null || dailyTask == null) return;

        if (user.getAppRegistered() == null) {
            surveyCard.setVisibility(View.GONE);
            return;
        }

        Date firstSurveyBegin = user.getAppRegistered();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(firstSurveyBegin);
        calendar.add(Calendar.DATE, 7);
        Date firstSurveyEnd = calendar.getTime();

        Date now = new Date();

        Date firstDateRegistered = user.getSurveyRegistered();

        if (firstDateRegistered == null) {
            if ((now.after(firstSurveyBegin) || now.equals(firstSurveyBegin)) && now.before(firstSurveyEnd)) {

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("hasRegisteredFirstSurvey", Context.MODE_PRIVATE);
                boolean hasRegisteredFirstSurvey = sharedPreferences.getBoolean("hasRegisteredFirstSurvey", false);

                if (!hasRegisteredFirstSurvey) {
                    surveyCard.setVisibility(View.VISIBLE);
                    updateSurveyText(firstSurveyEnd, true);
                }
                else surveyCard.setVisibility(View.GONE);
            }
            else surveyCard.setVisibility(View.GONE);
            return;
        }

        if (user.getSecondSurveyRegistered() == null) {
            Date secondDate = addDaysTo(firstDateRegistered, 56);
            Date secondDateEnd = addDaysTo(firstDateRegistered, 66);

            if ((now.after(secondDate) || now.equals(secondDate)) && now.before(secondDateEnd)) {
                surveyCard.setVisibility(View.VISIBLE);
                updateSurveyText(secondDateEnd, false);
                return;
            }
        }

        if (user.getThirdSurveyRegistered() == null) {
            Date thirdDate = addDaysTo(firstDateRegistered, 280);
            Date thirdDateEnd = addDaysTo(firstDateRegistered, 290);

            if ((now.after(thirdDate) || now.equals(thirdDate)) && now.before(thirdDateEnd)) {
                surveyCard.setVisibility(View.VISIBLE);
                updateSurveyText(thirdDateEnd, false);
                return;
            }
        }
        surveyCard.setVisibility(View.GONE);
    }

    private Date addDaysTo(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, days);
        return calendar.getTime();
    }

    private void updateSurveyText(Date date, boolean flag) {
        Date now = new Date();
        StringBuilder stringBuilder = new StringBuilder();
        if (flag) stringBuilder.append("Har du 10 minutter til å være med på en anonym undersøkelse om app som hjelpetilbud? Undersøkelsen er åpen i ");
        else stringBuilder.append("Har du 5 minutter til å svare på en anonym oppfølgingsundersøkelse om app som hjelpetilbud? Undersøkelsen er åpen i ");

        int secondsRemaining = Math.abs((int) (now.getTime() - date.getTime()) / 1000);

        if (secondsRemaining > 86400) {
            int days = secondsRemaining / 86400;
            if (days == 1) stringBuilder.append("1 dag");
            else {
                String concatString = days + " dager";
                stringBuilder.append(concatString);
            }
        }
        else if (secondsRemaining <= 86400 && secondsRemaining > 3600) {
            int hours = secondsRemaining / 3600;
            if (hours == 1) stringBuilder.append("1 time");
            else {
                String concatString = hours + " timer";
                stringBuilder.append(concatString);
            }
        }
        else if (secondsRemaining <= 3600 && secondsRemaining > 60) {
            int minutes = secondsRemaining / 60;
            if (minutes == 1) stringBuilder.append("1 minutt");
            else {
                String concatString = minutes + " minutter";
                stringBuilder.append(concatString);
            }
        }
        else if (secondsRemaining <= 60) {
            if (secondsRemaining == 1) stringBuilder.append("1 sekund");
            else {
                String concatString = secondsRemaining + " sekunder";
                stringBuilder.append(concatString);
            }
        }
        stringBuilder.append(" til.");
        surveyText.updateText(stringBuilder.toString());
    }

    @OnClick(R.id.survey_button)
    public void startSurvey() {
        User user = mainActivity.getUser();
        if (user == null || dailyTask == null) return;

        UserDao userDao = new UserDao(mainActivity);

        String url = "";

        String[] surveys = new String[]{"https://no.surveymonkey.com/r/VC9RY62", "https://no.surveymonkey.com/r/2RZ29SM", "https://no.surveymonkey.com/r/SGKKT2R"};
        String[] surveyAchievementTitles = new String[] {"Første undersøkelse utført!", "Andre undersøkelse utført!", "Tredje undersøkelse utført!"};
        String[] surveyAchievmentInfos = new String[]{"Første undersøkelse gjennomført, takk for at du deltok!", "Andre undersøkelse gjennomført, takk for at du deltok!", "Tredje undersøkelse gjennomført, takk for at du deltok!"};

        int currentSurvey = getCurrentSurvey();
        if (currentSurvey == 0 && user.getSurveyRegistered() == null) {
            SharedPreferences sharedPreferences = getContext().getSharedPreferences("hasRegisteredFirstSurvey", Context.MODE_PRIVATE);
            boolean hasRegisteredFirstSurvey = sharedPreferences.getBoolean("hasRegisteredFirstSurvey", false);

            if (hasRegisteredFirstSurvey) return;

            Date now = new Date();

            createNewSurveyAchievement(surveyAchievementTitles[currentSurvey], surveyAchievmentInfos[currentSurvey]);
            scheduleSurveyNotifications(now);

            user.setSurveyRegistered(now);
            userDao.persist(user);
            mainActivity.refreshUser();
            url = surveys[currentSurvey];

            sharedPreferences.edit().putBoolean("hasRegisteredFirstSurvey", true).apply();

        }
        else if (currentSurvey == 1 && user.getSurveyRegistered() != null && user.getSecondSurveyRegistered() == null) {
            createNewSurveyAchievement(surveyAchievementTitles[currentSurvey], surveyAchievmentInfos[currentSurvey]);
            user.setSurveyRegistered(new Date());
            userDao.persist(user);
            mainActivity.refreshUser();
            url = surveys[currentSurvey];
        }
        else if (currentSurvey == 2 && user.getSurveyRegistered() != null && user.getThirdSurveyRegistered() == null) {
            createNewSurveyAchievement(surveyAchievementTitles[currentSurvey], surveyAchievmentInfos[currentSurvey]);
            user.setSurveyRegistered(new Date());
            userDao.persist(user);
            mainActivity.refreshUser();
            url =  surveys[currentSurvey];
        }

        Intent intent = new Intent(getContext(), SurveyActivity.class);
        intent.putExtra(SurveyActivity.ID, url);
        startActivity(intent);
    }

    private void createNewSurveyAchievement(String title, String info) {
        AchievementDao achievementDao = new AchievementDao(mainActivity);
        Achievement achievement = new Achievement();
        achievement.setTitle(title);
        achievement.setDescription(info);
        achievement.setPointsRequired(1);
        achievement.setType(Achievement.Type.MILESTONE);
        achievementDao.persist(achievement);

        EventBus.getDefault().post(new AchievementTab.RefreshAchievementListEvent());
    }

    private void scheduleSurveyNotifications(Date date) {
        Date secondDate = addDaysTo(date, 56);
        Date thirdDate = addDaysTo(date, 280);

        Calendar cal = Calendar.getInstance();
        cal.setTime(secondDate);
        cal.add(Calendar.SECOND, 20);
        thirdDate = cal.getTime();

        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

        makeNotification(secondDate, alarmManager, 17541651);
        makeNotification(thirdDate, alarmManager, 17541652);
    }

    private void makeNotification(Date date, AlarmManager alarmManager, int id) {
        Intent notificationIntent = new Intent(getContext(), SurveyReceiver.class);
        //notificationIntent.putExtra(AchievementReceiver.NOTIFICATION, makeNotificationOf());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), id, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            alarmManager.setExact(AlarmManager.RTC, date.getTime(), pendingIntent); //Battery use increase :/
        else alarmManager.set(AlarmManager.RTC, date.getTime(), pendingIntent);
    }

    private int getCurrentSurvey() {
        User user = mainActivity.getUser();
        if (user == null || dailyTask == null) return -1;

        if (user.getAppRegistered() == null) return -1;

        Date firstSurveyBegin = user.getAppRegistered();
        Date firstSurveyEnd = addDaysTo(firstSurveyBegin, 7);
        Date now = new Date();

        Date firstDateRegistered = user.getSurveyRegistered();

        if (firstDateRegistered == null) {
            if ((now.after(firstSurveyBegin) || now.equals(firstSurveyBegin)) && now.before(firstSurveyEnd)) {

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("hasRegisteredFirstSurvey", Context.MODE_PRIVATE);
                boolean hasRegisteredFirstSurvey = sharedPreferences.getBoolean("hasRegisteredFirstSurvey", false);

                if (!hasRegisteredFirstSurvey) return 0;
                else return -1;
            }
            return -1;
        }

        if (user.getSecondSurveyRegistered() == null) {
            Date secondDate = addDaysTo(firstDateRegistered, 56);
            Date secondDateEnd = addDaysTo(firstDateRegistered, 66);

            if ((now.after(secondDate) || now.equals(secondDate)) && now.before(secondDateEnd)) {
                updateSurveyText(secondDateEnd, false);
                return 1;
            }
        }

        if (user.getThirdSurveyRegistered() == null) {
            Date thirdDate = addDaysTo(firstDateRegistered, 280);
            Date thirdDateEnd = addDaysTo(firstDateRegistered, 290);

            if ((now.after(thirdDate) || now.equals(thirdDate)) && now.before(thirdDateEnd)) {
                updateSurveyText(thirdDateEnd, false);
                return 2;
            }
        }
        return -1;
    }
}
