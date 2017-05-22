package com.hefan.pulltozoomview.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by hxd on 2017/5/11.
 */
public class ZoonIndexAdapter extends FragmentPagerAdapter {

    private List<Fragment> fragments;
    private String[] titles;

    public ZoonIndexAdapter(FragmentManager fm) {
        super(fm);
    }

    public void setDataList(List<Fragment> fragments, String[] titles){
        this.titles = titles;
        this.fragments = fragments;
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int arg0) {
        return fragments.get(arg0);
    }

    @Override
    public int getCount() {
        if (fragments != null)
            return fragments.size();
        else
            return 0;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}
