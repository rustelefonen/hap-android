package no.rustelefonen.hap.tabs.misc;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by Fredrik on 15.10.2015.
 */
public abstract class TabPage extends Fragment {

    public static TabPage newInstance(Class tabClass){
        try{
            TabPage t = (TabPage) tabClass.newInstance();
            Bundle bundle = new Bundle();
            t.setArguments(bundle);
            return t;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
