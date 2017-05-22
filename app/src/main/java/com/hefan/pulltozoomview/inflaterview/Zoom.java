package com.hefan.pulltozoomview.inflaterview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.hefan.pulltozoomview.R;
import com.hefan.pulltozoomview.iview.IInflaterView;
import com.hefan.pulltozoomview.util.OssFormatUrl;

/**
 * Created by hxd on 2017/5/12.
 */
public class Zoom extends IInflaterView {
    Context mContext;
    View zoonView;
    ImageView ivAnchorSpaceBg;

    ZoomBuilder mZoomBuilder;

    public Zoom(ZoomBuilder zoomBuilder) {
    }

    @Override
    public View creatView(Context context) {
        mContext = context;
        zoonView = LayoutInflater.from(context).inflate(R.layout.layout_anchor_zoomview, null, false);

        ivAnchorSpaceBg = (ImageView) zoonView.findViewById(R.id.iv_anchorSpace_bg);

        return zoonView;
    }

    public void bindData(ZoomBuilder zoomBuilder) {
        this.mZoomBuilder = zoomBuilder;
        setZoomBg();
    }

    private void setZoomBg() {
        //设置背景
        String formatUrl = OssFormatUrl.getInstance().formatUrl(mZoomBuilder.mBgUrl);
        if (formatUrl != null && !"".equals(formatUrl)) {
            Glide.with(mContext).load(formatUrl).error(R.drawable.pic_default).dontAnimate().into(ivAnchorSpaceBg);
        }
    }

    public static class ZoomBuilder {
        private String mBgUrl;
        public ZoomBuilder bgUrl(String url) {
            this.mBgUrl = url;
            return this;
        }

        public Zoom build() {
            return new Zoom(this);
        }

        public ZoomBuilder syncData(){
            return this;
        }
    }
}
