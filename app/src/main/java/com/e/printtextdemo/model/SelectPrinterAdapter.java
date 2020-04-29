package com.e.printtextdemo.model;

import android.app.Activity;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.e.printtextdemo.MyApplication;
import com.e.printtextdemo.R;
import com.e.printtextdemo.activity.SelectPrinterActivity;
import com.e.printtextdemo.utils.PrintUtil;

import java.util.List;

import print.Print;

/**
 * Created by weioule
 * on 2019/12/11
 */
public class SelectPrinterAdapter extends BaseQuickAdapter<BluetoothDeviceBean, BaseViewHolder> {

    private Activity activity;
    private int mSelectedPosition = -1;

    public SelectPrinterAdapter(Activity activity, @Nullable List<BluetoothDeviceBean> data) {
        super(R.layout.item_select_printer, data);
        this.activity = activity;
    }

    @Override
    protected void convert(@NonNull BaseViewHolder viewHolder, BluetoothDeviceBean device) {
        ((TextView) viewHolder.getView(R.id.tv_device_name)).setText(device.getName());
        CheckBox cb = viewHolder.getView(R.id.cb_connect);

        //当前有在连接的打印机，则回显
        if (-1 == mSelectedPosition && device.getAddress().equals(MyApplication.currentPrinAddress)) {
            MyApplication.showLoading(activity, "");
            if (device.getName().startsWith("MPT-")) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (Print.IsOpened() && PrintUtil.isConnect())
                            mSelectedPosition = viewHolder.getAdapterPosition();
                        updateCheckBox(cb, viewHolder, device);
                    }
                }).start();
            } else if (device.getName().startsWith("M22_BT_") && null != SelectPrinterActivity.mPrinter) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (SelectPrinterActivity.mPrinter.getPrinterStatus() == 0)
                            mSelectedPosition = viewHolder.getAdapterPosition();
                        updateCheckBox(cb, viewHolder, device);
                    }
                }).start();
            }
        } else {
            cb.setChecked(viewHolder.getAdapterPosition() == mSelectedPosition);
            device.setSelected(viewHolder.getAdapterPosition() == mSelectedPosition);
        }
    }

    private void updateCheckBox(CheckBox cb, @NonNull BaseViewHolder viewHolder, BluetoothDeviceBean device) {
        MyApplication.hideLoading();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cb.setChecked(viewHolder.getAdapterPosition() == mSelectedPosition);
                device.setSelected(viewHolder.getAdapterPosition() == mSelectedPosition);
            }
        });

        //若连接状态都为异常，则视为无连接
        if (mSelectedPosition == -1) {
            MyApplication.currentPrintType = 0;
            MyApplication.currentPrinAddress = null;
        }
    }

    public void setSelectedPosition(int position) {
        this.mSelectedPosition = position;
    }
}
