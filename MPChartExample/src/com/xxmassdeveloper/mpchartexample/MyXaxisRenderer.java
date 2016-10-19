package com.xxmassdeveloper.mpchartexample;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.ViewPortHandler;

public class MyXaxisRenderer extends com.github.mikephil.charting.renderer.XAxisRenderer
{
    public MyXaxisRenderer(ViewPortHandler viewPortHandler,
                           XAxis xAxis,
                           Transformer trans)
    {
        super(viewPortHandler, xAxis, trans);
    }

}
