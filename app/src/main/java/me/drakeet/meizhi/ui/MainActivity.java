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
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.assit.QueryBuilder;
import com.litesuits.orm.db.model.ConflictAlgorithm;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import me.drakeet.meizhi.App;
import me.drakeet.meizhi.R;
import me.drakeet.meizhi.data.MeizhiData;
import me.drakeet.meizhi.data.entity.Gank;
import me.drakeet.meizhi.data.entity.Meizhi;
import me.drakeet.meizhi.data.休息视频Data;
import me.drakeet.meizhi.func.OnMeizhiTouchListener;
import me.drakeet.meizhi.ui.adapter.MeizhiListAdapter;
import me.drakeet.meizhi.ui.base.SwipeRefreshBaseActivity;
import me.drakeet.meizhi.util.AlarmManagers;
import me.drakeet.meizhi.util.Dates;
import me.drakeet.meizhi.util.Once;
import me.drakeet.meizhi.util.PreferencesLoader;
import me.drakeet.meizhi.util.Toasts;
import okhttp3.ResponseBody;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class MainActivity extends SwipeRefreshBaseActivity {

    //预先加载大小
    private static final int PRELOAD_SIZE = 6;

    @Bind(R.id.list) RecyclerView mRecyclerView;

    Handler handler = new Handler();
    /**
     * 妹纸图片List适配器
     */
    private MeizhiListAdapter mMeizhiListAdapter;

    /**
     * 妹纸图片列表
     */
    private List<Meizhi> mMeizhiList;
    private boolean mIsFirstTimeTouchBottom = true;
    private int mPage = 1;
    private boolean mMeizhiBeTouched;


    @Override
    protected int provideContentViewId() {
        return R.layout.activity_main;
    }


    @Override protected void onCreate(Bundle savedInstanceState) {
        Log.e("输出", "main thread id:  " + android.os.Process.myTid() + "========================" );
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        mMeizhiList = new ArrayList<>();
        //查询数据库中的Meizhi表，在应用程序第一次查询是没有结果的
        //但链接网络查询到的数据会放到数据库中，下次再启动应用
        //程序查询数据库就会有数据了
        QueryBuilder query = new QueryBuilder(Meizhi.class);
        query.appendOrderDescBy("publishedAt");
        query.limit(0, 10);
        //把查询结果放到mMeizhiList集合中
        mMeizhiList.addAll(App.sDb.query(query));



        setupRecyclerView();
        //友盟的相关设置
        setupUmeng();

        //开启定时任务
        AlarmManagers.register(this);
    }


    /**
     * 页面完全加载完成后执行此方法
     */
    @Override protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //页面加载完毕使SwipeRefreshLayout处于刷新状态
        new Handler().postDelayed(() -> setRefresh(true), 358);
        //页面就开始加载数据
        loadData(true);
       // test();
    }

//  直接在浏览器请求Meizhi的json数据请求不到，所以在原作者的代码上加上了这个\
//  测试代码，用来打印Meizhi的json数据
//    private void test(){
//        sGankIO.getResponse(1)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<ResponseBody>() {
//                    @Override
//                    public void onCompleted() {
//                        Log.e("输出", "onCompleted()"  );
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Log.e("输出", e.getMessage()  );
//                    }
//
//                    @Override
//                    public void onNext(ResponseBody responseBody) {
//                        try {
//                            String str = responseBody.string();
//                            Log.e("输出", str  );
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//
//
//                    }
//                });
//    }

    private void setupUmeng() {
        UmengUpdateAgent.update(this);
        UmengUpdateAgent.setDeltaUpdate(false);
        UmengUpdateAgent.setUpdateOnlyWifi(false);
    }


    private void setupRecyclerView() {
        //设置RecyclerView的效果为瀑布流
        final StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2,
                StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        //RecyclerView的适配器
        mMeizhiListAdapter = new MeizhiListAdapter(this, mMeizhiList);
        mRecyclerView.setAdapter(mMeizhiListAdapter);
        new Once(this).show("tip_guide_6", () -> {
            Snackbar.make(mRecyclerView, getString(R.string.tip_guide), Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.i_know, v -> {
                    })
                    .show();
        });

        //添加滚动监听
        mRecyclerView.addOnScrollListener(getOnBottomListener(layoutManager));
        //RecyclerView中item的点击监听
        mMeizhiListAdapter.setOnMeizhiTouchListener(getOnMeizhiTouchListener());
    }


    /**
     * 获取服务数据
     *
     * @param clean 清除来自数据库缓存或者已有数据。
     */
    private void loadData(boolean clean) {
        mLastVideoIndex = 0;
        /**
         * 分析下面的RxJava代码，由于使用了Lambda表达式，看起来会不太易懂
         * zip()方法功能：合并多个数据流
         *  参数一：sGankIO.getMeizhiData(mPage) 返回值：Observable<MeizhiData>
         *  参数二：sGankIO.get休息视频Data(mPage) 返回值：Observable<休息视频Data>
         *  参数三：合并 Observable<MeizhiData> 和 Observable<休息视频Data> 数据流
         *           的方法：createMeizhiDataWith休息视频Desc()
         *  运行这个方法后最终得到一个处理后的MeiZhi集合
         *
         * map()方法功能：转换数据类型，泛型一是方法参数，泛型二是返回值类型
         * 原型应该是：map(new Fun1<MeizhiData, List<Meizhi>>(){
         *                  @Override
         *                   public List<Meizhi> call(MeizhiData meizhiData) {
         *                      return meizhiData.results;
         *              }
         *           })  这样取出 List<Meizhi> 供下面使用
         *flatMap(Observable::from)原型：
         *  flatMap(new Fun1<List<Meizhi>, Observable<MeiZhi>{
         *              public Observable<MeiZhi> call(List<Meizhi> results){
         *                  return Observable.from(results);
         *              }
         *          })  到这一步得到了Observable<MeiZhi>对象
         *
         *toSortedList()方法是对MeiZhi的数据进行排序，按照日期排列，原型：
         * public List<MeiZhi> toSortedList(new Fun2<MaiZhi, MaiZhi, Integer>{
         *               public MaiZhi call(MaiZhi maiZhi1, MaiZhi maiZhi2){
         *                     return meizhi2.publishedAt.compareTo(meizhi1.publishedAt);
         *               }
         *           })   重新返回Meizhi的集合
         *doOnNext(this::saveMeizhis) 在执行Subscriber中的onNext()之前对数据进行进一步处理
         *              因为它在observeOn(AndroidSchedulers.mainThread())之前调用，所有不会在主线程运行
         *         doOnNext(new Action1<List<Meizhi>>({
         *              public void call(List<Meizhi> meiZhis){
         *                   App.sDb.insert(meizhis, ConflictAlgorithm.Replace);
         *              }
         *          }))
         *observeOn(AndroidSchedulers.mainThread()) 把线程调度为主线程
         *
         * finallyDo() 会在Observable最终执行完毕之后调用，无论是异常结束还是正常结束。
         * public final Observable<List<MeiZhi>> finallyDo(new Action0(){
         *         public void call(){
         *              setRefresh(false);
         *         }
         *  })；
         *
         *  subscribe() 注册一个订阅者，其中的两个参数就是Subscriber中的onNext()方法和
         *         onError(Throwable throwable)方法的另一种实现
         *
         *  总算写完啦，好繁琐
         */
        Subscription s = Observable
               .zip(sGankIO.getMeizhiData(mPage),
                     sGankIO.get休息视频Data(mPage),
                     this::createMeizhiDataWith休息视频Desc)
               .map(meizhiData -> meizhiData.results)
               .flatMap(Observable::from)
               .toSortedList((meizhi1, meizhi2) ->
                     meizhi2.publishedAt.compareTo(meizhi1.publishedAt))
               .doOnNext(this::saveMeizhis)
               .observeOn(AndroidSchedulers.mainThread())
               .finallyDo(() -> setRefresh(false))
               .subscribe(meizhis -> {
                   //是否清除来自数据库或缓存的数据
                   if (clean) mMeizhiList.clear();
                   //把请求网络的得到的数据放入集合
                   mMeizhiList.addAll(meizhis);
                   //通知数据已经改变
                   mMeizhiListAdapter.notifyDataSetChanged();
                   //关闭刷新
                   setRefresh(false);
               }, throwable -> loadError(throwable));

        //把Subscription放入CompositeSubscription,方便页面销毁时统一取消订阅
        addSubscription(s);
    }


    /**
     * 加载错误的处理方法
     */
    private void loadError(Throwable throwable) {
        throwable.printStackTrace();
        Snackbar.make(mRecyclerView, R.string.snap_load_fail, Snackbar.LENGTH_LONG)
                .setAction(R.string.retry, v -> {
                    requestDataRefresh();
                })
                .show();
    }


    /**
     * 把数据保存到数据库，执行在子线程
     */
    private void saveMeizhis(List<Meizhi> meizhis) {
        /**
         * 第二个参数是为了解决约束冲突的问题
         ConflictAlgorithm.Replace：
         当发生UNIQUE约束冲突，先存在的，导致冲突的行在更改或插入发生冲突的行
         之前被删除。这样，更改和插入总是被执行。命令照常执行且不返回错误信息。
         当发生NOT NULL约束冲突，导致冲突的NULL值会被字段缺省值取代。若字段无缺省值，
         执行ABORT算法。当冲突应对策略为满足约束而删除行时，它不会调用删除触发器。
         */
        App.sDb.insert(meizhis, ConflictAlgorithm.Replace);
        Log.e("输出", "saveMeizhis:  " + android.os.Process.myTid() + "========================" );
    }


    /**
     * @param data  包含Meizhi{@me.drakeet.meizhi.data.entity.Meizhi}数据的集合的类
     * @param love  包含Gank(休息视频){@me.drakeet.meizhi.data.entity.Gank} 数据的集合的类
     * @return  处理后的MeiZhi数据的集合
     */
    private MeizhiData createMeizhiDataWith休息视频Desc(MeizhiData data, 休息视频Data love) {
        for (Meizhi meizhi : data.results) {
            //由Meizhi的 desc 字段加上休息视频的 desc 字段
            meizhi.desc = meizhi.desc + " " +
                    getFirstVideoDesc(meizhi.publishedAt, love.results);
        }
        return data;
    }


    private int mLastVideoIndex = 0;


    /**
     * 方法思路：循环List<Gank>，判断取出的Gank的publishedAt和Meizhi的publishedAt时间
     * 是否在同一天，如果是，mLastVideoIndex等于当前下标，跳出循环，结束方法。下次调用这个方法就会从
     * mLastVideoIndex开始匹配，mLastVideoIndex之前的数据无论有没有匹配成功都忽略
     *
     * @param publishedAt  Meizhi图片的发布时间
     * @param results    Gank(休息视频){@me.drakeet.meizhi.data.entity.Gank} 数据的集合
     * @return  视频的描述文字
     */
    private String getFirstVideoDesc(Date publishedAt, List<Gank> results) {
        String videoDesc = "";
        for (int i = mLastVideoIndex; i < results.size(); i++) {
            Gank video = results.get(i);
            //如果视频的发布时间为Null,那么发布时间等于video.createdAt(创建时间)
            if (video.publishedAt == null) video.publishedAt = video.createdAt;
            //判断Meizhi图片的时间和Gank(休息视频)的实际是否在同一天
            if (Dates.isTheSameDay(publishedAt, video.publishedAt)) {
                videoDesc = video.desc;
                mLastVideoIndex = i;
                break;
            }
        }
        return videoDesc;
    }


    private void loadData() {
        loadData(/* clean */false);
    }


    /**
     * @return RecyclerView 中 item 触摸事件的监听
     *  v ：接受点击事件的view
     *  meizhiView : 显示meizhi图片的view
     *  card ： 显示图片和描述文字的cardVeiw;
     *  meizhi：被点击项所所代表的meizhi对象数据
     *
     */
    private OnMeizhiTouchListener getOnMeizhiTouchListener() {
        return (v, meizhiView, card, meizhi) -> {
            if (meizhi == null) return;
            //如果触摸的是meizhiView打开PictureActivity，mMeizhiBeTouched控制不能同时打开两个图片
            if (v == meizhiView && !mMeizhiBeTouched) {
                mMeizhiBeTouched = true;
                /**
                 * 由于没有加载目标，加载的图片会缓存起来，然后会调用fetch()中
                 * 的Callback()回调方法
                 */
                Picasso.with(this).load(meizhi.url).fetch(new Callback() {

                    @Override public void onSuccess() {
                      mMeizhiBeTouched = false;
                        startPictureActivity(meizhi, meizhiView);
                    }


                    @Override public void onError() {mMeizhiBeTouched = false;}
                });
            } else if (v == card) {
                //打开GankActivity
                startGankActivity(meizhi.publishedAt);
            }
        };
    }


    /**
     * 开启休息视频的Activity，传入参数视频发布日期
     */
    private void startGankActivity(Date publishedAt) {
        Intent intent = new Intent(this, GankActivity.class);
        intent.putExtra(GankActivity.EXTRA_GANK_DATE, publishedAt);
        startActivity(intent);
    }


    /**
     *  开启查看放大图片的Activity
     */
    private void startPictureActivity(Meizhi meizhi, View transitView) {
        //开启Activity的时候传入图片的url 和 desc
        Intent intent = PictureActivity.newIntent(MainActivity.this, meizhi.url, meizhi.desc);
        /**
         * 转场动画参考：https://github.com/lgvalle/Material-Animations
         *  利用转场(Transition)动画中的共享元素（shared element）开启Activity，达到更好的视觉效果
         *  参数一： activity
         *  参数二：与要开启的Activity的共享元素
         *  参数三：共享元素的TransitionName
         */
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                MainActivity.this, transitView, PictureActivity.TRANSIT_PIC);
        try {
            ActivityCompat.startActivity(MainActivity.this, intent, optionsCompat.toBundle());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            startActivity(intent);
        }
    }


    /**
     * 当点击ToolBar时页面定位到RecyclerView的第一项
     */
    @Override public void onToolbarClick() {
        mRecyclerView.smoothScrollToPosition(0);
    }

    /**
     *  浮动按钮的点击事件
     *  如果mMeizhiList有数据，则打开第一个item的休息视频
     */
    @OnClick(R.id.main_fab) public void onFab(View v) {
        if (mMeizhiList != null && mMeizhiList.size() > 0) {
            startGankActivity(mMeizhiList.get(0).publishedAt);
        }
    }


    /**
     * 该方法是在MainActivity的父类SwipeRefreshBaseActivity的onPostCreate()方法中调用的
     */
    @Override
    public void requestDataRefresh() {
        super.requestDataRefresh();
        mPage = 1;
        //只要SwipeRefreshLayout 处于刷新状态就刷新数据
        loadData(true);
    }


    private void openGitHubTrending() {
        String url = getString(R.string.url_github_trending);
        String title = getString(R.string.action_github_trending);
        Intent intent = WebActivity.newIntent(this, url, title);
        startActivity(intent);
    }


    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.action_notifiable);
        initNotifiableItemState(item);
        return true;
    }

    //根据SP中存储的boolean值觉得是否开启每天中午提醒
    private void initNotifiableItemState(MenuItem item) {
        PreferencesLoader loader = new PreferencesLoader(this);
        item.setChecked(loader.getBoolean(R.string.action_notifiable, true));
    }


    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_trending:
                openGitHubTrending();
                return true;
            case R.id.action_notifiable:
                boolean isChecked = !item.isChecked();
                item.setChecked(isChecked);
                PreferencesLoader loader = new PreferencesLoader(this);
                loader.saveBoolean(R.string.action_notifiable, isChecked);
                Toasts.showShort(isChecked ? R.string.notifiable_on : R.string.notifiable_off);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    RecyclerView.OnScrollListener getOnBottomListener(StaggeredGridLayoutManager layoutManager) {
        return new RecyclerView.OnScrollListener() {
            @Override public void onScrolled(RecyclerView rv, int dx, int dy) {
                Log.e("输出 ", "int[1]: " + layoutManager.findLastCompletelyVisibleItemPositions(new int[3]).toString() );
                Log.e("输出 ", "int[1][0]: " + layoutManager.findLastCompletelyVisibleItemPositions(new int[3])[0] );
                Log.e("输出 ", "mPage: " + mPage );
                Log.e("输出 ", "mMeizhiListAdapter.getItemCount(): " + mMeizhiListAdapter.getItemCount() );


                boolean isBottom =
                        //最后完成显示的item的下标 是否大于等于 item的总数减去预加载大小
                        layoutManager.findLastCompletelyVisibleItemPositions(new int[2])[1] >=
                                mMeizhiListAdapter.getItemCount() - PRELOAD_SIZE;
                //SwipeRefreshLayout不处于刷新状态并且isBottom为true
                if (!mSwipeRefreshLayout.isRefreshing() && isBottom) {
                    //第一次到达底部不会刷新
                    if (!mIsFirstTimeTouchBottom) {
                        mSwipeRefreshLayout.setRefreshing(true);
                        mPage += 1;
                        loadData();
                    } else {
                        mIsFirstTimeTouchBottom = false;
                    }
                }
            }
        };
    }


    @Override public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }


    @Override public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }


    @Override protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }
}


/** 请求到的json数据信息(每次请求10条)：

 休息视频 响应数据片段
 url:http://gank.io/api/data/休息视频/10/1

 {
    "error": false,
    "results": [
         {
         "_id": "590f2b98421aa90c7a8b2ad2",
         "createdAt": "2017-05-07T22:13:44.239Z",
         "desc": "横扫阿凡达韩国票房纪录 海战史诗巨制 《鸣梁海战》",
         "publishedAt": "2017-05-10T11:56:10.18Z",
         "source": "chrome",
         "type": "休息视频",
         "url": "http://www.bilibili.com/video/av10379658/",
         "used": true,
         "who": "LHF"
         },
         {
         "_id": "59106976421aa90c7fefdd68",
         "createdAt": "2017-05-08T20:49:58.915Z",
         "desc": "【问舰】痞子英雄成长记！漫威经典电影《银河护卫队》",
         "publishedAt": "2017-05-09T12:13:25.467Z",
         "source": "chrome",
         "type": "休息视频",
         "url": "http://www.bilibili.com/video/av10396774/",
         "used": true,
         "who": "LHF"
         }....
    ]
 }

 Meizhi图片
 url :https://leancloud.cn:443/1.1/classes/data/福利/10/1

 {
    "error": false,
    "results": [
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
         },
         {
         "_id": "591264ce421aa90c7a8b2aec",
         "createdAt": "2017-05-10T08:54:38.531Z",
         "desc": "5-10",
         "publishedAt": "2017-05-10T11:56:10.18Z",
         "source": "chrome",
         "type": "福利",
         "url": "http://7xi8d6.com1.z0.glb.clouddn.com/2017-05-10-18382517_1955528334668679_3605707761767153664_n.jpg",
         "used": true,
         "who": "带马甲"
         }....
    ]
 }


 */