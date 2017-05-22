package com.hefan.pulltozoomview.inflaterview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.hefan.pulltozoomview.R;
import com.hefan.pulltozoomview.adapter.ZoonIndexAdapter;
import com.hefan.pulltozoomview.iview.IInflaterView;
import com.hefan.pulltozoomview.listener.contentlistener.OnHFAttentListener;
import com.hefan.pulltozoomview.listener.OnHFPageChangeListener;
import com.hefan.pulltozoomview.listener.contentlistener.OnHFFansContributionListener;
import com.hefan.pulltozoomview.listener.contentlistener.OnHFPersonalLetterListener;
import com.hefan.pulltozoomview.listener.OnHFTabSelectListener;
import com.hefan.pulltozoomview.util.OssFormatUrl;
import com.hefan.pulltozoomview.view.CircleImageView;
import com.jakewharton.rxbinding.view.RxView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.functions.Action1;

/**
 * Created by hxd on 2017/5/12.
 */
public class Content extends IInflaterView {
    Context mContext;
    View mContentView;
    CommonTabLayout ctlClubTab;
    ViewPager vpClubTab;
    LinearLayout llBottemLayout, llAttent, llPersonalLetter;
    ImageView imageAttent;
    TextView textAttent;
    RelativeLayout rlFans;
    CircleImageView civFansY1, civFansY2, civFansY3;

    ZoonIndexAdapter mZoonIndexAdapter;

    private OnHFTabSelectListener mTabSelectListener;
    private OnHFPageChangeListener mPageChangeListener;
    private OnHFPersonalLetterListener mPersonalLetterListener;
    private OnHFAttentListener mAttentListener;
    private OnHFFansContributionListener mFansContributionListener;
    private FragmentManager fm;
    private List<Fragment> mFragmentList = new ArrayList<>();
    private String[] mTitles;
    private ArrayList<CustomTabEntity> mTabEntities = new ArrayList<>();
    private int mScreenPageLimit = 0;
    private int mCurrentPage = 0;
    private int mCurrentTab = 0;
    private boolean isSelfZoon;

    private ContentBuilder mContentBuilder;

    //构造
    public Content(ContentBuilder contentBuilder) {
        this.mPageChangeListener = contentBuilder.mPageChangeListener;
        this.mTabSelectListener = contentBuilder.mTabSelectListener;
        this.fm = contentBuilder.fm;
        this.mFragmentList = contentBuilder.mFragmentList;
        this.mTitles = contentBuilder.mTitles;
        this.mTabEntities = contentBuilder.mTabEntities;
        this.mScreenPageLimit = contentBuilder.mScreenPageLimit;
        this.mCurrentPage = contentBuilder.mCurrentPage;
        this.mCurrentTab = contentBuilder.mCurrentTab;
        this.isSelfZoon = contentBuilder.isSelfZoon;
    }

    @Override
    public View creatView(Context context) {
        mContext = context;
        initView();
        bindListener();
        return mContentView;
    }

    public void bindData(ContentBuilder contentBuilder) {
        this.mContentBuilder = contentBuilder;
        setData();
    }

    private void initView() {
        mContentView = LayoutInflater.from(mContext).inflate(R.layout.activity_anchor_content, null, false);

        vpClubTab = (ViewPager) mContentView.findViewById(R.id.vp_club_tab);
        ctlClubTab = (CommonTabLayout) mContentView.findViewById(R.id.ctl_club_tab);

        llBottemLayout = (LinearLayout) mContentView.findViewById(R.id.ll_bottem_layout);
        llAttent = (LinearLayout) mContentView.findViewById(R.id.ll_attent);
        llPersonalLetter = (LinearLayout) mContentView.findViewById(R.id.ll_personal_letter);
        textAttent = (TextView) mContentView.findViewById(R.id.text_attent);
        imageAttent = (ImageView) mContentView.findViewById(R.id.image_attent);

        rlFans = (RelativeLayout) mContentView.findViewById(R.id.rl_fans);
        civFansY1 = (CircleImageView) mContentView.findViewById(R.id.civ_fans_y1);
        civFansY2 = (CircleImageView) mContentView.findViewById(R.id.civ_fans_y2);
        civFansY3 = (CircleImageView) mContentView.findViewById(R.id.civ_fans_y3);

        mZoonIndexAdapter = new ZoonIndexAdapter(fm);
        mZoonIndexAdapter.setDataList(mFragmentList, mTitles);
        vpClubTab.setAdapter(mZoonIndexAdapter);
        ctlClubTab.setTabData(mTabEntities);
        ;
        vpClubTab.setOffscreenPageLimit(mScreenPageLimit);
        vpClubTab.setCurrentItem(mCurrentPage);
        ctlClubTab.setCurrentTab(mCurrentTab);
    }

    private void setData() {
        this.mPersonalLetterListener = mContentBuilder.mPersonalLetterListener;
        this.mAttentListener = mContentBuilder.mAttentListener;
        this.mFansContributionListener = mContentBuilder.mFansContributionListener;

        //设置粉丝头像
        setFansHeadImg();
        setBottemLayoutState();
        setPersonalLetterState();
        setAttentUI(mContentBuilder.mAttentionState);
    }

    private void bindListener() {
        vpClubTab.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (mPageChangeListener != null) {
                    mPageChangeListener.onPageSelected(position);
                }
                ctlClubTab.setCurrentTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        ctlClubTab.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                if (mTabSelectListener != null) {
                    mTabSelectListener.onTabSelect(position);
                }
                vpClubTab.setCurrentItem(position);
            }

            @Override
            public void onTabReselect(int position) {

            }
        });

        //点击私信
        RxView.clicks(llPersonalLetter).throttleFirst(1, TimeUnit.SECONDS).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                if (mPersonalLetterListener != null) {
                    mPersonalLetterListener.OnPersonalLetterListener();
                }
            }
        });
        //点击关注
        RxView.clicks(llAttent).throttleFirst(1, TimeUnit.SECONDS).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                if (mAttentListener != null) {
                    mAttentListener.OnAttentListener();
                }
            }
        });
        //点击粉丝贡献榜
        RxView.clicks(rlFans).throttleFirst(1, TimeUnit.SECONDS).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                if (mFansContributionListener != null) {
                    mFansContributionListener.onFansContributionListener();
                }
            }
        });
    }

    private void setFansHeadImg() {
        //收集粉丝头像CircleImageView
        if (mContentBuilder.mFansHeadImg != null && mContentBuilder.mFansHeadImg.size() > 0) {
            CircleImageView[] imageList = {civFansY1, civFansY2, civFansY3};
            for (int i = 0; i < mContentBuilder.mFansHeadImg.size(); i++) {
                String headUrl = OssFormatUrl.getInstance().formatHeadUrl(mContentBuilder.mFansHeadImg.get(i));
                if (headUrl != null && !"".equals(headUrl)) {
                    Glide.with(mContext).load(headUrl).error(R.drawable.icon_qiuzan).into(imageList[i]);
                }
            }
        }
    }

    private void setBottemLayoutState() {
        if (isSelfZoon) {
            llBottemLayout.setVisibility(View.GONE);
        } else {
            llBottemLayout.setVisibility(View.VISIBLE);
        }
    }

    private void setPersonalLetterState() {
        if (mContentBuilder.mPersonalLetterClickable) {
            llPersonalLetter.setBackgroundColor(mContext.getResources().getColor(R.color.transparent));
            llPersonalLetter.setClickable(true);
        } else {
            //普通用户不能和普通用户私信
            llPersonalLetter.setBackgroundColor(mContext.getResources().getColor(R.color.gray));
            llPersonalLetter.setClickable(false);
        }
    }

    public void setAttentUI(int isAttention) {
        if (isAttention == 0) {
            imageAttent.setImageResource(R.drawable.icon_dayiguanzhu);
            textAttent.setText("已关注");
            textAttent.setTextColor(ColorStateList.valueOf(mContext.getResources().getColor(R.color.line_gray)));
        } else if (isAttention == 2) {
            imageAttent.setImageResource(R.drawable.icon_jiaguanzhu);
            textAttent.setText("关注");
            textAttent.setTextColor(ColorStateList.valueOf(mContext.getResources().getColor(R.color.recharge_text)));
        } else if (isAttention == 1) {
            imageAttent.setImageResource(R.drawable.icon_huxianghuanzhu);
            textAttent.setText("互相关注");
            textAttent.setTextColor(ColorStateList.valueOf(mContext.getResources().getColor(R.color.line_gray)));
        }
    }

    public static class ContentBuilder {
        private OnHFTabSelectListener mTabSelectListener;
        private OnHFPageChangeListener mPageChangeListener;
        private OnHFPersonalLetterListener mPersonalLetterListener;
        private OnHFAttentListener mAttentListener;
        private OnHFFansContributionListener mFansContributionListener;
        private FragmentManager fm;
        private List<Fragment> mFragmentList;
        private String[] mTitles;
        private ArrayList<CustomTabEntity> mTabEntities;
        private int mScreenPageLimit;
        private int mCurrentPage;
        private int mCurrentTab;
        private List<String> mFansHeadImg;
        private boolean mPersonalLetterClickable;
        private int mAttentionState;
        private boolean isSelfZoon;

        public ContentBuilder tabSelectListener(OnHFTabSelectListener tabSelectListener) {
            this.mTabSelectListener = tabSelectListener;
            return this;
        }

        public ContentBuilder pageChangeListener(OnHFPageChangeListener pageChangeListener) {
            this.mPageChangeListener = pageChangeListener;
            return this;
        }

        public ContentBuilder personalLetterListener(OnHFPersonalLetterListener personalLetterListener) {
            this.mPersonalLetterListener = personalLetterListener;
            return this;
        }

        public ContentBuilder attentListener(OnHFAttentListener attentListener) {
            this.mAttentListener = attentListener;
            return this;
        }

        public ContentBuilder fansContributionListener(OnHFFansContributionListener fansContributionListener) {
            this.mFansContributionListener = fansContributionListener;
            return this;
        }

        public ContentBuilder fragmentManager(FragmentManager fragmentManager) {
            this.fm = fragmentManager;
            return this;
        }

        public ContentBuilder fragmentList(List<Fragment> fragmentList) {
            this.mFragmentList = fragmentList;
            return this;
        }

        public ContentBuilder titles(String[] titles) {
            this.mTitles = titles;
            return this;
        }

        public ContentBuilder tabEntities(ArrayList<CustomTabEntity> tabEntities) {
            this.mTabEntities = tabEntities;
            return this;
        }

        public ContentBuilder screenPageLimit(int screenPageLimit) {
            this.mScreenPageLimit = screenPageLimit;
            return this;
        }

        public ContentBuilder currentPage(int currentPage) {
            this.mCurrentPage = currentPage;
            return this;
        }

        public ContentBuilder currentTab(int currentTab) {
            this.mCurrentTab = currentTab;
            return this;
        }

        public ContentBuilder fansHeadImg(List<String> fansHeadImg) {
            this.mFansHeadImg = fansHeadImg;
            return this;
        }

        public ContentBuilder personalLetterClickable(boolean isClickable) {
            this.mPersonalLetterClickable = isClickable;
            return this;
        }

        public ContentBuilder attentionState(int attentionState) {
            this.mAttentionState = attentionState;
            return this;
        }

        public ContentBuilder isSelfZoon(boolean isSelfZoon) {
            this.isSelfZoon = isSelfZoon;
            return this;
        }

        public Content build() {
            return new Content(this);
        }

        public ContentBuilder syncData() {
            return this;
        }
    }
}
