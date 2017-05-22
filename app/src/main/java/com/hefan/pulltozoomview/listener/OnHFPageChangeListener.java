package com.hefan.pulltozoomview.listener;

/**
 * Created by hxd on 2017/5/11.
 */
public interface OnHFPageChangeListener {
    void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);
    void onPageSelected(int position);
    void onPageScrollStateChanged(int state);
}
