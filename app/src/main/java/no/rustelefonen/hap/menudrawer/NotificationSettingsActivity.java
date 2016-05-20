package no.rustelefonen.hap.menudrawer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Switch;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import no.rustelefonen.hap.R;
import no.rustelefonen.hap.notifications.AchievementScheduler;
import no.rustelefonen.hap.persistence.OrmLiteActivity;

import static no.rustelefonen.hap.util.DialogHelper.showDiscardChangesAndFinishDialog;

/**
 * Created by martinnikolaisorlie on 08/04/16.
 */
public class NotificationSettingsActivity extends OrmLiteActivity{
    public static final String SETTING_PREFERENCES = "Settings_prefs";
    public static final String SMALL_TROPHY_KEY = "smallTrophyKey";
    public static final String GRAND_TROPHY_KEY = "grandTrophyKey";
    public static final String ECONOMIC_TROPHY_KEY = "economicTrophyKey";
    public static final String HEALTH_TROPHY_KEY = "healthTrophyKey";

    @BindView(R.id.small_trophy_switch) Switch smallTrophySwitch;
    @BindView(R.id.grand_trophy_switch) Switch grandTrophySwitch;
    @BindView(R.id.economic_trophy_switch) Switch economicTrophySwitch;
    @BindView(R.id.health_trophy_switch) Switch healthTrophySwitch;

    private SharedPreferences sharedPreferences;
    private Unbinder unbinder;

    public void onCreate(Bundle savedInstanceState){
        setContentView(R.layout.notification_settings_activity);
        super.onCreate(savedInstanceState);
        unbinder = ButterKnife.bind(this);
        initToolbar();
        sharedPreferences = getSharedPreferences(SETTING_PREFERENCES, MODE_PRIVATE);
        initPreviousValues();
    }

    private void initPreviousValues() {
        smallTrophySwitch.setChecked(loadMinorMilestoneSetting());
        grandTrophySwitch.setChecked(loadMilestoneSetting());
        economicTrophySwitch.setChecked(loadEconomicSetting());
        healthTrophySwitch.setChecked(loadHealthSetting());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.save) {
            saveSelectedValues();
            new AchievementScheduler(this).reScheduleNext();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveSelectedValues() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(SMALL_TROPHY_KEY, smallTrophySwitch.isChecked());
        editor.putBoolean(GRAND_TROPHY_KEY, grandTrophySwitch.isChecked());
        editor.putBoolean(ECONOMIC_TROPHY_KEY, economicTrophySwitch.isChecked());
        editor.putBoolean(HEALTH_TROPHY_KEY, healthTrophySwitch.isChecked());
        editor.apply();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public void onBackPressed() {
        if (noChangesMade()) super.onBackPressed();
        else showDiscardChangesAndFinishDialog(this, getString(R.string.not_saved_title), getString(R.string.not_saved_text));
    }

    private boolean noChangesMade() {
        return smallTrophySwitch.isChecked() == loadMinorMilestoneSetting() &&
                grandTrophySwitch.isChecked() == loadMilestoneSetting() &&
                economicTrophySwitch.isChecked() == loadEconomicSetting() &&
                healthTrophySwitch.isChecked() == loadHealthSetting();
    }

    private boolean loadMinorMilestoneSetting() {
        return sharedPreferences.getBoolean(SMALL_TROPHY_KEY, true);
    }

    private boolean loadMilestoneSetting() {
        return sharedPreferences.getBoolean(GRAND_TROPHY_KEY, true);
    }

    private boolean loadEconomicSetting() {
        return sharedPreferences.getBoolean(ECONOMIC_TROPHY_KEY, true);
    }

    private boolean loadHealthSetting() {
        return sharedPreferences.getBoolean(HEALTH_TROPHY_KEY, true);
    }
}