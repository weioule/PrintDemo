package com.e.printtextdemo.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.text.TextUtils;

import com.android_print_sdk.Barcode;
import com.android_print_sdk.bluetooth.BluetoothPrinter;
import com.caysn.autoreplyprint.AutoReplyPrint;
import com.e.printtextdemo.MyApplication;
import com.e.printtextdemo.R;
import com.e.printtextdemo.activity.SelectPrinterActivity;
import com.e.printtextdemo.model.FoodBean;
import com.e.printtextdemo.model.OrderBean;
import com.github.promeg.pinyinhelper.Pinyin;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.sun.jna.Pointer;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;
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

    public final static int WIDTH_PIXEL = 384;
    public final static int IMAGE_SIZE = 320;
    public final static int LEFT_MAX_SIZE = 240;
    private int fontSize;//字体类型

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
        this.fontSize = fontSize;
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

    public byte[] getGbk(String stText) throws UnsupportedEncodingException {
        byte[] returnText = stText.getBytes("GBK"); // 必须放在try内才可以
        return returnText;
    }

    //这里计算打印内容的长度，如果需要处理间距，或不同大小的字体调节后，可以在这里做适配处理
    private int getStringPixLength(String str) {
        int pixLength = 0;
        char c;
        for (int i = 0; i < str.length(); i++) {
            c = str.charAt(i);

            if (fontSize == FONT_BIG) {
                if (Pinyin.isChinese(c)) {
                    pixLength += 48;
                } else {
                    pixLength += 24;
                }
            } else if (Pinyin.isChinese(c)) {
                pixLength += 24;
            } else {
                pixLength += 12;
            }
        }
        return pixLength;
    }

    public int getOffset(String str) {
        int length = WIDTH_PIXEL - getStringPixLength(str);
        //如果长度大于纸张宽度，就右对齐打印，计算左边间距
        if (length < 0) return WIDTH_PIXEL - Math.abs(length);
        return length;
    }

    //这个排版格式是可以公用的，打印机基本都是可以打印byte数组
    public byte[] printTwoColumn(String left, String middle) throws UnsupportedEncodingException {
        int iNum = 0;
        byte[] byteBuffer = new byte[100];
        byte[] tmp;

        //手动添加间距
        left += " ";
        int pixLength = getStringPixLength(left) % WIDTH_PIXEL;
        int middleLength = getStringPixLength(middle) % WIDTH_PIXEL;
        int leftRemaining = WIDTH_PIXEL - middleLength;

        int offset = 0;
        //计算后需要换行
        if (pixLength > LEFT_MAX_SIZE || 0 != left.length() && pixLength == 0) {
            left += "\n";
        } else if (leftRemaining < 0) {
            //右侧内容长度大于纸张宽度，就右对齐打印，计算左边间距
            offset = WIDTH_PIXEL - Math.abs(leftRemaining);
        } else if (leftRemaining < pixLength) {
            //左间距小于左侧内容，就换行打印
            left += "\n";
        } else
            offset = leftRemaining;

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

        //手动添加间距
        left += " ";
        middle += " ";

        System.arraycopy(tmp, 0, byteBuffer, iNum, tmp.length);
        iNum += tmp.length;

        int pixLength = getStringPixLength(left) % WIDTH_PIXEL;
        int middleLength = getStringPixLength(middle) % WIDTH_PIXEL;
        int rightLength = getStringPixLength(right) % WIDTH_PIXEL;
        int leftRemaining = WIDTH_PIXEL - middleLength - rightLength;

        //计算后需要换行
        if (pixLength > LEFT_MAX_SIZE || 0 != left.length() && pixLength == 0 || leftRemaining < pixLength) {
            left += "\n";
        }

        tmp = getGbk(left);
        System.arraycopy(tmp, 0, byteBuffer, iNum, tmp.length);
        iNum += tmp.length;

        if (leftRemaining < 0) {
            //中间内容+右侧内容长度大于纸张宽度，将右侧内容换行打印，中间内容与头部“数量”对齐，右侧内容右对齐
            middle = middle.trim();//换行后删除右空格
            middleLength = getStringPixLength(middle) % WIDTH_PIXEL;//删除有空格后重新获取长度
            middle += "\n";
            //若中间内容长度较长，超出右侧的空间（WIDTH_PIXEL-170），则右对齐打印，左侧间距为：WIDTH_PIXEL - middleLength
            tmp = setLocation(Math.min(170, WIDTH_PIXEL - middleLength));
        } else if (middle.length() + right.length() >= 12 || fontSize == FONT_BIG && middle.length() + right.length() >= 8) {
            //数量与小计的字符长度超出其默认范围12，则往从右往左打印
            //大字体字符长度范围为8
            tmp = setLocation(leftRemaining);
        } else {
            if (fontSize == FONT_BIG)
                //大字体头部的“数量”位置调整为距左侧170像素
                tmp = setLocation(170);
            else
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
        if (fontSize == FONT_BIG)
            //大字体虚线长度调整
            printHY("----------------");
        else
            printHY("--------------------------------");
    }

    /*************************************************************************
     * 假设一个360*360的图片，分辨率设为24, 共分15行打印 每一行,是一个 360 * 24 的点阵,y轴有24个点,存储在3个byte里面。
     * 即每个byte存储8个像素点信息。因为只有黑白两色，所以对应为1的位是黑色，对应为0的位是白色
     **************************************************************************/
    private static byte[] draw2PxPoint(Bitmap bmp) {
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
    private static byte px2Byte(int x, int y, Bitmap bit) {
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
    private static int RGB2Gray(int r, int g, int b) {
        int gray = (int) (0.29900 * r + 0.58700 * g + 0.11400 * b); // 灰度转化公式
        return gray;
    }

    /**
     * 对图片进行压缩（去除透明度）
     *
     * @param bitmapOrg
     */
    private static Bitmap compressPic(Bitmap bitmapOrg) {
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
                if (!Print.IsOpened() || !"18".equals(isConnect())) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MyApplication.hideLoading();
                            if (TextUtils.isEmpty(PrintUtil.isConnect()))
                                MyApplication.showToast(activity.getString(R.string.abnormal_printer_close));
                            else
                                MyApplication.showToast(activity.getString(R.string.abnormal_printer_status));
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
                    orderBean[0].setCode("20211212155816000001");
                    orderBean[0].setName("小明");
                    orderBean[0].setPhone("177****8718");
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
                    print.setFontSizeCmd(FONT_BIG);
                    print.setFontBoldCmd(FONT_BOLD);
                    print.setAlignCmd(ALIGN_CENTER);
                    print.printHY("美团外卖");
                    print.printLine();
                    print.printLine();

                    Bitmap bitmap = drawableToBitmap(activity.getResources().getDrawable(R.drawable.print_header));
                    //调节图片大小
                    bitmap = getBitmap(activity, bitmap);
                    //转换成字节数组
                    byte[] bmpByteArray = draw2PxPoint(bitmap);
                    Print.WriteData(bmpByteArray);

                    print.printLine();
                    print.setAlignCmd(ALIGN_LEFT);
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

                    //参数1 bcData：二维码内容
                    //参数2 sizeOfModule：二维码大小，范围 1-16
                    //参数3 errorLevel：纠错等级( 48=7%、49=15%、50=25%、51=30% )
                    //参数4 justification：对齐方式( 0=左对齐、1=居中、2=右对齐 )
//                    Print.PrintQRCode("http://weixin.qq.com", 6, 50, 1);   该方法打印不了二维码，故使用下面的图片打印方式

                    //生成二维码
                    Bitmap codeBitmap = createQRCodeBitmap("http://weixin.qq.com", 150, 150, "UTF-8", "H", "0", Color.BLACK, Color.WHITE);
                    //转换成字节数组
                    byte[] array = draw2PxPoint(codeBitmap);
                    //打印
                    Print.WriteData(array);

                    print.setAlignCmd(ALIGN_CENTER);
                    print.printHY("关注“美团外卖”公众号，获取更多优惠信息\n\n");
                    print.printHY("商家电话：" + orderBean[0].getBusinessPhone());
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
                            if (16 == mPrinter.getPrinterStatus())
                                MyApplication.showToast(activity.getString(R.string.abnormal_printer_close));
                            else
                                MyApplication.showToast(activity.getString(R.string.abnormal_printer_status));
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
                    mPrinter.printText("\n");

                    //图片打印
                    Bitmap bitmap = drawableToBitmap(activity.getResources().getDrawable(R.drawable.print_header));
                    //大小调节可把bitmap设置宽高后再打印
                    byte[] bmpByteArray = draw2PxPoint(bitmap);
                    mPrinter.printByteData(bmpByteArray);

                    mPrinter.setCharacterMultiple(0, 0);
                    mPrinter.setPrintModel(false, false, false, false);
                    mPrinter.setPrinter(BluetoothPrinter.COMM_ALIGN_LEFT);
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                    mPrinter.printText("\n");
                    mPrinter.printByteData(print.printTwoColumn("订单编号:", "20211212155816000001\n"));
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
                    mPrinter.printText("177****8718\n");

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

    public static void printFK(Activity activity) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Pointer pointer = SelectPrinterActivity.pointer;
                int status = 0;
                if (null != pointer)
                    status = AutoReplyPrint.INSTANCE.CP_Pos_QueryRTStatus(pointer, 10000);
                if (303174162 != status) {
                    int finalStatus = status;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MyApplication.hideLoading();
                            if (0 == finalStatus)
                                MyApplication.showToast(activity.getString(R.string.abnormal_printer_close));
                            else
                                MyApplication.showToast(activity.getString(R.string.abnormal_printer_status));
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
                    AutoReplyPrint.INSTANCE.CP_Pos_ResetPrinter(pointer);
                    AutoReplyPrint.INSTANCE.CP_Pos_SetMultiByteMode(pointer);

                    AutoReplyPrint.INSTANCE.CP_Pos_SetTextBold(pointer, 1);
                    AutoReplyPrint.INSTANCE.CP_Pos_SetTextScale(pointer, 1, 1);
                    AutoReplyPrint.INSTANCE.CP_Pos_SetAlignment(pointer, AutoReplyPrint.CP_Pos_Alignment_HCenter);
                    AutoReplyPrint.INSTANCE.CP_Pos_SetMultiByteEncoding(pointer, AutoReplyPrint.CP_MultiByteEncoding_UTF8);
                    AutoReplyPrint.INSTANCE.CP_Pos_PrintText(pointer, "美团外卖");
                    AutoReplyPrint.INSTANCE.CP_Pos_FeedLine(pointer, 2);
                    AutoReplyPrint.INSTANCE.CP_Pos_SetTextBold(pointer, 0);
                    AutoReplyPrint.INSTANCE.CP_Pos_SetTextScale(pointer, 0, 0);

                    //图片打印
                    Bitmap bitmap = ((BitmapDrawable) activity.getResources().getDrawable(R.drawable.print_header)).getBitmap();
                    // dstw与desth为图片宽高，单位是像素
                    // 参数5为图片算法类型：0=抖动、1=黑白、2=聚焦
                    // 参数6为压缩等级：0不压缩
                    AutoReplyPrint.CP_Pos_PrintRasterImageFromData_Helper.PrintRasterImageFromBitmap(pointer, 160, 160, bitmap, AutoReplyPrint.CP_ImageBinarizationMethod_ErrorDiffusion, AutoReplyPrint.CP_ImageCompressionMethod_None);

                    AutoReplyPrint.INSTANCE.CP_Pos_FeedLine(pointer, 1);
                    AutoReplyPrint.INSTANCE.CP_Pos_SetMultiByteEncoding(pointer, AutoReplyPrint.CP_MultiByteEncoding_GBK);
                    AutoReplyPrint.INSTANCE.CP_Pos_SetAlignment(pointer, AutoReplyPrint.CP_Pos_Alignment_Left);

                    byte[] bytes = print.printTwoColumn("订单编号:", "20211212155816000001");
                    AutoReplyPrint.INSTANCE.CP_Port_Write(pointer, bytes, bytes.length, 10000);
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    byte[] bytes2 = print.printTwoColumn("下单时间:", format.format(SystemClock.currentThreadTimeMillis()));
                    AutoReplyPrint.INSTANCE.CP_Port_Write(pointer, bytes2, bytes2.length, 10000);

                    AutoReplyPrint.INSTANCE.CP_Pos_PrintText(pointer, "\n--------------------------------\n");
                    AutoReplyPrint.INSTANCE.CP_Pos_SetMultiByteEncoding(pointer, AutoReplyPrint.CP_MultiByteEncoding_UTF8);
                    AutoReplyPrint.INSTANCE.CP_Pos_PrintText(pointer, "客户信息：\n");

                    //中号字体
                    byte[] data = {(byte) 0x1d, (byte) 0x21, (byte) 0x01};
                    AutoReplyPrint.INSTANCE.CP_Port_Write(pointer, data, data.length, 10000);

                    AutoReplyPrint.INSTANCE.CP_Pos_SetTextBold(pointer, 1);

                    AutoReplyPrint.INSTANCE.CP_Pos_PrintText(pointer, "小明 ");
                    AutoReplyPrint.INSTANCE.CP_Pos_SetTextBold(pointer, 0);
                    AutoReplyPrint.INSTANCE.CP_Pos_PrintText(pointer, "177****8718");

                    AutoReplyPrint.INSTANCE.CP_Pos_SetTextScale(pointer, 0, 0);
                    AutoReplyPrint.INSTANCE.CP_Pos_FeedLine(pointer, 2);
                    AutoReplyPrint.INSTANCE.CP_Pos_PrintText(pointer, "上海市杨浦区政立路485号哔哩哔哩大厦5楼520室");
                    AutoReplyPrint.INSTANCE.CP_Pos_FeedLine(pointer, 1);

                    AutoReplyPrint.INSTANCE.CP_Pos_PrintText(pointer, "预约时间：\n");
                    AutoReplyPrint.INSTANCE.CP_Pos_PrintText(pointer, "2020-04-23 18:00-18:50");
                    AutoReplyPrint.INSTANCE.CP_Pos_PrintText(pointer, "\n--------------------------------\n");

                    AutoReplyPrint.INSTANCE.CP_Pos_PrintText(pointer, "备注：");
                    AutoReplyPrint.INSTANCE.CP_Pos_PrintText(pointer, "微微辣，可以微麻，多加点香菜，筷子两双，谢谢！");
                    AutoReplyPrint.INSTANCE.CP_Pos_PrintText(pointer, "\n--------------------------------\n");

                    AutoReplyPrint.INSTANCE.CP_Pos_SetMultiByteEncoding(pointer, AutoReplyPrint.CP_MultiByteEncoding_GBK);
                    byte[] bytes3 = print.printThreeColumn("商品", "数量", "小计");
                    AutoReplyPrint.INSTANCE.CP_Port_Write(pointer, bytes3, bytes3.length, 10000);
                    AutoReplyPrint.INSTANCE.CP_Pos_FeedLine(pointer, 1);

                    byte[] bytes4 = print.printThreeColumn("冬瓜炖冬瓜炖排骨排", " x9", "8.00");
                    AutoReplyPrint.INSTANCE.CP_Port_Write(pointer, bytes4, bytes4.length, 10000);
                    AutoReplyPrint.INSTANCE.CP_Pos_FeedLine(pointer, 1);
                    byte[] bytes5 = print.printThreeColumn("冬瓜炖冬瓜炖排骨排骨", " x999", "8.00");
                    AutoReplyPrint.INSTANCE.CP_Port_Write(pointer, bytes5, bytes5.length, 10000);
                    AutoReplyPrint.INSTANCE.CP_Pos_FeedLine(pointer, 1);
                    byte[] bytes6 = print.printThreeColumn("冬瓜炖冬瓜炖排骨排骨:", " x999", "8.00");
                    AutoReplyPrint.INSTANCE.CP_Port_Write(pointer, bytes6, bytes6.length, 10000);
                    AutoReplyPrint.INSTANCE.CP_Pos_FeedLine(pointer, 1);
                    byte[] bytes7 = print.printThreeColumn("冬瓜冬瓜炖排骨冬瓜炖排骨炖排骨:", " x9999899.9", "1008.00");
                    AutoReplyPrint.INSTANCE.CP_Port_Write(pointer, bytes7, bytes7.length, 10000);
                    AutoReplyPrint.INSTANCE.CP_Pos_FeedLine(pointer, 1);
                    byte[] bytes8 = print.printThreeColumn("冬瓜冬瓜炖排骨冬瓜炖排骨冬瓜炖排骨炖排骨:", " x999.99", "1008.00");
                    AutoReplyPrint.INSTANCE.CP_Port_Write(pointer, bytes8, bytes8.length, 10000);
                    AutoReplyPrint.INSTANCE.CP_Pos_FeedLine(pointer, 1);
                    byte[] bytes9 = print.printThreeColumn("冬瓜炖排冬瓜炖排骨冬瓜炖排骨冬瓜炖骨:", " x999.99", "10080.00");
                    AutoReplyPrint.INSTANCE.CP_Port_Write(pointer, bytes9, bytes9.length, 10000);
                    AutoReplyPrint.INSTANCE.CP_Pos_FeedLine(pointer, 1);
                    byte[] bytes0 = print.printThreeColumn("冬瓜冬瓜炖排骨冬瓜炖排骨冬瓜炖排骨冬瓜炖排骨冬瓜炖排骨冬瓜炖排骨炖排骨:", " x99899.99", "100980.00");
                    AutoReplyPrint.INSTANCE.CP_Port_Write(pointer, bytes0, bytes0.length, 10000);
                    AutoReplyPrint.INSTANCE.CP_Pos_FeedLine(pointer, 1);
                    byte[] bytes11 = print.printThreeColumn("冬瓜炖冬瓜炖排骨冬瓜炖排骨冬瓜炖排骨冬瓜炖排骨排骨:", "x10000", "998.00");
                    AutoReplyPrint.INSTANCE.CP_Port_Write(pointer, bytes11, bytes11.length, 10000);
                    AutoReplyPrint.INSTANCE.CP_Pos_FeedLine(pointer, 1);

                    AutoReplyPrint.INSTANCE.CP_Pos_SetMultiByteEncoding(pointer, AutoReplyPrint.CP_MultiByteEncoding_UTF8);
                    AutoReplyPrint.INSTANCE.CP_Pos_SetAlignment(pointer, AutoReplyPrint.CP_Pos_Alignment_Right);
                    AutoReplyPrint.INSTANCE.CP_Pos_PrintText(pointer, "总计：8889:99");

                    AutoReplyPrint.INSTANCE.CP_Pos_PrintText(pointer, "\n--------------------------------\n");
                    AutoReplyPrint.INSTANCE.CP_Pos_SetAlignment(pointer, AutoReplyPrint.CP_Pos_Alignment_HCenter);

                    AutoReplyPrint.INSTANCE.CP_Page_DrawQRCode(pointer, 0, 0, 8, AutoReplyPrint.CP_QRCodeECC_H, "http://weixin.qq.com");

                    AutoReplyPrint.INSTANCE.CP_Pos_FeedLine(pointer, 1);
                    AutoReplyPrint.INSTANCE.CP_Pos_PrintText(pointer, "关注“美团外卖”公众号，获取更多优惠信息");

                    AutoReplyPrint.INSTANCE.CP_Pos_FeedLine(pointer, 2);
                    AutoReplyPrint.INSTANCE.CP_Pos_PrintText(pointer, "商家电话：800-820-8820");

                    AutoReplyPrint.INSTANCE.CP_Pos_FeedLine(pointer, 4);
                } catch (Exception e) {
                }
            }
        }).start();
    }

    public static String isConnect() {
        String status = "";
        try {
            byte[] statusData = HPRTPrinterHelper.GetRealTimeStatus((byte) HPRTPrinterHelper.PRINTER_REAL_TIME_STATUS_ITEM_PRINTER);

            for (byte statusDatum : statusData) {
                status += statusDatum;
            }

        } catch (Exception e) {
        }
        return status;
    }

    //调节图片大小
    public static Bitmap getBitmap(Context context, Bitmap bitmap) {
        int width = bitmap.getWidth();//得到的单位是dp
        int height = bitmap.getHeight();
        // 设置想要的大小，这里像素单位
        int newWidth = IMAGE_SIZE;
        int newHeight = IMAGE_SIZE;
        // 计算缩放比例
        float scaleWidth = (float) newWidth / Utility.dp2px(context, width);//所以需要转换为像素后再计算比例
        float scaleHeight = (float) newHeight / Utility.dp2px(context, height);
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return bitmap;
    }

    /**
     * 生成简单二维码
     *
     * @param content                字符串内容
     * @param width                  二维码宽度
     * @param height                 二维码高度
     * @param character_set          编码方式（一般使用UTF-8）
     * @param error_correction_level 容错率 L：7% M：15% Q：25% H：35%
     * @param margin                 空白边距（二维码与边框的空白区域）
     * @param color_black            黑色色块
     * @param color_white            白色色块
     * @return BitMap
     */
    public static Bitmap createQRCodeBitmap(String content, int width, int height,
                                            String character_set, String error_correction_level,
                                            String margin, int color_black, int color_white) {
        // 字符串内容判空
        if (TextUtils.isEmpty(content)) {
            return null;
        }
        // 宽和高>=0
        if (width < 0 || height < 0) {
            return null;
        }
        try {
            /** 1.设置二维码相关配置 */
            Hashtable<EncodeHintType, String> hints = new Hashtable<>();
            // 字符转码格式设置
            if (!TextUtils.isEmpty(character_set)) {
                hints.put(EncodeHintType.CHARACTER_SET, character_set);
            }
            // 容错率设置
            if (!TextUtils.isEmpty(error_correction_level)) {
                hints.put(EncodeHintType.ERROR_CORRECTION, error_correction_level);
            }
            // 空白边距设置
            if (!TextUtils.isEmpty(margin)) {
                hints.put(EncodeHintType.MARGIN, margin);
            }
            /** 2.将配置参数传入到QRCodeWriter的encode方法生成BitMatrix(位矩阵)对象 */
            BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            /** 3.创建像素数组,并根据BitMatrix(位矩阵)对象为数组元素赋颜色值 */
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    //bitMatrix.get(x,y)方法返回true是黑色色块，false是白色色块
                    if (bitMatrix.get(x, y)) {
                        pixels[y * width + x] = color_black;//黑色色块像素设置
                    } else {
                        pixels[y * width + x] = color_white;// 白色色块像素设置
                    }
                }
            }
            /** 4.创建Bitmap对象,根据像素数组设置Bitmap每个像素点的颜色值,并返回Bitmap对象 */
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    //图片合成
    public static Bitmap synthesisImage(Bitmap bg, Bitmap icon, int left) {
        Bitmap b2 = icon;
        Paint paint = new Paint();
        Canvas canvas = new Canvas(bg);
        int b1w = bg.getWidth();
        int b1h = bg.getHeight();
        int b2w = b2.getWidth();
        int b2h = b2.getHeight();
        int bx = (b1w - b2w) / 2;
        int by = (b1h - b2h) / 2;
        canvas.drawBitmap(b2, left, by, paint);
        //叠加新图b2 并且居中
//        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.save();
        canvas.restore();
        return bg;
    }

    /**
     * Drawable转换成一个Bitmap
     *
     * @param drawable drawable对象
     * @return
     */
    public static final Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}