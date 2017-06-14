package no.rustelefonen.hap.intro;

import android.os.Bundle;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import no.rustelefonen.hap.R;
import no.rustelefonen.hap.persistence.OrmLiteActivity;

/**
 * Created by simenfonnes on 14.06.2017.
 */

public class PrivacyActivity extends OrmLiteActivity {

    public static final String ID = "PrivacyActivity";
    private UserDetailsTab userDetailsTab;

    private Unbinder unbinder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.privacy_activity);
        super.onCreate(savedInstanceState);

        unbinder = ButterKnife.bind(this);

        userDetailsTab = (UserDetailsTab) getIntent().getSerializableExtra(ID);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    public void accept(View view) {
        System.out.println("lol");
        System.out.println("lol");
        System.out.println("lol");
        System.out.println("lol");
        System.out.println("lol");
        System.out.println("lol");

    }
}
