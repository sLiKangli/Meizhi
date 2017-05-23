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

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import com.mingle.headsUp.HeadsUp;
import com.mingle.headsUp.HeadsUpManager;
// @formatter:off
/**
 * Created by drakeet on 7/1/15.
 */
public class HeadsUps {

    /**
     * @param context  上下文
     * @param targetActivity  要显示这个通知的Activity
     * @param title   通知的标题
     * @param content  通知的内容
     * @param largeIcon  通知的大图标
     * @param smallIcon  通知的小图标
     * @param code  本通知对应的code
     */
    public static void show(Context context, Class<?> targetActivity, String title, String content, int largeIcon, int smallIcon, int code) {
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 11,
                new Intent(context, targetActivity), PendingIntent.FLAG_UPDATE_CURRENT);
        HeadsUpManager manage = HeadsUpManager.getInstant(context);

        //构建一个通知
        HeadsUp.Builder builder = new HeadsUp.Builder(context);
        builder.setContentTitle(title)
               .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS)
               .setContentIntent(pendingIntent)
               .setFullScreenIntent(pendingIntent, false)
               .setAutoCancel(true)
               .setContentText(content);

        //如果版本大于等于21,加上大图标和小图标，否则只有小图标
        if (Build.VERSION.SDK_INT >= 21) {
            builder.setLargeIcon(
                    BitmapFactory.decodeResource(context.getResources(), largeIcon))
                   .setSmallIcon(smallIcon);
        }
        else {
            builder.setSmallIcon(largeIcon);
        }
        HeadsUp headsUp = builder.buildHeadUp();
        headsUp.setSticky(true);
        //让这个通知显示出来
        manage.notify(code, headsUp);
    }
}
