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
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * 简单重构了下，并且修复了重复插入图片问题
 * Created by drakeet on 8/10/15.
 */
public class RxMeizhi {
    //返回保存了图片uri的Observable的对象
    public static Observable<Uri> saveImageAndGetPathObservable(Context context, String url, String title) {
        return Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override public void call(Subscriber<? super Bitmap> subscriber) {
                Bitmap bitmap = null;
                try {
                    //得到bitmap
                    bitmap = Picasso.with(context).load(url).get();
                } catch (IOException e) {
                    subscriber.onError(e);
                }
                if (bitmap == null) {
                    subscriber.onError(new Exception("无法下载到图片"));
                }
                subscriber.onNext(bitmap);
                subscriber.onCompleted();
            }
        }).flatMap(bitmap -> {
            //得到保存图片的目录
            File appDir = new File(Environment.getExternalStorageDirectory(), "Meizhi");
            if (!appDir.exists()) {
                appDir.mkdir();
            }
            //要保存图片的名称
            String fileName = title.replace('/', '-') + ".jpg";
            File file = new File(appDir, fileName);
            try {
                //用流把图片写入保存文件中
                FileOutputStream outputStream = new FileOutputStream(file);
                assert bitmap != null;
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //得到文件的uri
            Uri uri = Uri.fromFile(file);
            // 通知图库更新
            Intent scannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
            context.sendBroadcast(scannerIntent);
            return Observable.just(uri);
        }).subscribeOn(Schedulers.io()); //让上面的代码运行在子线程
    }
}
