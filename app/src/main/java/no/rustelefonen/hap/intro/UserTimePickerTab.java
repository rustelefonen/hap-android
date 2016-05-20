package no.rustelefonen.hap.intro;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import icepick.Icepick;
import no.rustelefonen.hap.R;
import no.rustelefonen.hap.tabs.misc.TabPage;
import no.rustelefonen.hap.util.ViewCallback;

import static no.rustelefonen.hap.util.Dimensions.toDP;

/**
 * Created by martinnikolaisorlie on 11/02/16.
 */
public class UserTimePickerTab extends TabPage implements DatePicker.OnDateChangedListener {

    private IntroActivity introActivity;
    @BindView(R.id.datePicker) DatePicker datePicker;
    private Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.intro_user_time_registration_tab, container, false);
        unbinder = ButterKnife.bind(this, view);
        Icepick.restoreInstanceState(this, savedInstanceState);
        introActivity = (IntroActivity) getActivity();

        Calendar startDate = introActivity.getStartDate();
        if(startDate == null){
            startDate = Calendar.getInstance();
            datePicker.init(startDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH), startDate.get(Calendar.DAY_OF_MONTH), this);
            introActivity.setStartDate(startDate);
        }

        scaleDatePickerToLowDpi();
        return view;
    }

    private void scaleDatePickerToLowDpi(){
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        final float dpHeight = displayMetrics.heightPixels / displayMetrics.density;

        if (dpHeight < 550) {
            datePicker.setScaleY(0.90f);
            datePicker.setScaleX(0.90f);
            datePicker.setY(-90);
            datePicker.getLayoutParams().height = (int) toDP(600);
            datePicker.requestLayout();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Calendar startDate = introActivity.getStartDate();
        startDate.set(year, monthOfYear, dayOfMonth, 0, 0);
    }
}
