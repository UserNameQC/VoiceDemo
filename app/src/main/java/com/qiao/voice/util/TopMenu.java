package com.qiao.voice.util;

import android.app.Activity;
import android.view.View;

import com.qiao.voice.R;
import com.zaaach.toprightmenu.MenuItem;
import com.zaaach.toprightmenu.TopRightMenu;

import java.util.ArrayList;
import java.util.List;

public class TopMenu {

    public static TopRightMenu topRightMenu;
    public static void showMenu(final Activity activity, View view){
        topRightMenu = new TopRightMenu(activity);
        List<MenuItem> menuItems = new ArrayList<>();
        menuItems.add(new MenuItem(R.mipmap.icon_edit, "编辑"));
        menuItems.add(new MenuItem(R.mipmap.icon_phone, "拍照"));
        topRightMenu
                .setHeight(300)     //默认高度480
                //.setWidth(240)      //默认宽度wrap_content
                .showIcon(true)     //显示菜单图标，默认为true
                .dimBackground(true)        //背景变暗，默认为true
                .needAnimationStyle(true)   //显示动画，默认为true
                .setAnimationStyle(R.style.TRM_ANIM_STYLE)
                .addMenuList(menuItems)
                .setOnMenuItemClickListener(new TopRightMenu.OnMenuItemClickListener() {
                    @Override
                    public void onMenuItemClick(int position) {
                        //Toast.makeText(activity, "点击菜单:" + position, Toast.LENGTH_SHORT).show();
                        if (itemClickListener != null){
                            switch (position){
                                case 0:
                                    itemClickListener.edit();
                                    break;
                                case 1:
                                    itemClickListener.takePhotos();
                                    break;
                            }
                        }
                    }
                })
                //.showAsDropDown(view, -225, 0);	//带偏移量
      		.showAsDropDown(view, -162, 22);
    }

    public interface ItemClickListener{
        void takePhotos();
        void edit();
    }
    private static ItemClickListener itemClickListener;
    public static void setItemListener(ItemClickListener itemListener){
        itemClickListener = itemListener;
    }
}
