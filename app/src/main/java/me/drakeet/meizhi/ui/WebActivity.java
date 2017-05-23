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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.umeng.analytics.MobclickAgent;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.drakeet.meizhi.R;
import me.drakeet.meizhi.ui.base.ToolbarActivity;
import me.drakeet.meizhi.util.Androids;
import me.drakeet.meizhi.util.Toasts;

public class WebActivity extends ToolbarActivity {

    private static final String EXTRA_URL = "extra_url";
    private static final String EXTRA_TITLE = "extra_title";

    @Bind(R.id.progressbar) NumberProgressBar mProgressbar;
    @Bind(R.id.webView) WebView mWebView;
    @Bind(R.id.title) TextSwitcher mTextSwitcher;

    private String mUrl, mTitle;


    @Override protected int provideContentViewId() {
        return R.layout.activity_web;
    }


    //重写超类方法，允许回退
    @Override
    public boolean canBack() {
        return true;
    }


    /**
    * 对外提供开启这个Activity的静态方法，以及需要传入的参数
     */
    public static Intent newIntent(Context context, String extraURL, String extraTitle) {
        Intent intent = new Intent(context, WebActivity.class);
        intent.putExtra(EXTRA_URL, extraURL);
        intent.putExtra(EXTRA_TITLE, extraTitle);
        return intent;
    }


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        mUrl = getIntent().getStringExtra(EXTRA_URL);
        mTitle = getIntent().getStringExtra(EXTRA_TITLE);

        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true); //是否支持javaScript
        settings.setLoadWithOverviewMode(true); //是否以概览模式显示网页
        settings.setAppCacheEnabled(true); //设置是否可以使用缓存
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); //支持内容重新布局
        settings.setSupportZoom(true);  //设置是否支持缩放

        mWebView.setWebChromeClient(new ChromeClient());
        mWebView.setWebViewClient(new LoveClient());

        mWebView.loadUrl(mUrl);

        mTextSwitcher.setFactory(() -> {
            TextView textView = new TextView(this);
            textView.setTextAppearance(this, R.style.WebTitle);
            textView.setSingleLine(true);
            textView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            textView.postDelayed(() -> textView.setSelected(true), 1738);
            return textView;
        });
        mTextSwitcher.setInAnimation(this, android.R.anim.fade_in);
        mTextSwitcher.setOutAnimation(this, android.R.anim.fade_out);
        if (mTitle != null) setTitle(mTitle);
    }


    @Override public void setTitle(CharSequence title) {
        super.setTitle(title);
        mTextSwitcher.setText(title);
    }


    private void refresh() {
        mWebView.reload();
    }


    /**
     * 在屏幕上有键被按下会执行该方法
     */
    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                //判断是否是回退键被按下
                case KeyEvent.KEYCODE_BACK:
                    //判断是否可以后退
                    if (mWebView.canGoBack()) {
                        //后退一页
                        mWebView.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_web, menu);
        return true;
    }


    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_refresh:
                refresh();
                return true;
            case R.id.action_copy_url:
                String copyDone = getString(R.string.tip_copy_done);
                //复制文字到剪切板的工具类
                Androids.copyToClipBoard(this, mWebView.getUrl(), copyDone);
                return true;
            case R.id.action_open_url:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                Uri uri = Uri.parse(mUrl);
                intent.setData(uri);
                //查找是否有可以打开这个Intent的app
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toasts.showLong(R.string.tip_open_fail);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * 页面销毁时释放资源
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWebView != null) mWebView.destroy();
        ButterKnife.unbind(this);
    }


    @Override protected void onPause() {
        if (mWebView != null) mWebView.onPause();
        super.onPause();
        MobclickAgent.onPause(this);
    }


    @Override protected void onResume() {
        super.onResume();
        if (mWebView != null) mWebView.onResume();
        MobclickAgent.onResume(this);
    }


    private class ChromeClient extends WebChromeClient {

        /**
         * 监听页面加载时的进度，当加载完成是，隐藏进度条
         */
        @Override public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            mProgressbar.setProgress(newProgress);
            if (newProgress == 100) {
                mProgressbar.setVisibility(View.GONE);
            } else {
                mProgressbar.setVisibility(View.VISIBLE);
            }
        }

        /**
         * 获取当前打开的网页标题
         */
        @Override public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            setTitle(title);
        }
    }

    private class LoveClient extends WebViewClient {
        /**
         *  当在WebView中点击一个连接时，如果不想在浏览器中打开，则让这个方法返回true,
         *  说明我们自己来处理这个请求，返回false,则在浏览器中打开
         */
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url != null) view.loadUrl(url);
            return true;
        }
    }
}
