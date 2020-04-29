package com.e.printtextdemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

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
                    startActivity(new Intent(MainActivity.this, SelectPrinterActivity.class));
                    break;
                case R.id.print_text:
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
            MyApplication.showLoading(this, "");
            PrintUtil.printHY(this, null);
        } else if (2 == MyApplication.currentPrintType) {
            MyApplication.showLoading(this, "");
            PrintUtil.printAY(this);
        } else {
            MyApplication.showToast("请先选择打印机");
        }
    }
}
