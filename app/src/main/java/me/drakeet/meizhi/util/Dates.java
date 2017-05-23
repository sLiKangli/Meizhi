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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by drakeet on 6/20/15.
 */
public class Dates {

    /**
     * @param date  日期
     * @return  日期的字符串（yyyy/MM/dd）形式
     */
    public static String toDate(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        return dateFormat.format(date);
    }


    /**
     * @param date  日期
     * @param add  要添加的天数
     * @return  添加 N 天之后的字符串形式的日期
     */
    public static String toDate(Date date, int add) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        //在原来的日期之上加上 n 天
        calendar.add(Calendar.DATE, add);
        return toDate(calendar.getTime());
    }


    /**
     * @return 获取当前日期的最后一天
     */
    public static Date getLastdayDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, -1);
        return calendar.getTime();
    }


    /**
     * @return  获取当前日期的下一天
     */
    public static Date getNextdayDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, 1);
        return calendar.getTime();
    }


    /**
     * @return  两个日期是不是在同一天
     */
    public static boolean isTheSameDay(Date one, Date another) {
        Calendar _one = Calendar.getInstance();
        _one.setTime(one);
        Calendar _another = Calendar.getInstance();
        _another.setTime(another);
        int oneDay = _one.get(Calendar.DAY_OF_YEAR);
        int anotherDay = _another.get(Calendar.DAY_OF_YEAR);

        return oneDay == anotherDay;
    }
}
