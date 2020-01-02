package com.e.printtextdemo;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import HPRTAndroidSDK.HPRTPrinterHelper;
import print.Print;


/**
 * Created by weioule
 * on 2020/1/1
 */
public class MainActivity extends AppCompatActivity implements ConnectPrinterDialog.DismissListener {

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.print).setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                printPrepare(null);
            }
        });
    }

    public void printPrepare(OrderBean data) {
        if (!EnableBluetooth()) return;
        if (!Print.IsOpened()) {
            new ConnectPrinterDialog(this).show(this);
        } else if (!isConnect()) {
            ToastUtil.getInstance().show(getString(R.string.abnormal_printer_status));
            new ConnectPrinterDialog(this).show(this);
        } else {
            print(data);
        }
    }

    private void print(final OrderBean data) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    PrintUtil.printText(data);
                } catch (Exception e) {
                }
            }
        });
    }

    //EnableBluetooth
    private boolean EnableBluetooth() {
        boolean bRet = false;
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isEnabled())
                return true;
            mBluetoothAdapter.enable();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!mBluetoothAdapter.isEnabled())
                bRet = true;
            else
                ToastUtil.getInstance().show(getString(R.string.please_on_the_bluetooth_first));
        } else {
            ToastUtil.getInstance().show(getString(R.string.get_bluetooth_status_exception));
            Log.d("EnableBluetooth", (new StringBuilder("PickFragment --> EnableBluetooth() ").append("Bluetooth Adapter is null.")).toString());
        }
        return bRet;
    }


    private boolean isConnect() {
        String status = "";
        try {
            byte[] statusData = HPRTPrinterHelper.GetRealTimeStatus((byte) HPRTPrinterHelper.PRINTER_REAL_TIME_STATUS_ITEM_PRINTER);

            for (byte statusDatum : statusData) {
                status += statusDatum;
            }

        } catch (Exception e) {
            Log.d("HPRTSDKSample", (new StringBuilder("Activity_Status --> Refresh ")).append(e.getMessage()).toString());
        }

        return "18".equals(status) ? true : false;
    }

    @Override
    public void dismiss() {
        print(null);
    }
}
