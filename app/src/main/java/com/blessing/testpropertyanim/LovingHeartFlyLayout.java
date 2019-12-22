package com.blessing.testpropertyanim;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.Random;

/**
 * USER: fee(1176610771@qq.com)
 * DATE: 2017/3/9
 * TIME: 19:13
 * DESC: 爱心飘扬视图
 */

public class LovingHeartFlyLayout extends FrameLayout {
    private int mWidth;
    private int mHeight;
    private int[] lovingHeartIconResIds = {
            R.drawable.loving_heart_72px,
            R.drawable.love_heart_2,
            R.drawable.love_heart3,
            R.drawable.loving_heart_04,
            R.drawable.loving_heart_05,
            R.drawable.loving_heart_06,
            R.drawable.loving_heart_07,
            R.drawable.loving_heart_8,
            R.drawable.loving_heart_9
    };
    private int iconW,iconH;
    private Random mRandom = new Random();
    private LayoutParams flp4ChildView;
    /**
     * 下一个爱心产生的间隔时间
     */
    private long nextLovingHeartDuration = 800;
    /**
     * 贝塞尔曲线运动时间
     */
    private static final long beziaAnimatorDuration = 8000;
    private static final int MSG_WHAT_GOON_BORN_HEART = 0;
    public LovingHeartFlyLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        flp4ChildView = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        flp4ChildView.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;

        Drawable loveHeartIcon = getResources().getDrawable(lovingHeartIconResIds[0]);
        iconW = loveHeartIcon.getIntrinsicWidth();
        iconH = loveHeartIcon.getIntrinsicHeight();
    }

    public void aLovingHeartBorn() {
        ImageView holdLovingHeartIv = new ImageView(getContext());
        holdLovingHeartIv.setImageResource(lovingHeartIconResIds[mRandom.nextInt
                (lovingHeartIconResIds.length)]);
        addView(holdLovingHeartIv, flp4ChildView);
        AnimatorSet startAnimator = buildBornViewAnimator(holdLovingHeartIv, 500);

        ValueAnimator bezierAnimator = buildBezierAnimator(holdLovingHeartIv);

        AnimatorSet wholeAnimatorSet = new AnimatorSet();

        wholeAnimatorSet.playSequentially(startAnimator, bezierAnimator);
        wholeAnimatorSet.setTarget(holdLovingHeartIv);
        wholeAnimatorSet.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
    }

    /**
     * 构造生出一个子View时的开始动画集
     * @param theBornChildView
     * @param bornAnimatorDuration
     * @return
     */
    private AnimatorSet buildBornViewAnimator(View theBornChildView,long bornAnimatorDuration) {
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(theBornChildView, "alpha", 0.3f, 1.0f);

        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(theBornChildView, "scaleX", 0.2f, 1.0f);

        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(theBornChildView, "scaleY", 0.2f, 1.0f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(alphaAnimator, scaleXAnimator, scaleYAnimator);
        animatorSet.setDuration(bornAnimatorDuration);
        //以上动画集合作用在目标对象上
        animatorSet.setTarget(theBornChildView);

        return animatorSet;
    }

    private ValueAnimator buildBezierAnimator(View theBornChildView) {
        //构建贝塞尔曲线
        PointF pointF0 = getBezierPoint(0);
        
        PointF pointF1 = getBezierPoint(1);

        PointF pointF2 = getBezierPoint(2);

        PointF pointF3 = getBezierPoint(3);
//        Log.e("info", "-------> pointF0 = " + pointF0);
//        Log.e("info", "-------> pointF1 = " + pointF1);
//        Log.e("info", "-------> pointF2 = " + pointF2);
//        Log.e("info", "-------> pointF3 = " + pointF3);
        ValueAnimator bezierAnimator = ValueAnimator.ofObject(new BezierPointValuer(pointF1, pointF2),
                pointF0, pointF3);

        WrappedAnimatorViewListener wrappedTarget = new WrappedAnimatorViewListener(theBornChildView);
        //设置动画开始、结束的监听
        bezierAnimator.addListener(wrappedTarget);
        //设置动画运行过程中的变化监听
        bezierAnimator.addUpdateListener(wrappedTarget);
        bezierAnimator.setTarget(theBornChildView);
        bezierAnimator.setDuration(beziaAnimatorDuration);
        return bezierAnimator;
    }

    private PointF getBezierPoint(int pointIndex) {
        PointF thePoint = new PointF();
        Random random = new Random();
        thePoint.x = random.nextInt(mWidth);

        int halfMineH = mHeight/2;
        int halfMineW = mWidth/2;
        switch (pointIndex){
            case 0://第一个点
                thePoint.x = mWidth/2 - iconW/2;
                thePoint.y = mHeight - iconH;
                break;
            case 1:
//                thePoint.x = random.nextInt(halfMineW);
                thePoint.y = random.nextInt(halfMineH) + halfMineH;
                break;
            case 2:
//                thePoint.x = random.nextInt(halfMineW) + halfMineW;
                thePoint.y = random.nextInt(halfMineH);
                break;
            case 3://最后一个点
                thePoint.y = 0;
                break;
        }
        return thePoint;
    }

    private class WrappedAnimatorViewListener extends AnimatorListenerAdapter implements
            ValueAnimator
            .AnimatorUpdateListener{
        private View theTargetChildView;

        public WrappedAnimatorViewListener(View theTargetChildView) {
            this.theTargetChildView = theTargetChildView;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            //动画结束后，把目标子View移除掉
            if (theTargetChildView != null) {
                ViewGroup targetViewParentView = (ViewGroup) theTargetChildView.getParent();
                if (targetViewParentView != null) {
                    targetViewParentView.removeView(theTargetChildView);
                }
            }
        }

        @SuppressLint("NewApi")
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            //
            PointF curPointF = (PointF) animation.getAnimatedValue();
            if (curPointF != null) {
                if (theTargetChildView != null) {
                    theTargetChildView.setX(curPointF.x);
                    theTargetChildView.setY(curPointF.y);
                    //顺便设置一下透明度变化
                    theTargetChildView.setAlpha(1 - animation.getAnimatedFraction());
                }
            }
        }
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_WHAT_GOON_BORN_HEART:
                    aLovingHeartBorn();
                    sendEmptyMessageDelayed(MSG_WHAT_GOON_BORN_HEART,nextLovingHeartDuration);
                    break;
            }
        }
    };
    public void autoFlyLovingHearts(){
        autoBuildLovingHearts(0);
    }

    public void autoBuildLovingHearts(long nextIntervalMills) {
        if (nextIntervalMills > 0) {
            this.nextLovingHeartDuration = nextIntervalMills;
        }

        if (!mHandler.hasMessages(MSG_WHAT_GOON_BORN_HEART)) {
            mHandler.sendEmptyMessage(MSG_WHAT_GOON_BORN_HEART);
        }
    }

    public void clearLovingHearts() {
        mHandler.removeCallbacksAndMessages(null);
        removeAllViews();
    }
    @Override
    protected void onDetachedFromWindow() {
        Log.e("info", "-----> onDetachedFromWindow()");
        super.onDetachedFromWindow();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        removeAllViews();
    }

}
