package no.rustelefonen.hap.menudrawer;

import android.content.Context;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.mikepenz.aboutlibraries.LibsBuilder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import no.rustelefonen.hap.R;
import no.rustelefonen.hap.persistence.OrmLiteActivity;

/**
 * Created by martinnikolaisorlie on 21/03/16.
 */
public class AboutActivity extends OrmLiteActivity {

    @BindView(R.id.about_webview) WebView browser;
    private Context context;
    private Unbinder unbinder;

    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.about_activity);
        super.onCreate(savedInstanceState);
        unbinder = ButterKnife.bind(this);
        context = this;
        browser.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("libraries")) {
                    new LibsBuilder()
                            .withAutoDetect(true)
                            .withActivityTitle("Biblioteker")
                            .withAboutVersionString("Versjon 1.0")
                            //.withAboutDescription("Her er en liste over tredjepartsbiblioteker som er benyttet i denne applikasjonen.")
                            .withActivityTheme(R.style.AppTheme_AboutLibrariesTheme)
                            .withFields(R.string.class.getFields())
                            .start(context);
                }
                return true;
            }


        });

        browser.loadUrl("file:///android_asset/about/about_app.html");
    }

    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
