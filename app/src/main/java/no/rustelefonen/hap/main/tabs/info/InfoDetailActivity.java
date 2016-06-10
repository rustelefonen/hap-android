package no.rustelefonen.hap.main.tabs.info;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import no.rustelefonen.hap.R;
import no.rustelefonen.hap.entities.Info;
import no.rustelefonen.hap.persistence.OrmLiteActivity;
import no.rustelefonen.hap.persistence.dao.InfoDao;

/**
 * Created by martinnikolaisorlie on 01/03/16.
 */
public class InfoDetailActivity extends OrmLiteActivity {
    public static final String HELP_INFO_ID_EXTRA = "helpInfoExtra";
    private static final String htmlTemplate = "<!doctype html><html><head><link rel=\"stylesheet\" href=\"template.css\" type=\"text/css\"></head><body>%s</body></html>";

    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.info_detail_activity);
        super.onCreate(savedInstanceState);
        WebView webview = (WebView) findViewById(R.id.webviewer);
        int infoId = getIntent().getIntExtra(HELP_INFO_ID_EXTRA, 0);
        Info info = new InfoDao(this).getInfoById(infoId);

        if(info == null || webview == null){
            finish();
            return;
        }

        setActionBarTitle(info.getTitle());
        webview.loadDataWithBaseURL("file:///android_asset/", String.format(htmlTemplate, info.getHtmlContent()), "text/html", "utf-8", null);

        webview.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url){
                if(url.startsWith("file://")){
                    String lastPathComponent = extractLastPathComponent(url);
                    if(lastPathComponent != null){
                        spawnNewInfo(lastPathComponent);
                        return true;
                    }
                }
                if(isResourceFile(url)) return super.shouldOverrideUrlLoading(view, url);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                return true;
            }
        });
    }

    private void spawnNewInfo(String helpInfoName) {
        if (helpInfoName == null) return;

        InfoDao infoDao = new InfoDao(this);
        Info info = infoDao.getInfoByTitle(helpInfoName);
        if(info != null){
            Intent intent = new Intent(this, InfoDetailActivity.class);
            intent.putExtra(HELP_INFO_ID_EXTRA, info);
            startActivity(intent);
        }
    }

    private String extractLastPathComponent(String url){
        int lastPathStartIndex = url.lastIndexOf('/') + 1;
        int lastIndex = url.lastIndexOf('.');
        if(lastPathStartIndex > 0 && lastPathStartIndex < url.length()){
            String fileName = url.substring(lastPathStartIndex, url.length());
            if(lastIndex > 0) fileName = fileName.substring(0, lastIndex);
            return fileName;
        }

        return null;
    }

    private boolean isResourceFile(String url){
        String theUrl = url.toLowerCase();
        return theUrl.contains(".png") || theUrl.contains(".jpg")
                || theUrl.contains(".jpeg") || theUrl.contains(".gif")
                || theUrl.contains(".css");
    }
}
