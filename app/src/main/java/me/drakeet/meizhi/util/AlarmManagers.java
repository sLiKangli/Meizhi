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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import java.util.Calendar;
import me.drakeet.meizhi.service.AlarmReceiver;

/**
 * Created by drakeet on 7/1/15.
 */
public class AlarmManagers {

    public static void register(Context context) {
        //获取Calendar(日历)的实例
        Calendar today = Calendar.getInstance();
        Calendar now = Calendar.getInstance();
        //设置时间
        today.set(Calendar.HOUR_OF_DAY,10); //小时
        today.set(Calendar.MINUTE, 5);  //分钟
        today.set(Calendar.SECOND, 38);  //秒
        //now > today  true
        //now < today  false
        if (now.after(today)) {
            return;
        }
        //创建一个Broadcast
        Intent intent = new Intent("me.drakeet.meizhi.alarm");
        intent.setClass(context, AlarmReceiver.class);
        PendingIntent broadcast = PendingIntent.getBroadcast(context, 520, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        //在指定时间到来开启broadcast
        manager.set(AlarmManager.RTC_WAKEUP, today.getTimeInMillis(), broadcast);
    }
}
