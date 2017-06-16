package no.rustelefonen.hap.intro;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.Calendar;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import no.rustelefonen.hap.R;
import no.rustelefonen.hap.entities.User;
import no.rustelefonen.hap.main.tabs.activity.MainActivity;
import no.rustelefonen.hap.notifications.AchievementScheduler;
import no.rustelefonen.hap.persistence.OrmLiteActivity;
import no.rustelefonen.hap.persistence.dao.UserDao;
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

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.primitives.Ints.tryParse;

/**
 * Created by simenfonnes on 14.06.2017.
 */

public class PrivacyActivity extends OrmLiteActivity {
    private static final String SERVER_URL = "http://app.rustelefonen.no/";

    public static final String ID = "PrivacyActivity";

    private Unbinder unbinder;

    private PrivacyPojo privacyPojo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.privacy_activity);
        super.onCreate(savedInstanceState);

        unbinder = ButterKnife.bind(this);

        privacyPojo = (PrivacyPojo) getIntent().getSerializableExtra(ID);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    public void accept(View view) {
        if (privacyPojo != null) {
            saveDetailsAndStartProgram(privacyPojo);
        }

    }

    public void saveDetailsAndStartProgram(PrivacyPojo privacyPojo) {
        User user = new User();

        if(privacyPojo.isAgreedToParticipate()){
            user.setAge(privacyPojo.getAge());
            user.setCounty(privacyPojo.getCounty());
            user.setGender(privacyPojo.getGender());
            user.setUserType(privacyPojo.getUserType());
            submitResearchData(user);
        }
        user.setAppRegistered(new Date());

        user.setStartDate(getStartDateWithTimeAdjusted(privacyPojo.getStartDate()).getTime());

        new UserDao(this).persist(user);

        AchievementScheduler achievementScheduler = new AchievementScheduler(this);
        achievementScheduler.reScheduleNext();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public Calendar getStartDateWithTimeAdjusted(Calendar startDate) {
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

    private void submitResearchData(User user){
        IntroActivity.ResearchRemote researchRemote = new Retrofit.Builder()
                .baseUrl(SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(IntroActivity.ResearchRemote.class);

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
