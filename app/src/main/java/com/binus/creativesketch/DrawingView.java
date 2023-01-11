package com.binus.creativesketch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.binus.creativesketch.CustomPath;

import java.util.ArrayList;

public class DrawingView extends View {
    private CustomPath mDrawPath;
    private Bitmap mCanvasBitmap;
    private Paint mDrawPaint;
    private Paint mCanvasPaint;
    private float mBrushSize;
    private int color = Color.BLACK;
    private Canvas canvas;
    private ArrayList<CustomPath> mPaths = new ArrayList<>();
    private ArrayList<CustomPath> mUndoPaths = new ArrayList<>();

    public DrawingView(Context context, AttributeSet attr) {
        super(context, attr);
        setUpDrawing();
    }

    public void onClickUndo() {
        if (mPaths.size() > 0) {
            mUndoPaths.add(mPaths.remove(mPaths.size() - 1));
            invalidate();
        }
    }

    public void onClickRedo() {
        if (mUndoPaths.size() > 0) {
            mPaths.add(mUndoPaths.remove(mUndoPaths.size() - 1));
            invalidate();
        }
    }

    public void onClickErase() {
        if (mPaths.size() > 0) {
            mPaths.clear();
            invalidate();
        }
    }

    private void setUpDrawing() {
        mDrawPaint = new Paint();
        mDrawPath = new CustomPath(color, mBrushSize);
        mDrawPaint.setColor(color);
        mDrawPaint.setStyle(Paint.Style.STROKE);
        mDrawPaint.setStrokeJoin(Paint.Join.ROUND);
        mDrawPaint.setStrokeCap(Paint.Cap.ROUND);
        mCanvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(mCanvasBitmap);
    }

    public void setColor(String newColor) {
        color = Color.parseColor(newColor);
        mDrawPaint.setColor(color);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mCanvasBitmap, 0f, 0f, mCanvasPaint);

        for (CustomPath path : mPaths) {
            mDrawPaint.setStrokeWidth(path.getBrushThick());
            mDrawPaint.setColor(path.getColor());
            canvas.drawPath(path, mDrawPaint);
        }
        if (!mDrawPath.isEmpty()) {
            mDrawPaint.setStrokeWidth(mDrawPath.getBrushThick());
            mDrawPaint.setColor(mDrawPath.getColor());
            canvas.drawPath(mDrawPath, mDrawPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDrawPath.setColor(color);
                mDrawPath.setBrushThick(mBrushSize);

                mDrawPath.reset();
                mDrawPath.moveTo(touchX, touchY);
                break;

            case MotionEvent.ACTION_MOVE:
                mDrawPath.lineTo(touchX, touchY);
                break;

            case MotionEvent.ACTION_UP:
                mPaths.add(mDrawPath);
                mDrawPath = new CustomPath(color, mBrushSize);
                break;

            default:
                return false;
        }

        invalidate();
        return true;
    }

    public void setBrushSize(float newSize) {
        mBrushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newSize, getResources().getDisplayMetrics());
        mDrawPaint.setStrokeWidth(mBrushSize);
    }

    public void setSizeForBrush(float newSize) {
        mBrushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newSize, getResources().getDisplayMetrics());
        mDrawPaint.setStrokeWidth(mBrushSize);
    }

}
