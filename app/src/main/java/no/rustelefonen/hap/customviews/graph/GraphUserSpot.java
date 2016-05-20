package no.rustelefonen.hap.customviews.graph;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import lombok.Data;

import static no.rustelefonen.hap.util.Dimensions.toDP;

/**
 * Created by lon on 17/02/16.
 */
@Data
public class GraphUserSpot extends View {

    private Paint paint;
    private float dotRadius;
    private float padding;

    private int lineColor;
    private int dotColor;

    public GraphUserSpot(Context context) {
        super(context);
        init();
    }

    public GraphUserSpot(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        paint = new Paint();
        paint.setStrokeWidth(toDP(1));
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setAntiAlias(true);
        setDotColor(Color.RED);
        setLineColor(Color.WHITE);
        setDotRadius(toDP(8));
        padding = toDP(3);
    }

    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);

        paint.setColor(lineColor);
        canvas.drawLine(dotRadius, dotRadius, dotRadius, getHeight(), paint);

        paint.setColor(dotColor);
        canvas.drawCircle(dotRadius, dotRadius, dotRadius - padding, paint);
    }

    public float getDotDiameter(){
        return dotRadius * 2;
    }

    public void setLineWidth(float lineWidth){
        paint.setStrokeWidth(lineWidth);
    }
}
