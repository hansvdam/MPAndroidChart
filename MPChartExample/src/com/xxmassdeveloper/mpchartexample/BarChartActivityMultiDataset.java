
package com.xxmassdeveloper.mpchartexample;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.WindowManager;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.xxmassdeveloper.mpchartexample.notimportant.DemoBase;

import java.util.ArrayList;

public class BarChartActivityMultiDataset extends DemoBase
{

    private BarChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_barchart);

        mChart = (BarChart) findViewById(R.id.chart1);
        mChart.getDescription().setEnabled(false);
//        mChart.setRenderer(new BarChartRenderer());
        XAxis xAxis = mChart.getXAxis();
        ViewPortHandler viewPortHandler = mChart.getViewPortHandler();
        mChart.setXAxisRenderer(new MyXaxisRenderer(viewPortHandler, xAxis, mChart.getTransformer(
                YAxis.AxisDependency.LEFT)));
//        mChart.setXAxisRenderer(new MyXaxisRender(viewPortHandler, xAxis,
//                                                  new Transformer(viewPortHandler)));

        RoundedBarChartRenderer2 renderer = new RoundedBarChartRenderer2(mChart,
                                                                         mChart.getAnimator(),
                                                                         mChart.getViewPortHandler());
        Drawable drawable = getResources().getDrawable(R.drawable.chart_label_background);

        renderer.setLabelBackground(drawable);
        mChart.setRenderer(renderer);

//        mChart.setDrawBorders(true);

        // scaling can now only be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        mChart.setDrawBarShadow(false);

        mChart.setDrawValueAboveBar(true);
        mChart.setDrawGridBackground(false);
        setValues();
        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
//        MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);
//        mv.setChartView(mChart); // For bounds control
//        mChart.setMarker(mv); // Set the marker to the chart

        mChart.getLegend().setEnabled(false);

        xAxis.setTypeface(mTfLight);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setTextSize(15f);
        xAxis.setCenterAxisLabels(true);
        xAxis.setValueFormatter(new IAxisValueFormatter()
        {
            @Override
            public String getFormattedValue(float value, AxisBase axis)
            {
                return String.valueOf((int) value);
            }

            @Override
            public int getDecimalDigits()
            {
                return 0;
            }
        });

        YAxis axisLeft = mChart.getAxisLeft();
        axisLeft.setEnabled(false);
        axisLeft.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        mChart.getAxisRight().setEnabled(false);
    }

    public void setValues()
    {
        float barWidth = 0.3f; // x3 dataset
        float barSpace = -.5f*barWidth; // x3 dataset
        float groupSpace = 1-2*(barSpace+barWidth);
        // (0.2 + 0.03) * 4 + 0.08 = 1.00 -> interval per "group"

        int groupCount = 7;
        int startYear = 1980;
        int endYear = startYear + groupCount;

        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
        ArrayList<BarEntry> yVals2 = new ArrayList<BarEntry>();

        float randomMultiplier = 100 * 100000f;
        float diff = endYear-startYear;
        for (int i = startYear; i < endYear; i++)
        {
            yVals1.add(new BarEntry(.5f, (float) (Math.random() * randomMultiplier)));
            yVals2.add(new BarEntry(.5f, (float) (Math.random() * randomMultiplier)));
        }

        BarDataSet set1, set2;

        if (mChart.getData() != null && mChart.getData().getDataSetCount() > 0)
        {

            set1 = (BarDataSet) mChart.getData().getDataSetByIndex(0);
            set2 = (BarDataSet) mChart.getData().getDataSetByIndex(1);
            set1.setValues(yVals1);
            set2.setValues(yVals2);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        }
        else
        {
            // create 4 DataSets
            set1 = new BarDataSet(yVals1, "Company A");
            set1.setDrawValues(false);
            int rgb = Color.rgb(104, 241, 175);
            set1.setColor(rgb);
            set2 = new BarDataSet(yVals2, "Company B");
            rgb = Color.rgb(164, 228, 251);
            set2.setColor(rgb);
            set2.setValueTextColor(rgb);
            set2.setValueTextSize(10);

            BarData data = new BarData(set1, set2);
            data.setValueFormatter(new LargeValueFormatter());
            data.setValueTypeface(mTfLight);

            mChart.setData(data);
        }

        // specify the width each bar should have
        mChart.getBarData().setBarWidth(barWidth);

        // restrict the x-axis range
        mChart.getXAxis().setAxisMinimum(startYear);

        // barData.getGroupWith(...) is a helper that calculates the width each group needs based on the provided parameters
        mChart.getXAxis()
                .setAxisMaximum(endYear);
//        mChart.getXAxis()
//                .setAxisMaximum(startYear + mChart.getBarData()
//                        .getGroupWidth(groupSpace, barSpace) * groupCount);
        mChart.groupBars(startYear, groupSpace, barSpace);
        mChart.invalidate();
    }
}
