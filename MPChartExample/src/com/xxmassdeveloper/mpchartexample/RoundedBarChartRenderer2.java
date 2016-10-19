package com.xxmassdeveloper.mpchartexample;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.buffer.BarBuffer;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.renderer.BarChartRenderer;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.List;

public class RoundedBarChartRenderer2 extends BarChartRenderer
{
    private boolean mDrawRoundedBars = true;
    private Drawable mLabelBackgroundDrawable;

    public RoundedBarChartRenderer2(BarDataProvider chart,
                                    ChartAnimator animator,
                                    ViewPortHandler viewPortHandler)
    {
        super(chart, animator, viewPortHandler);
    }

    public void setLabelBackground(Drawable drawable)
    {
        mLabelBackgroundDrawable = drawable;
    }

    float mRoundedBarRadius = 30f;

    static public Path roundedRect(float left,
                                   float top,
                                   float right,
                                   float bottom,
                                   float rx,
                                   float ry,
                                   boolean conformToOriginalPost)
    {
        Path path = new Path();
        if (rx < 0)
        {
            rx = 0;
        }
        if (ry < 0)
        {
            ry = 0;
        }
        float width = right - left;
        float height = bottom - top;
        if (rx > width / 2)
        {
            rx = width / 2;
        }
        if (ry > height / 2)
        {
            ry = height / 2;
        }
        float widthMinusCorners = (width - (2 * rx));
        float heightMinusCorners = (height - (2 * ry));

        path.moveTo(right, top + ry);
        path.rQuadTo(0, - ry, - rx, - ry);//top-right corner
        path.rLineTo(- widthMinusCorners, 0);
        path.rQuadTo(- rx, 0, - rx, ry); //top-left corner
        path.rLineTo(0, heightMinusCorners);

        if (conformToOriginalPost)
        {
            path.rLineTo(0, ry);
            path.rLineTo(width, 0);
            path.rLineTo(0, - ry);
        }
        else
        {
            path.rQuadTo(0, ry, rx, ry);//bottom-left corner
            path.rLineTo(widthMinusCorners, 0);
            path.rQuadTo(rx, 0, rx, - ry); //bottom-right corner
        }

        path.rLineTo(0, - heightMinusCorners);

        path.close();//Given close, last lineto can be removed.

        return path;
    }

    @Override
    public void drawValues(Canvas c) {

        // if values are drawn
        if (isDrawingValuesAllowed(mChart)) {

            List<IBarDataSet> dataSets = mChart.getBarData().getDataSets();

            final float valueOffsetPlus = Utils.convertDpToPixel(10);
            float posOffset = 0f;
            float negOffset = 0f;
            boolean drawValueAboveBar = mChart.isDrawValueAboveBarEnabled();

            for (int i = 0; i < mChart.getBarData().getDataSetCount(); i++) {

                IBarDataSet dataSet = dataSets.get(i);

                if (!shouldDrawValues(dataSet))
                    continue;

                // apply the text-styling defined by the DataSet
                applyValueTextStyle(dataSet);

                boolean isInverted = mChart.isInverted(dataSet.getAxisDependency());

                // calculate the correct offset depending on the draw position of
                // the value
                float valueTextHeight = Utils.calcTextHeight(mValuePaint, "8");
                posOffset = (drawValueAboveBar ? -valueOffsetPlus : valueTextHeight + valueOffsetPlus);
                negOffset = (drawValueAboveBar ? valueTextHeight + valueOffsetPlus : -valueOffsetPlus);

                if (isInverted) {
                    posOffset = -posOffset - valueTextHeight;
                    negOffset = -negOffset - valueTextHeight;
                }

                // get the buffer
                BarBuffer buffer = mBarBuffers[i];

                final float phaseY = mAnimator.getPhaseY();

                // if only single values are drawn (sum)
                if (!dataSet.isStacked()) {

                    for (int j = 0; j < buffer.buffer.length * mAnimator.getPhaseX(); j += 4) {

                        float x = (buffer.buffer[j] + buffer.buffer[j + 2]) / 2f;

                        if (!mViewPortHandler.isInBoundsRight(x))
                            break;

                        if (!mViewPortHandler.isInBoundsY(buffer.buffer[j + 1])
                                || !mViewPortHandler.isInBoundsLeft(x))
                            continue;

                        BarEntry entry = dataSet.getEntryForIndex(j / 4);
                        float val = entry.getY();

                        drawValue(c, dataSet.getValueFormatter(), val, entry, i, x,
                                  val >= 0 ? (buffer.buffer[j + 1] + posOffset) : (buffer.buffer[j + 3] + negOffset),
                                  dataSet.getValueTextColor(j / 4));
                    }

                    // if we have stacks
                } else {

                    Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

                    int bufferIndex = 0;
                    int index = 0;

                    while (index < dataSet.getEntryCount() * mAnimator.getPhaseX()) {

                        BarEntry entry = dataSet.getEntryForIndex(index);

                        float[] vals = entry.getYVals();
                        float x = (buffer.buffer[bufferIndex] + buffer.buffer[bufferIndex + 2]) / 2f;

                        int color = dataSet.getValueTextColor(index);

                        // we still draw stacked bars, but there is one
                        // non-stacked
                        // in between
                        if (vals == null) {

                            if (!mViewPortHandler.isInBoundsRight(x))
                                break;

                            if (!mViewPortHandler.isInBoundsY(buffer.buffer[bufferIndex + 1])
                                    || !mViewPortHandler.isInBoundsLeft(x))
                                continue;

                            drawValue(c, dataSet.getValueFormatter(), entry.getY(), entry, i, x,
                                      buffer.buffer[bufferIndex + 1] + (entry.getY() >= 0 ? posOffset : negOffset),
                                      color);

                            // draw stack values
                        } else {

                            float[] transformed = new float[vals.length * 2];

                            float posY = 0f;
                            float negY = -entry.getNegativeSum();

                            for (int k = 0, idx = 0; k < transformed.length; k += 2, idx++) {

                                float value = vals[idx];
                                float y;

                                if (value >= 0f) {
                                    posY += value;
                                    y = posY;
                                } else {
                                    y = negY;
                                    negY -= value;
                                }

                                transformed[k + 1] = y * phaseY;
                            }

                            trans.pointValuesToPixel(transformed);

                            for (int k = 0; k < transformed.length; k += 2) {

                                float y = transformed[k + 1]
                                        + (vals[k / 2] >= 0 ? posOffset : negOffset);

                                if (!mViewPortHandler.isInBoundsRight(x))
                                    break;

                                if (!mViewPortHandler.isInBoundsY(y)
                                        || !mViewPortHandler.isInBoundsLeft(x))
                                    continue;

                                drawValue(c, dataSet.getValueFormatter(), vals[k / 2], entry, i, x, y, color);
                            }
                        }

                        bufferIndex = vals == null ? bufferIndex + 4 : bufferIndex + 4 * vals.length;
                        index++;
                    }
                }
            }
        }
    }

    @Override
    public void drawValue(Canvas c,
                          IValueFormatter formatter,
                          float value,
                          Entry entry,
                          int dataSetIndex,
                          float x,
                          float y,
                          int color)
    {
//        super.drawValue(c,formatter,value,entry,dataSetIndex,x,y,color);
        mValuePaint.setColor(color);
        String formattedValue = formatter.getFormattedValue(value,
                                                            entry,
                                                            dataSetIndex,
                                                            mViewPortHandler);

        Rect textBounds = new Rect();
        mValuePaint.getTextBounds(formattedValue, 0, formattedValue.length(), textBounds);
        float halfWidth = textBounds.width() / 2;
        float height = textBounds.height();
        float margin = (int) (height/1.2);
        mLabelBackgroundDrawable.setBounds(Math.round(x-halfWidth-margin), Math.round(y- height-margin), Math.round(x + halfWidth+margin), Math.round(y+margin));
        mLabelBackgroundDrawable.draw(c);
        c.drawText(formattedValue, x, y, mValuePaint);
    }

    protected void drawDataSet(Canvas c, IBarDataSet dataSet, int index)
    {

        Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

        mBarBorderPaint.setColor(dataSet.getBarBorderColor());
        mBarBorderPaint.setStrokeWidth(Utils.convertDpToPixel(dataSet.getBarBorderWidth()));

        final boolean drawBorder = dataSet.getBarBorderWidth() > 0.f;

        float phaseX = mAnimator.getPhaseX();
        float phaseY = mAnimator.getPhaseY();

        // initialize the buffer
        BarBuffer buffer = mBarBuffers[index];
        buffer.setPhases(phaseX, phaseY);
        buffer.setDataSet(index);
        buffer.setInverted(mChart.isInverted(dataSet.getAxisDependency()));
        buffer.setBarWidth(mChart.getBarData().getBarWidth());

        buffer.feed(dataSet);

        trans.pointValuesToPixel(buffer.buffer);

        final boolean isSingleColor = dataSet.getColors().size() == 1;

        if (isSingleColor)
        {
            mRenderPaint.setColor(dataSet.getColor());
        }

        for (int j = 0; j < buffer.size(); j += 4)
        {

            if (! mViewPortHandler.isInBoundsLeft(buffer.buffer[j + 2]))
            {
                continue;
            }

            if (! mViewPortHandler.isInBoundsRight(buffer.buffer[j]))
            {
                break;
            }

            // Set the color for the currently drawn value. If the index
            // is out of bounds, reuse colors.
            mRenderPaint.setColor(dataSet.getColor(j / 4));
            if (mDrawRoundedBars)
            {
                c.drawPath(roundedRect(buffer.buffer[j],
                                       buffer.buffer[j + 1],
                                       buffer.buffer[j + 2],
                                       buffer.buffer[j + 3],
                                       mRoundedBarRadius,
                                       mRoundedBarRadius,
                                       true), mRenderPaint);

//                c.drawRoundRect(new RectF(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
//                                          buffer.buffer[j + 3]), mRoundedBarRadius, mRoundedBarRadius, mRenderPaint);
            }
            else
            {
                c.drawRect(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                           buffer.buffer[j + 3], mRenderPaint);
            }

            if (drawBorder)
            {
                if (mDrawRoundedBars)
                {
                    c.drawPath(roundedRect(buffer.buffer[j],
                                           buffer.buffer[j + 1],
                                           buffer.buffer[j + 2],
                                           buffer.buffer[j + 3],
                                           mRoundedBarRadius,
                                           mRoundedBarRadius,
                                           true), mBarBorderPaint);
                }
                else
                {
                    c.drawRect(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                               buffer.buffer[j + 3], mBarBorderPaint);
                }
            }
        }
    }
}
