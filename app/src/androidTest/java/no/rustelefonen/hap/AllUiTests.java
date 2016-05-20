package no.rustelefonen.hap;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.content.SharedPreferencesCompat;
import android.test.ActivityInstrumentationTestCase2;

import junit.framework.TestSuite;

import no.rustelefonen.hap.introscreen.IntroActivityTest;
import no.rustelefonen.hap.mainscreen.MainActivityTest;

/**
 * Created by martinnikolaisorlie on 06/05/16.
 */
public class AllUiTests extends ActivityInstrumentationTestCase2<Activity> {

    public AllUiTests(Class<Activity> activityClass) {
        super(activityClass);
        SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    public static TestSuite suite(){
        TestSuite t = new TestSuite();
        t.addTestSuite(IntroActivityTest.class);
        t.addTest(TestSuite.createTest(MainActivityTest.class, "testHomeToNotificationSettings"));
        t.addTest(TestSuite.createTest(MainActivityTest.class, "testSavingsCalculator"));
        t.addTest(TestSuite.createTest(MainActivityTest.class, "testWholeMainApplication"));
        return t;
    }

    @Override
    public void setUp() throws Exception{}

    @Override
    public void tearDown() throws Exception{}
}
