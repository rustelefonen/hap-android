package no.rustelefonen.hap.intro;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Range;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextSwitcher;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import lombok.AllArgsConstructor;
import no.rustelefonen.hap.R;
import no.rustelefonen.hap.entities.User;
import no.rustelefonen.hap.entities.User;
import no.rustelefonen.hap.tabs.misc.TabPage;
import no.rustelefonen.hap.util.DialogHelper;
import no.rustelefonen.hap.util.ViewCallback;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.primitives.Ints.tryParse;

/**
 * Created by martinnikolaisorlie on 11/02/16.
 */
public class UserDetailsTab extends TabPage {
    public static final String INTRO_RESEARCH_SENT = "INTRO_RESEARCH_SENT";

    @BindView(R.id.user_research_data) LinearLayout researchData;
    @BindView(R.id.age_list) Spinner ageSpinner;
    @BindView(R.id.gender_list) Spinner genderSpinner;
    @BindView(R.id.county_list) Spinner countySpinner;
    @BindView(R.id.userType_list) Spinner userTypeSpinner;
    @BindView(R.id.info_research_agree_switch) Switch researchSwitch;
    @BindView(R.id.intro_start_program) TextSwitcher startProgram;
    @BindView(R.id.intro_research_info) TextView infoText;

    private Unbinder unbinder;
    private int buttonStartY;
    private IntroActivity introActivity;
    private boolean alreadyParticipated;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.intro_user_details_tab, container, false);
        unbinder = ButterKnife.bind(this, view);
        introActivity = (IntroActivity) getActivity();
        syncParticipationState();

        ageSpinner.setAdapter(new ArrayAdapter<>(getContext(), R.layout.spinner_layout, makeAgeList()));
        genderSpinner.setAdapter(new ArrayAdapter<>(getContext(), R.layout.spinner_layout, getResources().getStringArray(R.array.genders)));
        countySpinner.setAdapter(new ArrayAdapter<>(getContext(), R.layout.spinner_layout, getResources().getStringArray(R.array.counties)));
        userTypeSpinner.setAdapter(new ArrayAdapter<>(getContext(), R.layout.spinner_layout, getResources().getStringArray(R.array.userTypes)));

        return view;
    }

    private void syncParticipationState(){
        SharedPreferences sharedPreferences = introActivity.getSharedPreferences(INTRO_RESEARCH_SENT, Activity.MODE_PRIVATE);
        alreadyParticipated = sharedPreferences.getBoolean(INTRO_RESEARCH_SENT, false);

        if(alreadyParticipated){
            infoText.setText(getString(R.string.intro_research_already_participate));
            researchSwitch.setChecked(true);
            researchSwitch.setEnabled(false);
        }
        else {
            infoText.setText(getString(R.string.intro_research_text));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewCallback.onAppear(startProgram, new Runnable() {
            @Override public void run() {
                if(buttonStartY == 0){
                    buttonStartY = researchSwitch.getBottom() + ((RelativeLayout.LayoutParams) startProgram.getLayoutParams()).topMargin;
                }
                startResearchDataShowHideAnimation(0);
                startButtonTranslateAnimation(0);
            }
        });
    }

    private List<String> makeAgeList(){
        List<String> ageList = new ArrayList<>(58);
        ageList.add(getString(R.string.user_details_age_placeholder));

        for(int i = 13; i < 70; i++){
            ageList.add(""+i);
        }
        return ageList;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        researchData.clearAnimation();
        startProgram.clearAnimation();
        unbinder.unbind();
    }

    @OnClick(R.id.info_research_agree_switch)
    public void switchedTheSwitch(){
        int duration = 300;
        startResearchDataShowHideAnimation(duration);
        startButtonTranslateAnimation(duration);
    }

    private void startResearchDataShowHideAnimation(int duration){
        if(researchSwitch.isChecked() && !alreadyParticipated) {
            researchData.setVisibility(View.VISIBLE);
            researchData.animate().alpha(1).setDuration(duration).setListener(null);
        }
        else {
            researchData.animate().alpha(0).setDuration(duration).setListener(new AnimatorListenerAdapter() {
                @Override public void onAnimationEnd(Animator animation) {
                    researchData.setVisibility(researchSwitch.isChecked() && !alreadyParticipated ? View.VISIBLE : View.INVISIBLE);
                }
            });
        }
    }

    private void startButtonTranslateAnimation(int duration){
        startProgram.animate()
                .y(researchSwitch.isChecked() && !alreadyParticipated ? buttonStartY + researchData.getMeasuredHeight() : buttonStartY)
                .setDuration(duration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override public void onAnimationEnd(Animator animation) {
                        startProgram.setText(researchSwitch.isChecked() ? getString(R.string.intro_details_agree_button_text) : getString(R.string.intro_details_disagree_button_text));
                    }
                });
    }

    @OnClick({R.id.intro_start_program_without_details, R.id.intro_start_program_with_details})
    public void startProgram() {
        /*DialogHelper.showConfirmDialogWithAction(getActivity(), "Personvernerklæring", "For å bedre tilpasse appen til våre brukere ber vi om at du oppgir alder, kjønn og fylke. Denne informasjonen er frivillig å oppgi, og vil sendes til en lukket server hos RUStelefonen. Du vil ikke kunne identifiseres. All øvrig informasjon du legger til i appen vil kun registreres på din telefon og blir kryptert. Dette gjelder alle versjoner i iOS og versjoner fra og med 5.0 (lollipop) i Android. Kildekoden til appen og datainnsendingen ligger åpen på Github under brukeren rustelefonen: https://github.com/rustelefonen.", "Aksepterer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int ageInt = firstNonNull(tryParse(ageSpinner.getSelectedItem().toString()), -1); //first index is placeholder, and will fail parse
                User.Gender selectedGender = extractGender();
                String stateString = extractCountyString();
                String userTypeString = extractUserTypeString();

                //no values selected, no need to send
                if(ageInt == -1 && selectedGender == null && stateString == null){
                    researchSwitch.setChecked(false);
                }

                boolean agreedToParticipate = researchSwitch.isChecked() && !alreadyParticipated;
                UserDetailsValues userDetailsValues = new UserDetailsValues(agreedToParticipate, ageInt, selectedGender, stateString, userTypeString);
                introActivity.saveDetailsAndStartProgram(userDetailsValues);
            }
        });*/

        startActivity(new Intent(this.getContext(), PrivacyActivity.class));

    }

    private User.Gender extractGender(){
        return genderSpinner.getSelectedItemPosition() == 0 //first index is placeholder
                ? null
                : User.Gender.values()[genderSpinner.getSelectedItemPosition()-1];
    }

    private String extractCountyString(){
        return countySpinner.getSelectedItemPosition() == 0 //first index is placeholder
                ? null
                : countySpinner.getSelectedItem().toString();
    }

    private String extractUserTypeString(){
        return userTypeSpinner.getSelectedItemPosition() == 0 //first index is placeholder
                ? null
                : userTypeSpinner.getSelectedItem().toString();
    }

    @AllArgsConstructor
    static class UserDetailsValues{
        boolean agreedToParticipate;
        int age;
        User.Gender gender;
        String county;
        String userType;
    }
}