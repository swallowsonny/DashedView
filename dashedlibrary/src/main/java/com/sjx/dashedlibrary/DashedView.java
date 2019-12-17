package com.sjx.dashedlibrary;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

public class DashedView extends View implements LifecycleObserver {

    // 形状
    private BorderShape borderShape;
    // 圆角，圆半径
    private float borderRadis;
    // 虚线长
    private float dashedLength;
    // 虚线的线宽
    private float dashedWidth;
    // 虚线间隔
    private float spaceLength;
    // 虚线颜色
    private int dashedColor;
    // 动画方向
    private AnimateDirection animateDirection;
    // 动画速度
    private AnimateSpeed animateSpeed;

    private Handler mHandler = new Handler();
    private Runnable runnable;

    private Paint paint;
    private DashPathEffect dashPathEffect;
    private RectF rectF;

    private long phase;

    public DashedView(Context context) {
        this(context, null);
    }

    public DashedView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DashedView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAll(context, attrs, defStyleAttr);
    }

    private void initAll(Context context, AttributeSet attrs, int defStyleAttr) {
        getAttrs(context, attrs);
        initPaint();
    }

    private void getAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.DashedView);
        // 获取形状
        int shape = ta.getInt(R.styleable.DashedView_border_shape, 1);
        borderShape = shape == 0 ? BorderShape.CIRCLE : BorderShape.RECTANGLE;
        // 圆角大小
        borderRadis = ta.getFloat(R.styleable.DashedView_border_radis, 0);
        // 获取长度
        dashedLength = ta.getFloat(R.styleable.DashedView_dashed_length, 20);
        dashedWidth = ta.getFloat(R.styleable.DashedView_dashed_width, 4);
        spaceLength = ta.getFloat(R.styleable.DashedView_space_length, 10);
        // 获取颜色
        dashedColor = ta.getColor(R.styleable.DashedView_dashed_color, Color.RED);
        // 获取动画
        // 动画方向
        int direction = ta.getInt(R.styleable.DashedView_animate_direction, 0);
        switch (direction) {
            case 0:
                animateDirection = AnimateDirection.NONE;
                break;
            case 1:
                animateDirection = AnimateDirection.CLOCKWISE;
                break;
            case 2:
                animateDirection = AnimateDirection.ANTICLOCKWISE;
                break;
        }
        // 动画速度
        int speed = ta.getInt(R.styleable.DashedView_animate_speed, 1);
        switch (speed) {
            case 0:
                animateSpeed = AnimateSpeed.LOW;
                break;
            case 1:
                animateSpeed = AnimateSpeed.NORMAL;
                break;
            case 2:
                animateSpeed = AnimateSpeed.FAST;
                break;
        }
        ta.recycle();
    }

    private void initPaint() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(dashedColor);
        paint.setStrokeWidth(dashedWidth);

        dashPathEffect = new DashPathEffect(new float[]{dashedLength, spaceLength}, 0f);
        paint.setPathEffect(dashPathEffect);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private void startAnimate() {
        if (runnable != null) return;
        runnable = new Runnable() {
            @Override
            public void run() {
                phase += getPhase();
                dashPathEffect = new DashPathEffect(new float[]{dashedLength, spaceLength}, phase);
                paint.setPathEffect(dashPathEffect);
                postInvalidate();
                //延迟执行
                mHandler.postDelayed(this, 200);
            }
        };
        mHandler.post(runnable);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private void stopAnimate() {
        mHandler.removeCallbacks(runnable);
        runnable = null;
    }

    private float getPhase() {
        float speed = 10f;
        switch (animateSpeed) {
            case LOW:
                speed = (dashedLength + dashedWidth) / 8;
                break;
            case NORMAL:
                speed = (dashedLength + dashedWidth) / 4;
                break;
            case FAST:
                speed = (dashedLength + dashedWidth) / 2;
                break;
        }

        if (animateDirection == AnimateDirection.NONE) {
            speed = 0;
        } else if (animateDirection == AnimateDirection.CLOCKWISE) { // 顺时针
            speed *= -1;
        } else {
            speed *= 1;
        }
        return speed;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (rectF == null) {
            rectF = new RectF(dashedWidth, dashedWidth, getMeasuredWidth() - dashedWidth, getMeasuredHeight() - dashedWidth);
        }
        switch (borderShape) {
            case CIRCLE:
                canvas.drawOval(rectF, paint);
                break;
            case RECTANGLE:
                // 绘制圆角矩形
                canvas.drawRoundRect(rectF, borderRadis, borderRadis, paint);
                break;
        }
    }


    private Lifecycle mLifecycle;

    public void setLifecycleOwner(@NonNull LifecycleOwner owner) {
        if (mLifecycle != null) mLifecycle.removeObserver(this);
        mLifecycle = owner.getLifecycle();
        mLifecycle.addObserver(this);
    }

    public void setAnimateDirection(AnimateDirection animateDirection) {
        this.animateDirection = animateDirection;
    }

    public enum BorderShape {
        CIRCLE,
        RECTANGLE
    }

    public enum AnimateDirection {
        NONE,          // 无动画
        CLOCKWISE,     // 顺时针
        ANTICLOCKWISE  // 逆时针
    }

    public enum AnimateSpeed {
        LOW,
        NORMAL,
        FAST
    }
}
