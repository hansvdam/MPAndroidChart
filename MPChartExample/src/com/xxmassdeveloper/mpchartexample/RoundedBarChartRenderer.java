package com.xxmassdeveloper.mpchartexample;

import android.graphics.Canvas;
import android.graphics.RectF;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.buffer.BarBuffer;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.renderer.BarChartRenderer;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

public class RoundedBarChartRenderer extends BarChartRenderer
{
    private boolean mDrawRoundedBars = true;

    public RoundedBarChartRenderer(BarDataProvider chart,
                                   ChartAnimator animator,
                                   ViewPortHandler viewPortHandler)
    {
        super(chart, animator, viewPortHandler);
    }

    float mRoundedBarRadius = 100f;
    protected void drawDataSet(Canvas c, IBarDataSet dataSet, int index) {

        Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

        mShadowPaint.setColor(dataSet.getBarShadowColor());
        mBarBorderPaint.setColor(dataSet.getBarBorderColor());
        mBarBorderPaint.setStrokeWidth(Utils.convertDpToPixel(dataSet.getBarBorderWidth()));

        final boolean drawBorder = dataSet.getBarBorderWidth() > 0.f;

        float phaseX = mAnimator.getPhaseX();
        float phaseY = mAnimator.getPhaseY();

        // initialize the buffer
        BarBuffer buffer = mBarBuffers[index];
        buffer.setPhases(phaseX, phaseY);
//        buffer.setBarSpace(-.1f);
//        buffer.setBarSpace(dataSet.getBarSpace());
        buffer.setDataSet(index);
        buffer.setInverted(mChart.isInverted(dataSet.getAxisDependency()));

        buffer.feed(dataSet);

        trans.pointValuesToPixel(buffer.buffer);

        // draw the bar shadow before the values
        if (mChart.isDrawBarShadowEnabled()) {

            for (int j = 0; j < buffer.size(); j += 4) {

                if (!mViewPortHandler.isInBoundsLeft(buffer.buffer[j + 2]))
                    continue;

                if (!mViewPortHandler.isInBoundsRight(buffer.buffer[j]))
                    break;

                if (mDrawRoundedBars) {
                    c.drawRoundRect(new RectF(buffer.buffer[j], mViewPortHandler.contentTop(),
                                              buffer.buffer[j + 2],
                                              mViewPortHandler.contentBottom()), mRoundedBarRadius, mRoundedBarRadius, mShadowPaint);
                } else {
                    c.drawRect(buffer.buffer[j], mViewPortHandler.contentTop(),
                               buffer.buffer[j + 2],
                               mViewPortHandler.contentBottom(), mShadowPaint);
                }
            }
        }

        // if multiple colors
        if (dataSet.getColors().size() > 1) {

            for (int j = 0; j < buffer.size(); j += 4) {

                if (!mViewPortHandler.isInBoundsLeft(buffer.buffer[j + 2]))
                    continue;

                if (!mViewPortHandler.isInBoundsRight(buffer.buffer[j]))
                    break;

                // Set the color for the currently drawn value. If the index
                // is out of bounds, reuse colors.
                mRenderPaint.setColor(dataSet.getColor(j / 4));
                if (mDrawRoundedBars) {
                    c.drawRoundRect(new RectF(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                                              buffer.buffer[j + 3]), mRoundedBarRadius, mRoundedBarRadius, mRenderPaint);
                } else {
                    c.drawRect(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                               buffer.buffer[j + 3], mRenderPaint);
                }

                if (drawBorder) {
                    if (mDrawRoundedBars) {
                        c.drawRoundRect(new RectF(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                                                  buffer.buffer[j + 3]), mRoundedBarRadius, mRoundedBarRadius, mBarBorderPaint);
                    } else {
                        c.drawRect(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                                   buffer.buffer[j + 3], mBarBorderPaint);
                    }
                }
            }
        } else {

            mRenderPaint.setColor(dataSet.getColor());

            for (int j = 0; j < buffer.size(); j += 4) {

                if (!mViewPortHandler.isInBoundsLeft(buffer.buffer[j + 2]))
                    continue;

                if (!mViewPortHandler.isInBoundsRight(buffer.buffer[j]))
                    break;

                if (mDrawRoundedBars) {
                    c.drawRoundRect(new RectF(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                                              buffer.buffer[j + 3]), mRoundedBarRadius, mRoundedBarRadius, mRenderPaint);
                } else {
                    c.drawRect(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                               buffer.buffer[j + 3], mRenderPaint);
                }

                if (drawBorder) {
                    if (mDrawRoundedBars) {
                        c.drawRoundRect(new RectF(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                                                  buffer.buffer[j + 3]), mRoundedBarRadius, mRoundedBarRadius, mBarBorderPaint);
                    } else {
                        c.drawRect(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                                   buffer.buffer[j + 3], mBarBorderPaint);
                    }
                }
            }
        }
    }
}
