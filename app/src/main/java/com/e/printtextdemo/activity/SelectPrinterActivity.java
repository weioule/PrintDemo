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
import com.caysn.autoreplyprint.AutoReplyPrint;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.e.printtextdemo.MyApplication;
import com.e.printtextdemo.R;
import com.e.printtextdemo.model.BluetoothDeviceBean;
import com.e.printtextdemo.model.SelectPrinterAdapter;
import com.sun.jna.Pointer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import print.Print;

public class SelectPrinterActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnSetting, btn_connect;
    private RecyclerView devicesList;
    private SelectPrinterAdapter mAdapter;
    public static BluetoothPrinter mPrinter;
    private BluetoothPrinter oldAYPinter;
    public static Pointer pointer;
    private Pointer oldFKPinter;
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
                        break;
                    }
                }

                if (TextUtils.isEmpty(address)) {
                    MyApplication.showToast("请选择打印设备");
                } else if (address.equals(MyApplication.currentPrinAddress)) {
                    tofinish();
                } else {
                    if (name.startsWith("M22_BT_")) {
                        initAYPrinter(address, name);
                    } else if (name.startsWith("MPT-")) {
                        initHYPrinter(address);
                    } else if (name.startsWith("FK-")) {
                        initFKPrinter(address);
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
                    try {
                        //关闭旧的打印机，换当前型号打印机情况
                        Print.PortClose();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
        oldAYPinter = mPrinter;
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

    private void initFKPrinter(String address) {
        btn_connect.setText(R.string.bt_connecting);
        btn_connect.setEnabled(false);
        currentAddress = address;

        AutoReplyPrint.INSTANCE.CP_Port_AddOnPortOpenedEvent(opened_callback, Pointer.NULL);
        AutoReplyPrint.INSTANCE.CP_Port_AddOnPortOpenFailedEvent(openfailed_callback, Pointer.NULL);
        AutoReplyPrint.INSTANCE.CP_Port_AddOnPortClosedEvent(closed_callback, Pointer.NULL);

        new Thread(new Runnable() {
            @Override
            public void run() {
                oldFKPinter = pointer;
                pointer = AutoReplyPrint.INSTANCE.CP_Port_OpenBtSpp(address, 0);
            }
        }).start();
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
                    //关闭爱印打印机
                    if (null != mPrinter) {
                        mPrinter.closeConnection();
                        mPrinter = null;
                    }
                    closeFKPrinter(pointer);
                    tofinish();
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
                    try {
                        //关闭旧的打印机，换当前型号打印机情况
                        if (null != oldAYPinter) {
                            oldAYPinter.closeConnection();
                            oldAYPinter = null;
                        }
                        //关闭汉印打印机
                        Print.PortClose();
                        closeFKPrinter(pointer);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    tofinish();
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
    AutoReplyPrint.CP_OnPortOpenedEvent_Callback opened_callback = new AutoReplyPrint.CP_OnPortOpenedEvent_Callback() {
        @Override
        public void CP_OnPortOpenedEvent(Pointer handle, String name, Pointer private_data) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MyApplication.currentPrintType = 3;
                    MyApplication.currentPrinAddress = currentAddress;
                    btn_connect.setText(R.string.bt_connect_printer);
                    btn_connect.setEnabled(true);
                    try {
                        if (null != oldFKPinter)
                            closeFKPrinter(oldFKPinter);
                        //关闭汉印打印机
                        Print.PortClose();
                        //关闭爱印打印机
                        if (null != mPrinter) {
                            mPrinter.closeConnection();
                            mPrinter = null;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    tofinish();
                }
            });
        }
    };
    AutoReplyPrint.CP_OnPortOpenFailedEvent_Callback openfailed_callback = new AutoReplyPrint.CP_OnPortOpenFailedEvent_Callback() {
        @Override
        public void CP_OnPortOpenFailedEvent(Pointer handle, String name, Pointer private_data) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btn_connect.setText(R.string.bt_connect_printer);
                    btn_connect.setEnabled(true);
                    MyApplication.showToast(getString(R.string.bt_connect_failed));
                }
            });
        }
    };
    AutoReplyPrint.CP_OnPortClosedEvent_Callback closed_callback = new AutoReplyPrint.CP_OnPortClosedEvent_Callback() {
        @Override
        public void CP_OnPortClosedEvent(Pointer h, Pointer private_data) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    closeFKPrinter(h);
                    MyApplication.showToast(getString(R.string.bt_connect_closed));
                }
            });
        }
    };

    private void closeFKPrinter(Pointer pointer) {
        if (pointer != Pointer.NULL) {
            AutoReplyPrint.INSTANCE.CP_Port_Close(pointer);
            pointer = Pointer.NULL;
            pointer = null;
        }
    }

    public void tofinish() {
        setResult(MainActivity.RESULT_CODE);
        finish();
    }
}
