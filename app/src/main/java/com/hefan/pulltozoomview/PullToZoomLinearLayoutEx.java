package com.hefan.pulltozoomview;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.view.animation.Interpolator;
import android.widget.TextView;

import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.hefan.pulltozoomview.adapter.ZoonIndexAdapter;
import com.hefan.pulltozoomview.inflaterview.Content;
import com.hefan.pulltozoomview.inflaterview.Head;
import com.hefan.pulltozoomview.inflaterview.Zoom;
import com.hefan.pulltozoomview.listener.OnHFPageChangeListener;
import com.hefan.pulltozoomview.listener.OnHFTabSelectListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hxd on 2017/5/11.
 */
public class PullToZoomLinearLayoutEx extends PullToZoomBase<LinearLayout> {
    private static final String TAG = PullToZoomLinearLayoutEx.class.getSimpleName();
    private boolean isCustomHeaderHeight = false;
    private FrameLayout mHeaderContainer;
    private LinearLayout mRootContainer;
    private View mContentView;
    private ScalingRunnable mScalingRunnable;//头部动画

    private static final Interpolator sInterpolator = new Interpolator() {
        public float getInterpolation(float paramAnonymousFloat) {
            float f = paramAnonymousFloat - 1.0F;
            return 1.0F + f * (f * (f * (f * f)));
        }
    };

    public PullToZoomLinearLayoutEx(Context context) {
        this(context, null);
    }

    public PullToZoomLinearLayoutEx(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScalingRunnable = new ScalingRunnable();
    }

    @Override
    protected void pullHeaderToZoom(int newScrollValue) {
        if (mScalingRunnable != null && !mScalingRunnable.isFinished()) {
            mScalingRunnable.abortAnimation();
        }
        ViewGroup.LayoutParams localLayoutParams = mHeaderContainer.getLayoutParams();
        mHeaderHeight = Math.max(mHeaderHeight + newScrollValue, actionBarHeight);
        Log.d(TAG, "pullHeaderToZoom: mHeaderHeight=" + mHeaderHeight);
        localLayoutParams.height = mHeaderHeight;
        setHeaderHeight(mHeaderHeight);
        mHeaderContainer.setLayoutParams(localLayoutParams);

        if (isCustomHeaderHeight) {//修改zoom的大小
            ViewGroup.LayoutParams zoomLayoutParams = mZoomView.getLayoutParams();
            zoomLayoutParams.height = mHeaderHeight + newScrollValue;
            mZoomView.setLayoutParams(zoomLayoutParams);
        }
    }

    /**
     * 是否显示headerView
     *
     * @param isHideHeader true: show false: hide
     */
    @Override
    public void setHideHeader(boolean isHideHeader) {
        if (isHideHeader != isHideHeader() && mHeaderContainer != null) {
            super.setHideHeader(isHideHeader);
            if (isHideHeader) {
                mHeaderContainer.setVisibility(GONE);
            } else {
                mHeaderContainer.setVisibility(VISIBLE);
            }
        }
    }

    @Override
    public void setHeaderView(View headerView) {
        if (headerView != null) {
            mHeaderView = headerView;
            updateHeaderView();
        }
    }

    private void updateHeaderView() {
        if (mHeaderContainer != null) {
            mHeaderContainer.removeAllViews();

            if (mZoomView != null) {
                mHeaderContainer.addView(mZoomView);
            }

            if (mHeaderView != null) {
                mHeaderContainer.addView(mHeaderView);
            }
        }
    }

    @Override
    public void setZoomView(View zoomView) {
        if (zoomView != null) {
            mZoomView = zoomView;
            updateHeaderView();
        }
    }

    public void setContentView(View contentView) {
        if (contentView != null) {
            if (mContentView != null) {
                mRootContainer.removeView(mContentView);
            }
            mContentView = contentView;
            mRootContainer.addView(mContentView);
        }
    }

    @Override
    protected LinearLayout createRootView(Context context, AttributeSet attrs) {
        LinearLayout linearLayout = new LinearLayout(context, attrs);
        linearLayout.setId(R.id.linearlayout);
        return linearLayout;
    }

    @Override
    protected void smoothScrollToTop() {
        mScalingRunnable.startAnimation(200L);
    }

    @Override
    protected boolean isReadyForPullStart() {
        return mRootView.getScrollY() == 0;
    }

    @Override
    public void handleStyledAttributes(TypedArray a) {
        mRootContainer = new LinearLayout(getContext());
        mRootContainer.setOrientation(LinearLayout.VERTICAL);
        mHeaderContainer = new FrameLayout(getContext());

        if (mZoomView != null) {
            mHeaderContainer.addView(mZoomView);
        }
        if (mHeaderView != null) {
            mHeaderContainer.addView(mHeaderView);
        }
        int contentViewResId = a.getResourceId(R.styleable.PullToZoomView_contentView, 0);
        if (contentViewResId > 0) {
            LayoutInflater mLayoutInflater = LayoutInflater.from(getContext());
            mContentView = mLayoutInflater.inflate(contentViewResId, null, false);
        }

        mRootContainer.addView(mHeaderContainer);
        if (mContentView != null) {
            mRootContainer.addView(mContentView);
        }

        mRootContainer.setClipChildren(false);
        mHeaderContainer.setClipChildren(false);

        mRootView.addView(mRootContainer);
    }

    /**
     * 设置HeaderView高度
     *
     * @param width
     * @param height
     */
    public void setHeaderViewSize(int width, int height) {
        if (mHeaderContainer != null) {
            Object localObject = mHeaderContainer.getLayoutParams();
            if (localObject == null) {
                localObject = new ViewGroup.LayoutParams(width, height);
            }
            ((ViewGroup.LayoutParams) localObject).width = width;
            ((ViewGroup.LayoutParams) localObject).height = height;
            mHeaderContainer.setLayoutParams((ViewGroup.LayoutParams) localObject);
            mHeaderHeight = height;
            isCustomHeaderHeight = true;
        }
    }

    /**
     * 设置HeaderView LayoutParams
     *
     * @param layoutParams LayoutParams
     */
    public void setHeaderLayoutParams(LinearLayout.LayoutParams layoutParams) {
        if (mHeaderContainer != null) {
            mHeaderContainer.setLayoutParams(layoutParams);
            mHeaderHeight = layoutParams.height;
            mInitHeaderHeight = mHeaderHeight;
            isCustomHeaderHeight = true;
        }
    }

    protected void onLayout(boolean paramBoolean, int paramInt1, int paramInt2,
                            int paramInt3, int paramInt4) {
        super.onLayout(paramBoolean, paramInt1, paramInt2, paramInt3, paramInt4);
        if (mHeaderHeight == 0 && mZoomView != null) {
            mHeaderHeight = mHeaderContainer.getHeight();
        }
    }


    class ScalingRunnable implements Runnable {
        protected long mDuration;
        protected boolean mIsFinished = true;
        protected float mScale;
        protected long mStartTime;

        ScalingRunnable() {
        }

        public void abortAnimation() {
            mIsFinished = true;
        }

        public boolean isFinished() {
            return mIsFinished;
        }

        public void run() {
            if (mZoomView != null) {
                float f2;
                ViewGroup.LayoutParams localLayoutParams;
                if ((!mIsFinished) && (mScale > 1.0D)) {
                    float f1 = ((float) SystemClock.currentThreadTimeMillis() - (float) mStartTime) / (float) mDuration;
                    f2 = mScale - (mScale - 1.0F) * PullToZoomLinearLayoutEx.sInterpolator.getInterpolation(f1);
                    localLayoutParams = mHeaderContainer.getLayoutParams();
                    if (f2 > 1.0F) {
                        localLayoutParams.height = ((int) (f2 * mInitHeaderHeight));
                        mHeaderContainer.setLayoutParams(localLayoutParams);
                        if (isCustomHeaderHeight) {
                            ViewGroup.LayoutParams zoomLayoutParams;
                            zoomLayoutParams = mZoomView.getLayoutParams();
                            mHeaderHeight = (int) (f2 * mInitHeaderHeight);
                            zoomLayoutParams.height = mHeaderHeight;
                            mZoomView.setLayoutParams(zoomLayoutParams);
                        }
                        post(this);
                        return;
                    }
                    mIsFinished = true;
                }
            }
        }

        public void startAnimation(long paramLong) {
            if (mZoomView != null) {
                mStartTime = SystemClock.currentThreadTimeMillis();
                mDuration = paramLong;
                mScale = ((float) (mHeaderContainer.getBottom()) / mInitHeaderHeight);
                mIsFinished = false;
                post(this);
            }
        }
    }

    //设置头部内容
    public void showHeadView(Head head) {
        View headView = head.creatView(getContext());
        setHeaderView(headView);
    }

    //设置拉伸内容
    public void showZoomView(Zoom zoom) {
        View zoomView = zoom.creatView(getContext());
        setZoomView(zoomView);
    }

    //设置内容
    public void showContentView(Content content) {
        View contentView = content.creatView(getContext());
        setContentView(contentView);
    }

}
