package com.luckyblaze.waffle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

public class BarGraphView extends View {
    private double mPercent = 50;
    private Paint mPaint = new Paint();

    public BarGraphView(Context context) {
        super(context, null);
    }
    public BarGraphView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }
    public BarGraphView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = this.getWidth();
        int height = this.getHeight();

        double barHeight = 0.0;
        if(mPercent > 0){
            barHeight = mPercent / 100 * height;
        }

        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        canvas.drawRect(0, height-(float)barHeight, width, height, mPaint);
    }

    public void setPercent(double percent){
        mPercent = percent;
        invalidate();
    }
}
