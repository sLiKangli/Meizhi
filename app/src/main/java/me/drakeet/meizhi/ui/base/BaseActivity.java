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

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import me.drakeet.meizhi.GankApi;
import me.drakeet.meizhi.DrakeetFactory;
import me.drakeet.meizhi.R;
import me.drakeet.meizhi.ui.AboutActivity;
import me.drakeet.meizhi.ui.WebActivity;
import me.drakeet.meizhi.util.Once;
import me.drakeet.meizhi.util.Toasts;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by drakeet on 8/9/15.
 *  类的功能：
 *  1. 得到GankAi网络接口实例：sGankIO
 *  2. 初始化订阅者的集合类：CompositeSubscription
 *  3. 往CompositeSubscription中添加订阅者：Subscription;
 *  4. toolbar菜单项的点击事件：
 *      1） 点击 R.id.action_about 跳转到 AboutActivity
 *      2） 点击 R.id.action_login 跳转到 WebActivity
 *  5. 页面销毁的时候解除 CompositeSubscription 中的所有订阅
 */
public class BaseActivity extends AppCompatActivity {

    //获得GankApi网络接口实例
    public static final GankApi sGankIO = DrakeetFactory.getGankIOSingleton();

    //Subscription(订阅者)的集合
    private CompositeSubscription mCompositeSubscription;

    //得到CompositeSubscription实例
    public CompositeSubscription getCompositeSubscription() {
        if (this.mCompositeSubscription == null) {
            this.mCompositeSubscription = new CompositeSubscription();
        }

        return this.mCompositeSubscription;
    }

    //往集合中添加Subscription(注册订阅者，相当于观察者模式中观察者注册被观察者)
    public void addSubscription(Subscription s) {
        if (this.mCompositeSubscription == null) {
            this.mCompositeSubscription = new CompositeSubscription();
        }

        this.mCompositeSubscription.add(s);
    }


    /**
     * Toolbar菜单项的点击事件
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_about:
                //跳转到关于页面（ AboutActivity）
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            case R.id.action_login:
                //处理点击事件
                loginGitHub();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void loginGitHub() {
        //只有在第一次点击时会展示这个Toast，原因看Once类的show()方法
        new Once(this).show(R.string.action_github_login, () -> {
            Toasts.showLongX2(getString(R.string.tip_login_github));
        });
        String url = getString(R.string.url_login_github);
        Intent intent = WebActivity.newIntent(this, url,
                getString(R.string.action_github_login));
        startActivity(intent);
    }


    /**
     * 页面销毁的时候，解除所有注册者
     */
    @Override protected void onDestroy() {
        super.onDestroy();
        if (this.mCompositeSubscription != null) {
            this.mCompositeSubscription.unsubscribe();
        }
    }
}
