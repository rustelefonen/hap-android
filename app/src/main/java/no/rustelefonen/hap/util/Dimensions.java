package no.rustelefonen.hap.util;

import android.content.res.Resources;
import android.util.TypedValue;

/**
 * Created by Fredrik on 17.02.2016.
 */
public class Dimensions {

    public static float toDP(float units){
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, units, Resources.getSystem().getDisplayMetrics());
    }

    public static float toSP(float units){
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, units, Resources.getSystem().getDisplayMetrics());
    }
}
