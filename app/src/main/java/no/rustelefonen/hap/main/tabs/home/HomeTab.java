package no.rustelefonen.hap.main.tabs.home;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import no.rustelefonen.hap.R;
import no.rustelefonen.hap.customviews.LazyTextView;
import no.rustelefonen.hap.customviews.clock.ClockView;
import no.rustelefonen.hap.entities.User;
import no.rustelefonen.hap.main.tabs.achievements.AchievementTab;
import no.rustelefonen.hap.main.tabs.activity.MainActivity;
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
        String secondAndThirdText = "Har du 5 minutter til å svare på en anonym oppfølgingsundersøkelse om app som hjelpetilbud? Undersøkelsen er åpen i 6 dager til.";
        String firstText = "Har du 10 minutter til å være med på en anonym undersøkelse om app som hjelpetilbud? Undersøkelsen er åpen i 6 dager til.";
        surveyCard.setVisibility(View.VISIBLE);
        surveyText.updateText(firstText);
    }

    @OnClick(R.id.survey_button)
    public void startSurvey() {
        Intent intent = new Intent(getContext(), SurveyActivity.class);
        String url = "https://no.surveymonkey.com/r/VC9RY62";
        intent.putExtra(SurveyActivity.ID, url);
        startActivity(intent);
    }
}
