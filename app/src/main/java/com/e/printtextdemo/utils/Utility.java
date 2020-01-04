/**
 * Copyright (C) 2015 mvp dev demo
 * Company:  wondertek
 * Data:   2016.03.10
 * Description:通用工具集
 * Author: Jimsu
 * <p>
 * Fix history:
 **/
package com.e.printtextdemo.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("deprecation")
public class Utility {
    private final static String DEBUG_TAG = "Utility";

    /**
     * 获取屏幕的宽度
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }


    /**
     * 获取屏幕的高度,不包含状态栏和虚拟导航栏的高度
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        return metrics.heightPixels;
    }


    /**
     * 获取精确的屏幕高
     */
    public static int getAccurateScreenHeight(Activity activity) {
        int screenH = 0;
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        try {
            Class<?> c = Class.forName("android.view.Display");
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, dm);
            screenH = dm.heightPixels;
            //宽度：dm.widthPixels
        } catch (Exception e) {
            e.printStackTrace();
        }
        return screenH;
    }


    /**
     * 判断网络
     *
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(@NonNull Context context) {
        // return true;
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static long lastClickTime;

    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 800) {
            return true;
        }
        lastClickTime = time;
        return false;
    }


    public static boolean isFastDoubleClick2() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 3000) {
            return true;
        }
        lastClickTime = time;
        return false;
    }


    // 获得屏幕高宽
    @NonNull
    public static Point getScreenPoint(@NonNull Activity activity) {
        WindowManager windowManager = activity.getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        @SuppressWarnings("deprecation")
        Point point = new Point(display.getWidth(), display.getHeight());
        return point;
    }

    public static int getsW(@NonNull Activity a) {
        return getScreenPoint(a).x;
    }

    public static int getsH(@NonNull Activity a) {
        return getScreenPoint(a).y;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dp2px(@NonNull Context context, float dpValue) {
        final DisplayMetrics dm = context.getResources().getDisplayMetrics();
        final float scale = dm.density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dp(@NonNull Context context, float pxValue) {
        final DisplayMetrics dm = context.getResources().getDisplayMetrics();
        final float scale = dm.density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 sp 的单位 转成为 px(像素)
     */
    public static int sp2px(@NonNull Context context, float spValue) {
        final DisplayMetrics dm = context.getResources().getDisplayMetrics();
        final float scale = dm.scaledDensity;
        return (int) (spValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 sp
     */
    public static int px2sp(@NonNull Context context, float pxValue) {
        final DisplayMetrics dm = context.getResources().getDisplayMetrics();
        final float scale = dm.scaledDensity;
        return (int) (pxValue / scale + 0.5f);
    }

    // 隐藏软键盘
    public static void hiddenKeyboard(@NonNull Activity activity) {
        ((InputMethodManager) activity
                .getSystemService(Activity.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(activity.getCurrentFocus()
                        .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * 分享
     **/
    public static void share(@NonNull Activity mActivity, String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "分享");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mActivity.startActivity(Intent.createChooser(intent,
                mActivity.getTitle()));
    }

    /**
     * 拨打电话
     *
     * @param phonenumber
     */
    public static void tel(@NonNull Activity a, String phonenumber) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phonenumber));
        a.startActivity(intent);
    }

    // /*
    // * 要登录用户
    // */
    // public static void dialogShow(final Context context) {
    // new AlertDialog.Builder(context)
    // .setTitle("提示")
    // .setMessage("请登录用户")
    // .setPositiveButton("确定", new DialogInterface.OnClickListener() {
    //
    // @Override
    // public void onClick(DialogInterface dialog, int which) {
    // Intent intent = new Intent((Activity) context,
    // LoginActivity.class);
    // ((Activity) context).startActivity(intent);
    // }
    // })
    // .setNegativeButton("取消", new DialogInterface.OnClickListener() {
    //
    // @Override
    // public void onClick(DialogInterface dialog, int which) {
    // dialog.dismiss();
    // }
    // }).show();
    // }
    //
    // /** 提示 **/
    // public static void showAlert(final Activity a, String showString,
    // final IAlert listener) {
    // if (showString == null) {
    // return;
    // }
    //
    // play {
    // if (a.isFinishing()) {
    // return;
    // }
    // if (listener == null) {
    // new AlertDialog.Builder(a).setTitle("提示")
    // .setMessage(showString).setCancelable(false)
    // .setPositiveButton("确定", null).show();
    // } else {
    // new AlertDialog.Builder(a)
    // .setTitle("提示")
    // .setMessage(showString)
    // .setCancelable(false)
    // .setPositiveButton("确定",
    // new DialogInterface.OnClickListener() {
    //
    // @Override
    // public void onClick(DialogInterface dialog,
    // int which) {
    //
    // listener.addAlertListener();
    // }
    // }).show();
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }
    //
    // /** 提示退出 **/
    // public static void showAlertExit(final Activity a, String showString,
    // final IAlert listener) {
    // if (showString == null) {
    // return;
    // }
    //
    // play {
    // if (a.isFinishing()) {
    // return;
    // }
    // if (listener == null) {
    // new AlertDialog.Builder(a).setTitle("提示")
    // .setMessage(showString).setCancelable(false)
    // .setPositiveButton("确定", null).show();
    // } else {
    // new AlertDialog.Builder(a)
    // .setTitle("提示")
    // .setMessage(showString)
    // .setCancelable(false)
    // .setPositiveButton("确定",
    // new DialogInterface.OnClickListener() {
    //
    // @Override
    // public void onClick(DialogInterface dialog,
    // int which) {
    // listener.addAlertListener();
    // }
    // })
    // .setNegativeButton("取消",
    // new DialogInterface.OnClickListener() {
    //
    // @Override
    // public void onClick(DialogInterface dialog,
    // int which) {
    // listener.calelAlertListener();
    // }
    // }).show();
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }

    /* 验证手机号是否合法 */
    public static boolean checkPhone(@NonNull String phone) {
        // ^(13|15|18)\\d{9}$
        Pattern pattern = Pattern.compile("^(13|14|15|18)\\d{9}$");
        Matcher matcher = pattern.matcher(phone);
        return matcher.matches();
    }

    public static boolean isEmail(@NonNull String strEmail) {
        Pattern pattern = Pattern
                .compile("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$");
        Matcher mc = pattern.matcher(strEmail);
        return mc.matches();
    }

    public static boolean isPassword(@NonNull String strPassword) {
        // Pattern pattern = Pattern.compile("^(\\w){6,20}$");
        // Matcher mc = pattern.matcher(strPassword);
        // return mc.matches();
        if (strPassword.length() == 0)
            return false;
        return true;
    }

    // 验证固定电话号码
    public static boolean isPhone(@NonNull String strPhone) {
        boolean tag = true;
        String reg = "^((0\\d{2,3})-)(\\d{7,8})(-(\\d{3,}))?$";
        final Pattern pattern = Pattern.compile(reg);
        final Matcher mat = pattern.matcher(strPhone);
        if (!mat.find()) {
            tag = false;
        }
        return tag;
    }

    // 验证手机号码
    public static boolean isMobile(@NonNull String strMobile) {
        boolean tag = true;
        String reg = "^13[0-9]{1}[0-9]{8}$|^15[012356789]{1}[0-9]{8}$|^18[0256789]{1}[0-9]{8}$"; // 验证手机号码
        // String reg =
        // "^(\\d{2,4}[-_－—]?)?\\d{3,8}([-_－—]?\\d{3,8})?([-_－—]?\\d{1,7})?$)|(^0?1[35]\\d{9}$";
        final Pattern pattern = Pattern.compile(reg);
        final Matcher mat = pattern.matcher(strMobile);
        if (!mat.find()) {
            tag = false;
        }
        return tag;
    }

    // 检查邮编格式
    public static boolean isPostcode(@NonNull String code) {
        boolean tag = true;
        String reg = "^[1-9][0-9]{5}$";
        final Pattern pattern = Pattern.compile(reg);
        final Matcher mat = pattern.matcher(code);
        if (!mat.find()) {
            tag = false;
        }
        return tag;
    }

    // 检查帐号格式
    public static boolean isAccount(@NonNull String account) {
        boolean tag = true;
        if (account.length() == 0)
            tag = false;
        return tag;
        // String reg = "^[0-9a-zA-Z_\u4e00-\u9fa5]{2,15}$";
        // final Pattern pattern = Pattern.compile(reg);
        // final Matcher mat = pattern.matcher(account);
        // if (!mat.find()) {
        // tag = false;
        // }
        // return tag;
    }

    public static void share(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setItems(new String[]{"新浪微博", "腾讯微博", "微信", "QQ"},
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }

    @NonNull
    public static ArrayList<Activity> activities = new ArrayList<Activity>();

    @NonNull
    public static ArrayList<Activity> getActivityList() {
        return activities;
    }

    /**
     * 根据*换行，为html代码
     *
     * @param desc
     * @return
     */
    @Nullable
    public static String convert(@Nullable String desc) {
        String descFormat = "";
        if (desc != null) {
            String[] descs = desc.trim().split("\\*");

            if (descs != null) {
                for (int i = 0; i < descs.length; i++) {
                    if (i != descs.length - 1) {
                        if (!"".equals(descs[i]))
                            descFormat += "*" + descs[i] + "<br>";
                    } else {
                        if (!"".equals(descs[i]))
                            descFormat += "*" + descs[i];
                    }
                }

                if (descs.length == 1) {
                    descFormat = desc;
                }
            }
        }

        return descFormat;
    }

    /**
     * make true input string is number
     *
     * @param str
     * @return
     */
    public static boolean isNumeric(@Nullable String str) {
        final String number = "0123456789";

        if (str == null || str.equals("")) {
            return false;
        }

        for (int i = 0; i < str.length(); i++) {
            if (number.indexOf(str.charAt(i)) == -1) {
                return false;
            }
        }
        return true;
    }

    /**
     * 打开文件
     */
    public static void openOnlineFile(@NonNull String url, @NonNull Context context) {

        File file = new File(url);

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // 设置intent的Action属性
        intent.setAction(Intent.ACTION_VIEW);
        // 获取文件file的MIME类型
        String type = getMIMEType(file);
        // 设置intent的data和Type属性。
        intent.setDataAndType(Uri.parse(url), type);
        // 跳转
        context.startActivity(intent);

    }

    /**
     * 根据文件后缀名获得对应的MIME类型。
     *
     * @param file
     */
    private static String getMIMEType(@NonNull File file) {

        String type = "*/*";
        String fName = file.getName();
        // 获取后缀名前的分隔符"."在fName中的位置。
        int dotIndex = fName.lastIndexOf(".");
        if (dotIndex < 0) {
            return type;
        }
        /* 获取文件的后缀名 */
        String end = fName.substring(dotIndex, fName.length()).toLowerCase();
        if (end == "")
            return type;
        // 在MIME和文件类型的匹配表中找到对应的MIME类型。
        for (int i = 0; i < MIME_MapTable.length; i++) { // MIME_MapTable??在这里你一定有疑问，这个MIME_MapTable是什么？
            if (end.equals(MIME_MapTable[i][0]))
                type = MIME_MapTable[i][1];
        }
        return type;
    }

    private static final String[][] MIME_MapTable = {
            // {后缀名，MIME类型}
            {".3gp", "video/3gpp"},
            {".apk", "application/vnd.android.package-archive"},
            {".asf", "video/x-ms-asf"},
            {".avi", "video/x-msvideo"},
            {".bin", "application/octet-stream"},
            {".bmp", "image/bmp"},
            {".c", "text/plain"},
            {".class", "application/octet-stream"},
            {".conf", "text/plain"},
            {".cpp", "text/plain"},
            {".doc", "application/msword"},
            {".docx",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
            {".xls", "application/vnd.ms-excel"},
            {".xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
            {".exe", "application/octet-stream"},
            {".gif", "image/gif"},
            {".gtar", "application/x-gtar"},
            {".gz", "application/x-gzip"},
            {".h", "text/plain"},
            {".htm", "text/html"},
            {".html", "text/html"},
            {".jar", "application/java-archive"},
            {".java", "text/plain"},
            {".jpeg", "image/jpeg"},
            {".jpg", "image/jpeg"},
            {".js", "application/x-javascript"},
            {".log", "text/plain"},
            {".m3u", "audio/x-mpegurl"},
            {".m4a", "audio/mp4a-latm"},
            {".m4b", "audio/mp4a-latm"},
            {".m4p", "audio/mp4a-latm"},
            {".m4u", "video/vnd.mpegurl"},
            {".m4v", "video/x-m4v"},
            {".mov", "video/quicktime"},
            {".mp2", "audio/x-mpeg"},
            {".mp3", "audio/x-mpeg"},
            {".mp4", "video/mp4"},
            {".mpc", "application/vnd.mpohun.certificate"},
            {".mpe", "video/mpeg"},
            {".mpeg", "video/mpeg"},
            {".mpg", "video/mpeg"},
            {".mpg4", "video/mp4"},
            {".mpga", "audio/mpeg"},
            {".msg", "application/vnd.ms-outlook"},
            {".ogg", "audio/ogg"},
            {".pdf", "application/pdf"},
            {".png", "image/png"},
            {".pps", "application/vnd.ms-powerpoint"},
            {".ppt", "application/vnd.ms-powerpoint"},
            {".pptx",
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation"},
            {".prop", "text/plain"}, {".rc", "text/plain"},
            {".rmvb", "audio/x-pn-realaudio"}, {".rtf", "application/rtf"},
            {".sh", "text/plain"}, {".tar", "application/x-tar"},
            {".tgz", "application/x-compressed"}, {".txt", "text/plain"},
            {".wav", "audio/x-wav"}, {".wma", "audio/x-ms-wma"},
            {".wmv", "audio/x-ms-wmv"},
            {".wps", "application/vnd.ms-works"}, {".xml", "text/plain"},
            {".z", "application/x-compress"},
            {".zip", "application/x-zip-compressed"}, {"", "*/*"}};

    public static String getMapString(@Nullable Map<String, Object> map, String key) {
        if (map == null)
            return "";
        if (map.containsKey(key)) {
            Object obj = map.get(key);
            if (obj != null && obj instanceof String) {
                return obj.toString();
            }
        } else {
            Log.i("mapKeyNull", key);
        }
        return "";
    }

    public static int getMapInt(@NonNull Map<String, Object> map, String key) {
        if (map.containsKey(key)) {
            Object obj = map.get(key);
            if (obj != null && obj instanceof Integer) {
                return ((Integer) obj).intValue();
            }
        } else {
            Log.i("mapKeyNull", key);
        }
        return 0;
    }

    public static long getMapLong(@NonNull Map<String, Object> map, String key) {
        if (map.containsKey(key)) {
            Object obj = map.get(key);
            if (obj != null && obj instanceof Long) {
                return ((Long) obj).longValue();
            }
        } else {
            Log.i("mapKeyNull", key);
        }
        return 0;
    }

    public static boolean getMapBoolean(@NonNull Map<String, Object> map, String key) {
        if (map.containsKey(key)) {
            Object obj = map.get(key);
            if (obj != null && obj instanceof Boolean) {
                return ((Boolean) obj).booleanValue();
            }
        } else {
            Log.i("mapKeyNull", key);
        }
        return false;
    }

    public static String getCurrentTime(@NonNull String format) {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        String currentTime = sdf.format(date);
        return currentTime;
    }

    public static String getCurrentTime() {
        return getCurrentTime("yyyy-MM-dd  HH:mm:ss");
    }

    public static String getFormatedDateTime(@NonNull String pattern, long dateTime) {
        SimpleDateFormat sDateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
        return sDateFormat.format(new Date(dateTime + 0));
    }

    /**
     * make true current connect service is wifi
     *
     * @param mContext
     * @return
     */
    public static boolean isWIFIProviderAvaliable(@NonNull Context mContext) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null
                && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    /**
     * drawable to bitmap
     *
     * @param drawable
     * @return
     */
    @Nullable
    public static Bitmap drawable2Bitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof NinePatchDrawable) {
            Bitmap bitmap = Bitmap
                    .createBitmap(
                            drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight(),
                            drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                    : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);

            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            drawable.draw(canvas);

            return bitmap;
        } else {
            return null;
        }
    }


    /**
     * String to int
     *
     * @param intS
     * @return
     */
    public static int getIntFromString(final String intS) {
        int retInt = 0;

        try {
            retInt = Integer.parseInt(intS);
        } catch (NumberFormatException e) {

        }

        return retInt;
    }

    /**
     * String to long
     *
     * @param longS
     * @return
     */
    public static long getLongFromString(final String longS) {
        long retLong = 0;

        try {
            retLong = Long.parseLong(longS);
        } catch (NumberFormatException e) {

        }

        return retLong;
    }

    /**
     * 把时间字符串转换成Date
     *
     * @param date
     * @param dateParttens
     * @return
     * @throws Exception
     */
    @Nullable
    public static Date parseDate(String date, @NonNull String[] dateParttens)
            throws Exception {
        SimpleDateFormat df = null;
        Date d = null;
        boolean isParse = false;
        for (String partten : dateParttens) {
            df = new SimpleDateFormat(partten, Locale.getDefault());
            try {
                d = df.parse(date);
                isParse = true;
                break;
            } catch (ParseException e) {
                isParse = false;
            }
        }
        if (!isParse) {
            throw new Exception();
        }
        return d;
    }

    /**
     * 把时间字符串转换成Date
     *
     * @param date
     * @return
     * @throws Exception
     */
    @Nullable
    public static Date parseDate(String date) throws Exception {
        String[] datePartten = {"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd",
                "yyyy/MM/dd HH:mm;ss", "yyyy/MM/dd", "yyyyMMddHHmmss",
                "yyyyMMdd"};
        return parseDate(date, datePartten);
    }

    /**
     * yyyyMMddHHmmss
     *
     * @param c
     * @return
     */
    @NonNull
    public static String getFormatTime3(@NonNull Calendar c) {
        DecimalFormat df = new DecimalFormat("00");
        String strFileName = df.format((c.get(Calendar.MONTH) + 1)) + "-"
                + df.format(c.get(Calendar.DAY_OF_MONTH)) + " "
                + df.format(c.get(Calendar.HOUR_OF_DAY)) + ":"
                + df.format(c.get(Calendar.MINUTE));

        return strFileName;
    }

    /**
     * MM-dd HH:mm
     *
     * @param time
     * @return
     */
    @NonNull
    public static String getJiemuTime(long time) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        String strFileName = getFormatTime3(c);
        return strFileName;
    }

    /**
     * 判断字符是否字母,数字开头
     *
     * @param content
     * @return
     */
    public static boolean isStringBeginWithIndex(@NonNull final String content) {
        if (TextUtils.isEmpty(content)) {
            return false;
        }

        final String firstString = content.substring(0, 1);

        if (TextUtils.isEmpty(firstString)) {
            return false;
        }

        Log.d(DEBUG_TAG, "== firstString=" + firstString);

        Pattern p = Pattern.compile("[0-9]*");
        Matcher m = p.matcher(firstString);

        if (m.matches()) {
            return true;
        }

        p = Pattern.compile("[a-z]");
        m = p.matcher(firstString);

        if (m.matches()) {
            return true;
        }

        p = Pattern.compile("[A-Z]");
        m = p.matcher(firstString);

        if (m.matches()) {
            return true;
        }

        return false;
    }

    public static String getMd52(@NonNull String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }

        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
//				if (temp.length() == 1);
//					temp = "0" + temp;
                result += temp;
            }
            return result.toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getMd5(@NonNull String str) {
        try {
            // 生成一个MD5加密计算摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 计算md5函数
            md.update(str.getBytes());
            // digest()最后确定返回md5 hash值，返回值为8为字符串。因为md5 hash值是16位的hex值，实际上就是8位的字符
            // BigInteger函数则将8位的字符串转换成16位hex值，用字符串来表示；得到字符串形式的hash值
            String md5 = new BigInteger(1, md.digest()).toString(16);
            //BigInteger会把0省略掉，需补全至32位
            return fillMD5(md5);
        } catch (Exception e) {
            throw new RuntimeException("MD5加密错误:" + e.getMessage(), e);
        }
    }

    public static String fillMD5(String md5) {
        return md5.length() == 32 ? md5 : fillMD5("0" + md5);
    }

    public static String getRandom() {
        int max = 10000;
        int min = 1000;
        Random random = new Random();
        return random.nextInt(max) % (max - min + 1) + min + "";
    }

    private static final String SP_NAME = "vipmooc";

    private static SharedPreferences sp;

    public static boolean setPreference(@NonNull Context context, String key, String value) {
        if (sp == null) {
            sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        }

        SharedPreferences.Editor mEditor = sp.edit();
        mEditor.putString(key, value);
        return mEditor.commit();
    }

    @Nullable
    public static String getPreference(@NonNull Context context, String key, String defValue) {
        if (sp == null) {
            sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        }

        return sp.getString(key, defValue);
    }

    //清空sp
    public static void cleanSp() {
        if (sp != null) {
            sp.edit().clear().commit();
        }
    }


    public static void saveBooleanValue(Context context, String key, boolean value) {
        if (sp == null) {
            sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        }
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static Boolean getBooleanValue(Context context, String key) {
        if (sp == null) {
            sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        }
        return sp.getBoolean(key, false);
    }

    public static Boolean getBooleanValue(Context context, String key, boolean defValue) {
        if (sp == null) {
            sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        }
        return sp.getBoolean(key, defValue);
    }


    public static void saveIntValue(Context context, String key, int value) {
        if (sp == null) {
            sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        }
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static int getIntValue(Context context, String key) {
        if (sp == null) {
            sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        }
        return sp.getInt(key, 0);
    }

    public static void saveLongValue(Context context, String key, long value) {
        if (sp == null) {
            sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        }
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public static Long getLongValue(Context context, String key) {
        if (sp == null) {
            sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        }
        return sp.getLong(key, -1);
    }

    /**
     * 把网络资源图片转化成bitmap
     *
     * @param url 网络资源图片
     * @return Bitmap
     */
    public static Bitmap GetNetBitmap(String url) {
        Bitmap bitmap = null;
        InputStream in = null;
        BufferedOutputStream out = null;
        try {
            in = new BufferedInputStream(new URL(url).openStream(), 1024);
            final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
            out = new BufferedOutputStream(dataStream, 1024);
            copy(in, out);
            out.flush();
            byte[] data = dataStream.toByteArray();
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            data = null;
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void copy(InputStream in, OutputStream out)
            throws IOException {
        byte[] b = new byte[1024];
        int read;
        while ((read = in.read(b)) != -1) {
            out.write(b, 0, read);
        }
    }

    /**
     * 获取本周一零点
     *
     * @return
     */
    public static long getTimesWeekmorning() {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        if (cal.get(Calendar.DAY_OF_WEEK) == 1) {
            cal.add(Calendar.DATE, -7);
        }
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
//        LogUtil.w(sdf.format(cal.getTime()));
        return cal.getTimeInMillis();
    }

    public static long getZeroTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        //当天0点
        return calendar.getTimeInMillis();
    }
}
