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

package me.drakeet.meizhi.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by drakeet on 8/16/15.
 */
public class Once {

    SharedPreferences mSharedPreferences;
    Context mContext;


    //得到sp实例
    public Once(Context context) {
        mSharedPreferences = context.getSharedPreferences("once", Context.MODE_PRIVATE);
        mContext = context;
    }


    public void show(String tagKey, OnceCallback callback) {
        //得到key为tagkey的boolean值，第一次查找是sp文件中没有这个值,所有为默认值false
        boolean isSecondTime = mSharedPreferences.getBoolean(tagKey, false);
        if (!isSecondTime) {
            //调用传过来的回调方法
            callback.onOnce();
            //将key为tagKey所对应的boolean值设置为TRUE，所有后面不会再进入if代码块
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(tagKey, true);
            editor.apply();
        }
    }

    //会调用上面的方法
    public void show(int tagKeyResId, OnceCallback callback) {
        show(mContext.getString(tagKeyResId), callback);
    }

    //对外部提供的回调
    public interface OnceCallback {
        void onOnce();
    }
}
