package no.rustelefonen.hap.main.tabs.home;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.webkit.WebView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import no.rustelefonen.hap.R;
import no.rustelefonen.hap.entities.User;
import no.rustelefonen.hap.persistence.OrmLiteActivity;
import no.rustelefonen.hap.util.DialogHelper;

import static android.content.DialogInterface.BUTTON_POSITIVE;

/**
 * Created by simenfonnes on 14.06.2017.
 */

public class SurveyActivity extends OrmLiteActivity {

    public static final String ID = "SurveyActivity";

    @BindView(R.id.survey_web_view) WebView webView;

    private Unbinder unbinder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.survey_activity);
        super.onCreate(savedInstanceState);
        unbinder = ButterKnife.bind(this);
        webView.getSettings().setJavaScriptEnabled(true);

        String url = getIntent().getStringExtra(ID);
        if (!url.isEmpty() && url != null) webView.loadUrl(url);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public void onBackPressed() {
        System.out.println("kek");
        String dialogTitle = "Er du sikker på at du vil gå tilbake?";
        String dialogInfo = "Hvis du går tilbake uten å ha fullført undersøkelsen, vil du ikke kunne svare på denne undersøkelsen igjen.";
        DialogHelper.showConfirmDialogWithAction(this, dialogTitle, dialogInfo, "Gå tilbake", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == BUTTON_POSITIVE) {
                    finish();
                    dialog.dismiss();
                }
            }
        });
    }
}
