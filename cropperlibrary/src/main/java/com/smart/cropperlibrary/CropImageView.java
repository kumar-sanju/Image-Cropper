package com.smart.cropperlibrary;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CropImageView extends View {

    private Rect cropRect;
    private Paint borderPaint;
    private Paint handlePaint;
    private int handleSize = 30; // Size of the draggable handle
    private int touchSlop = 20; // Sensitivity for touch events

    private boolean isDragging = false;
    private boolean isResizing = false;
    private int dragEdge = 0;
    private float lastTouchX, lastTouchY;

    public CropImageView(Context context) {
        super(context);
        init();
    }

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        cropRect = new Rect(100, 100, 400, 400); // Initial crop area

        borderPaint = new Paint();
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(5);

        handlePaint = new Paint();
        handlePaint.setColor(Color.RED);
        handlePaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw the crop rectangle
        canvas.drawRect(cropRect, borderPaint);

        // Draw the corner handles
        drawHandles(canvas);
    }

    private void drawHandles(Canvas canvas) {
        canvas.drawRect(cropRect.left - handleSize, cropRect.top - handleSize, cropRect.left + handleSize, cropRect.top + handleSize, handlePaint);
        canvas.drawRect(cropRect.right - handleSize, cropRect.top - handleSize, cropRect.right + handleSize, cropRect.top + handleSize, handlePaint);
        canvas.drawRect(cropRect.left - handleSize, cropRect.bottom - handleSize, cropRect.left + handleSize, cropRect.bottom + handleSize, handlePaint);
        canvas.drawRect(cropRect.right - handleSize, cropRect.bottom - handleSize, cropRect.right + handleSize, cropRect.bottom + handleSize, handlePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isTouchInsideCropRect(event.getX(), event.getY())) {
                    isDragging = true;
                    lastTouchX = event.getX();
                    lastTouchY = event.getY();
                } else if (isTouchOnHandle(event.getX(), event.getY())) {
                    isResizing = true;
                    lastTouchX = event.getX();
                    lastTouchY = event.getY();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (isDragging) {
                    float dx = event.getX() - lastTouchX;
                    float dy = event.getY() - lastTouchY;

                    cropRect.offset((int) dx, (int) dy);
                    lastTouchX = event.getX();
                    lastTouchY = event.getY();
                    invalidate();
                } else if (isResizing) {
                    float dx = event.getX() - lastTouchX;
                    float dy = event.getY() - lastTouchY;

                    resizeCropRect(dx, dy);
                    lastTouchX = event.getX();
                    lastTouchY = event.getY();
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_UP:
                isDragging = false;
                isResizing = false;
                break;
        }
        return true;
    }

    private boolean isTouchInsideCropRect(float x, float y) {
        return cropRect.contains((int) x, (int) y);
    }

    private boolean isTouchOnHandle(float x, float y) {
        return (Math.abs(x - cropRect.left) <= handleSize && Math.abs(y - cropRect.top) <= handleSize) ||
                (Math.abs(x - cropRect.right) <= handleSize && Math.abs(y - cropRect.top) <= handleSize) ||
                (Math.abs(x - cropRect.left) <= handleSize && Math.abs(y - cropRect.bottom) <= handleSize) ||
                (Math.abs(x - cropRect.right) <= handleSize && Math.abs(y - cropRect.bottom) <= handleSize);
    }

    private void resizeCropRect(float dx, float dy) {
        if (dragEdge == 0) {
            cropRect.left += dx;
            cropRect.top += dy;
        } else if (dragEdge == 1) {
            cropRect.right += dx;
            cropRect.top += dy;
        } else if (dragEdge == 2) {
            cropRect.left += dx;
            cropRect.bottom += dy;
        } else if (dragEdge == 3) {
            cropRect.right += dx;
            cropRect.bottom += dy;
        }

        // Ensure minimum size for the crop rectangle
        int minWidth = 100;
        int minHeight = 100;
        if (cropRect.width() < minWidth) {
            cropRect.right = cropRect.left + minWidth;
        }
        if (cropRect.height() < minHeight) {
            cropRect.bottom = cropRect.top + minHeight;
        }
    }

    public Rect getCropRect() {
        return cropRect;
    }
}