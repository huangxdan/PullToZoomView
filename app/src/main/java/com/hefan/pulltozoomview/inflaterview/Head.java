package com.hefan.pulltozoomview.inflaterview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hefan.pulltozoomview.R;
import com.hefan.pulltozoomview.iview.IInflaterView;
import com.hefan.pulltozoomview.listener.headlistener.OnHFPersonInfoListener;
import com.jakewharton.rxbinding.view.RxView;

import java.util.concurrent.TimeUnit;

import rx.functions.Action1;

/**
 * Created by hxd on 2017/5/12.
 */
public class Head extends IInflaterView {
    Context mContext;
    View headView;
    TextView anchorName, personInfo, tv_renzheng, anchorId, anchorSign, guanZhuNum, fansNum;
    RelativeLayout rl_personInfo;
    LinearLayout ll_renzheng;

    private OnHFPersonInfoListener mPersonInfoListener;
    private boolean isSelfZoon,isCommonUser;

    HeadBuilder mHeadBuilder;

    public Head(HeadBuilder headBuilder) {
        this.isSelfZoon = headBuilder.isSelfZoon;
        this.isCommonUser = headBuilder.isCommonUser;
    }

    @Override
    public View creatView(Context context) {
        mContext = context;
        initView();

        return headView;
    }

    public void bindData(HeadBuilder headBuilder) {
        this.mHeadBuilder = headBuilder;
        this.mPersonInfoListener = headBuilder.mPersonInfoListener;
        setData();
        bindListener();
    }

    private void initView() {
        headView = LayoutInflater.from(mContext).inflate(R.layout.layout_anchor_headview, null, false);

        anchorName = (TextView) headView.findViewById(R.id.anchorName);
        rl_personInfo = (RelativeLayout) headView.findViewById(R.id.rl_personInfo);
        personInfo = (TextView) headView.findViewById(R.id.personInfo);
        ll_renzheng = (LinearLayout) headView.findViewById(R.id.ll_renzheng);
        tv_renzheng = (TextView) headView.findViewById(R.id.tv_renzheng);
        anchorId = (TextView) headView.findViewById(R.id.anchorId);
        anchorSign = (TextView) headView.findViewById(R.id.anchorSign);
        guanZhuNum = (TextView) headView.findViewById(R.id.guanZhuNum);
        fansNum = (TextView) headView.findViewById(R.id.fansNum);
    }

    private void setData() {
        anchorName.setText(mHeadBuilder.mNickName);
        anchorId.setText("盒饭ID " + mHeadBuilder.mHiFunId);
        setAuthenticate();
        anchorSign.setText(mHeadBuilder.mSign);
        setPersonInfoState();

        //粉丝个数
        if (mHeadBuilder.mFanCount > 999999) {
            fansNum.setText((mHeadBuilder.mFanCount / 10000) + "万");
        } else {
            fansNum.setText(mHeadBuilder.mFanCount + "");
        }
        //关注人数
        if (mHeadBuilder.mAttentCount > 999999) {
            guanZhuNum.setText((mHeadBuilder.mAttentCount / 10000) + "万");
        } else {
            guanZhuNum.setText(mHeadBuilder.mAttentCount + "");
        }
    }

    private void bindListener() {
        RxView.clicks(rl_personInfo).throttleFirst(1, TimeUnit.SECONDS).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                if (mPersonInfoListener != null) {
                    mPersonInfoListener.onPersonInfoListener();
                }
            }
        });
    }

    private void setAuthenticate() {
        if (mHeadBuilder.mAuthenticate != null && !"".equals(mHeadBuilder.mAuthenticate)) {
            ll_renzheng.setVisibility(View.VISIBLE);
            tv_renzheng.setText(mHeadBuilder.mAuthenticate);
        } else {
            ll_renzheng.setVisibility(View.GONE);
        }
    }


    private void setPersonInfoState() {
        //判断是我的俱乐部还是主播空间
        if (isSelfZoon) {
            if (isCommonUser) {
                rl_personInfo.setVisibility(View.GONE);
            } else {
                rl_personInfo.setVisibility(View.VISIBLE);
                if (mHeadBuilder.mInfoFile == null || "".equals(mHeadBuilder.mInfoFile)) {
                    personInfo.setText("[编辑资料]");
                } else {
                    personInfo.setText("[个人资料]");
                }
            }
        } else {
            if ((mHeadBuilder.mInfoFile == null || "".equals(mHeadBuilder.mInfoFile)) && (mHeadBuilder.mProfiles == null || "".equals(mHeadBuilder.mProfiles))) {
                rl_personInfo.setVisibility(View.GONE);
            } else {
                if (isCommonUser) {
                    rl_personInfo.setVisibility(View.GONE);
                } else {
                    rl_personInfo.setVisibility(View.VISIBLE);
                    personInfo.setText("[个人资料]");
                }
            }
        }
    }

    public static class HeadBuilder {
        private String mNickName, mHiFunId, mInfoFile, mProfiles, mAuthenticate, mSign;
        private long mAttentCount, mFanCount;
        private OnHFPersonInfoListener mPersonInfoListener;
        private boolean isSelfZoon,isCommonUser;

        public HeadBuilder personInfoListener(OnHFPersonInfoListener personInfoListener) {
            this.mPersonInfoListener = personInfoListener;
            return this;
        }

        public HeadBuilder nickName(String nickName) {
            this.mNickName = nickName;
            return this;
        }

        public HeadBuilder hiFunId(String hiFunId) {
            this.mHiFunId = hiFunId;
            return this;
        }

        public HeadBuilder infoFile(String infoFile) {
            this.mInfoFile = infoFile;
            return this;
        }

        public HeadBuilder profiles(String profiles) {
            this.mProfiles = profiles;
            return this;
        }

        public HeadBuilder authenticate(String authenticate) {
            this.mAuthenticate = authenticate;
            return this;
        }

        public HeadBuilder personSign(String sign) {
            this.mSign = sign;
            return this;
        }

        public HeadBuilder attentCount(long attentCount) {
            this.mAttentCount = attentCount;
            return this;
        }

        public HeadBuilder fanCount(long fanCount) {
            this.mFanCount = fanCount;
            return this;
        }

        public HeadBuilder isSelfZoon(boolean isSelfZoon) {
            this.isSelfZoon = isSelfZoon;
            return this;
        }

        public HeadBuilder isCommonUser(boolean isCommonUser) {
            this.isCommonUser = isCommonUser;
            return this;
        }

        public Head build() {
            return new Head(this);
        }
        public HeadBuilder syncData() {
            return this;
        }
    }
}
