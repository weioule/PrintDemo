package com.e.printtextdemo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * Author by weioule.
 * Date on 2018/9/5.
 */
public class ToastUtil {

    private Toast mToast;
    private int mResIdLast;
    private String mTextLast;
    private Context mContext;
    private long mShowTimeLast;
    private static ToastUtil instance;

    private ToastUtil() {
    }

    public static ToastUtil getInstance() {
        if (instance == null) {
            instance = new ToastUtil();
        }
        return instance;
    }

    public void show(int resId) {
        show(resId, Toast.LENGTH_SHORT);
    }

    public void show(final int resId, final int duration) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                if (resId <= 0) {
                    return;
                }

                if (resId == mResIdLast && System.currentTimeMillis() - mShowTimeLast < 2000) {
                    return;
                }

                if (mContext == null) {
                    mContext = MyApplication.appContext;
                }

                if (null == mToast) {
                    mToast = Toast.makeText(mContext, resId, duration);
                } else {
                    mToast.setText(resId);
                    mToast.setDuration(duration);
                }

//        mToast.setGravity(Gravity.CENTER, 0, 0);
                mToast.show();
                mShowTimeLast = System.currentTimeMillis();
                mResIdLast = resId;
            }
        });
    }

    public void show(String strText) {
        show(strText, Toast.LENGTH_SHORT);
    }

    public void show(final String strText, final int duration) {

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            private String mStr;

            @Override
            public void run() {
                if (TextUtils.isEmpty(strText)) {
                    return;
                }

                if (strText.equals(mTextLast) && System.currentTimeMillis() - mShowTimeLast < 2000) {
                    return;
                }

                if (strText.length() > 100) {
                    mStr = strText.substring(0, 100) + "...";
                } else {
                    mStr = strText;
                }

                if (mContext == null) {
                    mContext = MyApplication.appContext;
                }

                if (null == mToast) {
                    mToast = Toast.makeText(mContext, mStr, duration);
                } else {
                    mToast.setText(mStr);
                    mToast.setDuration(duration);
                }

//        mToast.setGravity(Gravity.CENTER, 0, 0);
                mToast.show();
                mShowTimeLast = System.currentTimeMillis();
                mTextLast = strText;
            }
        });
    }
}
