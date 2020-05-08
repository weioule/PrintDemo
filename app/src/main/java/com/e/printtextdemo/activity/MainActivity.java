package com.e.printtextdemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.e.printtextdemo.MyApplication;
import com.e.printtextdemo.NoDoubleClickListener;
import com.e.printtextdemo.R;
import com.e.printtextdemo.utils.PrintUtil;


/**
 * Created by weioule
 * on 2020/1/1
 */
public class MainActivity extends AppCompatActivity {

    private final static int REQUEST_CODE = 800;
    public final static int RESULT_CODE = 900;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.select_printer).setOnClickListener(listener);
        findViewById(R.id.print_text).setOnClickListener(listener);
        findViewById(R.id.mask_hint).setOnClickListener(listener);
    }

    private NoDoubleClickListener listener = new NoDoubleClickListener() {
        @Override
        public void onNoDoubleClick(View view) {
            switch (view.getId()) {
                case R.id.select_printer:
                    startActivityForResult(new Intent(MainActivity.this, SelectPrinterActivity.class), REQUEST_CODE);
                    break;
                case R.id.print_text:
                    MyApplication.showLoading(MainActivity.this, "");
                    print();
                    break;
                case R.id.mask_hint:
                    startActivity(new Intent(MainActivity.this, MaskHintActivity.class));
                    break;
            }
        }
    };

    private void print() {
        if (1 == MyApplication.currentPrintType) {
            PrintUtil.printHY(this, null);
        } else if (2 == MyApplication.currentPrintType) {
            PrintUtil.printAY(this);
        } else if (3 == MyApplication.currentPrintType) {
            PrintUtil.printFK(this);
        } else {
            MyApplication.hideLoading();
            MyApplication.showToast("请先选择打印机");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_CODE)
            MyApplication.showToast(getString(R.string.bt_connect_success));
    }
}
