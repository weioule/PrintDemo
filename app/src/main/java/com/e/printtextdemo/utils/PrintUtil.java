package com.e.printtextdemo.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.SystemClock;

import com.android_print_sdk.Barcode;
import com.android_print_sdk.bluetooth.BluetoothPrinter;
import com.e.printtextdemo.MyApplication;
import com.e.printtextdemo.R;
import com.e.printtextdemo.activity.SelectPrinterActivity;
import com.e.printtextdemo.model.FoodBean;
import com.e.printtextdemo.model.OrderBean;
import com.github.promeg.pinyinhelper.Pinyin;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import HPRTAndroidSDK.HPRTPrinterHelper;
import print.Print;


/**
 * 蓝牙打印工具类
 */
public class PrintUtil {

    //加粗模式
    public static final int FONT_BOLD = 0;         // 字体加粗
    public static final int FONT_BOLD_CANCEL = 1;  // 取消加粗

    //字体大小
    public static final int FONT_NORMAL = 0;    // 正常
    public static final int FONT_MIDDLE = 1;    // 中等
    public static final int FONT_MIDDLE_ = 3;   // 中小
    public static final int FONT_BIG = 2;       // 大

    // 对齐方式
    public static final int ALIGN_LEFT = 0;     // 靠左
    public static final int ALIGN_CENTER = 1;   // 居中
    public static final int ALIGN_RIGHT = 2;    // 靠右

    private OutputStreamWriter mWriter = null;
    private OutputStream mOutputStream = null;

    public final static int WIDTH_PIXEL = 384;
    public final static int IMAGE_SIZE = 320;
    public final static int LEFT_MAX_SIZE = 240;

    /**
     * 打印换行
     *
     * @return length 需要打印的空行数
     */
    public void printLine(int lineNum) {
        for (int i = 0; i < lineNum; i++) {
            printHY("\n");
        }
    }

    /**
     * 打印换行(只换一行)
     */
    public void printLine() {
        printLine(1);
    }

    /**
     * 打印空白(一个Tab的位置，约4个汉字)
     *
     * @param length 需要打印空白的长度,
     */
    public void printTabSpace(int length) {
        for (int i = 0; i < length; i++) {
            printHY("  ");
        }
    }

    /**
     * 绝对打印位置
     *
     * @return
     */
    public byte[] setLocation(int offset) {
        byte[] bs = new byte[4];
        bs[0] = 0x1B;
        bs[1] = 0x24;
        bs[2] = (byte) (offset % 256);
        bs[3] = (byte) (offset / 256);
        return bs;
    }


    /**
     * 字体大小
     *
     * @param fontSize
     */
    public void setFontSizeCmd(int fontSize) {
        byte[] data = {(byte) 0x1d, (byte) 0x21, (byte) 0x0};
        if (fontSize == FONT_NORMAL) {
            data[2] = (byte) 0x00;
        } else if (fontSize == FONT_MIDDLE_) {
            data[2] = (byte) 18;
        } else if (fontSize == FONT_MIDDLE) {
            data[2] = (byte) 0x01;
        } else if (fontSize == FONT_BIG) {
            data[2] = (byte) 0x11;
        }

        try {
            Print.WriteData(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加粗模式
     *
     * @param fontBold
     */
    public void setFontBoldCmd(int fontBold) {
        byte[] data = {(byte) 0x1b, (byte) 0x45, (byte) 0x0};

        if (fontBold == FONT_BOLD) {
            data[2] = (byte) 0x01;
        } else if (fontBold == FONT_BOLD_CANCEL) {
            data[2] = (byte) 0x00;
        }

        try {
            Print.WriteData(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 对齐方式
     *
     * @param alignMode
     * @return
     */
    public void setAlignCmd(int alignMode) {
        byte[] data = {(byte) 0x1b, (byte) 0x61, (byte) 0x0};
        if (alignMode == ALIGN_LEFT) {
            data[2] = (byte) 0x00;
        } else if (alignMode == ALIGN_CENTER) {
            data[2] = (byte) 0x01;
        } else if (alignMode == ALIGN_RIGHT) {
            data[2] = (byte) 0x02;
        }

        try {
            Print.WriteData(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void print(byte[] bs) {
        try {
            Print.WriteData(bs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printHY(byte[] bs) {
        try {
            Print.WriteData(bs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 打印文字
     *
     * @param text
     */
    public void printHY(String text) {
        try {
            Print.WriteData(text.getBytes("GBK"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printRawBytes(byte[] bytes) throws IOException {
        mOutputStream.write(bytes);
        mOutputStream.flush();
    }


    public byte[] getGbk(String stText) throws UnsupportedEncodingException {
        byte[] returnText = stText.getBytes("GBK"); // 必须放在try内才可以
        return returnText;
    }

    private int getStringPixLength(String str) {
        int pixLength = 0;
        char c;
        for (int i = 0; i < str.length(); i++) {
            c = str.charAt(i);
            if (Pinyin.isChinese(c)) {
                pixLength += 24;
            } else {
                pixLength += 12;
            }
        }
        return pixLength;
    }

    public int getOffset(String str) {
        return WIDTH_PIXEL - getStringPixLength(str);
    }

    //这个排版格式是可以公用的，打印机基本都是可以打印byte数组
    public byte[] printTwoColumn(String left, String middle) throws UnsupportedEncodingException {
        int iNum = 0;
        byte[] byteBuffer = new byte[100];
        byte[] tmp;

        int pixLength = getStringPixLength(left) % WIDTH_PIXEL;
        int middleLength = getStringPixLength(middle) % WIDTH_PIXEL;
        //这里的25是右边距的宽度
        int leftRemaining = WIDTH_PIXEL - middleLength - 25;

        int offset = 0;
        //计算后需要换行
        if (pixLength > LEFT_MAX_SIZE || 0 != left.length() && pixLength == 0) {
            left += "\n";
        } else if (leftRemaining < pixLength) {
            //设置两个文案间隔25
            offset = pixLength + 25;
        }

        tmp = getGbk(left);
        System.arraycopy(tmp, 0, byteBuffer, iNum, tmp.length);
        iNum += tmp.length;

        if (offset > 0)
            tmp = setLocation(offset);
        else
            tmp = setLocation(getOffset(middle));
        System.arraycopy(tmp, 0, byteBuffer, iNum, tmp.length);
        iNum += tmp.length;

        tmp = getGbk(middle);
        System.arraycopy(tmp, 0, byteBuffer, iNum, tmp.length);

        return byteBuffer;
    }

    //这个排版格式是可以公用的，打印机基本都是可以打印byte数组
    public byte[] printThreeColumn(String left, String middle, String right) throws UnsupportedEncodingException {
        int iNum = 0;
        byte[] byteBuffer = new byte[200];
        byte[] tmp = new byte[0];

        System.arraycopy(tmp, 0, byteBuffer, iNum, tmp.length);
        iNum += tmp.length;

        int pixLength = getStringPixLength(left) % WIDTH_PIXEL;
        int middleLength = getStringPixLength(middle) % WIDTH_PIXEL;
        int rightLength = getStringPixLength(right) % WIDTH_PIXEL;
        //这里的25是右边距的宽度
        int leftRemaining = WIDTH_PIXEL - middleLength - rightLength - 25;

        //计算后需要换行
        if (pixLength > LEFT_MAX_SIZE || 0 != left.length() && pixLength == 0 || leftRemaining < pixLength) {
            left += "\n";
        }

        tmp = getGbk(left);
        System.arraycopy(tmp, 0, byteBuffer, iNum, tmp.length);
        iNum += tmp.length;

        if (middle.length() + right.length() >= 12) {
            //数量与小计的字符长度超出其默认范围，则往从右往左打印 25即当做数量与小计的间隔
            tmp = setLocation(leftRemaining);
        } else {
            tmp = setLocation(240);
        }

        System.arraycopy(tmp, 0, byteBuffer, iNum, tmp.length);
        iNum += tmp.length;

        tmp = getGbk(middle);
        System.arraycopy(tmp, 0, byteBuffer, iNum, tmp.length);
        iNum += tmp.length;

        tmp = setLocation(getOffset(right));
        System.arraycopy(tmp, 0, byteBuffer, iNum, tmp.length);
        iNum += tmp.length;

        tmp = getGbk(right);
        System.arraycopy(tmp, 0, byteBuffer, iNum, tmp.length);

        return byteBuffer;
    }

    public void printDashLine() {
        printHY("--------------------------------");
    }

    public void printBitmap(Bitmap bmp) throws IOException {
        bmp = compressPic(bmp);
        byte[] bmpByteArray = draw2PxPoint(bmp);
        printRawBytes(bmpByteArray);
    }

    /*************************************************************************
     * 假设一个360*360的图片，分辨率设为24, 共分15行打印 每一行,是一个 360 * 24 的点阵,y轴有24个点,存储在3个byte里面。
     * 即每个byte存储8个像素点信息。因为只有黑白两色，所以对应为1的位是黑色，对应为0的位是白色
     **************************************************************************/
    private byte[] draw2PxPoint(Bitmap bmp) {
        //先设置一个足够大的size，最后在用数组拷贝复制到一个精确大小的byte数组中
        int size = bmp.getWidth() * bmp.getHeight() / 8 + 1000;
        byte[] tmp = new byte[size];
        int k = 0;
        // 设置行距为0
        tmp[k++] = 0x1B;
        tmp[k++] = 0x33;
        tmp[k++] = 0x00;
        // 居中打印
        tmp[k++] = 0x1B;
        tmp[k++] = 0x61;
        tmp[k++] = 1;
        for (int j = 0; j < bmp.getHeight() / 24f; j++) {
            tmp[k++] = 0x1B;
            tmp[k++] = 0x2A;// 0x1B 2A 表示图片打印指令
            tmp[k++] = 33; // m=33时，选择24点密度打印
            tmp[k++] = (byte) (bmp.getWidth() % 256); // nL
            tmp[k++] = (byte) (bmp.getWidth() / 256); // nH
            for (int i = 0; i < bmp.getWidth(); i++) {
                for (int m = 0; m < 3; m++) {
                    for (int n = 0; n < 8; n++) {
                        byte b = px2Byte(i, j * 24 + m * 8 + n, bmp);
                        tmp[k] += tmp[k] + b;
                    }
                    k++;
                }
            }
            tmp[k++] = 10;// 换行
        }
        // 恢复默认行距
        tmp[k++] = 0x1B;
        tmp[k++] = 0x32;

        byte[] result = new byte[k];
        System.arraycopy(tmp, 0, result, 0, k);
        return result;
    }

    /**
     * 图片二值化，黑色是1，白色是0
     *
     * @param x   横坐标
     * @param y   纵坐标
     * @param bit 位图
     * @return
     */
    private byte px2Byte(int x, int y, Bitmap bit) {
        if (x < bit.getWidth() && y < bit.getHeight()) {
            byte b;
            int pixel = bit.getPixel(x, y);
            int red = (pixel & 0x00ff0000) >> 16; // 取高两位
            int green = (pixel & 0x0000ff00) >> 8; // 取中两位
            int blue = pixel & 0x000000ff; // 取低两位
            int gray = RGB2Gray(red, green, blue);
            if (gray < 128) {
                b = 1;
            } else {
                b = 0;
            }
            return b;
        }
        return 0;
    }

    /**
     * 图片灰度的转化
     */
    private int RGB2Gray(int r, int g, int b) {
        int gray = (int) (0.29900 * r + 0.58700 * g + 0.11400 * b); // 灰度转化公式
        return gray;
    }

    /**
     * 对图片进行压缩（去除透明度）
     *
     * @param bitmapOrg
     */
    private Bitmap compressPic(Bitmap bitmapOrg) {
        // 获取这个图片的宽和高
        int width = bitmapOrg.getWidth();
        int height = bitmapOrg.getHeight();
        // 定义预转换成的图片的宽度和高度
        int newWidth = IMAGE_SIZE;
        int newHeight = IMAGE_SIZE;
        Bitmap targetBmp = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
        Canvas targetCanvas = new Canvas(targetBmp);
        targetCanvas.drawColor(0xffffffff);
        targetCanvas.drawBitmap(bitmapOrg, new Rect(0, 0, width, height), new Rect(0, 0, newWidth, newHeight), null);
        return targetBmp;
    }

    public static void printHY(Activity activity, OrderBean data) {
        final OrderBean[] orderBean = new OrderBean[1];
        //当打印机状态异常时，获取状态会耗时，故使用线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!Print.IsOpened() || !isConnect()) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MyApplication.hideLoading();
                            MyApplication.showToast(R.string.abnormal_printer_status);
                        }
                    });
                    return;
                }

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MyApplication.hideLoading();
                    }
                });

                if (null == data) {
                    orderBean[0] = new OrderBean();
                    orderBean[0].setTime(1577359134114l);
                    orderBean[0].setCode("008985956590840973");
                    orderBean[0].setName("小明");
                    orderBean[0].setPhone("17721358718");
                    orderBean[0].setAddress("上海市杨浦区政立路485号哔哩哔哩大厦5楼");
                    orderBean[0].setExpectedReach("18:50");
                    orderBean[0].setRemark("微微辣，可以微麻，多加一点香菜，谢谢！");
                    orderBean[0].setBusinessPhone("800-820-8820");
                    orderBean[0].setTotal("889");

                    ArrayList<FoodBean> list = new ArrayList<>();
                    for (int i = 0; i < 5; i++) {
                        FoodBean bean = new FoodBean();
                        bean.setCount(10 + i);
                        bean.setPrice(10 * i);
                        bean.setName("红烧肉");
                        list.add(bean);
                    }
                    orderBean[0].setFoodList(list);
                } else
                    orderBean[0] = data;

                try {
                    PrintUtil print = new PrintUtil();
                    print.setAlignCmd(ALIGN_CENTER);
                    print.setFontSizeCmd(FONT_BIG);
                    print.setFontBoldCmd(FONT_BOLD);
                    print.printHY("美团外卖");
                    print.printLine();
                    print.setAlignCmd(ALIGN_LEFT);
                    print.printLine();

                    print.setFontSizeCmd(FONT_NORMAL);
                    print.setFontBoldCmd(FONT_BOLD_CANCEL);
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    print.print(print.printTwoColumn("下单时间:", format.format(orderBean[0].getTime())));
                    print.printLine();

                    print.print(print.printTwoColumn("订单编号:", orderBean[0].getCode()));
                    print.printLine();

                    print.printDashLine();
                    print.printLine();

                    print.setFontBoldCmd(FONT_BOLD);
                    print.setFontSizeCmd(FONT_MIDDLE);
                    print.printHY(orderBean[0].getName());
                    print.printLine();
                    print.setFontSizeCmd(FONT_MIDDLE_);
                    print.setFontBoldCmd(FONT_BOLD_CANCEL);
                    String receiveMobile = orderBean[0].getPhone();
                    print.printHY(receiveMobile.substring(0, 3) + "****" + receiveMobile.substring(7, 11));

                    print.setFontSizeCmd(FONT_NORMAL);
                    print.setFontBoldCmd(FONT_BOLD);
                    Print.PrintAndFeedNLine((byte) 30);
                    print.printHY(orderBean[0].getAddress());
                    print.printLine();

                    Print.PrintAndFeedNLine((byte) 40);
                    print.setFontBoldCmd(FONT_BOLD_CANCEL);
                    print.printHY("预计到达：");
                    print.printLine();
                    print.setFontBoldCmd(FONT_BOLD);
                    print.printHY(orderBean[0].getExpectedReach());
                    print.printLine();

                    print.printDashLine();
                    print.printLine();

                    print.setFontBoldCmd(FONT_BOLD_CANCEL);
                    print.printHY("备注：");
                    print.setFontBoldCmd(FONT_BOLD);
                    print.printHY(orderBean[0].getRemark());
                    print.printLine();

                    print.printDashLine();
                    print.printLine();

                    print.setFontBoldCmd(FONT_BOLD_CANCEL);
                    print.print(print.printThreeColumn("商品", "数量", "小计"));
                    print.printLine();

                    print.setFontBoldCmd(FONT_BOLD);

                    List<FoodBean> beans = orderBean[0].getFoodList();
                    if (null != beans && beans.size() > 0) {
                        for (FoodBean bean : beans) {
                            print.print(print.printThreeColumn(bean.getName(), "x" + bean.getCount(), bean.getPrice() + ""));
                        }
                    }

                    print.printLine();
                    print.setAlignCmd(ALIGN_RIGHT);
                    Print.PrintAndFeedNLine((byte) 35);
                    print.printHY("订单金额：" + orderBean[0].getTotal());
                    print.printLine();
                    print.printHY("实收金额：" + orderBean[0].getTotal());
                    print.printLine();

                    print.printDashLine();
                    print.printLine();

                    print.setAlignCmd(ALIGN_CENTER);
                    print.printHY("商家电话:" + orderBean[0].getBusinessPhone());
                    print.printLine();

                    Print.PrintAndFeed(80);

                } catch (Exception e) {
                }
            }
        }).start();
    }

    public static void printAY(Activity activity) {
        //当打印机状态异常时，获取状态会耗时，故使用线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                BluetoothPrinter mPrinter = SelectPrinterActivity.mPrinter;
                if (null == mPrinter || mPrinter.getPrinterStatus() != 0) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MyApplication.hideLoading();
                            MyApplication.showToast(R.string.abnormal_printer_status);
                        }
                    });
                    return;
                }

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MyApplication.hideLoading();
                    }
                });

                PrintUtil print = new PrintUtil();

                try {
                    mPrinter.init();
                    mPrinter.setPrinter(BluetoothPrinter.COMM_ALIGN, BluetoothPrinter.COMM_ALIGN_CENTER);
                    mPrinter.setPrintModel(true, false, false, false);
                    mPrinter.setCharacterMultiple(1, 1);
                    mPrinter.printText("美团外卖\n");

                    mPrinter.setCharacterMultiple(0, 0);
                    mPrinter.setPrintModel(false, false, false, false);
                    mPrinter.setPrinter(BluetoothPrinter.COMM_ALIGN_LEFT);
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                    mPrinter.printText("\n");
                    mPrinter.printByteData(print.printTwoColumn("订单编号:", "2edfjdfndjfndfdsn\n"));
                    mPrinter.printByteData(print.printTwoColumn("下单时间:", format.format(SystemClock.currentThreadTimeMillis()) + "\n"));

                    mPrinter.printText("--------------------------------\n");

                    mPrinter.printText("客户信息：\n");

                    //16点阵
                    byte cmd[] = new byte[]{27, (byte) 128, 1};
                    mPrinter.printByteData(cmd);

                    //字体加大一倍，由16点阵变为32点阵 （字体大中小对应为 24点阵、32点阵、48点阵）
                    mPrinter.setCharacterMultiple(1, 1);

                    //加粗
                    byte cmd4[] = new byte[]{27, 69, 1};
                    mPrinter.printByteData(cmd4);

                    mPrinter.printText("小明 ");
                    mPrinter.printText("\n");

                    //取消加粗
                    byte cmd3[] = new byte[]{27, 69, 0};
                    mPrinter.printByteData(cmd3);
                    mPrinter.printText("17721358718\n");

                    //24点阵 (默认字体大小)
                    byte cmd1[] = new byte[]{27, (byte) 128, 0};
                    mPrinter.printByteData(cmd1);
                    mPrinter.setCharacterMultiple(0, 0);

                    mPrinter.printText("上海市杨浦区政立路485号哔哩哔哩大厦5楼520室\n\n");

                    mPrinter.printText("预约时间：\n");
                    mPrinter.printText("2020-04-23 18:00-18:50\n");

                    mPrinter.printText("--------------------------------\n");

                    mPrinter.printText("备注：");
                    mPrinter.printText("\n微微辣，可以微麻，多加点香菜，筷子两双，谢谢！\n");
                    mPrinter.printText("--------------------------------\n");

                    mPrinter.printByteData(print.printThreeColumn("商品", "数量", "小计\n"));

                    mPrinter.printByteData(print.printThreeColumn("冬瓜炖排骨:", "x10000", "8.00\n"));
                    mPrinter.printByteData(print.printThreeColumn("小野鸡炖蘑菇 + 冬瓜排骨汤:", "x1000", "99.00\n"));
                    mPrinter.printByteData(print.printThreeColumn("百合莲子粥:", "x10000", "899.00\n"));
                    mPrinter.printByteData(print.printThreeColumn("外婆家红烧肉:", "x100", "899.00\n"));
                    mPrinter.printByteData(print.printThreeColumn("猪肉顿粉条+锅包肉+玉米排骨汤:", "x10", "89993.00\n"));
                    mPrinter.printByteData(print.printThreeColumn("辣椒炒肉+佛跳墙+手撕鸡:", "x1000", "8\n"));
                    mPrinter.printByteData(print.printThreeColumn("叉烧包+白菜+刀豆+芋头:", "x108", "89\n"));
                    mPrinter.printByteData(print.printThreeColumn("清蒸鲈鱼:", "x1008", "89099.00\n"));
                    mPrinter.printByteData(print.printThreeColumn("雪菜黑鱼:", "x10008", "89\n"));
                    mPrinter.printByteData(print.printThreeColumn("麻辣十三香徐氏小龙虾:", "x1000", "89.00\n"));
                    mPrinter.printByteData(print.printThreeColumn("我吃火锅你吃火锅底料", "x10000", "89999.00\n\n"));

//                    mPrinter.printByteData(print.printThreeColumn("冬瓜炖冬瓜炖排骨排", " x9", "8.00"));
//                    mPrinter.printByteData(print.printThreeColumn("冬瓜炖冬瓜炖排骨排骨", " x99", "8.00"));
//                    mPrinter.printByteData(print.printThreeColumn("冬瓜炖冬瓜炖排骨排骨:", " x999", "8.00"));
//                    mPrinter.printByteData(print.printThreeColumn("冬瓜冬瓜炖排骨冬瓜炖排骨炖排骨:", " x999.9", "1008.00"));
//                    mPrinter.printByteData(print.printThreeColumn("冬瓜冬瓜炖排骨冬瓜炖排骨冬瓜炖排骨炖排骨:", " x999.99", "1008.00"));
//                    mPrinter.printByteData(print.printThreeColumn("冬瓜炖排冬瓜炖排骨冬瓜炖排骨冬瓜炖骨:", " x999.99", "10080.00"));
//                    mPrinter.printByteData(print.printThreeColumn("冬瓜冬瓜炖排骨冬瓜炖排骨冬瓜炖排骨冬瓜炖排骨冬瓜炖排骨冬瓜炖排骨炖排骨:", " x9999.99", "10080.00"));
//                    mPrinter.printByteData(print.printThreeColumn("冬瓜炖冬瓜炖排骨冬瓜炖排骨冬瓜炖排骨冬瓜炖排骨排骨:", "x10000", "8.00"));

                    mPrinter.setPrinter(BluetoothPrinter.COMM_ALIGN, BluetoothPrinter.COMM_ALIGN_RIGHT);
                    mPrinter.printText("总计：999.00\n");

                    mPrinter.setPrinter(BluetoothPrinter.COMM_ALIGN, BluetoothPrinter.COMM_ALIGN_CENTER);
                    mPrinter.printText("--------------------------------\n");

                    Barcode barcode = new Barcode(BluetoothPrinter.BAR_CODE_TYPE_QRCODE, 0, 72, 4, "http://weixin.qq.com");
                    mPrinter.printBarCode(barcode);

                    mPrinter.printText("关注“美团外卖”公众号，获取更多优惠信息\n\n");

                    mPrinter.printText("商家电话：800-820-8820\n");

                    mPrinter.cutPaper();

                } catch (
                        Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static boolean isConnect() {
        String status = "";
        try {
            byte[] statusData = HPRTPrinterHelper.GetRealTimeStatus((byte) HPRTPrinterHelper.PRINTER_REAL_TIME_STATUS_ITEM_PRINTER);

            for (byte statusDatum : statusData) {
                status += statusDatum;
            }

        } catch (Exception e) {
        }

        return "18".equals(status) ? true : false;
    }
}