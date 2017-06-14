package no.rustelefonen.hap;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import java.sql.SQLException;

import no.rustelefonen.hap.intro.IntroActivity;
import no.rustelefonen.hap.main.tabs.activity.MainActivity;
import no.rustelefonen.hap.persistence.OrmLiteActivity;
import no.rustelefonen.hap.persistence.dao.UserDao;
import no.rustelefonen.hap.server.RemoteInfoDataSource;

/**
 * Created by Fredrik on 20.04.2016.
 */
public class SplashActivity extends OrmLiteActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UserDao dao = new UserDao(this);

        System.out.println(dao.getFirst());

        if (dao.getFirst() != null) startActivity(new Intent(this, MainActivity.class));
        else startActivity(new Intent(this, IntroActivity.class));

        new RemoteInfoDataSource(this).syncDatabase();
        finish();
    }
}