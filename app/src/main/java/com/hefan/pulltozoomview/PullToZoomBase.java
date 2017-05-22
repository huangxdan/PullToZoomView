package com.hefan.pulltozoomview;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Scroller;

import com.hefan.pulltozoomview.util.DisplayUtil;

/**
 * Created by hxd on 2017/5/11.
 */
public abstract class PullToZoomBase<T extends View> extends LinearLayout implements IPullToZoom<T> {
    private static final String TAG = "PullToZoomBase";
    private static final float FRICTION = 1.5f;
    private int maxScrollEdge = 1000;
    protected T mRootView;
    protected View mHeaderView;//头部View
    protected View mZoomView;//缩放拉伸View

    protected int mScreenHeight;
    protected int mScreenWidth;

    private boolean isZoomEnabled = true;
    private boolean isParallax = true;
    private boolean isZooming = false;
    private boolean isHideHeader = false;

    private int mTouchSlop;
    private boolean mIsBeingDragged = false;
    private float mLastMotionY;
    private float mLastMotionX;
    private float mCurMotionY;
    private float mActionDownY;
    private float mInitialMotionY;
    private float mInitialMotionX;
    private OnPullZoomListener onPullZoomListener;
    private OnPullScrollListener mOnPullScrollListener;
    private Scroller mScroller;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private VelocityTracker mVelocityTracker;

    protected int mHeaderHeight;
    protected int mInitHeaderHeight;
    private ScrollRunnable mScrollRunnable;

    private View actionBarView;//用于显示隐藏View,暂时只支持为设置背景透明度
    protected int actionBarHeight;//用于计算透明度

    public PullToZoomBase(Context context) {
        this(context, null);
    }

    public PullToZoomBase(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setGravity(Gravity.CENTER);

        mScroller = new Scroller(getContext());
        maxScrollEdge = DisplayUtil.dip2px(getContext(), 340);

        ViewConfiguration config = ViewConfiguration.get(context);
        mTouchSlop = config.getScaledTouchSlop();
        mMinimumVelocity = config.getScaledMinimumFlingVelocity();
//        mMaximumVelocity = maxScrollEdge * 5;//设置最大速度,设置为最大速度为5倍于高度
        mMaximumVelocity = config.getScaledMaximumFlingVelocity();

        DisplayMetrics localDisplayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(localDisplayMetrics);
        mScreenHeight = localDisplayMetrics.heightPixels;
        mScreenWidth = localDisplayMetrics.widthPixels;

        mScrollRunnable = new ScrollRunnable();
        // Refreshable View
        // By passing the attrs, we can add ListView/GridView params via XML
        mRootView = createRootView(context, attrs);

        if (attrs != null) {
            LayoutInflater mLayoutInflater = LayoutInflater.from(getContext());
            //初始化状态View
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PullToZoomView);

            int zoomViewResId = a.getResourceId(R.styleable.PullToZoomView_zoomView, 0);
            if (zoomViewResId > 0) {
                mZoomView = mLayoutInflater.inflate(zoomViewResId, null, false);
            }

            int headerViewResId = a.getResourceId(R.styleable.PullToZoomView_headerView, 0);
            if (headerViewResId > 0) {
                mHeaderView = mLayoutInflater.inflate(headerViewResId, null, false);
            }

            isParallax = a.getBoolean(R.styleable.PullToZoomView_isHeaderParallax, true);

            // Let the derivative classes have a go at handling attributes, then
            // recycle them...
            handleStyledAttributes(a);
            a.recycle();
        }
        addView(mRootView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    public void setOnPullZoomListener(OnPullZoomListener onPullZoomListener) {
        this.onPullZoomListener = onPullZoomListener;
    }

    @Override
    public T getPullRootView() {
        return mRootView;
    }

    @Override
    public View getZoomView() {
        return mZoomView;
    }

    @Override
    public View getHeaderView() {
        return mHeaderView;
    }

    @Override
    public boolean isPullToZoomEnabled() {
        return isZoomEnabled;
    }

    @Override
    public boolean isZooming() {
        return isZooming;
    }

    @Override
    public boolean isParallax() {
        return isParallax;
    }

    @Override
    public boolean isHideHeader() {
        return isHideHeader;
    }

    public void setZoomEnabled(boolean isZoomEnabled) {
        this.isZoomEnabled = isZoomEnabled;
    }

    public void setParallax(boolean isParallax) {
        this.isParallax = isParallax;
    }

    public void setHideHeader(boolean isHideHeader) {//header显示才能Zoom
        this.isHideHeader = isHideHeader;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!isPullToZoomEnabled() || isHideHeader()) {
            return false;
        }

        final int action = event.getAction();

        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            mIsBeingDragged = false;
            return false;
        }

        if (action != MotionEvent.ACTION_DOWN && mIsBeingDragged) {
            return true;
        }
        switch (action) {
            case MotionEvent.ACTION_MOVE: {
                if (isReadyForPullStart()) {
                    final float y = event.getY(), x = event.getX();
                    final float diff, absDiff;

                    if (mHeaderHeight <= actionBarHeight){
                        if (mOnPullScrollListener == null){
                            throw new NullPointerException("mOnPullScrollListener must not be null!");
                        }
                        if (!mOnPullScrollListener.isOnTopEdge()
                                || y < mInitialMotionY){//如果列表没有在最顶端,或者是上滑的就不拦截了
                            mIsBeingDragged = false;
                            return false;
                        }
                    }else {
                        if (!mOnPullScrollListener.isOnTopEdge()
                                && y > mInitialMotionY){
                            mIsBeingDragged = false;
                            return false;
                        }
                    }

                    // We need to use the correct values, based on scroll
                    // direction

                    diff = y - mInitialMotionY;
                    absDiff = Math.abs(diff);

                    if (absDiff > mTouchSlop
                            && (getNestedScrollAxes() & SCROLL_AXIS_VERTICAL) == 0) {
                        if (isReadyForPullStart()) {
                            mCurMotionY = y;
                            mLastMotionX = x;
                            mIsBeingDragged = true;
                        }
                    }
                }
                break;
            }
            case MotionEvent.ACTION_DOWN: {
                if (isReadyForPullStart()) {
                    mActionDownY = mLastMotionY = mInitialMotionY = event.getY();
                    mLastMotionX = mInitialMotionX = event.getX();
                    mIsBeingDragged = false;
                    return false;
                }
                break;
            }
        }

        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (!isPullToZoomEnabled() || isHideHeader()) {
            return false;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN && event.getEdgeFlags() != 0) {
            return false;
        }

        obtainVelocityTracker(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE: {
                Log.d(TAG, "onTouchEvent: ACTION_MOVE mIsBeingDragged = "+mIsBeingDragged);
                Log.d(TAG, "onTouchEvent: ACTION_MOVE mCurMotionY = "+mCurMotionY);
                if (mIsBeingDragged) {
                    mCurMotionY = event.getY();
                    mLastMotionX = event.getX();
                    pullEvent();
                    isZooming = true;
                    return true;
                }
                break;
            }

            case MotionEvent.ACTION_DOWN: {
                if (isReadyForPullStart()) {
                    mLastMotionY = mInitialMotionY = event.getY();
//                    mLastMotionX = mInitialMotionX = event.getX();
                    if (!mScroller.isFinished()) {
                        mScroller.abortAnimation();
                    }
                    if (!mScrollRunnable.isFinish()){
                        mScrollRunnable.abortScroller();
                    }

                    mIsBeingDragged = true;
                    return true;
                }
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                if (mIsBeingDragged) {
                    mIsBeingDragged = false;
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int initialVelocity = (int) velocityTracker.getYVelocity();
                    Log.d(TAG, "onTouchEvent: [initialVelocity = "+Math.abs(initialVelocity)+"]");
                    Log.d(TAG, "onTouchEvent: mMinimumVelocity = "+mMinimumVelocity);
                    if ((Math.abs(initialVelocity) > mMinimumVelocity)
                            && getChildCount() > 0) {
//                        if (initialVelocity > maxScrollEdge){//如果速度大于
//                            initialVelocity = maxScrollEdge;
//                        }
                        fling(-initialVelocity);
                    }else {
                        resetZoom();
                    }
                    releaseVelocityTracker();

                    if (onPullZoomListener != null) {
//                        onPullZoomListener.onPullZooming(newScrollValue);
                        onPullZoomListener.onPullZoomEnd(mActionDownY,mCurMotionY);
                    }
                    return true;
                }
                break;
            }
        }
        return false;
    }

    private void pullEvent() {
        final int newScrollValue;
        float diffY;
        diffY = mCurMotionY - mLastMotionY;
        mLastMotionY = mCurMotionY;
        if (diffY > 0){//向下拉加阻力
            newScrollValue = Math.round(diffY / FRICTION);
        }else {//向上无阻力
            newScrollValue = Math.round(diffY / 1);
        }
        invalidateBarAlpha();
        pullHeaderToZoom(newScrollValue);

    }


    /**
     * 速度值
     * @param velocityY
     */
    public void fling(int velocityY) {
        Log.d(TAG, "fling: velocityY = "+velocityY);
        int initialY = velocityY < 0 ? Integer.MAX_VALUE : 0;
        int startY = velocityY<0 ? Integer.MAX_VALUE : 0;
        if (getChildCount() > 0) {
            Log.d(TAG, "fling: getScrollY() = "+getScrollY());
            mScroller.fling(0, initialY, 0, velocityY,
                    0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
            final boolean movingDown = velocityY > 0;
            Log.d(TAG, "fling: mScroller.getDuration() = "+mScroller.getDuration());

            mScrollRunnable.startScroll(mScroller, startY);

//            computeZoonY();
        }
    }


//    /**
//     * 计算滑动距离
//     */
//    public synchronized void computeZoonY() {
//        Log.d(TAG, "computeZoonY: computeScrollOffset start time="+System.currentTimeMillis());
//        if (mScroller.computeScrollOffset()) {//如果动画没有完成
//            int scrollY;
//            int y = mScroller.getCurrY();
//            scrollY = computY - y;
//            computY = y;
//            if (scrollY > 0)
//                pullHeaderToZoom(scrollY+10);
//            else if (scrollY < 0)
//                pullHeaderToZoom(scrollY-10);
//            if (mHeaderHeight >= mInitHeaderHeight){
//                mScroller.forceFinished(true);
//            }
//            try {
//                Thread.sleep(10);
//                Log.d(TAG, "computeZoonY: computeScrollOffset end time="+System.currentTimeMillis());
//                computeZoonY();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }else{
//            computY = 0;
//            resetZoom();
//        }
//    }

    private void resetZoom(){
        // If we're already refreshing, just scroll back to the top
        Log.d(TAG, "onTouchEvent: isZoom = "+isZooming());
        if (isZooming()) {
            smoothScrollToTop();
            if (onPullZoomListener != null) {
                onPullZoomListener.onPullZoomEnd(0,0);//此时不判断刷新
            }
            isZooming = false;

        }
    }

    /**
     * 初始化速度Tracker
     * @param event
     */
    private void obtainVelocityTracker(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    /**
     * 释放
     */
    private void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    public class ScrollRunnable implements Runnable{
        Scroller mScroller;
        int computY = 0;
        boolean isFinish;
        public void abortScroller(){
            isFinish = true;
        }

        public boolean isFinish(){
            return isFinish;
        }

        @Override
        public void run() {
            if (!isFinish) {
                Log.d(TAG, "computeZoonY: computeScrollOffset start time="+System.currentTimeMillis());
                if (mScroller.computeScrollOffset()) {//如果动画没有完成
                    int scrollY;
                    int y = mScroller.getCurrY();
                    scrollY = computY - y;
                    Log.d(TAG, "run: scrollY = "+scrollY+",[ computY="+computY+", y="+y+"]");
                    computY = y;
                    if ((mHeaderHeight>=mInitHeaderHeight && scrollY>0)
                            || (mHeaderHeight<=actionBarHeight && scrollY<0)){//如果比action高度还小或者比总高度还大
                        mScroller.forceFinished(true);
                    }else {
                        if (scrollY > 0){
                            if (mHeaderHeight+scrollY >= mInitHeaderHeight){
                                scrollY = mInitHeaderHeight - mHeaderHeight+100;
                            }
                            pullHeaderToZoom(scrollY);
                        } else if (scrollY < 0) {
                            if (mHeaderHeight+scrollY <= actionBarHeight){//如果小则变成actionbar大小
                                scrollY = actionBarHeight - mInitHeaderHeight;
                            }
                            pullHeaderToZoom(scrollY);
                        }
                    }
                }else{
                    computY = 0;
                    resetZoom();
                    isFinish = true;
                }
                invalidateBarAlpha();
                post(this);
            }
        }

        /**
         * 开始滑动
         * @param mScroller
         * @param startY    从什么位置开始滑动
         */
        public void startScroll(Scroller mScroller, int startY){
            this.mScroller = mScroller;
            isFinish = false;
            computY = startY;
            post(this);
        }
    }

    /**
     * 用于展示判断头部是否全部隐藏
     * @param mHeaderHeight
     */
    public void setHeaderHeight(int mHeaderHeight){
        this.mHeaderHeight = mHeaderHeight;
    }

    /**
     * 设置顶部anctionBar的布局
     * @param antionBar
     */
    public void setActionBarView(View antionBar){
        this.actionBarView = antionBar;
        actionBarHeight = actionBarView.getLayoutParams().height>0?actionBarView.getLayoutParams().height:0;
    }

    /**
     * 刷新ActionBar的透明度
     */
    public void invalidateBarAlpha(){
        if (actionBarView != null){
            int alpha = 255 - (int) (((double)mHeaderHeight-actionBarHeight)/(mInitHeaderHeight-actionBarHeight) * 255);
            if(alpha < 0){
                alpha = 0;
            }
            actionBarView.getBackground().setAlpha(alpha);
        }
    }

    protected abstract void pullHeaderToZoom(int newScrollValue);

    public abstract void setHeaderView(View headerView);

    public abstract void setZoomView(View zoomView);

    protected abstract T createRootView(Context context, AttributeSet attrs);

    protected abstract void smoothScrollToTop();

    protected abstract boolean isReadyForPullStart();

    public interface OnPullZoomListener {
        public void onPullZooming(int newScrollValue);

        public void onPullZoomEnd(float mActionDownY,float mCurMotionY);
    }

    public void setOnPullScrollListener(OnPullScrollListener pullScrollListener){
        this.mOnPullScrollListener = pullScrollListener;
    }

    public interface OnPullScrollListener{
        public boolean isOnTopEdge();
    }
}

