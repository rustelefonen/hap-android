package no.rustelefonen.hap.mainscreen;


import android.support.design.widget.AppBarLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.robotium.solo.By;
import com.robotium.solo.Solo;
import com.robotium.solo.Timeout;

import no.rustelefonen.hap.R;
import no.rustelefonen.hap.entities.User;
import no.rustelefonen.hap.main.tabs.activity.MainActivity;
import no.rustelefonen.hap.main.tabs.info.InfoDetailActivity;
import no.rustelefonen.hap.menudrawer.NotificationSettingsActivity;
import no.rustelefonen.hap.persistence.dao.UserDao;

/**
 * Created by martinnikolaisorlie on 05/05/16.
 */


public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity>{

    private Solo solo;

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Override
    public void setUp() throws Exception{
        super.setUp();
        User user = new User();
        UserDao dao = new UserDao(getActivity());
        if(dao.getFirst() == null){
            dao.persist(user);
        }
        solo = new Solo(getInstrumentation(), getActivity());

    }

    public void openCompatNavigationDrawer() {
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                ((DrawerLayout) solo.getView(R.id.drawer_layout))
                        .openDrawer(GravityCompat.START);
            }
        });
    }

    public void uglyHack() {
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                AppBarLayout appBarLayout = (AppBarLayout) solo.getView(R.id.toolbar_top_layout);
                appBarLayout.setExpanded(false);
            }
        });
    }

    public void forceSwitchToBeChecked(){
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                ((Switch) solo.getView(R.id.health_trophy_switch)).setChecked(true);
            }
        });
    }


    public void testHomeToNotificationSettings() throws Exception{
        solo.assertCurrentActivity("Start on hometab", MainActivity.class);
        openCompatNavigationDrawer();
        solo.waitForView(solo.getView(R.id.drawer_layout));
        solo.clickOnText("Varslingsinnstillinger");
        solo.waitForActivity(NotificationSettingsActivity.class);
        solo.assertCurrentActivity("NotificationActivity  should be open", NotificationSettingsActivity.class);
        Switch switcher = (Switch) solo.getView(R.id.health_trophy_switch);
        forceSwitchToBeChecked();
        solo.clickOnView(solo.getView(R.id.health_trophy_switch));
        solo.clickOnMenuItem("Lagre");
        solo.sleep(1000);
        openCompatNavigationDrawer();
        solo.clickOnText("Varslingsinnstillinger");
        assertFalse(switcher.isChecked());
    }

    @Override
    public void tearDown() throws Exception{
        solo.finishOpenedActivities();
        super.tearDown();
    }


    public void testSavingsCalculator()throws Exception{
        //Wait for activity: 'no.rustelefonen.hap.SplashActivity'
        solo.waitForActivity(no.rustelefonen.hap.SplashActivity.class, 2000);
        //Wait for activity: 'no.rustelefonen.hap.main.tabs.activity.MainActivity'
        assertTrue("no.rustelefonen.hap.main.tabs.activity.MainActivity is not found!", solo.waitForActivity(no.rustelefonen.hap.main.tabs.activity.MainActivity.class));
        //Set default small timeout to 12865 milliseconds
        Timeout.setSmallTimeout(12865);

        TextView textView = (TextView) solo.getView(R.id.money_saved_label);
        //Click on Start Sparekalkulator
        solo.clickOnView(solo.getView(no.rustelefonen.hap.R.id.money_saved_label));
        //Wait for activity: 'no.rustelefonen.hap.main.tabs.home.SavingsCalculatorActivity'
        assertTrue("no.rustelefonen.hap.main.tabs.home.SavingsCalculatorActivity is not found!", solo.waitForActivity(no.rustelefonen.hap.main.tabs.home.SavingsCalculatorActivity.class));
        //Click on 150
        solo.clickOnView(solo.getView(no.rustelefonen.hap.R.id.user_price));
        //Enter the text: '175'
        solo.clearEditText((android.widget.EditText) solo.getView(no.rustelefonen.hap.R.id.user_price));
        solo.enterText((android.widget.EditText) solo.getView(no.rustelefonen.hap.R.id.user_price), "175");
        assertEquals("175", solo.getEditText(0).getText().toString());
        //Click on 20
        solo.clickOnView(solo.getView(no.rustelefonen.hap.R.id.usage_gram));
        //Enter the text: '15'
        solo.clearEditText((android.widget.EditText) solo.getView(no.rustelefonen.hap.R.id.usage_gram));
        solo.enterText((android.widget.EditText) solo.getView(no.rustelefonen.hap.R.id.usage_gram), "15");
        assertEquals("15", solo.getEditText(1).getText().toString());
        //Press next button
        solo.pressSoftKeyboardNextButton();
        //Click on Per dag
        solo.clickOnView(solo.getView(no.rustelefonen.hap.R.id.per_day_usage));
        //Click on lagre
        solo.clickOnView(solo.getView(no.rustelefonen.hap.R.id.save_calculator_btn));
        //Click on Du har spart 2 669,48 kr!
        solo.clickOnView(solo.getView(no.rustelefonen.hap.R.id.money_saved_label));
        //Wait for activity: 'no.rustelefonen.hap.main.tabs.home.SavingsCalculatorActivity'
        assertTrue("no.rustelefonen.hap.main.tabs.home.SavingsCalculatorActivity is not found!", solo.waitForActivity(no.rustelefonen.hap.main.tabs.home.SavingsCalculatorActivity.class));

        assertFalse("Start sparekalkulator should not be showing", textView.getText().equals("Start sparekalkulator nå"));

    }

    public void testWholeMainApplication() throws Exception {
        //Wait for activity: 'no.rustelefonen.hap.SplashActivity'
        solo.waitForActivity(no.rustelefonen.hap.SplashActivity.class, 2000);
        //Wait for activity: 'no.rustelefonen.hap.main.tabs.activity.MainActivity'
        assertTrue("no.rustelefonen.hap.main.tabs.activity.MainActivity is not found!", solo.waitForActivity(no.rustelefonen.hap.main.tabs.activity.MainActivity.class));
        //Scroll View to the right side
        solo.scrollViewToSide(solo.getView(no.rustelefonen.hap.R.id.viewpager), Solo.RIGHT);
        //Scroll View to the right side
        solo.scrollViewToSide(solo.getView(no.rustelefonen.hap.R.id.viewpager), Solo.RIGHT);
        //Click on Neste prestasjon Første dagen uten! Du har klart din første dag uten cannabis!
        solo.clickOnView(solo.getView(no.rustelefonen.hap.R.id.next_upcoming_card));
        //Wait for activity: 'no.rustelefonen.hap.main.tabs.achievements.AllAchievementsActivity'
        assertTrue("no.rustelefonen.hap.main.tabs.achievements.AllAchievementsActivity is not found!", solo.waitForActivity(no.rustelefonen.hap.main.tabs.achievements.AllAchievementsActivity.class));
        //assertTrue("test", solo.waitForView(solo.getView(R.id.upcoming_recyclerview)));
        RecyclerView recyclerView = (RecyclerView) solo.getView(R.id.upcoming_recyclerview);
        NestedScrollView nested = (NestedScrollView) solo.getView(R.id.nested_scroll_view);
        nested.smoothScrollTo(0, recyclerView.getHeight());
        //Press menu back key
        solo.sleep(2000);
        solo.goBack();
        //Scroll View to the right side
        solo.scrollViewToSide(solo.getView(no.rustelefonen.hap.R.id.viewpager), Solo.RIGHT);


        nested = (NestedScrollView) solo.getView(R.id.nested_scroll_view);
        solo.waitForView(nested);
        AppBarLayout appBarLayout = (AppBarLayout) solo.getView(R.id.toolbar_top_layout);
        uglyHack();
        nested.fullScroll(View.FOCUS_DOWN);
        solo.sleep(2000);

        //Click on Send anonymt spørsmål
        solo.clickOnView(solo.getView(no.rustelefonen.hap.R.id.chat_row));
        //Wait for activity: 'no.rustelefonen.hap.main.tabs.info.ContactRustlfActivity'
        assertTrue("no.rustelefonen.hap.main.tabs.info.ContactRustlfActivity is not found!", solo.waitForActivity(no.rustelefonen.hap.main.tabs.info.ContactRustlfActivity.class));


        //Click on Velg alder
        solo.clickOnView(solo.getView(no.rustelefonen.hap.R.id.age_list));
        //Scroll to 19
        android.widget.ListView listView0 = (android.widget.ListView) solo.getView(android.widget.ListView.class, 0);
        solo.scrollListToLine(listView0, 3);
        //Click on 19
        solo.clickOnText(java.util.regex.Pattern.quote("19"));
        //Click on Velg Kjønn
        solo.clickOnView(solo.getView(no.rustelefonen.hap.R.id.gender_list));
        //Click on Mann
        solo.clickOnText(java.util.regex.Pattern.quote("Mann"));
        //Click on Velg Fylke
        solo.clickOnView(solo.getView(no.rustelefonen.hap.R.id.county_list));
        //Click on Akershus
        solo.clickOnText(java.util.regex.Pattern.quote("Akershus"));
        //Click on Empty Text View
        solo.clickOnView(solo.getView(no.rustelefonen.hap.R.id.title_input));
        //Enter the text: 'test fra studentgruppe'
        solo.clearEditText((android.widget.EditText) solo.getView(no.rustelefonen.hap.R.id.title_input));
        solo.enterText((android.widget.EditText) solo.getView(no.rustelefonen.hap.R.id.title_input), "test fra studentgruppe");
        //Enter the text: 'test fra studentgruppe'
        solo.clearEditText((android.widget.EditText) solo.getView(no.rustelefonen.hap.R.id.content_input));
        solo.enterText((android.widget.EditText) solo.getView(no.rustelefonen.hap.R.id.content_input), "test fra studentgruppe");
        //Click on test fra studentgruppe
        solo.clickOnView(solo.getView(no.rustelefonen.hap.R.id.title_input));
        //Click on Send spørsmål
        solo.clickOnView(solo.getView(no.rustelefonen.hap.R.id.submit_form_btn));

        solo.waitForView(nested);
        nested.fullScroll(View.FOCUS_UP);
        //Click on Cannabisinformasjon
        solo.clickInRecyclerView(3, 0);
        //Wait for activity: 'no.rustelefonen.hap.main.tabs.info.InfoCategoryActivity'
        //assertTrue("no.rustelefonen.hap.main.tabs.info.InfoCategoryActivity is not found!", solo.waitForActivity(no.rustelefonen.hap.main.tabs.info.InfoCategoryActivity.class));
        //Click on Mulige skadevirkninger ved bruk av cannabis
        solo.clickInRecyclerView(1, 0);
        //Wait for activity: 'no.rustelefonen.hap.main.tabs.info.InfoDetailActivity'
        assertTrue("no.rustelefonen.hap.main.tabs.info.InfoDetailActivity is not found!", solo.waitForActivity(InfoDetailActivity.class));
        //Press menu back key
        solo.goBack();
        //Press menu back key
        solo.goBack();
        //Press menu back key
        solo.goBack();
        //Press menu back key
        solo.goBack();
        openCompatNavigationDrawer();
        solo.waitForView(solo.getView(R.id.drawer_layout));
        solo.clickOnText("Nullstill positive triggere");
        solo.clickOnButton("Nullstill");
        solo.waitForDialogToClose();
        openCompatNavigationDrawer();
        solo.waitForView(solo.getView(R.id.drawer_layout));
        solo.clickOnText("Nullstill negative triggere");
        solo.clickOnButton("Nullstill");
        solo.waitForDialogToClose();
        //Click on ImageView
        solo.clickOnView(solo.getView(no.rustelefonen.hap.R.id.avoided_info));
        solo.sleep(500);
        //Click on OK
        solo.clickOnView(solo.getView(android.R.id.button2));
        //Click on ImageView
        solo.clickOnView(solo.getView(no.rustelefonen.hap.R.id.smoked_info));
        //Click on OK
        solo.clickOnView(solo.getView(android.R.id.button2));
        //Click on ImageView
        solo.clickOnView(solo.getView(no.rustelefonen.hap.R.id.graph_info));
        //Click on OK
        solo.clickOnView(solo.getView(android.R.id.button2));
        //Click on Graph
        solo.clickOnView(solo.getView(no.rustelefonen.hap.R.id.graph_view));
        //Click on ImageView
        solo.clickOnView(solo.getView(no.rustelefonen.hap.R.id.add_triggers));
        //Wait for activity: 'no.rustelefonen.hap.main.tabs.trigger.TriggerActivity'
        assertTrue("no.rustelefonen.hap.main.tabs.trigger.TriggerActivity is not found!", solo.waitForActivity(no.rustelefonen.hap.main.tabs.trigger.TriggerActivity.class));
        //Click on ImageView
        solo.clickOnView(solo.getView(no.rustelefonen.hap.R.id.passed_situation_btn));
        //Click on FrameLayout Belønning
        solo.clickInRecyclerView(2, 0);
        //Click on FrameLayout Ensom
        solo.clickInRecyclerView(5, 0);
        //Click on FrameLayout Fest
        solo.clickInRecyclerView(7, 0);
        //Click on Lagre
        solo.clickOnView(solo.getView(no.rustelefonen.hap.R.id.save));
        //Click on ImageView
        solo.clickOnView(solo.getView(no.rustelefonen.hap.R.id.add_triggers));
        //Wait for activity: 'no.rustelefonen.hap.main.tabs.trigger.TriggerActivity'
        assertTrue("no.rustelefonen.hap.main.tabs.trigger.TriggerActivity is not found!", solo.waitForActivity(no.rustelefonen.hap.main.tabs.trigger.TriggerActivity.class));
        //Assert that: 'ImageView' is shown
        assertTrue("'ImageView' is not shown!", solo.waitForView(solo.getView(no.rustelefonen.hap.R.id.passed_situation_checkmark)));
        //Click on ImageView
        solo.clickOnView(solo.getView(no.rustelefonen.hap.R.id.passed_situation_btn));
        //Click on FrameLayout Angst
        solo.clickInRecyclerView(1, 0);
        //Click on FrameLayout Ensom
        solo.clickInRecyclerView(5, 0);
        //Click on Lagre
        solo.clickOnView(solo.getView(no.rustelefonen.hap.R.id.save));
        //Click on ImageView
        solo.clickOnView(solo.getView(no.rustelefonen.hap.R.id.add_triggers));
        //Wait for activity: 'no.rustelefonen.hap.main.tabs.trigger.TriggerActivity'
        assertTrue("no.rustelefonen.hap.main.tabs.trigger.TriggerActivity is not found!", solo.waitForActivity(no.rustelefonen.hap.main.tabs.trigger.TriggerActivity.class));
        //Click on ImageView
        solo.clickOnView(solo.getView(no.rustelefonen.hap.R.id.failed_situation_btn));
        //Click on FrameLayout Fest
        solo.clickInRecyclerView(7, 0);
        //Click on Lagre
        solo.clickOnView(solo.getView(no.rustelefonen.hap.R.id.save));
        //Scroll View to the left side
        solo.goBack();
        //Click on Start Sparekalkulator
        solo.clickOnView(solo.getView(no.rustelefonen.hap.R.id.money_saved_label));
        //Wait for activity: 'no.rustelefonen.hap.main.tabs.home.SavingsCalculatorActivity'
        assertTrue("no.rustelefonen.hap.main.tabs.home.SavingsCalculatorActivity is not found!", solo.waitForActivity(no.rustelefonen.hap.main.tabs.home.SavingsCalculatorActivity.class));
        //Click on Lagre
        solo.clickOnButton("Lagre");
        solo.sleep(2000);
        //Click on Om applikasjonen FrameLayout
        openCompatNavigationDrawer();
        solo.waitForView(solo.getView(R.id.drawer_layout));
        solo.clickOnText("Om applikasjonen");
        //Wait for activity: 'no.rustelefonen.hap.menudrawer.AboutActivity'
        assertTrue("no.rustelefonen.hap.menudrawer.AboutActivity is not found!", solo.waitForActivity(no.rustelefonen.hap.menudrawer.AboutActivity.class));
        //Click on Se benyttede biblioteker
        solo.clickOnWebElement(By.textContent("Se benyttede biblioteker"));
        //Wait for activity: 'com.mikepenz.aboutlibraries.ui.LibsActivity'
        assertTrue("com.mikepenz.aboutlibraries.ui.LibsActivity is not found!", solo.waitForActivity(com.mikepenz.aboutlibraries.ui.LibsActivity.class));
        //Press menu back key
        solo.goBack();
        //Press menu back key
        solo.goBack();
    }

}
