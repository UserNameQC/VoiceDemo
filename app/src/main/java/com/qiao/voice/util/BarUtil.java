package com.qiao.voice.util;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class BarUtil {

    /**
     * 动态设置控件高度
     *
     * @param activity
     * @param view
     */
    public static void setViewHeight(Activity activity, View view) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        /*if (view instanceof LinearLayout){
            params = (LinearLayout.LayoutParams)
        }else {
            params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        }*/
        params.height = getStatuesBarHeight(activity);
        view.setLayoutParams(params);
    }

    /**
     * 通过系统resource 获取状态栏高度
     *
     * @param activity 上下文
     * @return 状态栏高度
     */
    public static int getStatuesBarHeight(Activity activity) {
        /**
         * 获取状态栏高度——方法1
         * */
        int statusBarHeight = -1;
        //获取status_bar_height资源的ID
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight = activity.getResources().getDimensionPixelSize(resourceId);
        }
        //Log.e("WangJ", "状态栏-方法1:" + statusBarHeight1);
        return statusBarHeight;
    }

    /**
     * 动态设置控件距离上边距
     *
     * @param activity
     * @param view
     */
    public static void setMargin(Activity activity, View view, int type) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (type == 0){
            ((LinearLayout.LayoutParams)params).setMargins(0, getStatuesBarHeight(activity), 0, 0);
        } else {
            ((RelativeLayout.LayoutParams)params).setMargins(0, getStatuesBarHeight(activity), 0, 0);
        }
        view.setLayoutParams(params);
    }

    public static void setMargin(Activity activity, View view) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        params.setMargins(0, getStatuesBarHeight(activity), 0, 0);
        view.setLayoutParams(params);
    }
}
