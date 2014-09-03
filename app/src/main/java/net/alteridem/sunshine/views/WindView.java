package net.alteridem.sunshine.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class WindView extends View {

    private final int SIZE = 60;
    private final int STROKE_WIDTH = 3;
    private Paint mPaint;
    private double mDegrees;

    public WindView(Context context) {
        super(context);
        init();
    }

    public WindView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WindView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.DKGRAY);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(STROKE_WIDTH);
    }

    public void setWindDirection(double degrees) {
        mDegrees = degrees;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Try for a width based on our minimum
        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int w = resolveSizeAndState(minw, widthMeasureSpec, 1);

        // Whatever the width ends up being, ask for a height that would let the pie
        // get as big as it can
        int minh = getPaddingBottom() + getPaddingTop() + getSuggestedMinimumHeight();
        int h = resolveSizeAndState(minh, heightMeasureSpec, 0);

        setMeasuredDimension(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = canvas.getWidth();
        int height = canvas.getHeight();
        int xCenter = width/2;
        int yCenter = height/2;

        int radius = Math.min(xCenter, yCenter) - (2 * STROKE_WIDTH);

        int arrowSize = (int)(radius * .2);
        int yLineStart = yCenter - radius + 2 * STROKE_WIDTH;
        int yLineEnd = yCenter + radius - 2 * STROKE_WIDTH;

        canvas.rotate((float)mDegrees, xCenter, yCenter);
        canvas.drawCircle(xCenter, yCenter, radius, mPaint);
        canvas.drawLine(xCenter, yLineStart, xCenter, yLineEnd, mPaint);
        canvas.drawLine(xCenter, yLineEnd, xCenter - arrowSize, yLineEnd - arrowSize, mPaint);
        canvas.drawLine(xCenter, yLineEnd, xCenter + arrowSize, yLineEnd - arrowSize, mPaint);
    }
}
