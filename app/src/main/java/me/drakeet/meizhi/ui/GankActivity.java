/*
 * Copyright (C) 2015 Drakeet <drakeet.me@gmail.com>
 *
 * This file is part of Meizhi
 *
 * Meizhi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Meizhi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Meizhi.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.drakeet.meizhi.ui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.umeng.analytics.MobclickAgent;
import java.util.Date;
import me.drakeet.meizhi.LoveBus;
import me.drakeet.meizhi.R;
import me.drakeet.meizhi.ui.adapter.GankPagerAdapter;
import me.drakeet.meizhi.event.OnKeyBackClickEvent;
import me.drakeet.meizhi.ui.base.ToolbarActivity;
import me.drakeet.meizhi.util.Dates;

public class GankActivity extends ToolbarActivity implements ViewPager.OnPageChangeListener {

    public static final String EXTRA_GANK_DATE = "gank_date";

    @Bind(R.id.pager) ViewPager mViewPager;
    @Bind(R.id.tab_layout) TabLayout mTabLayout;

    GankPagerAdapter mPagerAdapter;
    Date mGankDate;


    @Override protected int provideContentViewId() {
        return R.layout.activity_gank;
    }


    @Override public boolean canBack() {
        return true;
    }


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        //得到视频的发布日期
        mGankDate = (Date) getIntent().getSerializableExtra(EXTRA_GANK_DATE);
        //设置标题
        setTitle(Dates.toDate(mGankDate));
        initViewPager();
        initTabLayout();
    }


    private void initViewPager() {
        mPagerAdapter = new GankPagerAdapter(getSupportFragmentManager(), mGankDate);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(1); //ViewPager加载当前页面的情况下会缓存左右两边几个页面
        mViewPager.addOnPageChangeListener(this); //ViewPager添加页面改变监听
    }


    private void initTabLayout() {
        //有多少ViewPager页面，就添加多少tab
        for (int i = 0; i < mPagerAdapter.getCount(); i++) {
            mTabLayout.addTab(mTabLayout.newTab());

        }
        //关联TabLayout和ViewPager
        mTabLayout.setupWithViewPager(mViewPager);
    }


    /**
     * 配置改变时调用（如横竖屏变化）
     */
    @Override public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //如果是横屏
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideOrShowToolbar();
        } else {
            hideOrShowToolbar(); //如果是竖屏
        }
    }


    @Override protected void hideOrShowToolbar() {
        View toolbar = findViewById(R.id.toolbar_with_indicator);
        assert toolbar != null;
        toolbar.animate()
               .translationY(mIsHidden ? 0 : -mToolbar.getHeight())
               .setInterpolator(new DecelerateInterpolator(2))
               .start();
        mIsHidden = !mIsHidden;
        if (mIsHidden) {
            mViewPager.setTag(mViewPager.getPaddingTop());
            mViewPager.setPadding(0, 0, 0, 0);
        } else {
            mViewPager.setPadding(0, (int) mViewPager.getTag(), 0, 0);
            mViewPager.setTag(null);
        }
    }


    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (getResources().getConfiguration().orientation ==
                        Configuration.ORIENTATION_LANDSCAPE) {
                    LoveBus.getLovelySeat().post(new OnKeyBackClickEvent());
                    return true;
                }
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_gank, menu);
        return true;
    }


    @Override public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


    @Override public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        LoveBus.getLovelySeat().register(this);
    }


    @Override public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        LoveBus.getLovelySeat().unregister(this);
    }


    @Override protected void onDestroy() {
        mViewPager.removeOnPageChangeListener(this);
        super.onDestroy();
        ButterKnife.unbind(this);
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }


    @Override public void onPageSelected(int position) {
        setTitle(Dates.toDate(mGankDate, -position));
    }


    @Override public void onPageScrollStateChanged(int state) {

    }
}
