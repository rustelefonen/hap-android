package no.rustelefonen.hap.main.tabs.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.widget.EditText;
import android.widget.RadioButton;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import no.rustelefonen.hap.R;
import no.rustelefonen.hap.entities.User;
import no.rustelefonen.hap.notifications.AchievementScheduler;
import no.rustelefonen.hap.persistence.OrmLiteActivity;
import no.rustelefonen.hap.persistence.dao.UserDao;

import static no.rustelefonen.hap.util.DialogHelper.showDiscardChangesAndFinishDialog;

/**
 * Created by martinnikolaisorlie on 16/03/16.
 */
public class SavingsCalculatorActivity extends OrmLiteActivity {
    public static final String USER_INFO_EXTRA = "user_info_extra";
    private static final String CALCULATOR_PREFERENCES = "calculator_prefs";
    private static final String PRICE_PER_GRAM_KEY = "pricePerGramKey";
    private static final String GRAM_USEAGE_KEY = "gramUseageKey";
    private static final String USE_FREQUENCY_KEY = "userFrequencyKey";

    @BindView(R.id.user_price) TextInputEditText userPrice;
    @BindView(R.id.usage_gram) TextInputEditText gramUsage;
    @BindViews({R.id.per_day_usage, R.id.per_week_usage, R.id.per_month_usage}) List<RadioButton> usageFrequencyButtons;

    private User user;
    private SharedPreferences sharedPreferences;
    private Unbinder unbinder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.calculator_activity);
        super.onCreate(savedInstanceState);
        unbinder = ButterKnife.bind(this);
        user = getIntent().getParcelableExtra(USER_INFO_EXTRA);
        sharedPreferences = getSharedPreferences(CALCULATOR_PREFERENCES, Context.MODE_PRIVATE);
        initPreviousValues();
    }

    private void initPreviousValues() {
        userPrice.setText(loadPricePerGram());
        gramUsage.setText(loadGramUsage());
        usageFrequencyButtons.get(loadUseFrequency()).setChecked(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public void onBackPressed() {
        if (noChangesMade()) super.onBackPressed();
        else showDiscardChangesAndFinishDialog(this, getString(R.string.not_saved_title), getString(R.string.not_saved_text));
    }

    @OnClick(R.id.save_calculator_btn)
    public void saveCalculatorInfo() {
        if (isEmpty(userPrice)) {
            userPrice.setError(getString(R.string.price_error_string));
            return;
        }
        if (isEmpty(gramUsage)) {
            gramUsage.setError(getString(R.string.gram_error_string));
            return;
        }

        double pricePerGram = Double.parseDouble(userPrice.getText().toString());
        double gramsPerUse = Double.parseDouble(gramUsage.getText().toString());
        double gramsPerDay = calculateGramsPerDay(gramsPerUse);

        user.setMoneySpentPerDayOnHash(pricePerGram * gramsPerDay);
        new UserDao(this).persist(user);
        saveSelectedValues();

        Intent intent = new Intent();
        intent.putExtra(USER_INFO_EXTRA, user);
        setResult(RESULT_OK, intent);
        new AchievementScheduler(this).reScheduleNext();
        finish();
    }

    private boolean isEmpty(EditText editText) {
        return editText.getText().toString().trim().isEmpty();
    }

    private double calculateGramsPerDay(double grams) {
        int selectedPosition = getSelectedRadioButtonPosition();

        if (selectedPosition == 0) return grams;
        else if (selectedPosition == 1) return grams / 7;
        else return grams / 30;
    }

    private void saveSelectedValues() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(PRICE_PER_GRAM_KEY, userPrice.getText().toString());
        editor.putString(GRAM_USEAGE_KEY, gramUsage.getText().toString());
        editor.putInt(USE_FREQUENCY_KEY, getSelectedRadioButtonPosition());
        editor.apply();
    }

    private int getSelectedRadioButtonPosition(){
        for (int i = 0; i < usageFrequencyButtons.size(); i++){
            if(usageFrequencyButtons.get(i).isChecked()) return i;
        }
        return 0;
    }

    private boolean noChangesMade() {
        return userPrice.getText().toString().equals(loadPricePerGram()) &&
                gramUsage.getText().toString().equals(loadGramUsage()) &&
                getSelectedRadioButtonPosition() == loadUseFrequency();
    }

    private String loadPricePerGram() {
        return sharedPreferences.getString(PRICE_PER_GRAM_KEY, "150");
    }

    private String loadGramUsage() {
        return sharedPreferences.getString(GRAM_USEAGE_KEY, "1");
    }

    private int loadUseFrequency() {
        return sharedPreferences.getInt(USE_FREQUENCY_KEY, 0);
    }
}
