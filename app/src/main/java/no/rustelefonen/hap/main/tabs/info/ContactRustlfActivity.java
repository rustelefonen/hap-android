package no.rustelefonen.hap.main.tabs.info;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import no.rustelefonen.hap.R;
import no.rustelefonen.hap.persistence.OrmLiteActivity;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

import static android.widget.Toast.LENGTH_SHORT;

/**
 * Created by martinnikolaisorlie on 21/03/16.
 */

public class ContactRustlfActivity extends OrmLiteActivity {
    private static final String SERVER_URL = "http://www.rustelefonen.no/";
    private static final String QUESTIONS_URL = "http://www.rustelefonen.no/besvarte-sporsmal-og-svar/";

    @BindView(R.id.age_list) Spinner ageSpinner;
    @BindView(R.id.gender_list) Spinner genderSpinner;
    @BindView(R.id.county_list) Spinner countySpinner;
    @BindView(R.id.title_input) TextInputEditText titleInput;
    @BindView(R.id.content_input) TextInputEditText contentInput;
    private ProgressDialog progressDialog;
    private Unbinder unbinder;

    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.contact_rustlf_activity);
        super.onCreate(savedInstanceState);
        unbinder = ButterKnife.bind(this);

        ageSpinner.setAdapter(new ArrayAdapter<>(this, R.layout.spinner_layout, makeAgeList()));
        genderSpinner.setAdapter(new ArrayAdapter<>(this, R.layout.spinner_layout, getResources().getStringArray(R.array.genders)));
        countySpinner.setAdapter(new ArrayAdapter<>(this, R.layout.spinner_layout, getResources().getStringArray(R.array.counties)));
    }

    private List<String> makeAgeList(){
        List<String> ageList = new ArrayList<>(58);
        ageList.add("Velg alder");

        for(int i = 13; i < 70; i++){
            ageList.add(""+i);
        }
        return ageList;
    }

    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @OnClick(R.id.submit_form_btn)
    public void sendForm() {
        if (!validateForm()) return;

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sender spørsmål!");
        progressDialog.show();
        sendFormRequest(ageSpinner.getSelectedItem().toString(),
                        genderSpinner.getSelectedItem().toString(),
                        countySpinner.getSelectedItem().toString(),
                        titleInput.getText().toString(),
                        contentInput.getText().toString() + "\n\nSendt fra Android-applikasjonen.");
    }

    @OnClick(R.id.info_link)
    public void loadQuestionsSite() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(QUESTIONS_URL));
        startActivity(intent);
    }

    private boolean validateForm() {
        if (ageSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Du må oppgi alder", LENGTH_SHORT).show();
        }
        else if(genderSpinner.getSelectedItemPosition() == 0){
            Toast.makeText(this, "Du må oppgi kjønn", LENGTH_SHORT).show();
        }
        else if(countySpinner.getSelectedItemPosition() == 0){
            Toast.makeText(this, "Du må velge et fylke", LENGTH_SHORT).show();
        }
        else if (titleInput.getText().length() <= 0){
            titleInput.setError("Fyll inn tittel");
        }
        else if (contentInput.getText().length() <= 0){
            contentInput.setError("Skriv inn et spørsmål");
        }
        else{
            return true;
        }

        return false;
    }

    public void sendFormRequest(String age, String gender , String county, String title, String content) {
        QuestionRemote questionRemote = new Retrofit.Builder()
                .baseUrl(SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(QuestionRemote.class);


        Call<ResponseBody> call = questionRemote.postQuestion(age, gender, county, title, content, "5", "17", "Send inn ditt spørsmål!");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Toast.makeText(ContactRustlfActivity.this, "Skjemaet er nå innsendt!", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                finish();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(ContactRustlfActivity.this, "Noe gikk galt, prøv igjen!", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        });
    }

    public interface QuestionRemote {
        @FormUrlEncoded
        @POST("/still-sporsmal")
        Call<ResponseBody> postQuestion(@Field("user-submitted-age") String age,
                                        @Field("user-submitted-sex") String gender,
                                        @Field("user-submitted-county") String county,
                                        @Field("user-submitted-title") String title,
                                        @Field("user-submitted-content") String content,
                                        @Field("user-submitted-captcha") String captcha,
                                        @Field("user-submitted-category") String category,
                                        @Field("user-submitted-post") String post);
    }
}




