package net.fengg.app.tool;

import android.widget.RadioGroup;

/**
 * @author zhangfeng_2017
 * @date 2018/1/4
 */

public class Util {

    public static String formatTime(Long ms) {
        Integer ss = 1000;
        Integer mi = ss * 60;
        Integer hh = mi * 60;
        Integer dd = hh * 24;

        Long day = ms / dd;
        Long hour = (ms - day * dd) / hh;
        Long minute = (ms - day * dd - hour * hh) / mi;
        Long second = (ms - day * dd - hour * hh - minute * mi) / ss;
        Long milliSecond = ms - day * dd - hour * hh - minute * mi - second * ss;

        StringBuffer sb = new StringBuffer();
        if (day > 0) {
            if(day < 10) {
                sb.append("0");
            }
            sb.append(day);
            sb.append(":");
        }
        if(hour >= 0 && hour < 10) {
            sb.append("0");
        }
        sb.append(hour);
        sb.append(":");
        if(minute >= 0 && minute < 10) {
            sb.append("0");
        }
        sb.append(minute);
        sb.append(":");
        if(second >= 0 && second < 10) {
            sb.append("0");
        }
        sb.append(second);
        return sb.toString();
    }

    public static void disableRadioGroup(RadioGroup testRadioGroup) {
        for (int i = 0; i < testRadioGroup.getChildCount(); i++) {
            testRadioGroup.getChildAt(i).setEnabled(false);
        }
    }

    public static void enableRadioGroup(RadioGroup testRadioGroup) {
        for (int i = 0; i < testRadioGroup.getChildCount(); i++) {
            testRadioGroup.getChildAt(i).setEnabled(true);
        }
    }
}
