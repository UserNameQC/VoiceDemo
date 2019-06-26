package com.qiao.voice.adapter;

import android.content.Context;

import com.qiao.voice.R;

import java.util.List;

public class FileNameAdapter extends UniversalRecyclerAdapter<String> {

    public FileNameAdapter(Context mContext, List<String> mDatas) {
        super(mContext, mDatas, R.layout.item_voice_layout);
    }

    @Override
    protected void convert(Context mContext, BaseViewHolder holder, String s, int position) {
        holder.setText(R.id.item_value, s);
    }
}
