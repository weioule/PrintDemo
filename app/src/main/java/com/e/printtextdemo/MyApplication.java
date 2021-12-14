package com.e.printtextdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.view.Window;
import android.widget.TextView;

import com.e.printtextdemo.activity.MainActivity;
import com.e.printtextdemo.utils.ToastUtil;

/**
 * Created by weioule
 * on 2020/1/1
 */
public class MyApplication extends Application {
    //自定义内容加载提示窗
    private static AlertDialog loadingDialog;
    private static ToastUtil toastUtil;
    public static Context appContext;
    private Activity activity;

    public static int currentPrintType;//打印机类型：1 汉印   2 爱印   3 复坤
    public static String currentPrinAddress;//打印机mac地址

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
    }

    public void setMainActivity(MainActivity activity) {
        this.activity = activity;
    }

    public Activity getMainActivity() {
        return activity;
    }

    public static void showLoading(Activity activity, String content) {
        hideLoading();
        loadingDialog = new AlertDialog.Builder(activity, R.style.alert_dialog).create();
        loadingDialog.setCancelable(false);
        if (!activity.isFinishing()) {
            loadingDialog.show();
            Window window = loadingDialog.getWindow();
            window.setContentView(R.layout.layout_dialog_progress);
            if (!TextUtils.isEmpty(content))
                ((TextView) window.findViewById(R.id.contentView)).setText(content);
        }
    }

    /*******
     * 关闭loading
     */
    public static void hideLoading() {
        if (loadingDialog != null) {
            if (loadingDialog.isShowing()) {
                try {
                    loadingDialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        loadingDialog = null;
    }

    public static void showToast(int id) {
        if (null == toastUtil) {
            toastUtil = ToastUtil.getInstance();
        }

        toastUtil.show(id);
    }

    public static void showToast(String msg) {
        if (null == toastUtil) {
            toastUtil = ToastUtil.getInstance();
        }

        toastUtil.show(msg);
    }
}
