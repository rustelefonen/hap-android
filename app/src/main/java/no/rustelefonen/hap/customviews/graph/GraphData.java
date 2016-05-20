package no.rustelefonen.hap.customviews.graph;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;

import java.util.List;

import lombok.Data;

import static no.rustelefonen.hap.util.Dimensions.toDP;

/**
 * Created by lon on 17/02/16.
 */
@Data
public class GraphData{
    private List<Float> data;
    private float dotRadius;
    private int gradientFillTopColor;
    private int gradientFillBottomColor;

    private Paint linePaint;
    private Paint fillPaint;

    public GraphData() {
        setGradientFillTopColor(Color.argb(230, 0, 0, 0));
        setGradientFillBottomColor(Color.argb(26, 255, 255, 255));
        setDotRadius(toDP(2));

        linePaint = new Paint();
        linePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        linePaint.setAntiAlias(true);
        linePaint.setColor(Color.WHITE);
        linePaint.setStrokeWidth(toDP(1));

        fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.FILL);
    }

    public void initGradientIfNeeded(int height){
        if(fillPaint.getShader() != null) return;
        fillPaint.setShader(new LinearGradient(0, 0, 0, height, gradientFillTopColor, gradientFillBottomColor, Shader.TileMode.CLAMP));
    }

    public void resetGradient(int height){
        fillPaint.setShader(null);
        initGradientIfNeeded(height);
    }
}
