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

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.bumptech.glide.Glide;
import com.squareup.otto.Subscribe;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import me.drakeet.meizhi.DrakeetFactory;
import me.drakeet.meizhi.LoveBus;
import me.drakeet.meizhi.R;
import me.drakeet.meizhi.data.GankData;
import me.drakeet.meizhi.data.entity.Gank;
import me.drakeet.meizhi.event.OnKeyBackClickEvent;
import me.drakeet.meizhi.ui.adapter.GankListAdapter;
import me.drakeet.meizhi.ui.base.BaseActivity;
import me.drakeet.meizhi.util.LoveStrings;
import me.drakeet.meizhi.util.Once;
import me.drakeet.meizhi.util.Shares;
import me.drakeet.meizhi.util.Toasts;
import me.drakeet.meizhi.widget.LoveVideoView;
import me.drakeet.meizhi.widget.VideoImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by drakeet on 8/11/15.
 */
public class GankFragment extends Fragment {

    private final String TAG = "GankFragment";
    private static final String ARG_YEAR = "year";
    private static final String ARG_MONTH = "month";
    private static final String ARG_DAY = "day";

    @Bind(R.id.list) RecyclerView mRecyclerView;
    @Bind(R.id.stub_empty_view) ViewStub mEmptyViewStub; //没有视频会显示的内容
    @Bind(R.id.stub_video_view) ViewStub mVideoViewStub;  //
    @Bind(R.id.video_image) VideoImageView mVideoImageView;  //占据视频区域的图片
    LoveVideoView mVideoView;

    int mYear, mMonth, mDay;
    List<Gank> mGankList;
    String mVideoPreviewUrl;
    boolean mIsVideoViewInflated = false;
    Subscription mSubscription;
    GankListAdapter mAdapter;


    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static GankFragment newInstance(int year, int month, int day) {
        GankFragment fragment = new GankFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_YEAR, year);
        args.putInt(ARG_MONTH, month);
        args.putInt(ARG_DAY, day);
        fragment.setArguments(args);
        return fragment;
    }


    public GankFragment() {
    }


    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGankList = new ArrayList<>();
        mAdapter = new GankListAdapter(mGankList); //RecyclerViewLayout的Adapter
        parseArguments();  //得到传入的年月日
        setRetainInstance(true);  //在配置变化(如横竖屏变化)的时候将这个fragment保存下来
        setHasOptionsMenu(true);  //fragment有菜单配置，用来代替Activity 中的onCreateMenu方法
    }


    private void parseArguments() {
        Bundle bundle = getArguments();
        mYear = bundle.getInt(ARG_YEAR);
        mMonth = bundle.getInt(ARG_MONTH);
        mDay = bundle.getInt(ARG_DAY);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_gank, container, false);
        ButterKnife.bind(this, rootView);
        initRecyclerView();
        setVideoViewPosition(getResources().getConfiguration());
        return rootView;
    }


    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mGankList.size() == 0) loadData(); //视图创建完毕后如果数据为空则加载数据
        if (mVideoPreviewUrl != null) {
            Glide.with(this).load(mVideoPreviewUrl).into(mVideoImageView);
        }
    }


    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }


    private void loadData() {
        //设置mVideoImageView的图片
        loadVideoPreview();
        // @formatter:off
        mSubscription = BaseActivity.sGankIO
                .getGankData(mYear, mMonth, mDay)
                .map(data -> data.results)
                .map(this::addAllResults)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    if (list.isEmpty()) {showEmptyView();}
                    else {mAdapter.notifyDataSetChanged();}
                }, Throwable::printStackTrace);
        // @formatter:on
    }

    //访问网络得到Ganks数据，取出数据中的第一项，让后得到第一项中图片的url用来给mVideoImageView加载图片
    private void loadVideoPreview() {
        //拼接视频发布日期
        String where = String.format("{\"tag\":\"%d-%d-%d\"}", mYear, mMonth, mDay);
        DrakeetFactory.getDrakeetSingleton()
                      .getDGankData(where)
                      .map(dGankData -> dGankData.results)
                      .single(dGanks -> dGanks.size() > 0)
                      .map(dGanks -> dGanks.get(0))
                      .observeOn(AndroidSchedulers.mainThread())
                      .subscribe(dGank -> startPreview(dGank.preview),
                              throwable -> getOldVideoPreview(new OkHttpClient()));
    }

    //拼接url获取要放置在mVideoImageView上的图片
    private void getOldVideoPreview(OkHttpClient client) {
        String url = "http://gank.io/" + String.format("%s/%s/%s", mYear, mMonth, mDay);
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }


            @Override public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                mVideoPreviewUrl = LoveStrings.getVideoPreviewImageUrl(body);
                startPreview(mVideoPreviewUrl);
            }
        });
    }


    //给mVideoImageView加载图片
    private void startPreview(String preview) {
        mVideoPreviewUrl = preview;
        if (preview != null && mVideoImageView != null) {
            // @formatter:off
            mVideoImageView.post(() ->
                Glide.with(mVideoImageView.getContext())
                   .load(preview)
                   .into(mVideoImageView));
            // @formatter:on
        }
    }


    private void showEmptyView() {mEmptyViewStub.inflate();}


    private List<Gank> addAllResults(GankData.Result results) {
        if (results.androidList != null) mGankList.addAll(results.androidList);
        if (results.iOSList != null) mGankList.addAll(results.iOSList);
        if (results.appList != null) mGankList.addAll(results.appList);
        if (results.拓展资源List != null) mGankList.addAll(results.拓展资源List);
        if (results.瞎推荐List != null) mGankList.addAll(results.瞎推荐List);
        if (results.休息视频List != null) mGankList.addAll(0, results.休息视频List);
        return mGankList;
    }


    @OnClick(R.id.header_appbar) void onPlayVideo() {
        resumeVideoView();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        if (mGankList.size() > 0 && mGankList.get(0).type.equals("休息视频")) {
            Toasts.showLongX2(R.string.loading);
        } else {
            closePlayer();
        }
    }


    private void setVideoViewPosition(Configuration newConfig) {
        switch (newConfig.orientation) {
            //横屏，在点击了item项的时候屏幕会变为横屏
            case Configuration.ORIENTATION_LANDSCAPE: {
                //如果视屏已经填充，让它显示出来
                if (mIsVideoViewInflated) {
                    mVideoViewStub.setVisibility(View.VISIBLE);
                } else {
                    //把这个ViewStub代表的View填充进来
                    mVideoView = (LoveVideoView) mVideoViewStub.inflate();
                    mIsVideoViewInflated = true;
                    String tip = getString(R.string.tip_video_play);
                    // @formatter:off
                    new Once(mVideoView.getContext()).show(tip, () ->
                            Snackbar.make(mVideoView, tip, Snackbar.LENGTH_INDEFINITE)
                                    .setAction(R.string.i_know, v -> {})
                                    .show());
                    // @formatter:on
                }
                if (mGankList.size() > 0 && mGankList.get(0).type.equals("休息视频")) {
                    mVideoView.loadUrl(mGankList.get(0).url);
                }
                break;
            }
            case Configuration.ORIENTATION_PORTRAIT:
            case Configuration.ORIENTATION_UNDEFINED:
            default: {
                //只有不是横屏隐藏mVideoViewStub
                mVideoViewStub.setVisibility(View.GONE);
                break;
            }
        }
    }

    // 置为竖屏，弹出提示
    void closePlayer() {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Toasts.showShort(getString(R.string.tip_for_no_gank));
    }

    //得到配置改变的方法
    @Override public void onConfigurationChanged(Configuration newConfig) {
        setVideoViewPosition(newConfig);
        super.onConfigurationChanged(newConfig);
    }

    //按下返回键时，如果当时是横屏，改为竖屏
    @Subscribe public void onKeyBackClick(OnKeyBackClickEvent event) {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        clearVideoView();
    }

    // 菜单项被点击
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_share:
                if (mGankList.size() != 0) {
                    Gank gank = mGankList.get(0);
                    String shareText = gank.desc + gank.url +
                            getString(R.string.share_from);
                    Shares.share(getActivity(), shareText);
                } else {
                    Shares.share(getContext(), R.string.share_text);
                }
                return true;
            case R.id.action_subject:
                openTodaySubject();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void openTodaySubject() {
        String url = getString(R.string.url_gank_io) +
                String.format("%s/%s/%s", mYear, mMonth, mDay);
        Intent intent = WebActivity.newIntent(getActivity(), url,
                getString(R.string.action_subject));
        startActivity(intent);
    }


    @Override public void onResume() {
        super.onResume();
        LoveBus.getLovelySeat().register(this);
        resumeVideoView();
    }


    @Override public void onPause() {
        super.onPause();
        LoveBus.getLovelySeat().unregister(this);
        pauseVideoView();
        clearVideoView();
    }


    @Override public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }


    @Override public void onDestroy() {
        super.onDestroy();
        if (mSubscription != null) mSubscription.unsubscribe();
        resumeVideoView();
    }

    //暂停视频
    private void pauseVideoView() {
        if (mVideoView != null) {
            mVideoView.onPause();
            mVideoView.pauseTimers();
        }
    }

    //开始视频
    private void resumeVideoView() {
        if (mVideoView != null) {
            mVideoView.resumeTimers();
            mVideoView.onResume();
        }
    }

    //清空视频
    private void clearVideoView() {
        if (mVideoView != null) {
            mVideoView.clearHistory();
            mVideoView.clearCache(true);
            mVideoView.loadUrl("about:blank");
            mVideoView.pauseTimers();
        }
    }
}

/**
 * 请求url:http://gank.io/api/day/2017/05/11
 数据请求返回：
 {
     "category": [
     "休息视频",
     "Android",
     "前端",
     "拓展资源",
     "瞎推荐",
     "iOS",
     "福利"
     ],
    "error": false,
    "results": {
        "Android": [
             {
             "_id": "5913cd08421aa90c7d49ad80",
             "createdAt": "2017-05-11T10:31:36.254Z",
             "desc": "找到阻碍你 Android App 性能的罪魁祸首！",
             "images": [
             "http://img.gank.io/7bef123d-8055-47de-a1d0-f70e69b9430d"
             ],
             "publishedAt": "2017-05-11T12:03:09.581Z",
             "source": "chrome",
             "type": "Android",
             "url": "https://github.com/seiginonakama/BlockCanaryEx",
             "used": true,
             "who": "代码家"
             },
             {
             "_id": "5913cf6e421aa90c7fefdd8b",
             "createdAt": "2017-05-11T10:41:50.51Z",
             "desc": "把音乐的音频提出来，做成音轨。",
             "publishedAt": "2017-05-11T12:03:09.581Z",
             "source": "chrome",
             "type": "Android",
             "url": "https://github.com/akshay2211/MusicWave",
             "used": true,
             "who": "Allen"
             },
             {
             "_id": "5913cfeb421aa90c7d49ad84",
             "createdAt": "2017-05-11T10:43:55.585Z",
             "desc": "利用 Databinding 来实现自定义字体功能，这个可以有。",
             "images": [
             "http://img.gank.io/1655dcd2-5886-4f0c-b455-8de35b1a3114"
             ],
             "publishedAt": "2017-05-11T12:03:09.581Z",
             "source": "chrome",
             "type": "Android",
             "url": "https://github.com/EngrAhsanAli/AACustomFont",
             "used": true,
             "who": "Allen"
             }
         ],
        "iOS": [
             {
             "_id": "5913d040421aa90c7a8b2aff",
             "createdAt": "2017-05-11T10:45:20.782Z",
             "desc": "更直观的 Log 前端组件",
             "images": [
                "http://img.gank.io/a7b9c11f-361e-48dd-8b7b-a317675eb412"
                ],
             "publishedAt": "2017-05-11T12:03:09.581Z",
             "source": "chrome",
             "type": "iOS",
             "url": "https://github.com/leoneparise/backlogger",
             "used": true,
             "who": "代码家"
             },
             {
             "_id": "5913d068421aa90c7a8b2b00",
             "createdAt": "2017-05-11T10:46:00.226Z",
             "desc": "快捷内置屏幕尺寸模拟器，方便调试和测试。",
             "images": [
             "http://img.gank.io/5e3f48c0-e81c-40d1-b736-6bb5bf894506"
             ],
             "publishedAt": "2017-05-11T12:03:09.581Z",
             "source": "chrome",
             "type": "iOS",
             "url": "https://github.com/felipeferri/ScreenSizesSimulator",
             "used": true,
             "who": "代码家"
             }
        ],
        "休息视频": [
             {
             "_id": "591067c5421aa90c83a513fa",
             "createdAt": "2017-05-08T20:42:45.664Z",
             "desc": "在地下，拥有258平方公里的城区，上万居民，是世界10大奇景",
             "publishedAt": "2017-05-11T12:03:09.581Z",
             "source": "chrome",
             "type": "休息视频",
             "url": "http://www.bilibili.com/video/av10408797/",
             "used": true,
             "who": "LHF"
             }
         ],
        "前端": [
             {
             "_id": "5913cd3f421aa90c7a8b2afd",
             "createdAt": "2017-05-11T10:32:31.922Z",
             "desc": "Vue 漂亮的 Tab 组件效果。",
             "publishedAt": "2017-05-11T12:03:09.581Z",
             "source": "chrome",
             "type": "前端",
             "url": "http://vue-tabs-component.spatie.be/#first-tab",
             "used": true,
             "who": "TCO"
             }
        ],
        "拓展资源": [
             {
             "_id": "5913ce8c421aa90c83a51416",
             "createdAt": "2017-05-11T10:38:04.182Z",
             "desc": "晋升 Python 数据科学家之路。",
             "images": [
             "http://img.gank.io/c0dad69e-b583-4e2a-b56d-fb5a835e0f82"
             ],
             "publishedAt": "2017-05-11T12:03:09.581Z",
             "source": "chrome",
             "type": "拓展资源",
             "url": "https://github.com/jakevdp/PythonDataScienceHandbook",
             "used": true,
             "who": "PTH"
             },
             {
             "_id": "5913cf02421aa90c83a51418",
             "createdAt": "2017-05-11T10:40:02.449Z",
             "desc": "开源的类 PostMan，API 管理工具，强烈推荐。",
             "images": [
             "http://img.gank.io/6919ba82-dc5d-4932-b51a-1a8e609c3977"
                ],
             "publishedAt": "2017-05-11T12:03:09.581Z",
             "source": "chrome",
             "type": "拓展资源",
             "url": "https://github.com/getinsomnia/insomnia",
             "used": true,
             "who": "代码家"
             }
        ],
        "瞎推荐": [
             {
             "_id": "5913ced0421aa90c7fefdd89",
             "createdAt": "2017-05-11T10:39:12.387Z",
             "desc": "微软发布正式版 Visual Studio for Mac，快来尝鲜吧。",
             "publishedAt": "2017-05-11T12:03:09.581Z",
             "source": "chrome",
             "type": "瞎推荐",
             "url": "https://www.visualstudio.com/zh-hans/vs/visual-studio-mac/?rr=https%3A%2F%2Fnews.ycombinator.com%2F",
             "used": true,
             "who": "代码家"
             }
        ],
        "福利": [
             {
             "_id": "5913d09d421aa90c7fefdd8e",
             "createdAt": "2017-05-11T10:46:53.608Z",
             "desc": "5-11",
             "publishedAt": "2017-05-11T12:03:09.581Z",
             "source": "chrome",
             "type": "福利",
             "url": "http://7xi8d6.com1.z0.glb.clouddn.com/2017-05-11-18380166_305443499890139_8426655762360565760_n.jpg",
             "used": true,
             "who": "代码家"
             }
        ]
    }
 }




 */
