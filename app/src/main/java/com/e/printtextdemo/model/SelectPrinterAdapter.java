package com.e.printtextdemo.model;

import android.app.Activity;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.caysn.autoreplyprint.AutoReplyPrint;
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
                        //PrintUtil.isConnect()=""为断开连接
                        if (Print.IsOpened() && !TextUtils.isEmpty(PrintUtil.isConnect()))
                            mSelectedPosition = viewHolder.getAdapterPosition();
                        updateCheckBox(cb, viewHolder, device);
                    }
                }).start();
            } else if (device.getName().startsWith("M22_BT_") && null != SelectPrinterActivity.mPrinter) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //status=16为断开连接
                        if (SelectPrinterActivity.mPrinter.isConnected() && SelectPrinterActivity.mPrinter.getPrinterStatus() != 16)
                            mSelectedPosition = viewHolder.getAdapterPosition();
                        updateCheckBox(cb, viewHolder, device);
                    }
                }).start();
            } else if (device.getName().startsWith("FK-") && null != SelectPrinterActivity.pointer) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean isOpened = AutoReplyPrint.INSTANCE.CP_Port_IsOpened(SelectPrinterActivity.pointer);
                        int status = AutoReplyPrint.INSTANCE.CP_Pos_QueryRTStatus(SelectPrinterActivity.pointer, 10000);
                        //status=0为断开连接
                        if (isOpened && status != 0)
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
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MyApplication.hideLoading();
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
