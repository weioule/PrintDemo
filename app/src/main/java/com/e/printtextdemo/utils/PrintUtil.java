package com.e.printtextdemo.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

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
            printText("\n");
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
            printText("  ");
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

    /**
     * 打印文字
     *
     * @param text
     */
    public void printText(String text) {
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

    public void printTwoColumn(String title, String content) throws UnsupportedEncodingException {
        int iNum = 0;
        byte[] byteBuffer = new byte[100];
        byte[] tmp;

        tmp = getGbk(title);
        System.arraycopy(tmp, 0, byteBuffer, iNum, tmp.length);
        iNum += tmp.length;

        tmp = setLocation(getOffset(content));
        System.arraycopy(tmp, 0, byteBuffer, iNum, tmp.length);
        iNum += tmp.length;

        tmp = getGbk(content);
        System.arraycopy(tmp, 0, byteBuffer, iNum, tmp.length);

        print(byteBuffer);
    }

    public void printThreeColumn(String left, String middle, String right) throws UnsupportedEncodingException {
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

        //计算后需要换行的
        if (pixLength > LEFT_MAX_SIZE || pixLength == 0 || leftRemaining < pixLength) {
            left += "\n";
        }

        tmp = getGbk(left);
        System.arraycopy(tmp, 0, byteBuffer, iNum, tmp.length);
        iNum += tmp.length;

        int spaceCount = 0;
        if (middle.length() + right.length() >= 11) {

            if (middle.length() > 4) {
                spaceCount += (middle.length() - 4);
            }

            if (right.length() > 7) {
                spaceCount += (right.length() - 7);
            } else if (spaceCount > 0) {
                spaceCount -= 7 - right.length();
            }

            //都是大数的情况，需要特殊处理
            if (middle.length() + right.length() > 13 && (right.length() > 8 || middle.length() > 8))
                spaceCount += (2 * (1 + (right.length() - 8) * 0.1));

            int location = 240 - spaceCount * 15;
            tmp = setLocation(Math.max(0, location));
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

        print(byteBuffer);

    }

    public void printDashLine() {
        printText("--------------------------------");
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

    public static void printText(OrderBean data) {
        if (null == data) {
            data = new OrderBean();
            data.setTime(1577359134114l);
            data.setCode("008985956590840973");
            data.setName("小明");
            data.setPhone("17721358718");
            data.setAddress("上海市杨浦区政立路485号哔哩哔哩大厦5楼");
            data.setExpectedReach("18:50");
            data.setRemark("微微辣，可以微麻，多加一点香菜，谢谢！");
            data.setBusinessPhone("800-820-8820");
            data.setTotal("889");

            ArrayList<FoodBean> list = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                FoodBean bean = new FoodBean();
                bean.setCount(10 + i);
                bean.setPrice(10 * i);
                bean.setName("红烧肉");
                list.add(bean);
            }
            data.setFoodList(list);
        }

        try {
            PrintUtil print = new PrintUtil();
            print.setAlignCmd(ALIGN_CENTER);
            print.setFontSizeCmd(FONT_BIG);
            print.setFontBoldCmd(FONT_BOLD);
            print.printText("美团外卖");
            print.printLine();
            print.setAlignCmd(ALIGN_LEFT);
            print.printLine();

            print.setFontSizeCmd(FONT_NORMAL);
            print.setFontBoldCmd(FONT_BOLD_CANCEL);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            print.printTwoColumn("下单时间:", format.format(data.getTime()));
            print.printLine();

            print.printTwoColumn("订单编号:", data.getCode());
            print.printLine();

            print.printDashLine();
            print.printLine();

            print.setFontBoldCmd(FONT_BOLD);
            print.setFontSizeCmd(FONT_MIDDLE);
            print.printText(data.getName());
            print.printLine();
            print.setFontSizeCmd(FONT_MIDDLE_);
            print.setFontBoldCmd(FONT_BOLD_CANCEL);
            String receiveMobile = data.getPhone();
            print.printText(receiveMobile.substring(0, 3) + "****" + receiveMobile.substring(7, 11));

            print.setFontSizeCmd(FONT_NORMAL);
            print.setFontBoldCmd(FONT_BOLD);
            Print.PrintAndFeedNLine((byte) 30);
            print.printText(data.getAddress());
            print.printLine();

            Print.PrintAndFeedNLine((byte) 40);
            print.setFontBoldCmd(FONT_BOLD_CANCEL);
            print.printText("预计到达：");
            print.printLine();
            print.setFontBoldCmd(FONT_BOLD);
            print.printText(data.getExpectedReach());
            print.printLine();

            print.printDashLine();
            print.printLine();

            print.setFontBoldCmd(FONT_BOLD_CANCEL);
            print.printText("备注：");
            print.setFontBoldCmd(FONT_BOLD);
            print.printText(data.getRemark());
            print.printLine();

            print.printDashLine();
            print.printLine();

            print.setFontBoldCmd(FONT_BOLD_CANCEL);
            print.printThreeColumn("商品", "数量", "小计");
            print.printLine();

            print.setFontBoldCmd(FONT_BOLD);

            List<FoodBean> beans = data.getFoodList();
            if (null != beans && beans.size() > 0) {
                for (FoodBean bean : beans) {
                    print.printThreeColumn(bean.getName(), "x" + bean.getCount(), bean.getPrice() + "");
                }
            }

            print.printLine();
            print.setAlignCmd(ALIGN_RIGHT);
            Print.PrintAndFeedNLine((byte) 35);
            print.printText("订单金额：" + data.getTotal());
            print.printLine();
            print.printText("实收金额：" + data.getTotal());
            print.printLine();

            print.printDashLine();
            print.printLine();

            print.setAlignCmd(ALIGN_CENTER);
            print.printText("商家电话:" + data.getBusinessPhone());
            print.printLine();

            Print.PrintAndFeed(80);

        } catch (Exception e) {
        }
    }
}