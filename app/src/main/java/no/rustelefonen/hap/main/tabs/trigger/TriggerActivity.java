package no.rustelefonen.hap.main.tabs.trigger;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import no.rustelefonen.hap.R;
import no.rustelefonen.hap.entities.Trigger;
import no.rustelefonen.hap.entities.User;
import no.rustelefonen.hap.entities.UserTrigger;
import no.rustelefonen.hap.main.tabs.program.ProgramTab;
import no.rustelefonen.hap.persistence.OrmLiteActivity;
import no.rustelefonen.hap.persistence.dao.TriggerDao;
import no.rustelefonen.hap.persistence.dao.UserDao;
import no.rustelefonen.hap.util.SizeAnimator;

import static no.rustelefonen.hap.util.DialogHelper.showDiscardChangesAndFinishDialog;
import static no.rustelefonen.hap.util.DialogHelper.showInfoDialog;
import static no.rustelefonen.hap.util.Dimensions.toDP;

/**
 * Created by martinnikolaisorlie on 23/02/16.
 */
public class TriggerActivity extends OrmLiteActivity {
    public static final String USER_INFO_EXTRA = "user_info_extra";

    private User user;
    private List<CheckableTrigger> checkableTriggers;

    @BindView(R.id.situation_list_recyclerview) RecyclerView recyclerView;
    @BindView(R.id.trigger_results) LinearLayout triggerResults;
    @BindView(R.id.triggers) LinearLayout triggersLayout;
    @BindView(R.id.passed_situation_btn) ImageView passedSituationBtn;
    @BindView(R.id.failed_situation_btn) ImageView failedSituationBtn;
    @BindView(R.id.passed_situation_checkmark) ImageView passedSituationCheckmark;
    @BindView(R.id.failed_situation_checkmark) ImageView failedSituationCheckmark;
    @BindView(R.id.situation_header) TextView situationHeader;

    private Unbinder unbinder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.trigger_activity);
        super.onCreate(savedInstanceState);
        unbinder = ButterKnife.bind(this);
        initDataObjects();
        initSituationsList();
    }

    private void initDataObjects() {
        checkableTriggers = new ArrayList<>();
        user = getIntent().getExtras().getParcelable(USER_INFO_EXTRA);
        List<Trigger> triggers = new TriggerDao(this).getAll();

        for (Trigger trigger : triggers) {
            checkableTriggers.add(new CheckableTrigger(trigger));
        }
    }

    private void initSituationsList() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));

        TriggerAdapter triggerAdapter = new TriggerAdapter(checkableTriggers, new View.OnClickListener() {
            @Override public void onClick(View v) {
                int i = recyclerView.getChildAdapterPosition(v);
                boolean newCheckedState = !checkableTriggers.get(i).isChecked;
                checkableTriggers.get(i).isChecked = newCheckedState;
                v.findViewById(R.id.passed_situation_checkmark).setAlpha(newCheckedState ? 1f : 0f);
            }
        });
        recyclerView.setAdapter(triggerAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.save) {
            if (noTriggersChecked()) showInfoDialog(this, getString(R.string.no_triggers_selected_dialog_title), getString(R.string.no_triggers_selected_dialog_text));
            else if (getSelectetTriggerType() == null) showInfoDialog(this, getString(R.string.no_trigger_type_selected_dialog_title), getString(R.string.no_trigger_type_selected_dialog_text));
            else {
                saveTriggers();
                Intent intent = new Intent();
                intent.putExtra(USER_INFO_EXTRA, user);
                setResult(RESULT_OK, intent);
                finish();
                EventBus.getDefault().post(new ProgramTab.TriggerAddedEvent());
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.passed_situation_btn)
    public void setPassedSituationCheckmark() {
        situationHeader.setText(R.string.positive_trigger_title);
        invertAlpha(passedSituationCheckmark);
        failedSituationCheckmark.setAlpha(0f);
        if (triggerResults.getY() > 0) showTriggerListWithAnimation();
    }

    @OnClick(R.id.failed_situation_btn)
    public void setFailedSituationCheckmark() {
        situationHeader.setText(R.string.negative_trigger_title);
        invertAlpha(failedSituationCheckmark);
        passedSituationCheckmark.setAlpha(0f);
        if (triggerResults.getY() > 0) showTriggerListWithAnimation();
    }

    private void invertAlpha(View view) {
        float alpha = view.getAlpha() == 1f ? 0f : 1f;
        view.setAlpha(alpha);
    }

    private void showTriggerListWithAnimation() {
        float triggerResultY = -toDP(50);
        float distance = Math.abs(triggerResultY - triggerResults.getTop() - toDP(25));

        triggerResults.animate().y(triggerResultY).setDuration(300).start();
        SizeAnimator.size(passedSituationBtn, (int) toDP(75), (int) toDP(75), 300);
        SizeAnimator.size(failedSituationBtn, (int) toDP(75), (int) toDP(75), 300);

        //because it's clipped by default..
        triggersLayout.getLayoutParams().height = (int) (triggersLayout.getHeight() + distance + toDP(2));
        triggersLayout.requestLayout();

        triggersLayout.animate().yBy(-distance).setDuration(300).start();
        triggersLayout.animate().alpha(1).setDuration(500).start();
    }

    private void saveTriggers() {
        UserTrigger.Type type = getSelectetTriggerType();

        //no trigger type selected
        if (type == null) return;

        for (CheckableTrigger checkableTrigger : checkableTriggers) {
            if (!checkableTrigger.isChecked) continue;
            user.addTrigger(checkableTrigger.trigger, type);
        }

        //save
        new UserDao(this).batchPersistUnsavedTriggers(user);
    }

    class CheckableTrigger {
        Trigger trigger;
        boolean isChecked;

        public CheckableTrigger(Trigger trigger) {
            this.trigger = trigger;
        }
    }

    @Override
    public void onBackPressed() {
        if (noTriggersChecked()) super.onBackPressed();
        else showDiscardChangesAndFinishDialog(this, getString(R.string.not_saved_title), getString(R.string.not_saved_text));
    }

    private boolean noTriggersChecked() {
        for (CheckableTrigger trigger : checkableTriggers) {
            if (trigger.isChecked) return false;
        }
        return true;
    }

    private UserTrigger.Type getSelectetTriggerType() {
        UserTrigger.Type type = null;
        if (passedSituationCheckmark.getAlpha() == 1f) type = UserTrigger.Type.RESISTED;
        else if (failedSituationCheckmark.getAlpha() == 1f) type = UserTrigger.Type.SMOKED;
        return type;
    }
}
