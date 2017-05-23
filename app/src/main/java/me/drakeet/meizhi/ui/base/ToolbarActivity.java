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

package me.drakeet.meizhi.ui.base;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.animation.DecelerateInterpolator;

import me.drakeet.meizhi.R;

/**
 * 包含的功能:
 * 1. 对外提供一个抽象方法，用来设置内容的id：provideContentViewId();
 * 2. 对toolbar进行初始化，提供一个onToolbarClick()方法，让子类可以接收Toolbar的单击事件
 * 3. toolbar是否支持返回功能：canBack();  子类可以根据是否需要进行重写
 * 4.设置toolbar透明度的方法：setAppBarAlpha(float alpha)
 * 5. 设置toolbar显示或隐藏，用的动画效果：hideOrShowToolbar()
 */

public abstract class ToolbarActivity extends BaseActivity {

    //子类必须实现的，返回内容layout的id
    abstract protected int provideContentViewId();

    //Toolbar的点击事件，方便子类重写
    public void onToolbarClick() {}

    protected AppBarLayout mAppBar;
    protected Toolbar mToolbar;
    //是否隐藏Toolbar
    protected boolean mIsHidden = false;


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(provideContentViewId());
        mAppBar = (AppBarLayout) findViewById(R.id.app_bar_layout);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar == null || mAppBar == null) {
            throw new IllegalStateException(
                    "The subclass of ToolbarActivity must contain a toolbar.");
        }
        //对外提供Toolbar的点击事件回调方法，由子类选择是否实现
        mToolbar.setOnClickListener(v -> onToolbarClick());
        setSupportActionBar(mToolbar);

        //toolbar是否支持返回功能，是则把canBack()方法返回TRUE，子类可以重写改变
        if (canBack()) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
        }
        //如果手机系统大于等于21,设置toolbar的高度属性
        if (Build.VERSION.SDK_INT >= 21) {
            mAppBar.setElevation(10.6f);
        }
    }



    //toolbar是否支持返回功能
    public boolean canBack() {
        return false;
    }


    //实现toolbar的返回功能
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    //设置toolbar的透明度
    protected void setAppBarAlpha(float alpha) {
        mAppBar.setAlpha(alpha);
    }


    //设置toolbar显示或者隐藏
    protected void hideOrShowToolbar() {
        mAppBar.animate()
               .translationY(mIsHidden ? 0 : -mAppBar.getHeight())
               .setInterpolator(new DecelerateInterpolator(2))
               .start();
        mIsHidden = !mIsHidden;
    }
}
