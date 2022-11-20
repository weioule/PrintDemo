package com.e.printtextdemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.e.printtextdemo.MyApplication;
import com.e.printtextdemo.NoDoubleClickListener;
import com.e.printtextdemo.R;
import com.e.printtextdemo.model.FoodBean;
import com.e.printtextdemo.model.OrderBean;
import com.e.printtextdemo.utils.PrintUtil;
import com.e.printtextdemo.utils.TemplateUtil;
import com.weioule.receiptprint.ReceiptPrintUtil;
import com.weioule.receiptprint.bean.OriginalDataBean;

import java.util.ArrayList;


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

        findViewById(R.id.connect_and_prints).setOnClickListener(listener);
        findViewById(R.id.connect_and_print).setOnClickListener(listener);
        findViewById(R.id.select_printer).setOnClickListener(listener);
        findViewById(R.id.print_text).setOnClickListener(listener);
        findViewById(R.id.mask_hint).setOnClickListener(listener);
    }

    private NoDoubleClickListener listener = new NoDoubleClickListener() {
        @Override
        public void onNoDoubleClick(View view) {
            switch (view.getId()) {
                case R.id.connect_and_print:
                    ReceiptPrintUtil.connectAndPrint(MainActivity.this, TemplateUtil.getTemplate(getOrderInfo()));
                    break;
                case R.id.connect_and_prints:
                    ArrayList<OriginalDataBean> list = new ArrayList<>();
                    for (int i = 0; i < 5; i++) {
                        OriginalDataBean dataBean = new OriginalDataBean();
                        dataBean.setPrintLineInfoBeanList(TemplateUtil.getTemplate(getOrderInfo()));
                        list.add(dataBean);
                    }

                    ReceiptPrintUtil.connectAndPrintList(MainActivity.this, list);
                    break;
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

    private OrderBean getOrderInfo() {
        OrderBean bean = new OrderBean();
        bean.setOrderCode("20211212155816000001");
        bean.setCreateTime(SystemClock.currentThreadTimeMillis());
        bean.setReceiveMan("小明");
        bean.setReceiveMobile("177****8718");
        bean.setReceiveAddress("上海市杨浦区政立路485号哔哩哔哩大厦5楼");
        bean.setExpectedReach("2022-10-01:18:00");
        bean.setBusinessPhone("800-820-8820");
        bean.setRemark("微微辣，可以微麻，多加一点香菜，谢谢！");
        bean.setTotal("888888");

        ArrayList<FoodBean> ls = new ArrayList<>();
        ArrayList<String> order = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            FoodBean dto = new FoodBean();

            StringBuffer sb = new StringBuffer("红烧肉");
            for (int j = 0; j < i; j++) {
                sb.append("红烧肉");
            }
            dto.setName(sb.toString());

            if (i % 2 == 0)
                dto.setCount(100 + i);
            else
                dto.setCount(10000 + i);

            if (i % 2 == 0)
                dto.setPrice(80 + i);
            else
                dto.setPrice(8000 + i);

            ls.add(dto);

            order.add("1234567890" + i);
        }
        bean.setFoodList(ls);
        return bean;
    }
}
