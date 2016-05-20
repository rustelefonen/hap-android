package no.rustelefonen.hap.main.tabs.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Date;

import no.rustelefonen.hap.R;
import no.rustelefonen.hap.entities.UserTrigger;
import no.rustelefonen.hap.intro.IntroActivity;
import no.rustelefonen.hap.main.tabs.achievements.AchievementTab;
import no.rustelefonen.hap.main.tabs.program.ProgramTab;
import no.rustelefonen.hap.menudrawer.AboutActivity;
import no.rustelefonen.hap.menudrawer.NotificationSettingsActivity;
import no.rustelefonen.hap.persistence.dao.UserDao;
import no.rustelefonen.hap.util.DialogHelper;

/**
 * Created by Fredrik on 04.05.2016.
 */
public class MainDrawerItemHandler implements NavigationView.OnNavigationItemSelectedListener {
    private MainActivity activity;
    private DrawerLayout drawer;

    public MainDrawerItemHandler(MainActivity activity, DrawerLayout drawer) {
        this.activity = activity;
        this.drawer = drawer;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.notification_settings:
                activity.startActivity(new Intent(activity, NotificationSettingsActivity.class));
                drawer.closeDrawers();
                return true;
            case R.id.about_app:
                Intent about = new Intent(activity, AboutActivity.class);
                activity.startActivity(about);
                drawer.closeDrawers();
                return true;
            /*case R.id.research_settings:
                Intent research_intent =
                startActivity(new Intent(this, ResearchSettingsActivity.class));
                return true;*/
            case R.id.reset_timer:
                showResetTimerDialog();
                return true;
            case R.id.reset_positive_triggers:
                showResetTriggersDialog("Nullstill positive triggere", true);
                return true;
            case R.id.reset_negative_triggers:
                showResetTriggersDialog("Nullstill negative triggere", false);
                return true;
            case R.id.reset_app:
                showResetAppAlertDialog();
                return true;
        }
        return false;
    }

    private void showResetAppAlertDialog() {
        DialogHelper.showConfirmDialogWithAction(activity,
                "Tilbakestill applikasjonen",
                "Denne handlingen kan ikke angres!",
                "Tilbakestill",
                new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        new UserDao(activity).delete(activity.getUser());
                        activity.getSharedPreferences(MainTabPageHandler.TAB_BADGE_VALUES, Activity.MODE_PRIVATE).edit().clear().commit();
                        activity.startActivity(new Intent(activity, IntroActivity.class));
                        activity.finish();
                    }
                });
    }

    private void showResetTimerDialog() {
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle("Nullstill tidtaker")
                .setMessage("Vil du nullstille bare tidtaker, eller både tidtaker og oppnådde prestasjoner?\n\nDenne handlingen kan ikke angres!")
                .setNeutralButton("Avbryt", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setNegativeButton("Bare tidtaker", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        activity.getUser().setSecondsLastedBeforeLastReset(activity.getUser().secondsSinceStarted());
                        activity.getUser().setStartDate(new Date());
                        new UserDao(activity).persist(activity.getUser());
                        drawer.closeDrawers();
                        EventBus.getDefault().post(new AchievementTab.RefreshAchievementListEvent());
                    }
                })
                .setPositiveButton("Tidtaker og prestasjoner", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        activity.getUser().setSecondsLastedBeforeLastReset(0);
                        activity.getUser().setStartDate(new Date());
                        new UserDao(activity).persist(activity.getUser());
                        drawer.closeDrawers();
                        EventBus.getDefault().post(new AchievementTab.RefreshAchievementListEvent());
                    }
                })
                .create();

        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.RED);
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.RED);
    }

    private void showResetTriggersDialog(String dialogTitle, final boolean isPositiveTrigger) {
        DialogHelper.showConfirmDialogWithAction(activity,
                dialogTitle,
                "Denne handlingen kan ikke angres!",
                "Nullstill",
                new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        if (isPositiveTrigger)
                            activity.getUser().setResistedTriggers(new ArrayList<UserTrigger>());
                        else activity.getUser().setSmokedTriggers(new ArrayList<UserTrigger>());
                        new UserDao(activity).persist(activity.getUser());
                        EventBus.getDefault().post(new ProgramTab.TriggerResetEvent());
                        drawer.closeDrawers();
                    }
                });
    }
}

//        DialogHelper.showConfirmDialogWithAction(this,
//                "Tilbakestill tidstelleren",
//                "Vil du tilbakestille tidstelleren og dine prestasjoner? Denne handlingen kan ikke angres!",
//                "Tilbakestill",
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        final int mYear;
//                        final int mMonth;
//                        final int mDay;
//
//
//                        final Calendar c = Calendar.getInstance();
//                        mYear = c.get(Calendar.YEAR);
//                        mMonth = c.get(Calendar.MONTH);
//                        mDay = c.get(Calendar.DAY_OF_MONTH);
//
//                        DatePickerDialog dpd = new DatePickerDialog((MainActivity.this),
//                                new DatePickerDialog.OnDateSetListener() {
//
//                                    @Override
//                                    public void onDateSet(DatePicker view, int year,
//                                                          int monthOfYear, int dayOfMonth) {
//
//                                        try {
//                                            c.set(Calendar.YEAR, year);
//                                            c.set(Calendar.MONTH, monthOfYear);
//                                            c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
//                                            userInfo.setStartDate(c.getTime());
//                                            new UserDao(MainActivity.this).persist(userInfo);
//                                            EventBus.getDefault().post(new AchievementTab.RefreshAchievementListEvent());
//                                            getSharedPreferences(TAB_BADGE_VALUES, MODE_PRIVATE).edit().clear().commit();
//                                        } catch (SQLException ignored) {
//                                            Log.v("except", "" + ignored);
//                                        }
//                                    }
//                                }, mYear, mMonth, mDay);
//
//                        dpd.show();
//                    }
//                });