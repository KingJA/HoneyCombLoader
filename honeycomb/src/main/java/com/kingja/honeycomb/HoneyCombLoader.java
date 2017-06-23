package com.kingja.honeycomb;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

/**
 * Description:TODO
 * Create Time:2017/6/22 14:14
 * Author:KingJA
 * Email:kingjavip@gmail.com
 */
public class HoneyCombLoader extends View {
    private String TAG = "HoneyCombLoader";
    private float mViewSize;
    private boolean isFirst = true;
    private Paint mHoneyCombPaint;
    private PointF[] pointFs = new PointF[3];
    private Paint mMoveBallPaint;
    private float mHoneyCombRadius;
    private float mPaintWidth;
    private int color = 0xffCA5313;
    private float mMoveBallRadius;
    private float mCurrentMoveBallX;
    private float mCurrentMOveBallY;
    private int mCurrentHoneyCombIndex;
    private float mFadeInAlpha = 255f;
    private float mFadeOutAlpha = 255f;
    private AnimatorSet animatorSet;


    public HoneyCombLoader(Context context) {
        this(context, null);
    }

    public HoneyCombLoader(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HoneyCombLoader(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs();

    }

    private void initAttrs() {

    }

    private void initHoneyCombLoader() {
        mHoneyCombPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHoneyCombPaint.setColor(color);
        mHoneyCombPaint.setStyle(Paint.Style.STROKE);
        mHoneyCombPaint.setStrokeWidth(mPaintWidth);

        mMoveBallPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMoveBallPaint.setColor(color);
        mMoveBallPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mViewSize = Math.min(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension((int) mViewSize, (int) mViewSize);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mPaintWidth = (int) (0.02f * mViewSize);
        float marginX = 0.5f*mPaintWidth;
        float marginY = (float) ((1.5f*marginX)/ Math.cos(30 * Math.PI / 180));
        mHoneyCombRadius = (float) ((mViewSize - 2f * marginX -marginY) / 4f / Math.cos(30 * Math.PI / 180));
        mMoveBallRadius = 0.1f * mHoneyCombRadius;
        float mOffsetXFromAnother = (float) (Math.cos(30 * Math.PI / 180) * mHoneyCombRadius );
        float mOffsetYFromAnother = 1.5f * mHoneyCombRadius ;
        pointFs[0] = new PointF(0.5f * mViewSize, mHoneyCombRadius);
        pointFs[1] = new PointF(0.5f * mViewSize - mOffsetXFromAnother - marginX, mHoneyCombRadius +
                mOffsetYFromAnother+ marginY);
        pointFs[2] = new PointF(0.5f * mViewSize + mOffsetXFromAnother + marginX, mHoneyCombRadius +
                mOffsetYFromAnother+ marginY);
        initHoneyCombLoader();
    }


    @Override
    protected void onDraw(Canvas canvas) {

        for (int i = 0; i < pointFs.length; i++) {
            if ((mCurrentHoneyCombIndex) % pointFs.length == i) {
                mHoneyCombPaint.setAlpha((int) mFadeInAlpha);
            } else if ((mCurrentHoneyCombIndex + 1) % pointFs.length == i) {
                mHoneyCombPaint.setAlpha((int) mFadeOutAlpha);
            } else {
                mHoneyCombPaint.setAlpha(255);
            }
            canvas.drawPath(getMirrorPath(mHoneyCombRadius, pointFs[i].x, pointFs[i].y), mHoneyCombPaint);
        }

        if (isFirst) {
            Log.e(TAG, "onDraw: ");
            startAnimator();
            isFirst = false;
        } else {
            animateCircle(mCurrentMoveBallX, mCurrentMOveBallY, canvas);
        }
    }


    private void startAnimator() {
        ValueAnimator fadeInAlphaAnimator = ValueAnimator.ofFloat(255 * 0.3f, 255f);
        fadeInAlphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mFadeInAlpha = (float) animation.getAnimatedValue();
            }
        });

        ValueAnimator fadeOutAlphaAnimator = ValueAnimator.ofFloat(255f, 255 * 0.3f);
        fadeOutAlphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mFadeOutAlpha = (float) animation.getAnimatedValue();
            }
        });
        ValueAnimator pointAnimator = ValueAnimator.ofObject(new TypeEvaluator() {
            @Override
            public Object evaluate(float fraction, Object startValue, Object endValue) {
                PointF startArr = (PointF) startValue;
                PointF endArr = (PointF) endValue;
                return new PointF(startArr.x + fraction * (endArr.x - startArr.x), startArr
                        .y + fraction * (endArr.y - startArr.y));
            }
        }, pointFs[mCurrentHoneyCombIndex % pointFs.length], pointFs[++mCurrentHoneyCombIndex % pointFs.length]);
        pointAnimator.setInterpolator(new AccelerateInterpolator());
        pointAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                PointF currentArr = (PointF) animation.getAnimatedValue();
                mCurrentMoveBallX = currentArr.x;
                mCurrentMOveBallY = currentArr.y;
//                Log.e(TAG, "mCurrentMoveBallX: " + mCurrentMoveBallX + " mCurrentMOveBallY: " + mCurrentMOveBallY);
                invalidate();
            }
        });
        animatorSet = new AnimatorSet();
        animatorSet.playTogether(fadeInAlphaAnimator, fadeOutAlphaAnimator, pointAnimator);
        animatorSet.setDuration(500);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                startAnimator();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                Log.e(TAG, "onAnimationCancel: " );
            }
        });
        animatorSet.start();
    }


    public void animateCircle(float moveX, float moveY, Canvas canvas) {
        canvas.drawCircle(moveX, moveY, mMoveBallRadius, mMoveBallPaint);
    }

    public Path getMirrorPath(float radius, float cx, float cy) {
        Path path = new Path();
        radius -= mPaintWidth * 0.5f;
        float offsetAngle = 0;
        offsetAngle = (float) (Math.PI * offsetAngle / 180);
        for (int i = 0; i < 6; i++) {
            float x = (float) (cx + radius * Math.cos(offsetAngle));
            float y = (float) (cy + radius * Math.sin(offsetAngle));
            offsetAngle += 2 * Math.PI / 6;
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        path.close();
        Matrix mMatrix = new Matrix();
        mMatrix.postRotate(-90, cx, cy);
        path.transform(mMatrix);
        return path;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        Log.e(TAG, "onWindowFocusChanged: " + hasWindowFocus);
        super.onWindowFocusChanged(hasWindowFocus);
        if (animatorSet == null) {
            return;
        }
        if (hasWindowFocus) {
            if (!animatorSet.isRunning()) {
                startAnimator();
            }
        } else {
            animatorSet.removeAllListeners();
            animatorSet.cancel();
        }
    }


}
