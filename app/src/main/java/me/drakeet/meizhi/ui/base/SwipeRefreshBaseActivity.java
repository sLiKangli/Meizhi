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

import android.os.Bundle;

import android.os.PersistableBundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.drakeet.meizhi.R;
import me.drakeet.meizhi.widget.MultiSwipeRefreshLayout;

/**
 * Created by drakeet on 1/3/15.
 * 类的功能：
 * 1.初始化MultiSwipeRefreshLayout
 * 2.作者自定义接口：SwipeRefreshLayer的实现
 */
public abstract class SwipeRefreshBaseActivity extends ToolbarActivity
        implements SwipeRefreshLayer {

    @Bind(R.id.swipe_refresh_layout)
    public MultiSwipeRefreshLayout mSwipeRefreshLayout;
    //是否可以请求数据
    private boolean mIsRequestDataRefresh = false;

    @Override public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        //绑定ButterKnife
        ButterKnife.bind(this);
    }


    /**
     * 在页面全部加载完成之后会执行该方法
     */
    @Override protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        trySetupSwipeRefresh();
    }


    /**
     * 设置SwipeRefreshLayout的监听器
     */
    void trySetupSwipeRefresh() {
        if (mSwipeRefreshLayout != null) {
            //设置SwipeRefreshLayout控件的颜色
            mSwipeRefreshLayout.setColorSchemeResources(R.color.refresh_progress_3,
                    R.color.refresh_progress_2, R.color.refresh_progress_1);
            // Do not use lambda here!
            //设置开始请求刷新的监听
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override public void onRefresh() {
                    //请求数据刷新
                    requestDataRefresh();
                }
            });
        }
    }

    //SwipeRefreshLayout处于刷新状态时执行的方法，供子类重写
    @Override
    public void requestDataRefresh() {
        mIsRequestDataRefresh = true;
    }


    /**
     * 如果传入TRUE，继续保持控件处于刷新状态，如果是false，则让控件
     * 在1秒后停止刷新
     */
    public void setRefresh(boolean requestDataRefresh) {
        //判断是否有SwipeRefreshLayout对象
        if (mSwipeRefreshLayout == null) {
            return;
        }
        //如果不请求数据刷新
        if (!requestDataRefresh) {
            mIsRequestDataRefresh = false;
            // 防止刷新消失太快，让子弹飞一会儿.
            mSwipeRefreshLayout.postDelayed(new Runnable() {
                @Override public void run() {
                    if (mSwipeRefreshLayout != null) {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }
            }, 1000);
        } else {
            mSwipeRefreshLayout.setRefreshing(true);
        }
    }


    //作者自己定义的接口
    @Override public void setProgressViewOffset(boolean scale, int start, int end) {
        /**
         * @param scale 刷新圆形进度是否缩放,如果为true表示缩放,圆形进度图像就会从小到大展示出来,为false就不缩放
         * @param start end 刷新进度条展示的相对于默认的展示位置,start和end组成一个范围，
         *              在这个y轴范围就是那个圆形进度ProgressView展示的位置
         */
        mSwipeRefreshLayout.setProgressViewOffset(scale, start, end);
    }

    /**
     * 作者自己定义的接口
     *  参数是一个回调方法，该回调方法的作用是判断被 {@Link:me.drakeet.meizhi.widget.MultiSwipeRefreshLayout}
     *  控件包裹的View是否也可以下拉刷新，MultiSwipeRefreshLayout是作者自定义的一个控件，目的是可以包含多个子view.
     */

    @Override
    public void setCanChildScrollUpCallback(MultiSwipeRefreshLayout.CanChildScrollUpCallback canChildScrollUpCallback) {
        mSwipeRefreshLayout.setCanChildScrollUpCallback(canChildScrollUpCallback);
    }


    public boolean isRequestDataRefresh() {
        return mIsRequestDataRefresh;
    }
}
