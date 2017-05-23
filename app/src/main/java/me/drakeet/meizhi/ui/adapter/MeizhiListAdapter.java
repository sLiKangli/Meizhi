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

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import java.util.List;
import me.drakeet.meizhi.R;
import me.drakeet.meizhi.data.entity.Meizhi;
import me.drakeet.meizhi.func.OnMeizhiTouchListener;
import me.drakeet.meizhi.widget.RatioImageView;

/**
 * Created by drakeet on 6/20/15.
 * 类的功能：创建RecyclerView的适配器
 */
public class MeizhiListAdapter
        extends RecyclerView.Adapter<MeizhiListAdapter.ViewHolder> {

    public static final String TAG = "MeizhiListAdapter";

    private List<Meizhi> mList;
    private Context mContext;
    private OnMeizhiTouchListener mOnMeizhiTouchListener;


    /**
     *  传入Meizhi列表
     */
    public MeizhiListAdapter(Context context, List<Meizhi> meizhiList) {
        mList = meizhiList;
        mContext = context;
    }


    /**
     * 创建ViewHolder
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext())
                               .inflate(R.layout.item_meizhi, parent, false);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        Meizhi meizhi = mList.get(position);
        int limit = 48;
        //如果Meizhi的desc字段长度超过48，则截断加上“...”号
        String text = meizhi.desc.length() > limit ? meizhi.desc.substring(0, limit) +
                "..." : meizhi.desc;
        //到此出meizhi字段有值
        viewHolder.meizhi = meizhi;
        viewHolder.titleView.setText(text);
        viewHolder.card.setTag(meizhi.desc);
        //glide图片加载库加载图片
        Glide.with(mContext)
             .load(meizhi.url)
             .centerCrop()
             .into(viewHolder.meizhiView)
             .getSize((width, height) -> {
                 //card是否是显示的，没有显示就显示出来
                 if (!viewHolder.card.isShown()) {
                     viewHolder.card.setVisibility(View.VISIBLE);
                 }
             });
    }


    @Override public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
    }


    @Override public int getItemCount() {
        return mList.size();
    }


    /**
     * 设置RecyclerView中item触摸时的监听
     */
    public void setOnMeizhiTouchListener(OnMeizhiTouchListener onMeizhiTouchListener) {
        this.mOnMeizhiTouchListener = onMeizhiTouchListener;
    }

    //创建holder类
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @Bind(R.id.meizhi) RatioImageView meizhiView;
        @Bind(R.id.title) TextView titleView;
        View card;
        Meizhi meizhi;


        public ViewHolder(View itemView) {
            super(itemView);
            card = itemView;
            ButterKnife.bind(this, itemView);
            meizhiView.setOnClickListener(this);
            //设置Item的单击事件
            card.setOnClickListener(this);
            //设置这个ImageView的宽高的原始大小
            meizhiView.setOriginalSize(50, 50);
        }


        @Override public void onClick(View v) {
            mOnMeizhiTouchListener.onTouch(v, meizhiView, card, meizhi);
        }
    }
}
