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

package me.drakeet.meizhi.ui.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.drakeet.meizhi.R;
import me.drakeet.meizhi.data.entity.Gank;
import me.drakeet.meizhi.ui.WebActivity;
import me.drakeet.meizhi.util.StringStyles;

/**
 * Created by drakeet on 8/11/15.
 *
 * AnimRecyclerViewAdapter功能：给item添加一个进入动画
 */
public class GankListAdapter extends AnimRecyclerViewAdapter<GankListAdapter.ViewHolder> {

    private List<Gank> mGankList;


    public GankListAdapter(List<Gank> gankList) {
        mGankList = gankList;
    }


    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                               .inflate(R.layout.item_gank, parent, false);
        return new ViewHolder(v);
    }


    @Override public void onBindViewHolder(ViewHolder holder, int position) {
        Gank gank = mGankList.get(position);
        //如果是第一个显示category
        if (position == 0) {
            showCategory(holder);
        }
        else {
            //判断上一个item和这个item的类型是否相同，相同则隐藏Category,否则显示
            boolean theCategoryOfLastEqualsToThis = mGankList.get(
                    position - 1).type.equals(mGankList.get(position).type);
            if (!theCategoryOfLastEqualsToThis) {
                showCategory(holder);
            }
            else {
                hideCategory(holder);
            }
        }
        //设置分类名称
        holder.category.setText(gank.type);
        //富文本操作
        SpannableStringBuilder builder = new SpannableStringBuilder(gank.desc).append(
                StringStyles.format(holder.gank.getContext(), " (via. " +
                        gank.who +
                        ")", R.style.ViaTextAppearance));
        CharSequence gankText = builder.subSequence(0, builder.length());

        holder.gank.setText(gankText);
        showItemAnim(holder.gank, position);
    }


    @Override public int getItemCount() {
        return mGankList.size();
    }

    private void showCategory(ViewHolder holder) {

        if (!isVisibleOf(holder.category)) holder.category.setVisibility(View.VISIBLE);
    }


    private void hideCategory(ViewHolder holder) {
        if (isVisibleOf(holder.category)) holder.category.setVisibility(View.GONE);
    }


    /**
     * view.isShown() is a kidding...
     */
    private boolean isVisibleOf(View view) {
        return view.getVisibility() == View.VISIBLE;
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.category) TextView category;
        @Bind(R.id.title) TextView gank;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        //item点击事件
        @OnClick(R.id.gank_layout) void onGank(View v) {
            Gank gank = mGankList.get(getLayoutPosition());
            Intent intent = WebActivity.newIntent(v.getContext(), gank.url, gank.desc);
            v.getContext().startActivity(intent);
        }
    }
}
