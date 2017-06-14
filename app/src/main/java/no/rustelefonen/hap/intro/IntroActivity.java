package no.rustelefonen.hap.intro;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.viewpagerindicator.CirclePageIndicator;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import icepick.Icepick;
import icepick.State;
import no.rustelefonen.hap.R;
import no.rustelefonen.hap.entities.User;
import no.rustelefonen.hap.main.tabs.activity.MainActivity;
import no.rustelefonen.hap.notifications.AchievementScheduler;
import no.rustelefonen.hap.persistence.OrmLiteActivity;
import no.rustelefonen.hap.persistence.dao.UserDao;
import no.rustelefonen.hap.tabs.misc.SwipePageAdapter;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by martinnikolaisorlie on 31/01/16.
 */
public class IntroActivity extends OrmLiteActivity {
    private static final String SERVER_URL = "http://app.rustelefonen.no/";

    @State Calendar startDate;
    @BindView(R.id.intro_viewpager) ViewPager viewPager;
    @BindView(R.id.titles) CirclePageIndicator pageIndicator;
    private Unbinder unbinder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro_activity);
        unbinder = ButterKnife.bind(this);
        Icepick.restoreInstanceState(this, savedInstanceState);

        SwipePageAdapter swipePageAdapter = new SwipePageAdapter(getSupportFragmentManager());
        swipePageAdapter.addItem(WelcomeTab.class);
        swipePageAdapter.addItem(DisclaimerTab.class);
        swipePageAdapter.addItem(UserTimePickerTab.class);
        swipePageAdapter.addItem(UserDetailsTab.class);

        viewPager.setAdapter(swipePageAdapter);
        viewPager.setOffscreenPageLimit(4);
        pageIndicator.setViewPager(viewPager);
    }

    public void saveDetailsAndStartProgram(UserDetailsTab.UserDetailsValues userDetailsValues) {
        User user = new User();

        if(userDetailsValues.agreedToParticipate){
            user.setAge(userDetailsValues.age);
            user.setCounty(userDetailsValues.county);
            user.setGender(userDetailsValues.gender);
            user.setUserType(userDetailsValues.userType);
            submitResearchData(user);
        }

        user.setStartDate(getStartDateWithTimeAdjusted().getTime());
        new UserDao(this).persist(user);

        AchievementScheduler achievementScheduler = new AchievementScheduler(this);
        achievementScheduler.reScheduleNext();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public Calendar getStartDateWithTimeAdjusted() {
        Calendar startTime = Calendar.getInstance();

        //if start time is in future, set time to start of day
        if(startDate.getTime().after(startTime.getTime())){
            startTime.set(Calendar.HOUR_OF_DAY, 0);
            startTime.set(Calendar.MINUTE, 0);
            startTime.set(Calendar.SECOND, 0);
        }

        startTime.set(startDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH), startDate.get(Calendar.DAY_OF_MONTH));
        return startTime;
    }

    public Calendar getStartDate() {
        return startDate;
    }

    public void setStartDate(Calendar startDate) {
        this.startDate = startDate;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    private void submitResearchData(User user){
        ResearchRemote researchRemote = new Retrofit.Builder()
                .baseUrl(SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ResearchRemote.class);

        Call<ResponseBody> call = researchRemote.postResearchData(""+ user.getAge(), user.getGenderAsString(), user.getCounty(), user.getUserType());

        call.enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                getSharedPreferences(UserDetailsTab.INTRO_RESEARCH_SENT, MODE_PRIVATE)
                        .edit()
                        .putBoolean(UserDetailsTab.INTRO_RESEARCH_SENT, true)
                        .apply();
                Log.e("Submitted", " research data Success");
            }

            @Override public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Submitted", " research data Failed");
            }
        });
    }

    public interface ResearchRemote {
        @FormUrlEncoded
        @Headers("Content-Type: application/x-www-form-urlencoded;charset=UTF-8")
        @POST("/api/research")
        Call<ResponseBody> postResearchData(@Field("age") String age,
                                            @Field("gender") String gender,
                                            @Field("county") String county,
                                            @Field("userType") String userType);
    }
}
