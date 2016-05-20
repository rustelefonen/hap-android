package no.rustelefonen.hap.customviews.clock;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import no.rustelefonen.hap.R;
import no.rustelefonen.hap.customviews.LazyTextView;

import static no.rustelefonen.hap.util.Dimensions.toDP;

/**
 * Created by Fredrik on 20.02.2016.<br>
 * This is a custom widget which shows a timespan in days, hours and minutes
 * <p>
 *     <h3>XML-attributes</h3>
 *     <h5>Strings</h5>
 *     {@link R.styleable#ClockView_unit_days_plural_text Days unit string Attribute}<br>
 *     {@link R.styleable#ClockView_unit_hours_plural_text Hours unit string Attribute}<br>
 *     {@link R.styleable#ClockView_unit_minutes_plural_text Minutes unit string Attribute}<br>
 *     <h5>Text color</h5>
 *     {@link R.styleable#ClockView_unit_label_text_color Unit label text color Attribute}<br>
 *     {@link R.styleable#ClockView_number_label_text_color Number label text color Attribute}<br>
 *     <h5>Font family</h5>
 *     {@link R.styleable#ClockView_unit_label_font_family Unit label font family Attribute}<br>
 *     {@link R.styleable#ClockView_number_label_font_family Number label font family Attribute}<br>
 *     <h5>Text size</h5>
 *     {@link R.styleable#ClockView_unit_label_text_size Unit label text size Attribute}<br>
 *     {@link R.styleable#ClockView_number_label_text_size Number label text size Attribute}
 * </p>
 */
public class ClockView extends ViewGroup {
    private List<LazyTextView> unitLabels, numberLabels;
    private List<ProgressBar> circleProgressBars;
    private List<String> pluralLabels, singularLabels;
    private int maxCircleHeight, circleDiameter;

    public ClockView(Context context) {
        super(context);
        init();
    }

    public ClockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        initAttrs(attrs);
    }

    public ClockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        initAttrs(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ClockView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
        initAttrs(attrs);
    }

    private void init(){
        unitLabels = new ArrayList<>();
        numberLabels = new ArrayList<>();
        circleProgressBars = new ArrayList<>();
        pluralLabels = new ArrayList<>();
        singularLabels = new ArrayList<>();
        for(int i = 0; i < 3; i++){
            unitLabels.add(new LazyTextView(getContext()));
            numberLabels.add(new LazyTextView(getContext()));
            circleProgressBars.add((ProgressBar) View.inflate(getContext(), R.layout.circle_progress_bar, null));
            addView(unitLabels.get(i));
            addView(numberLabels.get(i));
            addView(circleProgressBars.get(i));
        }
    }

    private void initAttrs(AttributeSet attrs){
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ClockView);
        pluralLabels = Arrays.asList(a.getString(R.styleable.ClockView_unit_days_plural_text),
            a.getString(R.styleable.ClockView_unit_hours_plural_text),
            a.getString(R.styleable.ClockView_unit_minutes_plural_text));

        singularLabels = Arrays.asList(a.getString(R.styleable.ClockView_unit_days_singular_text),
                a.getString(R.styleable.ClockView_unit_hours_singular_text),
                a.getString(R.styleable.ClockView_unit_minutes_singular_text));

        maxCircleHeight = a.getDimensionPixelSize(R.styleable.ClockView_circle_max_height, (int) toDP(120));

        int unitLabelColor = a.getColor(R.styleable.ClockView_unit_label_text_color, Color.BLACK);
        int numberLabelColor = a.getColor(R.styleable.ClockView_number_label_text_color, Color.RED);
        String numberLabelFontFamily = a.getString(R.styleable.ClockView_number_label_font_family);
        String unitLabelFontFamily = a.getString(R.styleable.ClockView_unit_label_font_family);
        float unitLabelTextSize = a.getDimensionPixelSize(R.styleable.ClockView_unit_label_text_size, 50);
        float numberLabelTextSize = a.getDimensionPixelSize(R.styleable.ClockView_number_label_text_size, 25);

        for(int i = 0; i < 3; i++){
            unitLabels.get(i).setTextColor(unitLabelColor);
            unitLabels.get(i).setTextSize(TypedValue.COMPLEX_UNIT_PX, unitLabelTextSize);
            unitLabels.get(i).setTypeface(Typeface.create(unitLabelFontFamily, Typeface.NORMAL));
            unitLabels.get(i).setText(pluralLabels.get(i));

            numberLabels.get(i).setTextColor(numberLabelColor);
            numberLabels.get(i).setTextSize(TypedValue.COMPLEX_UNIT_PX, numberLabelTextSize);
            numberLabels.get(i).setTypeface(Typeface.create(numberLabelFontFamily, Typeface.NORMAL));
            numberLabels.get(i).setText("0");
        }
        a.recycle();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        positionCircles();
        positionLabels();
    }

    private void positionCircles() {
        int width = getWidth() - getPaddingRight() - getPaddingLeft();
        circleDiameter = (int) (width / 3 - toDP(8));
        if (circleDiameter > maxCircleHeight) circleDiameter = maxCircleHeight;
        getLayoutParams().height = circleDiameter;

        int radius = circleDiameter / 2;
        int circleY = getPaddingTop();

        int leftCircleX = getPaddingLeft() + width / 3 / 2 - radius;
        int middleCircleX = getPaddingLeft() + width / 2 - radius;
        int rightCircleX = (getPaddingLeft() * 2) + (width - leftCircleX - circleDiameter);

        circleProgressBars.get(0).layout(leftCircleX, circleY, leftCircleX + circleDiameter, circleY + circleDiameter);
        circleProgressBars.get(1).layout(middleCircleX, circleY, middleCircleX + circleDiameter, circleY + circleDiameter);
        circleProgressBars.get(2).layout(rightCircleX, circleY, rightCircleX + circleDiameter, circleY + circleDiameter);
    }

    private void positionLabels() {
        int width = getWidth() - getPaddingRight() - getPaddingLeft();
        int yMargin = (int) toDP(8);
        int labelWidth = width / 3;

        measureLabel(numberLabels.get(0), labelWidth, 0); //measuring once for performance
        measureLabel(unitLabels.get(0), labelWidth, 0);
        int numberLabelHeight = numberLabels.get(0).getMeasuredHeight();
        int unitLabelHeight =  unitLabels.get(0).getMeasuredHeight();

        int unitLabelYPos = (circleDiameter / 2) + ((numberLabelHeight + unitLabelHeight - (yMargin * 2)) / 2) - unitLabelHeight;
        int numberLabelYPos = unitLabelYPos - numberLabelHeight + yMargin;

        int labelX = getPaddingLeft();
        for (int i = 0; i < 3; i++){
            positionLabel(numberLabels.get(i), labelX, numberLabelYPos, labelWidth, numberLabelHeight);
            positionLabel(unitLabels.get(i), labelX, unitLabelYPos, labelWidth, unitLabelHeight);
            labelX += labelWidth;
        }
    }

    private void positionLabel(TextView v, int xPos, int yPos, int width, int height){
        v.setGravity(Gravity.CENTER);
        v.layout(xPos, yPos, xPos + width, yPos + height);
    }

    private void measureLabel(TextView v, int width, int height){
        int widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        int heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED);
        v.measure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * Updates the clockview to reflect the timespanInSeconds attribute
     */
    public void updateClock(double timespanInSeconds) {
        int[] clock = {
                (int) (timespanInSeconds / 86400), //days
                (int) ((timespanInSeconds / 3600) % 24), //hours
                (int) ((timespanInSeconds / 60) % 60) //minutes
        };

        for(int i = 0; i < numberLabels.size(); i++){
            numberLabels.get(i).updateText(String.valueOf(clock[i]));
        }
        updateClockLabels(clock[0], clock[1], clock[2]);
        updateClockCircularProgressBar(timespanInSeconds);
    }

    private void updateClockCircularProgressBar(double counter){
        int maxProgress = circleProgressBars.get(0).getMax();
        int dayPercentage = (int) (((counter / 3600) % 24) / 24.0 * maxProgress);
        int hourPercentage = (int) (((counter / 60) % 60) / 60.0 * maxProgress);
        int minutePercentage = (int) ((counter % 60) / 60.0 * maxProgress);

        circleProgressBars.get(0).setProgress(dayPercentage);
        circleProgressBars.get(1).setProgress(hourPercentage);
        circleProgressBars.get(2).setProgress(minutePercentage);
    }

    private void updateClockLabels(int days, int hours, int minutes){
        unitLabels.get(0).updateText(days == 1 ? singularLabels.get(0) : pluralLabels.get(0));
        unitLabels.get(1).updateText(hours == 1 ? singularLabels.get(1) : pluralLabels.get(1));
        unitLabels.get(2).updateText(minutes == 1 ? singularLabels.get(2) : pluralLabels.get(2));
    }
}