package no.rustelefonen.hap.intro;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import no.rustelefonen.hap.R;
import no.rustelefonen.hap.tabs.misc.TabPage;

/**
 * Created by martinnikolaisorlie on 11/02/16.
 */
public class DisclaimerTab extends TabPage {

    private Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.intro_disclaimer_tab, container, false);
        unbinder = ButterKnife.bind(this, v);
        return v;
    }

    @OnClick(R.id.rustlf_link)
    public void navigateToRusUrl(){
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.rustlf_url))));
    }

    @OnClick(R.id.utesek_link)
    public void navigateToUteSekUrl(){
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.uteseksjonen_url))));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}