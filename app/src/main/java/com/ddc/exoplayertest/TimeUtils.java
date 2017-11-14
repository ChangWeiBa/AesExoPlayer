package com.ddc.exoplayertest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by QYBM_Android_two on 2017/1/17.
 * 时间工具类
 */

public class TimeUtils {
    //将时间转换为时间戳
    public static String dateToStamp(String s)throws ParseException
    {
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = simpleDateFormat.parse(s);
        long ts = date.getTime();
        res = String.valueOf(ts);
        return res;
    }
    //将时间戳转换为时间
    public static String stampToDate(String s)
    {
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long lt = new Long(s);
        Date date = new Date(lt);
        res = simpleDateFormat.format(date);
        return res;
    }
    //将时间戳转换为时间
    public static String stampToDates(String s)
    {
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        long lt = new Long(s);
        Date date = new Date(lt);
        res = simpleDateFormat.format(date);
        return res;
    }
    //将秒转为 天 小时 分钟
    public static String secToTime(int time) {
        int day;
        int hour;
        int minute;
        int second;
        if (time <= 0)
        {
            return "00:00";
        }
        else
        {
            minute = time / 60;//还有--分钟
            if (minute < 60) {
                second = time % 60;
                return unitFormat(minute) + ":" + unitFormat(second)+"";
            }

            hour = minute / 60;//还有--小时
            if (hour < 24)
            {
                minute = minute % 60;
                second = time - hour * 3600 - minute * 60;
                return unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second)+":";
            }

            day = hour/24;//还有--天
            hour = hour % 24;//剩余多少小时
            minute = minute - day * 24 * 60 - hour * 60;//剩余的分钟数
            return unitFormat(day)+"天"+ unitFormat(hour) + "小时" + unitFormat(minute)+"分钟";
        }
    }

    private static String unitFormat(int i) {
        String retStr;
        if (i >= 0 && i < 10)
            retStr = "0" + Integer.toString(i);
        else
            retStr = "" + i;
        return retStr;
    }

    public static void main(String[] args) {
        System.out.println(secToTime(600000));
    }
}










































