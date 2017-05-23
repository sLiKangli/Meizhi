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

package me.drakeet.meizhi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.schedulers.Schedulers;

/**
 * 2015-08-07T03:57:47.229Z
 * Created by drakeet on 8/9/15.
 *
 *  在构造方法中得到 GankApi 和 DrakeetApi 网络请求接口的实例
 */
public class DrakeetRetrofit {

    final GankApi gankService;
    final DrakeetApi drakeetService;

    // @formatter:off
    final static Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .serializeNulls()
            .create();
    // @formatter:on


    DrakeetRetrofit() {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        //是否是调试阶段，如果是，添加日志拦截器
        if (DrakeetFactory.isDebug) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClient.addInterceptor(logging);
        }
        //设置链接超时
        httpClient.connectTimeout(12, TimeUnit.SECONDS);
        //成功构建OkHttpClient实例
        OkHttpClient client = httpClient.build();

        //创建Retrofit实例
        Retrofit.Builder builder = new Retrofit.Builder();
        builder.baseUrl("http://gank.io/api/") //添加url前缀
            .client(client) //添加OkHttpClient，不需要拦截器的话不用添加了
                //Retrofit和Rxjava联合使用才需要添加，RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io())
                //表示发起的网络是异步请求，如果用 RxJavaCallAdapterFactory.create() 参数，则是同步请求
            .addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io()))
             //添加json解析器，可以把请求网络得到的json数据解析为JavaBean
            .addConverterFactory(GsonConverterFactory.create(gson));
        Retrofit gankRest = builder.build();
        //修改url前缀，用来构建另一个Retrofit实例
        builder.baseUrl("https://leancloud.cn:443/1.1/classes/");
        Retrofit drakeetRest = builder.build();
        gankService = gankRest.create(GankApi.class); //GankApi网络接口实例
        drakeetService = drakeetRest.create(DrakeetApi.class); //DrakeetApi网络接口实例
    }


    /**
     * @return GankApi网络接口实例
     */
    public GankApi getGankService() {
        return gankService;
    }

    /**
     * @return DrakeetApi网络接口实例
     */
    public DrakeetApi getDrakeetService() {
        return drakeetService;
    }
}
