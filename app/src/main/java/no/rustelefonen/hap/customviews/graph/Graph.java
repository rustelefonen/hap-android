package no.rustelefonen.hap.customviews.graph;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

import com.google.common.collect.Iterables;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import no.rustelefonen.hap.R;

import static no.rustelefonen.hap.util.Dimensions.toDP;
import static no.rustelefonen.hap.util.Dimensions.toSP;

/**
 * Created by Fredrik on 17.02.2016.
 */
public class Graph extends ViewGroup {
    class GraphConstraints {
        float lowestPoint;
        float highestPoint;
        float pointDistance;
    }

    @AllArgsConstructor
    class DrawablePath {
        Path path;
        Paint paint;
    }

    @AllArgsConstructor
    public static class AxisDetails{
        String label;
        float atPercentage;
        boolean drawLine;

        public AxisDetails(String label, float atPercentage) {
            this(label, atPercentage, true);
        }
    }

    @AllArgsConstructor
    public static class Section{
        public String label;
        public float atPercentage;
    }

    public interface OnSelectSectionListener{
        void onSelect(int sectionIndex);
    }

    private Paint borderLinePaint;
    private Paint axisTextPaint;
    private Paint sectionPaint;
    private Rect tmpTextBounds;

    private Set<GraphData> data;
    private List<AxisDetails> xAxisDetails;
    private List<AxisDetails> yAxisDetails;
    private List<Section> sections;

    private Set<DrawablePath> graphBodyLayer;
    private Set<DrawablePath> lineLayer;
    private Set<GraphUserSpot> userSpots;

    private boolean drawMiddleLine;

    private OnSelectSectionListener sectionListener;

    public Graph(Context context) {
        super(context);
        init();
    }

    public Graph(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        initAttrs(attrs);
    }

    public Graph(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        initAttrs(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Graph(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
        initAttrs(attrs);
    }

    private void init(){
        setClipToPadding(false);
        setWillNotDraw(false);

        graphBodyLayer = new HashSet<>();
        lineLayer = new HashSet<>();
        data = new HashSet<>();
        userSpots = new HashSet<>();

        borderLinePaint = new Paint();
        borderLinePaint.setStyle(Paint.Style.STROKE);
        axisTextPaint = new Paint();
        tmpTextBounds = new Rect();
        sectionPaint = new Paint();
        sectionPaint.setTextAlign(Paint.Align.CENTER);

        if(isInEditMode()){
            GraphData g = new GraphData();
            GraphData g2 = new GraphData();
            g.setData(Arrays.asList(-5f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f));
            g2.setData(Arrays.asList(9f, 8f, 7f, 6f, 5f, 4f, 3f, 2f, -3f));
            data.add(g);
            data.add(g2);

            addUserSpot(new GraphUserSpot(getContext()));
        }
    }

    private void initAttrs(AttributeSet attrs){
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.Graph);
        float lineWidth = a.getDimensionPixelSize(R.styleable.Graph_line_width, (int) toDP(1));
        int lineColor = a.getColor(R.styleable.Graph_line_color, Color.WHITE);
        borderLinePaint.setStrokeWidth(lineWidth);
        borderLinePaint.setColor(lineColor);
        axisTextPaint.setColor(borderLinePaint.getColor());
        sectionPaint.setColor(borderLinePaint.getColor());

        int metaDataTextSize = a.getDimensionPixelSize(R.styleable.Graph_meta_data_text_size, (int) toSP(10));
        axisTextPaint.setTextSize(metaDataTextSize);
        sectionPaint.setTextSize(metaDataTextSize);
        a.recycle();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        initUserSpots();
    }

    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);
        drawTopLine(canvas);
        if(drawMiddleLine) drawMiddleDashedLine(canvas);
        drawBottomLine(canvas);
        drawXDetails(canvas);
        drawYDetails(canvas);
        drawSections(canvas);
        makeGraphDrawables();

        for (DrawablePath path : Iterables.concat(graphBodyLayer, lineLayer)){ // splitting graphBody and line, because we dont want a graphBody to overlap a line
            canvas.drawPath(path.path, path.paint);
        }
    }

    private void makeGraphDrawables(){
        if(graphBodyLayer.size() >= data.size()) return;

        GraphConstraints graphConstraints = calculateConstraints();
        for (GraphData graphData : data) {
            graphData.initGradientIfNeeded(getHeight() - getPaddingBottom() - getPaddingTop());
            makeDataGraph(graphData, graphConstraints);
        }
    }

    private void makeDataGraph(GraphData graphData, GraphConstraints constraints){
        Path graphBody = new Path();
        Path graphLine = new Path();

        //making the graphLine and graphBody
        PointF point = new PointF(getPaddingLeft(), getYLocForData(graphData.getData().get(0), constraints));
        graphBody.moveTo(point.x, point.y);
        graphLine.moveTo(point.x, point.y);
        appendGraphDot(graphLine, point, graphData);
        for(int i = 1; i < graphData.getData().size(); i++) {
            point.x += constraints.pointDistance;
            point.y = getYLocForData(graphData.getData().get(i), constraints);
            graphBody.lineTo(point.x, point.y);
            graphLine.lineTo(point.x, point.y);
            appendGraphDot(graphLine, point, graphData);
        }

        //Closing graphPath
        point.y = getHeight() - getPaddingBottom();
        graphBody.lineTo(point.x, point.y);
        point.x = getPaddingLeft();
        graphBody.lineTo(point.x, point.y);
        point.y = getYLocForData(graphData.getData().get(0), constraints);
        graphBody.lineTo(point.x, point.y);

        graphBodyLayer.add(new DrawablePath(graphBody, graphData.getFillPaint()));
        lineLayer.add(new DrawablePath(graphLine, graphData.getLinePaint()));
    }

    private float getYLocForData(float data, GraphConstraints constraints){
        float maxDistance = Math.abs(constraints.lowestPoint - constraints.highestPoint);
        float y = (data - constraints.lowestPoint) / maxDistance; //getting data's percentage of maxDistance
        y *= getHeight() - getPaddingTop() - getPaddingBottom(); //converting Y to a percentage of available space
        y = getHeight() - getPaddingBottom() - y; //flipping y, because coords x=0, y=0 is upper left corner.
        return y;
    }

    private void appendGraphDot(Path path, PointF point, GraphData graphData) {
        path.addCircle(point.x, point.y, graphData.getDotRadius(), Path.Direction.CW);
        path.moveTo(point.x, point.y);
    }

    private GraphConstraints calculateConstraints(){
        GraphConstraints constrains = new GraphConstraints();
        int maxCount = 0;

        for (GraphData graphData : data) {
            maxCount = Math.max(maxCount, graphData.getData().size());
            constrains.lowestPoint = Math.min(constrains.lowestPoint, Collections.min(graphData.getData()));
            constrains.highestPoint = Math.max(constrains.highestPoint, Collections.max(graphData.getData()));
        }

        constrains.pointDistance = (float) Math.ceil((getWidth() - getPaddingLeft() - getPaddingRight()) / (maxCount - 1));
        return constrains;
    }

    private void drawTopLine(Canvas canvas){
        PointF fromPoint = new PointF(getPaddingLeft(), getTopLineYPos());
        PointF toPoint = new PointF(getWidth() - getPaddingRight(), fromPoint.y);
        canvas.drawLine(fromPoint.x, fromPoint.y, toPoint.x, toPoint.y, borderLinePaint);
    }

    private void drawMiddleDashedLine(Canvas canvas){
        PointF fromPoint = new PointF(getPaddingLeft(), getMiddleLineYPos());
        PointF toPoint = new PointF(getWidth() - getPaddingRight(), fromPoint.y);

        borderLinePaint.setPathEffect(new DashPathEffect(new float[]{10f, 5f}, 0));
        Path dashedPath = new Path(); //using path for dashed line because canvas.drawline dont work with hardware accel
        dashedPath.moveTo(fromPoint.x, fromPoint.y);
        dashedPath.lineTo(toPoint.x, toPoint.y);
        canvas.drawPath(dashedPath, borderLinePaint);

        borderLinePaint.setPathEffect(null);
    }

    private void drawBottomLine(Canvas canvas){
        PointF fromPoint = new PointF(getPaddingLeft(), getBottomLineYPos());
        PointF toPoint = new PointF(getWidth() - getPaddingRight(), fromPoint.y);
        canvas.drawLine(fromPoint.x, fromPoint.y, toPoint.x, toPoint.y, borderLinePaint);
    }

    private void drawXDetails(Canvas canvas){
        if (xAxisDetails == null) return;

        float minX = getPaddingLeft();
        float maxX = getWidth() - getPaddingRight();
        float yLoc = getHeight() - getPaddingBottom();
        axisTextPaint.setTextAlign(Paint.Align.CENTER);

        for (AxisDetails xDetail : xAxisDetails) {
            float xLoc = xDetail.atPercentage * Math.abs(minX - maxX) + minX;
            if (xDetail.drawLine) canvas.drawLine(xLoc, yLoc, xLoc, yLoc + toDP(5), borderLinePaint);

            axisTextPaint.getTextBounds(xDetail.label, 0, xDetail.label.length(), tmpTextBounds);
            canvas.drawText(xDetail.label, xLoc, yLoc + tmpTextBounds.height() + toDP(7), axisTextPaint);
        }
    }

    private void drawYDetails(Canvas canvas) {
        if (yAxisDetails == null) return;

        float minY = getPaddingTop();
        float maxY = getHeight() - getPaddingBottom();
        float xLoc = getPaddingLeft() - toDP(5);
        axisTextPaint.setTextAlign(Paint.Align.RIGHT);

        for (AxisDetails yDetail : yAxisDetails) {
            axisTextPaint.getTextBounds(yDetail.label, 0, yDetail.label.length(), tmpTextBounds);
            float minYRelativeToTxtHeight = minY + tmpTextBounds.height() - toDP(2);
            float yLoc = yDetail.atPercentage * Math.abs(minYRelativeToTxtHeight - maxY) + minYRelativeToTxtHeight;
            canvas.drawText(yDetail.label, xLoc, yLoc, axisTextPaint);
        }
    }

    private void drawSections(Canvas canvas){
        if(sections == null) return;

        float minX = getPaddingLeft();
        float maxX = getWidth() - getPaddingRight();
        float yLoc = getPaddingTop() - toDP(5);

        float lastX = minX;

        for (Section section : sections) {
            float endX = section.atPercentage * Math.abs(minX - maxX) + minX;
            float middleX = lastX + Math.abs(lastX - endX) / 2;
            canvas.drawText(section.label, middleX, yLoc, sectionPaint);
            if(section.atPercentage < 1) canvas.drawLine(endX, getPaddingTop(), endX, getHeight()-getPaddingBottom(), borderLinePaint);
            lastX = endX;
        }
    }

    private void initUserSpots(){
        int topY = getPaddingTop();
        int bottomY = getHeight() - getPaddingBottom();

        for(GraphUserSpot userSpot : userSpots){
            int x = (int) (getPaddingLeft() - userSpot.getDotRadius());
            userSpot.layout(x, topY - (int)userSpot.getDotDiameter(), x + (int)userSpot.getDotDiameter(), bottomY);
        }
    }

    public void animateUserSpotTo(GraphUserSpot userSpot, double percentage){
        float maximumDistance = getWidth() - getPaddingLeft() - getPaddingRight();
        int x = (int) (percentage * maximumDistance);
        x += getPaddingLeft() - userSpot.getDotRadius(); //offset from left

        if(x != userSpot.getX()) userSpot.animate().x(x).setDuration(500);
    }

    @Override
    public void invalidate() {
        graphBodyLayer = new HashSet<>();
        lineLayer = new HashSet<>();
        super.invalidate();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(event.getActionMasked() == MotionEvent.ACTION_UP){
            handleSectionTapAction(event);
        }
        return super.onTouchEvent(event);
    }

    private void handleSectionTapAction(MotionEvent event){
        if(sections == null || sectionListener == null) return;

        float fromX = getPaddingLeft();
        float toX = getWidth() - getPaddingRight();
        float lastX = fromX;

        for (int i = 0; i < sections.size(); i++) {
            float endX = sections.get(i).atPercentage * Math.abs(fromX - toX) + fromX;
            if(touchIsInSquare(event, lastX, 0, endX, getPaddingTop() + toDP(20))){
                sectionListener.onSelect(i);
            }
            lastX = endX;
        }
    }

    private boolean touchIsInSquare(MotionEvent event, float startX, float startY, float endX, float endY){
        return event.getX() >= startX && event.getX() <= endX
                && event.getY() >= startY && event.getY() <= endY;
    }

    public void addGraphData(GraphData graphData){
        data.add(graphData);
    }

    public void removeGraphData(GraphData graphData){
        data.remove(graphData);
    }

    public void addUserSpot(GraphUserSpot userSpot){
        boolean added = userSpots.add(userSpot);
        if(added) addView(userSpot);
    }

    public void removeUserSpot(GraphUserSpot userSpot){
        boolean removed = userSpots.remove(userSpot);
        if(removed) removeView(userSpot);
    }

    public List<AxisDetails> getxAxisDetails() {
        return xAxisDetails;
    }

    public void setxAxisDetails(List<AxisDetails> xAxisDetails) {
        this.xAxisDetails = xAxisDetails;
    }

    public List<AxisDetails> getyAxisDetails() {
        return yAxisDetails;
    }

    public void setyAxisDetails(List<AxisDetails> yAxisDetails) {
        this.yAxisDetails = yAxisDetails;
    }

    public List<Section> getSections() {
        return sections;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }

    public void setSectionListener(OnSelectSectionListener sectionListener) {
        this.sectionListener = sectionListener;
    }

    private int getAvailableWidth(){
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    private int getAvailableHeight(){
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }

    private float getTopLineYPos(){
        return getPaddingTop();
    }

    private float getMiddleLineYPos(){
        return getTopLineYPos() + (Math.abs(getTopLineYPos() - getBottomLineYPos()) / 2);
    }

    private float getBottomLineYPos(){
        return getHeight() - getPaddingBottom();
    }

    public boolean isDrawMiddleLine() {
        return drawMiddleLine;
    }

    public void setDrawMiddleLine(boolean drawMiddleLine) {
        this.drawMiddleLine = drawMiddleLine;
    }
}