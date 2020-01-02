package com.e.printtextdemo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import print.Print;


/**
 * Created by weioule
 * on 2020/1/1
 */
public class ConnectPrinterDialog {

    private Thread thread;
    private Context mContext;
    private AlertDialog mDialog;
    private BluetoothAdapter btAdapt;
    private MaxHeightRecyclerView paired_devices;
    private List<BluetoothDeviceBean> list = new ArrayList<>();
    private ConnectPrinterAdapter adapter;
    private DismissListener listener;

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            MyApplication.hideLoading();
            if (msg.what == 0) {
                doDiscovery();
            } else {
                ToastUtil.getInstance().show(R.string.connection_fails);
            }
        }

        ;
    };

    public ConnectPrinterDialog(Context context) {
        this.mContext = context;
    }

    public void show(DismissListener listener) {
        if (mContext == null) {
            return;
        }
        if (mDialog == null) {
            mDialog = new AlertDialog.Builder(mContext).create();
        }
        mDialog.show();
        mDialog.getWindow().setBackgroundDrawableResource(R.drawable.border_white_color_rounded_radius10_bg);
        mDialog.getWindow().setContentView(R.layout.connect_printer);

        addPairedDevice();

        int cententHeight = Utility.dp2px(mContext, 70) + list.size() * Utility.dp2px(mContext, 40);
        int height;
        if (cententHeight >= 305) {
            height = 305;
        } else {
            height = cententHeight;
        }

        WindowManager.LayoutParams lp = mDialog.getWindow().getAttributes();
        lp.width = 350;
        lp.height = height;
        mDialog.getWindow().setAttributes(lp);

        initView();
        this.listener = listener;
    }

    private void initView() {
        paired_devices = mDialog.getWindow().findViewById(R.id.paired_devices);
        paired_devices.setOverScrollMode(View.OVER_SCROLL_NEVER);
        paired_devices.setLayoutManager(new LinearLayoutManager(mContext));
        adapter = new ConnectPrinterAdapter(list);
        paired_devices.setAdapter(adapter);
        paired_devices.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter adapter, View view, int position) {
                BluetoothDeviceBean item = (BluetoothDeviceBean) adapter.getItem(position);
                if (TextUtils.isEmpty(item.getAddress())) return;
                MyApplication.showLoading((Activity) mContext, mContext.getResources().getString(R.string.in_the_connection));

                try {
                    if (btAdapt.isDiscovering()) {
                        btAdapt.cancelDiscovery();
                    }
                    //取得蓝牙mvc地址
                    final String info = item.getAddress();
                    if (!info.contains(":")) {
                        return;
                    }
                    thread = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                int portOpen = Print.PortOpen(mContext.getApplicationContext(), "Bluetooth," + info);
                                Message message = new Message();
                                message.what = portOpen;
                                handler.sendMessage(message);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    thread.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void addPairedDevice() {
        btAdapt = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = btAdapt.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                BluetoothClass klass = device.getBluetoothClass();
                // 关于蓝牙设备分类参考 http://stackoverflow.com/q/23273355/4242112
                if (klass.getMajorDeviceClass() == BluetoothClass.Device.Major.IMAGING) {
                    BluetoothDeviceBean bean = new BluetoothDeviceBean();
                    bean.setName(device.getName());
                    bean.setAddress(device.getAddress());
                    list.add(bean);
                }
            }
        }

        if (list.size() <= 0) {
            BluetoothDeviceBean bean = new BluetoothDeviceBean();
            bean.setName(mContext.getResources().getString(R.string.no_matching_device_available));
            list.add(bean);
        }
    }

    public void doDiscovery() {
        // 确认是否还需要做扫描
        if (btAdapt != null)
            btAdapt.cancelDiscovery();
        if (thread != null) {
            Thread dummy = thread;
            thread = null;
            dummy.interrupt();
        }

        if (null != mDialog) {
            mDialog.dismiss();
            if (null != listener) listener.dismiss();
        }
    }

    public interface DismissListener {
        void dismiss();
    }
}
