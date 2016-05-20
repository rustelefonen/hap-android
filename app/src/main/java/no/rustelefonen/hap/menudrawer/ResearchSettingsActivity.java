package no.rustelefonen.hap.menudrawer;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import no.rustelefonen.hap.R;
import no.rustelefonen.hap.entities.User;
import no.rustelefonen.hap.persistence.OrmLiteActivity;
import no.rustelefonen.hap.persistence.dao.UserDao;

/**
 * Created by martinnikolaisorlie on 21/03/16.
 */
public class ResearchSettingsActivity extends OrmLiteActivity {

    @BindViews({R.id.user_age_layout, R.id.user_gender_layout, R.id.user_state_layout}) List<TextInputLayout> textInputLayouts;
    @BindView(R.id.research_btn) Button researchBtn;
    @BindView(R.id.settings_info) TextView textView;
    @BindView(R.id.user_age) TextInputEditText ageInput;
    @BindView(R.id.gender_spinner) Spinner genderInput;
    @BindView(R.id.state_spinner) Spinner state_input;

    private User user;
    private UserDao userDao;
    private int age;
    private String geostate;
    private User.Gender gender;
    private Unbinder unbinder;

    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.research_settings_activity);
        super.onCreate(savedInstanceState);
        unbinder = ButterKnife.bind(this);
        initTextInputLayouts();
        userDao = new UserDao(this);
        user = userDao.getFirst();
        initViews(user);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    private void initViews(User user) {
        List<String> errors = new ArrayList<>();

        if (user == null) return;

        if (user.getAge() == 0) {
            //Do nothing
            errors.add("Age is empty");
        } else {
            ageInput.setText("" + user.getAge());
            ageInput.setActivated(false);
            ageInput.setFocusable(false);
            ageInput.setFocusableInTouchMode(false);
        }

        if (user.getGender() == null) {
            //Do nothing
            errors.add("Gender is not set");
        } else {
            genderInput.setSelection(setSelectedGender(user.getGender()));
            genderInput.setEnabled(false);
            genderInput.setClickable(false);
        }

        if (user.getCounty() == null) {
            errors.add("State is not set");
        } else {
            state_input.setEnabled(false);
            state_input.setClickable(false);
        }

        if (errors.size() > 0) {
            researchBtn.setText("Lagre");
        } else {
            researchBtn.setText("Slett");
        }
    }

    private void initTextInputLayouts() {
        for (TextInputLayout layouts : textInputLayouts) {
            layouts.setErrorEnabled(true);
        }
    }

    @OnClick(R.id.research_btn)
    public void saveOrDelete(Button button) {
        if (button.getText().toString().equalsIgnoreCase("lagre")) {
            validateInput();
            user.setAge(age);
            user.setGender(gender);
            user.setCounty(geostate);

            userDao.persist(user);
            textView.setText("Du har bidratt med følgende data til forbedring av behandlingstilbud. Disse kan trekkes tilbake ved å trykke på slett-knappen.");
            Toast.makeText(getApplicationContext(), "Forskningsdata lagret. Takk for ditt bidrag!", Toast.LENGTH_SHORT).show();
            finish();

        } else {
            Log.v("Sure ", "you wanna delete?");
            showDeleteAlert("Sletting", "Sikker på at du vil trekke dine delte forskningsdata? Du er med på å forbedre behandlingstilbudene i landet.");
        }
    }

    private boolean validateInput() {

        if (ageInput.getText().length() <= 0) {
            textInputLayouts.get(0).setError("Alder må fylles ut");
        } else {
            age = Integer.parseInt(ageInput.getText().toString());
        }

        gender = User.Gender.values()[genderInput.getSelectedItemPosition()];
        geostate = state_input.getSelectedItem().toString();

        return age > 0 && gender != null && geostate != null;

    }

    private void showDeleteAlert(String title, String displayedText) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(displayedText);
        builder.setNegativeButton("Avbryt", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });


        builder.setPositiveButton("Slett", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                user.setAge(0);
                user.setGender(null);
                user.setCounty(null);

                userDao.persist(user);
                Toast.makeText(getApplicationContext(), "Forskningsdata slettet.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.RED);
    }

    private int setSelectedGender(User.Gender gender){
        switch (gender){
            case MALE:
                return 0;
            case FEMALE:
                return 1;
            case OTHER:
                return 2;
        }
        return -1;
    }
}
