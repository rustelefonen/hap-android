package no.rustelefonen.hap.main.tabs.program;

import android.graphics.Color;
import android.os.AsyncTask;
import android.view.View;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.List;

import no.rustelefonen.hap.entities.UserTrigger;

import static no.rustelefonen.hap.util.Dimensions.toDP;
import static no.rustelefonen.hap.util.Dimensions.toSP;

/**
 * Created by Fredrik on 10.03.2016.
 */
class AsyncTriggerPieBuilder extends AsyncTask<Void, Void, Void> {
    private Highlight highlight;

    private PieChart chartPie;
    private View chartPieNoData;
    private List<UserTrigger> userTriggers;

    public AsyncTriggerPieBuilder(PieChart chartPie, View chartPieNoData, List<UserTrigger> userTriggers) {
        this.chartPie = chartPie;
        this.chartPieNoData = chartPieNoData;
        this.userTriggers = userTriggers;
    }

    @Override
    protected Void doInBackground(Void... params) {
        if(!userTriggers.isEmpty()) highlight = buildPie(chartPie, userTriggers, getTotalTriggerCount(userTriggers));
        return null;
    }

    @Override
    protected void onPostExecute(Void res) {
        super.onPostExecute(res);
        hideChartIfEmptyList(chartPie, chartPieNoData, userTriggers.isEmpty());

        if(!userTriggers.isEmpty()) animateChart(chartPie, highlight);
    }

    private Highlight buildPie(final PieChart chartPie, List<UserTrigger> userTriggers, int totalNumberOfTriggers){
        chartPie.setDescription("");
        chartPie.setRotationEnabled(false);
        chartPie.setCenterTextSizePixels(toSP(12));

        ArrayList<Entry> values = new ArrayList<>();
        final ArrayList<String> labels = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        int i = 0;
        int highestEntryIndex = 0;
        for(UserTrigger userTrigger : userTriggers){
            values.add(new Entry((userTrigger.getCount() / (float) totalNumberOfTriggers) * 100, i++));
            labels.add(userTrigger.getTrigger().getTitle());
            colors.add(userTrigger.getTrigger().getColor());
            if(values.get(i-1).getVal() > values.get(highestEntryIndex).getVal()) highestEntryIndex = i - 1;
        }

        final PieDataSet dataSet = new PieDataSet(values, "");
        dataSet.setSliceSpace(toDP(1));
        dataSet.setSelectionShift(toDP(2.5f));
        dataSet.setColors(colors);

        PieData data = new PieData(labels, dataSet);
        data.setValueTextColor(Color.TRANSPARENT);
        chartPie.setData(data);

        Legend l = chartPie.getLegend();
        l.setEnabled(false);

        chartPie.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                int percentage = Math.round(e.getVal());
                chartPie.setCenterText(labels.get(dataSet.getEntryIndex(e)) + "\n" + percentage + "%");
            }

            @Override
            public void onNothingSelected() {
                chartPie.setCenterText("Velg sektor");
            }
        });

        return new Highlight(highestEntryIndex, 0);
    }

    private int getTotalTriggerCount(List<UserTrigger> userTriggers){
        int count = 0;
        for(UserTrigger userTrigger : userTriggers){
            count += userTrigger.getCount();
        }
        return count;
    }

    private void hideChartIfEmptyList(PieChart chart, View showIfHidden, boolean hide){
        chart.setVisibility(hide ? View.GONE : View.VISIBLE);
        showIfHidden.setVisibility(hide ? View.VISIBLE : View.GONE);
    }

    private void animateChart(PieChart chart, Highlight highlight){
        chart.highlightValue(highlight, true);
        chart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
    }
}