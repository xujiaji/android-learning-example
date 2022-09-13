package com.xujiaji.uiviewpagertransition;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import androidx.cardview.widget.CardView;


public class AspectRatioCardView extends CardView {

    private ValueAnimator valueAnimator;
    private boolean isMini = true;

    public AspectRatioCardView(Context context) {
        this(context, null);
    }

    public AspectRatioCardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AspectRatioCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        post(new Runnable() {
            @Override
            public void run() {
                LayoutParams lp = (LayoutParams) getLayoutParams();
                lp.gravity = Gravity.CENTER;
                lp.topMargin = getCurrentTopBottomMargin();
                lp.bottomMargin = lp.topMargin;
                lp.leftMargin = getCurrentLeftRightMargin();
                lp.rightMargin = lp.leftMargin;
                setLayoutParams(lp);
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        int height = getCurrentHeight();
//        setMeasuredDimension(getMeasuredWidth(), height);


    }

    private int getCurrentLeftRightMargin() {
        if (isMini) {
            return ScreenUtils.dipTopx(getContext(), 48);
        } else {
            return 0;
        }
    }

    private int getCurrentTopBottomMargin() {
        if (isMini) {
            int mainBottomHeight = ScreenUtils.dipTopx(getContext(), 60);
            int fullHeight = ScreenUtils.getScreenHeight(getContext()) - mainBottomHeight;
            float ratio = fullHeight * 1F / ScreenUtils.getScreenWidth(getContext());
            int height = (int) (getMeasuredWidth() * ratio);
            return (fullHeight - height) / 2;
        } else {
            return 0;
        }
    }

    private int getCurrentHeight() {
        if (isMini) {
            int mainBottomHeight = ScreenUtils.dipTopx(getContext(), 60);
            float ratio = (ScreenUtils.getScreenHeight(getContext()) - mainBottomHeight) * 1F / ScreenUtils.getScreenWidth(getContext());
            return (int) (getMeasuredWidth() * ratio);
        } else {
            return ScreenUtils.getScreenHeight(getContext()) - ScreenUtils.dipTopx(getContext(), 60);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        View btnChange = findViewById(R.id.btnChange);
        if (btnChange != null) {
            btnChange.setOnClickListener(v -> changeSize());
        }
    }

    public void changeSize() {
        isMini = !isMini;
        if (valueAnimator != null && valueAnimator.isRunning()) {
            valueAnimator.cancel();
        }
        LayoutParams lp = (LayoutParams) getLayoutParams();
        final int currentTopMargin = lp.topMargin;
        final int topBottomMarginDiff = getCurrentTopBottomMargin() - currentTopMargin;

        final int currentLeftMargin = lp.leftMargin;
        final int leftMarginDiff = getCurrentLeftRightMargin() - currentLeftMargin;

        valueAnimator = ValueAnimator.ofFloat(0, 1F);
        valueAnimator.setDuration(1000);
        valueAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
//            lp.height = (int) (currentHeight + heightDiff * value);
//            Log.e("xxxxx", "height = " + lp.height);
            int tb = (int) (currentTopMargin + topBottomMarginDiff * value);
            int lr = (int) (currentLeftMargin + leftMarginDiff * value);
            Log.e("xxxxx", "tb = " + lp.height + ", lr = " + lr);
            lp.setMargins(lr, tb, lr, tb);
            setLayoutParams(lp);
        });
        valueAnimator.start();
    }
}
