package no.rustelefonen.hap.introscreen;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.DatePicker;

import com.robotium.solo.Solo;

import no.rustelefonen.hap.R;
import no.rustelefonen.hap.SplashActivity;
import no.rustelefonen.hap.intro.IntroActivity;
import no.rustelefonen.hap.intro.UserDetailsTab;
import no.rustelefonen.hap.main.tabs.activity.MainActivity;


/**
 * Created by martinnikolaisorlie on 26/02/16.
 */

public class IntroActivityTest extends ActivityInstrumentationTestCase2<SplashActivity> {

    private Solo solo;

    public IntroActivityTest() {
        super(SplashActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        solo = new Solo(getInstrumentation());
        Intent intent = new Intent(getActivity(), IntroActivity.class);
        getActivity().startActivity(intent);
        SharedPreferences prefs = getActivity().getSharedPreferences(UserDetailsTab.INTRO_RESEARCH_SENT,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    public void testIntro() throws Exception {
        solo.assertCurrentActivity("Test", IntroActivity.class);
        solo.waitForView(solo.getView(R.id.intro_viewpager));
        solo.scrollViewToSide(solo.getView(no.rustelefonen.hap.R.id.intro_viewpager), Solo.RIGHT);
        //Scroll View to the right side
        solo.scrollViewToSide(solo.getView(no.rustelefonen.hap.R.id.intro_viewpager), Solo.RIGHT);


        DatePicker datePicker = (DatePicker) solo.getView(R.id.datePicker);
        solo.waitForView(R.id.datePicker);
        solo.setDatePicker(0, 2016, 4, 2); // 2. mai 2016
        solo.sleep(1000);
        assertEquals(datePicker.getYear(), 2016);
        assertEquals(datePicker.getMonth(), 4);
        assertEquals(datePicker.getDayOfMonth(), 2);

        solo.scrollViewToSide(solo.getView(R.id.intro_viewpager), Solo.RIGHT);

        View research_data_input_layout = solo.getCurrentActivity().findViewById(R.id.user_research_data);
        assertFalse("View is not visible", research_data_input_layout.isShown());

        solo.waitForView(R.id.info_research_agree_switch);
        solo.clickOnView(solo.getView(R.id.info_research_agree_switch));
        solo.sleep(1000);
        solo.waitForView(research_data_input_layout);
        assertTrue("View is now visible", research_data_input_layout.isShown());

        solo.waitForView(R.id.age_list);
        solo.pressSpinnerItem(0, 14);
        assertTrue(solo.isSpinnerTextSelected("26"));

        solo.pressSpinnerItem(1, 1);
        assertTrue(solo.isSpinnerTextSelected("Mann"));

        solo.pressSpinnerItem(2, 1);
        assertTrue(solo.isSpinnerTextSelected("Akershus"));

        assertEquals("Start appen nå", solo.getButton(1).getText());
        solo.clickOnButton(1);

        solo.assertCurrentActivity("Correct mainactivity loaded", MainActivity.class);
    }


    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }
}
