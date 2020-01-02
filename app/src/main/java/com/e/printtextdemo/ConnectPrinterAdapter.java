package com.e.printtextdemo;

import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

/**
 * Created by weioule
 * on 2020/1/1
 */
public class ConnectPrinterAdapter extends BaseQuickAdapter<BluetoothDeviceBean, BaseViewHolder> {

    public ConnectPrinterAdapter(@Nullable List<BluetoothDeviceBean> data) {
        super(R.layout.item_select_shop, data);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void convert(@NonNull BaseViewHolder viewHolder, BluetoothDeviceBean item) {
        TextView textView = viewHolder.getView(R.id.shop_name);
        textView.setText(item.getName());
        RelativeLayout rootView = viewHolder.getView(R.id.root_view);
        if (mData.size() == 1 && TextUtils.isEmpty(item.getAddress())) {
            rootView.setBackgroundResource(R.color.white);
            textView.setTextColor(mContext.getResources().getColor(R.color.txt_color_c9));
            viewHolder.getView(R.id.line).setVisibility(View.GONE);
        } else {
            rootView.setBackground(mContext.getResources().getDrawable(R.drawable.select_item_click_bg));
            textView.setTextColor(mContext.getResources().getColor(R.color.txt_color));
            viewHolder.getView(R.id.line).setVisibility(View.VISIBLE);
        }

        if (viewHolder.getAdapterPosition() == mData.size() - 1) {
            viewHolder.getView(R.id.line).setVisibility(View.GONE);
        } else {
            viewHolder.getView(R.id.line).setVisibility(View.VISIBLE);
        }
    }

}
