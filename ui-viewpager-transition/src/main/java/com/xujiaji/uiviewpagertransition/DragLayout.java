package com.xujiaji.uiviewpagertransition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.view.GestureDetectorCompat;
import androidx.core.view.ScrollingView;
import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;


/**
 * 尽量考虑了所有操作系统版本的分辨率适配
 * Created by xmuSistone on 2016/9/18.
 */
public class DragLayout extends FrameLayout {
    private final ViewDragHelper mDragHelper;
    private final GestureDetectorCompat moveDetector;
    private int mTouchSlop = 5; // 判定为滑动的阈值，单位是像素
    private int originX, originY; // 初始状态下，topView的坐标
    private CardView rootView; // FrameLayout的两个子View
    private View statusBarSpaceView; // 状态栏占位View
    private ImageView btnChange;
    private ScrollingView scrollingView;
    private boolean isMini = true;
    private ValueAnimator valueAnimator;
    private String titleString = "标题";
    private TextView tvTitle;
    private FrameLayout titleView;
    private ViewGroup contentView;

    public DragLayout(Context context) {
        this(context, null);
    }

    public DragLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

//        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DragLayout, 0, 0);
//        bottomDragVisibleHeight = (int) a.getDimension(R.styleable.DragLayout_bottomDragVisibleHeight, 0);
//        bototmExtraIndicatorHeight = (int) a.getDimension(R.styleable.DragLayout_bototmExtraIndicatorHeight, 0);
//        a.recycle();

        mDragHelper = ViewDragHelper
                .create(this, 10f, new DragHelperCallback());
        mDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_TOP);
        moveDetector = new GestureDetectorCompat(context, new MoveDetector());
        moveDetector.setIsLongpressEnabled(false); // 不处理长按事件

        // 滑动的距离阈值由系统提供
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        contentView = (ViewGroup) getChildAt(0);
        checkScrollingView(contentView);

        removeView(contentView);
        CardView cardView = new CardView(getContext());
        cardView.setCardElevation(getCurrentElevation());
        cardView.setMaxCardElevation(getCurrentElevation());
        cardView.setRadius(getCurrentElevation());

        LayoutParams cardLp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        LinearLayout ll = new LinearLayout(getContext());
        ll.setId(R.id.llBG);
//        ll.setBackground(SkinResourcesUtils.getDrawable(R.drawable.confectionary));
        ll.setOrientation(LinearLayout.VERTICAL);
        addTitleView(ll);
        LinearLayout.LayoutParams llLp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        ll.addView(contentView, llLp);
        cardView.addView(ll, cardLp);

        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lp.gravity = Gravity.CENTER;
        lp.topMargin = getCurrentTopBottomMargin();
        lp.bottomMargin = lp.topMargin;
        lp.leftMargin = getCurrentLeftRightMargin();
        lp.rightMargin = lp.leftMargin;
        addView(cardView, lp);
        rootView = cardView;

        btnChange.setOnClickListener(v -> changeSize());
//        topView.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
    }

    public void setTitle(String title) {
        this.titleString = title;
        if (tvTitle != null) {
            tvTitle.setText(title);
        }
    }

    private void addTitleView(LinearLayout ll) {
        statusBarSpaceView = new View(getContext());
        LinearLayout.LayoutParams sbsvLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, getCurrentStatusBarHeight());
        ll.addView(statusBarSpaceView, sbsvLp);

        titleView = new FrameLayout(getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ScreenUtils.dipTopx(getContext(), 48));
        ll.addView(titleView, lp);

        View line = new View(getContext());
//        line.setBackgroundColor(SkinResourcesUtils.getColor(R.color.down_line_grey));
        LinearLayout.LayoutParams lineLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ScreenUtils.dipTopx(getContext(), 1));
        ll.addView(line, lineLp);

        tvTitle = new TextView(getContext());
        tvTitle.setId(R.id.tvTitle);
        tvTitle.setText(titleString);
        tvTitle.setTextSize(18);
//        tvTitle.setTextColor(SkinResourcesUtils.getColor(R.color.textThemeColorPrimary));
        LayoutParams textLp = new LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textLp.gravity = Gravity.CENTER;
        titleView.addView(tvTitle, textLp);

        btnChange = new ImageView(getContext());
        btnChange.setId(R.id.btnChange);
        btnChange.setFocusable(true);
        btnChange.setClickable(true);
        btnChange.setImageResource(R.drawable.ic_to_big);
        btnChange.setScaleType(ImageView.ScaleType.FIT_XY);
        int padding = ScreenUtils.dipTopx(getContext(), 16);
        btnChange.setPadding(padding, padding, padding, padding);
//        btnChange.setColorFilter(SkinResourcesUtils.getColor(R.color.colorAccent));
        int size = ScreenUtils.dipTopx(getContext(), 48);
        LayoutParams imgLp = new LayoutParams(size, size);
        imgLp.gravity = Gravity.END;
        titleView.addView(btnChange, imgLp);
    }

    private int getCurrentStatusBarHeight() {
        if (isMini) {
            return 0;
        } else {
            return ScreenUtils.getStatusHeight(getContext());
        }
    }

    private int getCurrentElevation() {
        if (isMini) {
            return ScreenUtils.dipTopx(getContext(), 8);
        } else {
            return 0;
        }
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
            int fullHeight = ScreenUtils.getScreenHeight(getContext()) - ScreenUtils.dipTopx(getContext(), 60);
            float ratio = fullHeight * 1F / ScreenUtils.getScreenWidth(getContext());
            int height = (int) ((ScreenUtils.getScreenWidth(getContext()) - getCurrentLeftRightMargin() * 2) * ratio);
            return (fullHeight - height) / 2;
        } else {
            return 0;
        }
    }

    private void checkScrollingView(View view) {
        if (view instanceof ScrollingView) {
            scrollingView = (ScrollingView) view;
            return;
        }
        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                checkScrollingView(vg.getChildAt(i));
            }
        }
    }

    private class DragHelperCallback extends ViewDragHelper.Callback {

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            if (changedView == rootView) {
//                processLinkageView();
            }
        }

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            if (child == rootView) {
                return true;
            }
            return false;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            int currentTop = child.getTop();
            Log.i("DragLayout", "clampViewPositionVertical dy = " + dy);
            if (dy < 0 && scrollingView != null && scrollingView.computeVerticalScrollOffset() == 0 && scrollingView.computeVerticalScrollRange() > scrollingView.computeVerticalScrollExtent()) {
                Log.i("DragLayout", "顶部，又向下翻，并且可滚动范围大于控件本身高度");
                return currentTop;
            } else if (dy > 0 && scrollingView != null && scrollingView.computeVerticalScrollOffset() == scrollingView.computeVerticalScrollRange() - scrollingView.computeVerticalScrollExtent()) {
                Log.i("DragLayout", "底部，又向上翻，并且滚动已经滚动到底部");
                return currentTop;
            }
//            if (top > currentTop) {
                // 往下拉的时候，阻力最小
                return currentTop + (top - currentTop) / 2;
//            }
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return child.getLeft();
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return 600;
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return 600;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            if (mDragHelper.smoothSlideViewTo(releasedChild, originX, originY)) {
                ViewCompat.postInvalidateOnAnimation(DragLayout.this);
            }
        }
    }


    class MoveDetector extends GestureDetector.SimpleOnGestureListener {
        private boolean scrolled = false;

        public boolean onDown(MotionEvent e) {
            Log.i("DragLayout", "onDown scrolled = " + scrolled);
            return scrolled;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx,
                                float dy) {
            if (scrollingView != null) {
                final int extent = scrollingView.computeVerticalScrollExtent();
                final int offset = scrollingView.computeVerticalScrollOffset();
                final int range = scrollingView.computeVerticalScrollRange();
                //extent = 1382, offset = 166, range = 1332, viewHeight = 1382
                Log.i("DragLayout",
                        String.format("dy = %s, extent = %s, offset = %s, range = %s, viewHeight = %s",
                                dy,
                                extent,
                                offset,
                                range,
                                ((View) scrollingView).getMeasuredHeight()));
                if (range < extent || dy < 0 && offset == 0 || dy > 0 && offset == range - extent) {
                    scrolled = false;
                    return Math.abs(dy)  > mTouchSlop;
                } else {
                    scrolled = true;
                    return false;
                }
            }
            scrolled = false;
            // 拖动了，touch不往下传递
            return Math.abs(dy)  > mTouchSlop;
        }
    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
//        if (!changed) {
//            return;
//        }

        super.onLayout(changed, left, top, right, bottom);

        originX = (int) rootView.getX();
        originY = (int) rootView.getY();
    }

    /* touch事件的拦截与处理都交给mDraghelper来处理 */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // 1. detector和mDragHelper判断是否需要拦截
        boolean yScroll = moveDetector.onTouchEvent(ev);
        boolean shouldIntercept = false;
        try {
            shouldIntercept = mDragHelper.shouldInterceptTouchEvent(ev);
        } catch (Exception e) {
        }

        // 2. 触点按下的时候直接交给mDragHelper
        int action = ev.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
//            downState = getCurrentState();
            mDragHelper.processTouchEvent(ev);
        }

        Log.i("DragLayout", String.format("shouldIntercept = %s, yScroll = %s", shouldIntercept, yScroll));

        return shouldIntercept && yScroll && isMini;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (!isMini) {
            return false;
        }
        // 统一交给mDragHelper处理，由DragHelperCallback实现拖动效果
        try {
            mDragHelper.processTouchEvent(e);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return true;
    }

    private void setParentScrollControlViewPager(boolean isScroll) {
        if (getParent() == null) {
            return;
        }
        if (getParent() instanceof ScrollControlViewPager) {
            ((ScrollControlViewPager) getParent()).setScanScroll(isScroll);
            return;
        }
        setParentScrollControlViewPager(isScroll);
    }

    public FrameLayout getTitleView() {
        return titleView;
    }

    public void changeSize() {
        isMini = !isMini;
        if (valueAnimator != null && valueAnimator.isRunning()) {
            valueAnimator.cancel();
        }
        final CardView cardView = (CardView) rootView;

        ViewGroup.LayoutParams statusBarSpaceViewLayoutParams = statusBarSpaceView.getLayoutParams();
        final int currentStatusBarHeight = statusBarSpaceViewLayoutParams.height;
        final int statusBarHeightDiff = getCurrentStatusBarHeight() - currentStatusBarHeight;

        LayoutParams lp = (LayoutParams) rootView.getLayoutParams();
        final int currentTopMargin = lp.topMargin;
        final int topBottomMarginDiff = getCurrentTopBottomMargin() - currentTopMargin;

        final int currentLeftMargin = lp.leftMargin;
        final int leftMarginDiff = getCurrentLeftRightMargin() - currentLeftMargin;

        final float currentElevation = cardView.getCardElevation();
        final float elevationDiff = getCurrentElevation() - currentElevation;

        valueAnimator = ValueAnimator.ofFloat(0, 1F);
        valueAnimator.setDuration(500);
        valueAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
//            lp.height = (int) (currentHeight + heightDiff * value);
//            Log.i("xxxxx", "height = " + lp.height);
            int tb = (int) (currentTopMargin + topBottomMarginDiff * value);
            int lr = (int) (currentLeftMargin + leftMarginDiff * value);
            lp.setMargins(lr, tb, lr, tb);
            float elevation = currentElevation + elevationDiff * value;
            cardView.setRadius(elevation);
            cardView.setCardElevation(elevation);
            cardView.setMaxCardElevation(elevation);
            cardView.setLayoutParams(lp);

            statusBarSpaceViewLayoutParams.height = (int) (currentStatusBarHeight + statusBarHeightDiff * value);
            statusBarSpaceView.setLayoutParams(statusBarSpaceViewLayoutParams);
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                btnChange.setImageResource(isMini ? R.drawable.ic_to_big : R.drawable.ic_to_small);
                setParentScrollControlViewPager(isMini);
            }
        });
        valueAnimator.start();
    }

//    public void showVipBuyView() {
//        if (App.Login.userBean() != null && App.Login.userBean().isVip()) {
//            return;
//        }
//        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_center_vip, null);
//        rootView.addView(view);
//        view.setOnClickListener(v -> {
//            Activity currentActivity = App.getInstance().getCurrentActivity();
//            if (App.Login.userBean() != null && RechargeActivity.isVipAndVipNotLaunch(currentActivity)) {
//                rootView.removeView(view);
//            }
//        });
//    }

    public CardView getRootCardView() {
        return rootView;
    }

//    public void bgSkin(SkinInflaterFactory skinInflaterFactory) {
//        if (skinInflaterFactory != null) {
//            View llBG = findViewById(R.id.llBG);
//            llBG.post(() -> skinInflaterFactory.dynamicAddSkinEnableView(getContext(), llBG, "background", R.drawable.confectionary));
//        }
//    }
}
