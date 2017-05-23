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

package me.drakeet.meizhi.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;

import me.drakeet.meizhi.R;

/**
 * Pick from Google io 2014
 * Created by drakeet on 1/3/15.
 */
public class MultiSwipeRefreshLayout extends SwipeRefreshLayout {

    private CanChildScrollUpCallback mCanChildScrollUpCallback;
    private Drawable mForegroundDrawable;


    public MultiSwipeRefreshLayout(Context context) {
        this(context, null);
    }


    public MultiSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        final TypedArray array = context.obtainStyledAttributes(attrs,
                R.styleable.MultiSwipeRefreshLayout, 0, 0);

        //研究很久没有没搞清楚这段的作用
        mForegroundDrawable = array.getDrawable(R.styleable.MultiSwipeRefreshLayout_foreground);
        if (mForegroundDrawable != null) {
            mForegroundDrawable.setCallback(this);
            setWillNotDraw(false);
        }
        array.recycle();
    }


    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mForegroundDrawable != null) {
            mForegroundDrawable.setBounds(0, 0, w, h);
        }
    }

    //设置回调方法
    public void setCanChildScrollUpCallback(CanChildScrollUpCallback canChildScrollUpCallback) {
        mCanChildScrollUpCallback = canChildScrollUpCallback;
    }
    //对外提供的回调接口
    public interface CanChildScrollUpCallback {
        //用了判断被下拉的View 是否可以刷新
        boolean canSwipeRefreshChildScrollUp();
    }


    /**
     * 重写父类的这个方法，使 MultiSwipeRefreshLayout 包裹多个子view也可以具有刷新功能
     * canChildScrollUp()原本的功能是判断SwipeRefreshLayout的“第一个”View是否处于可刷新状态，
     * 如果是，进行刷新，只关注第一个。所以重写这个方法可以让 MultiSwipeRefreshLayout 拥有多
     * 个子View.
     */
    @Override public boolean canChildScrollUp() {
        if (mCanChildScrollUpCallback != null) {
            return mCanChildScrollUpCallback.canSwipeRefreshChildScrollUp();
        }
        return super.canChildScrollUp();
    }
}
