package com.example.mobile;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;

public class ZoomableImageView extends AppCompatImageView {

    private static final String TAG = "ZoomableImageView";

    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();

    private static final float MIN_ZOOM = 1.0f;
    private static final float MAX_ZOOM = 5.0f;
    private float scaleFactor = 1.0f;

    private PointF start = new PointF();
    private PointF mid = new PointF();
    private float oldDist = 1f;

    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;

    private ScaleGestureDetector scaleDetector;
    private boolean isInitialized = false;

    public ZoomableImageView(Context context) {
        super(context);
        init(context);
    }

    public ZoomableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ZoomableImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        setScaleType(ScaleType.MATRIX);

        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());

        addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (getWidth() > 0 && getHeight() > 0 && !isInitialized && getDrawable() != null) {
                    setupInitialMatrix();
                    isInitialized = true;
                }
            }
        });
    }

    private void setupInitialMatrix() {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            Log.d(TAG, "Drawable is null");
            return;
        }

        try {
            int drawableWidth = drawable.getIntrinsicWidth();
            int drawableHeight = drawable.getIntrinsicHeight();
            int viewWidth = getWidth();
            int viewHeight = getHeight();

            if (viewWidth == 0 || viewHeight == 0 || drawableWidth <= 0 || drawableHeight <= 0) {
                Log.d(TAG, "Invalid dimensions - View: " + viewWidth + "x" + viewHeight +
                        ", Drawable: " + drawableWidth + "x" + drawableHeight);
                return;
            }

            // Hitung scale untuk fit center
            float scaleX = (float) viewWidth / drawableWidth;
            float scaleY = (float) viewHeight / drawableHeight;
            scaleFactor = Math.min(scaleX, scaleY);

            // Hitung translation untuk center
            float translateX = (viewWidth - drawableWidth * scaleFactor) / 2;
            float translateY = (viewHeight - drawableHeight * scaleFactor) / 2;

            // Apply transformasi
            matrix.reset();
            matrix.setScale(scaleFactor, scaleFactor);
            matrix.postTranslate(translateX, translateY);
            setImageMatrix(matrix);

            Log.d(TAG, "Initial setup - Scale: " + scaleFactor +
                    ", Translate: " + translateX + ", " + translateY +
                    ", View: " + viewWidth + "x" + viewHeight);

        } catch (Exception e) {
            Log.e(TAG, "Error in setupInitialMatrix: " + e.getMessage(), e);
        }
    }

    @Override
    protected void onDraw(android.graphics.Canvas canvas) {
        try {
            if (!isInitialized && getDrawable() != null && getWidth() > 0 && getHeight() > 0) {
                setupInitialMatrix();
                isInitialized = true;
            }
            super.onDraw(canvas);
        } catch (Exception e) {
            Log.e(TAG, "Error in onDraw: " + e.getMessage());
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            if (!isInitialized) return true;

            scaleDetector.onTouchEvent(event);

            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    savedMatrix.set(matrix);
                    start.set(event.getX(), event.getY());
                    mode = DRAG;
                    break;

                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = spacing(event);
                    if (oldDist > 10f) {
                        savedMatrix.set(matrix);
                        midPoint(mid, event);
                        mode = ZOOM;
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (mode == DRAG) {
                        matrix.set(savedMatrix);
                        float dx = event.getX() - start.x;
                        float dy = event.getY() - start.y;
                        matrix.postTranslate(dx, dy);
                        setImageMatrix(matrix);
                    } else if (mode == ZOOM && event.getPointerCount() == 2) {
                        float newDist = spacing(event);
                        if (newDist > 10f) {
                            matrix.set(savedMatrix);
                            float scale = newDist / oldDist;

                            scaleFactor *= scale;
                            scaleFactor = Math.max(MIN_ZOOM, Math.min(scaleFactor, MAX_ZOOM));

                            matrix.postScale(scale, scale, mid.x, mid.y);
                            setImageMatrix(matrix);
                        }
                    }
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    mode = NONE;
                    break;
            }

            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error in onTouchEvent: " + e.getMessage(), e);
            return true;
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            try {
                if (!isInitialized) return false;

                float scale = detector.getScaleFactor();
                scaleFactor *= scale;
                scaleFactor = Math.max(MIN_ZOOM, Math.min(scaleFactor, MAX_ZOOM));

                matrix.set(savedMatrix);
                matrix.postScale(scale, scale, detector.getFocusX(), detector.getFocusY());
                setImageMatrix(matrix);

                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error in onScale: " + e.getMessage());
                return false;
            }
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mode = ZOOM;
            savedMatrix.set(matrix);
            return true;
        }
    }

    private float spacing(MotionEvent event) {
        try {
            if (event.getPointerCount() < 2) return 0f;

            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return (float) Math.sqrt(x * x + y * y);
        } catch (Exception e) {
            return 0f;
        }
    }

    private void midPoint(PointF point, MotionEvent event) {
        try {
            if (event.getPointerCount() < 2) return;

            float x = event.getX(0) + event.getX(1);
            float y = event.getY(0) + event.getY(1);
            point.set(x / 2, y / 2);
        } catch (Exception e) {
            Log.e(TAG, "Error in midPoint: " + e.getMessage());
        }
    }

    public void resetZoom() {
        try {
            scaleFactor = 1.0f;
            setupInitialMatrix();
            invalidate();
            Log.d(TAG, "Zoom reset to initial state");
        } catch (Exception e) {
            Log.e(TAG, "Error in resetZoom: " + e.getMessage());
        }
    }

    public float getCurrentScale() {
        return scaleFactor;
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        isInitialized = false;

        post(() -> {
            if (getWidth() > 0 && getHeight() > 0) {
                setupInitialMatrix();
                isInitialized = true;
                invalidate();
            }
        });
    }
}