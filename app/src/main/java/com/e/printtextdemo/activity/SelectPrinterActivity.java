package com.e.printtextdemo.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android_print_sdk.PrinterType;
import com.android_print_sdk.bluetooth.BluetoothPrinter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.e.printtextdemo.MyApplication;
import com.e.printtextdemo.R;
import com.e.printtextdemo.model.BluetoothDeviceBean;
import com.e.printtextdemo.model.SelectPrinterAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import print.Print;

public class SelectPrinterActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnSetting, btn_connect;
    private RecyclerView devicesList;
    private SelectPrinterAdapter mAdapter;
    public static BluetoothPrinter mPrinter;
    private String currentAddress;
    private Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_printer);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fillAdapter();
    }

    private void initView() {
        findViewById(R.id.tv_back).setOnClickListener(this);
        devicesList = findViewById(R.id.devices_list);
        btnSetting = findViewById(R.id.btn_goto_setting);
        btn_connect = findViewById(R.id.btn_connect);
        btn_connect.setOnClickListener(this);
        btnSetting.setOnClickListener(this);

        mAdapter = new SelectPrinterAdapter(this, null);
        devicesList.setLayoutManager(new LinearLayoutManager(this));
        devicesList.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                mAdapter.setSelectedPosition(position);
                adapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * 从所有已配对设备中找出打印设备并显示
     */
    private void fillAdapter() {
        List<BluetoothDeviceBean> printerDevices = getPairedDevices();
        mAdapter.replaceData(printerDevices);

        if (printerDevices.size() > 0) {
            btn_connect.setVisibility(View.VISIBLE);
            btnSetting.setText("配对更多设备");
        } else {
            btn_connect.setVisibility(View.GONE);
            btnSetting.setText("还未配对打印机，去设置");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_connect:
                String name = null;
                String address = null;
                for (BluetoothDeviceBean device : mAdapter.getData()) {
                    if (device.isSelected()) {
                        name = device.getName();
                        address = device.getAddress();
                    }
                }

                if (TextUtils.isEmpty(address)) {
                    MyApplication.showToast("请选择打印设备");
                } else if (address.equals(MyApplication.currentPrinAddress)) {
                    finish();
                } else {
                    if (name.startsWith("M22_BT_")) {
                        initAYPrinter(address, name);
                    } else if (name.startsWith("MPT-")) {
                        initHYPrinter(address);
                    } else {
                        MyApplication.showToast("暂不支持该打印设备");
                    }
                }
                break;
            case R.id.btn_goto_setting:
                startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
                break;
            case R.id.tv_back:
                finish();
                break;
        }
    }

    /**
     * 获取所有已配对的设备
     */
    public static List<BluetoothDeviceBean> getPairedDevices() {
        List<BluetoothDeviceBean> deviceList = new ArrayList<>();
        Set<BluetoothDevice> pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                BluetoothClass klass = device.getBluetoothClass();
                // 关于蓝牙设备分类参考 http://stackoverflow.com/q/23273355/4242112
                // 具体分类：https://blog.csdn.net/strivebus/article/details/65628628
                if (klass.getMajorDeviceClass() == BluetoothClass.Device.Major.IMAGING) {
                    BluetoothDeviceBean bean = new BluetoothDeviceBean();
                    bean.setName(device.getName());
                    bean.setAddress(device.getAddress());
                    deviceList.add(bean);
                }
            }
        }
        return deviceList;
    }

    private void initHYPrinter(String address) {
        btn_connect.setText(R.string.bt_connecting);
        btn_connect.setEnabled(false);
        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Print.Initialize();
                    Print.SetPrintDensity((byte) 127);

                    int portOpen = Print.PortOpen(getApplicationContext(), "Bluetooth," + address);
                    Message message = new Message();
                    message.what = portOpen;
                    currentAddress = address;
                    handler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void initAYPrinter(String address, String deviceName) {
        mPrinter = new BluetoothPrinter(address, 0);

        if (mPrinter.isPrinterNull()) {
            MyApplication.showToast(getString(R.string.no_bounded_device, deviceName));
            return;
        }

        mPrinter.setCurrentPrintType(PrinterType.Printer_58);
        //set handler for receive message of connect state from sdk.
        mPrinter.setHandler(bHandler);
        mPrinter.setEncoding("GBK");
        mPrinter.setNeedVerify(false);
        currentAddress = address;
        mPrinter.openConnection();
    }


    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            btn_connect.setText(R.string.bt_connect_printer);
            btn_connect.setEnabled(true);
            if (msg.what == 0) {
                MyApplication.currentPrintType = 1;
                MyApplication.currentPrinAddress = currentAddress;
                if (thread != null) {
                    Thread dummy = thread;
                    thread = null;
                    dummy.interrupt();
                    MyApplication.showToast("连接成功");
                    //关闭爱印打印机
                    if (null != mPrinter)
                        mPrinter.closeConnection();
                    finish();
                }
            } else {
                MyApplication.showToast(getString(R.string.connection_fails));
            }
        }
    };

    // The Handler that gets information back from the bluetooth printer.
    private final Handler bHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothPrinter.Handler_Connect_Connecting:
                    btn_connect.setText(R.string.bt_connecting);
                    btn_connect.setEnabled(false);
                    break;
                case BluetoothPrinter.Handler_Connect_Success:
                    MyApplication.currentPrintType = 2;
                    MyApplication.currentPrinAddress = currentAddress;
                    btn_connect.setText(R.string.bt_connect_printer);
                    btn_connect.setEnabled(true);
                    MyApplication.showToast(getString(R.string.bt_connect_success));
                    try {
                        //关闭汉印打印机
                        Print.PortClose();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    finish();
                    break;
                case BluetoothPrinter.Handler_Connect_Failed:
                    btn_connect.setText(R.string.bt_connect_printer);
                    btn_connect.setEnabled(true);
                    MyApplication.showToast(getString(R.string.bt_connect_failed));
                    break;
                case BluetoothPrinter.Handler_Connect_Closed:
                    btn_connect.setText(R.string.bt_connect_printer);
                    btn_connect.setEnabled(true);
                    MyApplication.showToast(getString(R.string.bt_connect_closed));
                    break;
            }
        }
    };
}
