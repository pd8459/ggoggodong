package com.example.ggoggodong.story;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


public class DrawingView extends View {
    private Paint paint;
    private Path path;
    private boolean isEraseMode;
    private float eraseRadius = 30f;
    private Path currentPath;
    private int currentColor;
    public DrawingView(Context context) {
        super(context);
        init();
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public void setColor(int color) {
        currentColor = color;
        isEraseMode = false;
        paint.setColor(currentColor);
        invalidate();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(0xFF000000); // 검정색
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8);
        path = new Path();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(path, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(x, y);
                return true;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(x, y);
                break;
            default:
                return false;
        }
        invalidate();
        return true;
    }
}
