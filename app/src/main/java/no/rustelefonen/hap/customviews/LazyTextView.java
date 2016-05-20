package no.rustelefonen.hap.customviews;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Fredrik on 07.05.2016.
 */
public class LazyTextView extends TextView {

    public LazyTextView(Context context) {
        super(context);
    }

    public LazyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LazyTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LazyTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * Text is only updated if it differs from the previously set text
     */
    public void updateText(CharSequence text){
        if(getText().equals(text)) return;
        setText(text);
    }

    /**
     * Text is only updated if it differs from the previously set text
     */
    public void updateText(@StringRes int resid) {
        updateText(getContext().getResources().getText(resid));
    }
}
