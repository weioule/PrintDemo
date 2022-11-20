package com.e.printtextdemo.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;

import com.caysn.autoreplyprint.AutoReplyPrint;
import com.e.printtextdemo.model.FoodBean;
import com.e.printtextdemo.model.OrderBean;
import com.weioule.receiptprint.ReceiptPrintUtil;
import com.weioule.receiptprint.bean.PrintLineInfoBean;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 小票打印模板
 * Author by weioule.
 * Date on 2022/11/10.
 */
public class TemplateUtil {

    public static ArrayList<PrintLineInfoBean> getTemplate(OrderBean data) {
        ArrayList<PrintLineInfoBean> list = new ArrayList<>();

        //汉印走纸空行
        PrintLineInfoBean infoBean1 = new PrintLineInfoBean();
        infoBean1.setContentType(PrintLineInfoBean.CONTENT_BLANK_LINE);
        infoBean1.setPrintAndFeedNLine((byte) 50);
        list.add(infoBean1);

        PrintLineInfoBean infoBean2 = new PrintLineInfoBean();
        infoBean2.setContentCenter("美团外卖");
        infoBean2.setFontBold(PrintLineInfoBean.FONT_BOLD);
        infoBean2.setAyFontBold(PrintLineInfoBean.FONT_BOLD);
        infoBean2.setFontSize(PrintLineInfoBean.FONT_BIG);
        infoBean2.setAyFontSize(PrintLineInfoBean.FONT_BIG);
        infoBean2.setFontAlign(PrintLineInfoBean.ALIGN_CENTER);
        infoBean2.setMultiByteEncoding(AutoReplyPrint.CP_MultiByteEncoding_UTF8);
        list.add(infoBean2);

        list.add(new PrintLineInfoBean(PrintLineInfoBean.CONTENT_NEW_LINE));
        list.add(new PrintLineInfoBean(PrintLineInfoBean.CONTENT_NEW_LINE));

        PrintLineInfoBean infoBean4 = new PrintLineInfoBean();
        infoBean4.setFontBold(PrintLineInfoBean.FONT_BOLD);
        infoBean4.setAyFontBold(PrintLineInfoBean.FONT_BOLD_NORMAL);
        infoBean4.setMultiByteEncoding(AutoReplyPrint.CP_MultiByteEncoding_GBK);
        infoBean4.setColumnNum(PrintLineInfoBean.TWO_COLUMN);
        infoBean4.setContentLeft("订单编号:");
        infoBean4.setContentRight(data.getOrderCode());
        list.add(infoBean4);

        list.add(new PrintLineInfoBean(PrintLineInfoBean.CONTENT_NEW_LINE));

        PrintLineInfoBean infoBean5 = new PrintLineInfoBean();
        infoBean5.setFontBold(PrintLineInfoBean.FONT_BOLD);
        infoBean5.setAyFontBold(PrintLineInfoBean.FONT_BOLD_NORMAL);
        infoBean5.setMultiByteEncoding(AutoReplyPrint.CP_MultiByteEncoding_GBK);
        infoBean5.setColumnNum(PrintLineInfoBean.TWO_COLUMN);
        infoBean5.setContentLeft("下单时间:");
        infoBean5.setContentRight(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(data.getCreateTime()));
        list.add(infoBean5);

        list.add(new PrintLineInfoBean(PrintLineInfoBean.CONTENT_NEW_LINE));
        list.add(new PrintLineInfoBean(PrintLineInfoBean.CONTENT_DOTTED_LINE));
        list.add(new PrintLineInfoBean(PrintLineInfoBean.CONTENT_NEW_LINE));

        PrintLineInfoBean infoBean6 = new PrintLineInfoBean();
        infoBean6.setContentLeft("客户信息：\n");
        infoBean6.setFontBold(PrintLineInfoBean.FONT_BOLD_NORMAL);
        infoBean6.setAyFontBold(PrintLineInfoBean.FONT_BOLD_NORMAL);
        infoBean6.setMultiByteEncoding(AutoReplyPrint.CP_MultiByteEncoding_UTF8);
        list.add(infoBean6);

        PrintLineInfoBean infoBean7 = new PrintLineInfoBean();
        infoBean7.setContentLeft(data.getReceiveMan() + " ");
        infoBean7.setFontBold(PrintLineInfoBean.FONT_BOLD);
        infoBean7.setAyFontBold(PrintLineInfoBean.FONT_BOLD);
        infoBean7.setFontSize(PrintLineInfoBean.FONT_MIDDLE);
        infoBean7.setAyFontSize(PrintLineInfoBean.FONT_MIDDLE_);
        infoBean7.setMultiByteEncoding(AutoReplyPrint.CP_MultiByteEncoding_UTF8);
        list.add(infoBean7);

        PrintLineInfoBean infoBean8 = new PrintLineInfoBean();
        String receiveMobile = data.getReceiveMobile();
        infoBean8.setContentLeft(receiveMobile.substring(0, 3) + "****" + receiveMobile.substring(7, 11));
        infoBean8.setFontSize(PrintLineInfoBean.FONT_MIDDLE);
        infoBean8.setAyFontSize(PrintLineInfoBean.FONT_MIDDLE_2);
        infoBean8.setFontBold(PrintLineInfoBean.FONT_BOLD_NORMAL);
        infoBean8.setAyFontBold(PrintLineInfoBean.FONT_BOLD_NORMAL);
        infoBean8.setMultiByteEncoding(AutoReplyPrint.CP_MultiByteEncoding_UTF8);
        list.add(infoBean8);

        PrintLineInfoBean infoBean9 = new PrintLineInfoBean(PrintLineInfoBean.CONTENT_NEW_LINE);
        infoBean9.setFeedLine(2);
        list.add(infoBean9);

        PrintLineInfoBean infoBean10 = new PrintLineInfoBean();
        infoBean10.setContentType(PrintLineInfoBean.CONTENT_BLANK_LINE);
        infoBean10.setPrintAndFeedNLine((byte) 30);
        list.add(infoBean10);

        PrintLineInfoBean infoBean11 = new PrintLineInfoBean();
        infoBean11.setContentLeft(data.getReceiveAddress());
        infoBean11.setFontBold(PrintLineInfoBean.FONT_BOLD);
        infoBean11.setAyFontBold(PrintLineInfoBean.FONT_BOLD_NORMAL);
        infoBean11.setMultiByteEncoding(AutoReplyPrint.CP_MultiByteEncoding_UTF8);
        list.add(infoBean11);

        list.add(new PrintLineInfoBean(PrintLineInfoBean.CONTENT_NEW_LINE));
        list.add(new PrintLineInfoBean(PrintLineInfoBean.CONTENT_NEW_LINE));

        PrintLineInfoBean infoBean12 = new PrintLineInfoBean();
        infoBean12.setFontBold(PrintLineInfoBean.FONT_BOLD_NORMAL);
        infoBean12.setAyFontBold(PrintLineInfoBean.FONT_BOLD_NORMAL);
        infoBean12.setMultiByteEncoding(AutoReplyPrint.CP_MultiByteEncoding_UTF8);
        infoBean12.setContentLeft("预计到达：\n");
        list.add(infoBean12);

        PrintLineInfoBean infoBean13 = new PrintLineInfoBean();
        infoBean13.setContentLeft(data.getExpectedReach());
        infoBean13.setFontBold(PrintLineInfoBean.FONT_BOLD);
        infoBean13.setAyFontBold(PrintLineInfoBean.FONT_BOLD_NORMAL);
        infoBean13.setMultiByteEncoding(AutoReplyPrint.CP_MultiByteEncoding_UTF8);
        list.add(infoBean13);

        list.add(new PrintLineInfoBean(PrintLineInfoBean.CONTENT_NEW_LINE));
        list.add(new PrintLineInfoBean(PrintLineInfoBean.CONTENT_DOTTED_LINE));
        list.add(new PrintLineInfoBean(PrintLineInfoBean.CONTENT_NEW_LINE));

        PrintLineInfoBean infoBean14 = new PrintLineInfoBean();
        infoBean14.setFontBold(PrintLineInfoBean.FONT_BOLD_NORMAL);
        infoBean14.setAyFontBold(PrintLineInfoBean.FONT_BOLD_NORMAL);
        infoBean14.setMultiByteEncoding(AutoReplyPrint.CP_MultiByteEncoding_UTF8);
        infoBean14.setContentLeft("备注：");
        list.add(infoBean14);

        if (!TextUtils.isEmpty(data.getRemark())) {
            list.add(new PrintLineInfoBean(PrintLineInfoBean.CONTENT_NEW_LINE));
            PrintLineInfoBean infoBean15 = new PrintLineInfoBean();
            infoBean15.setContentLeft(data.getRemark());
            infoBean15.setFontBold(PrintLineInfoBean.FONT_BOLD);
            infoBean15.setAyFontBold(PrintLineInfoBean.FONT_BOLD_NORMAL);
            infoBean15.setMultiByteEncoding(AutoReplyPrint.CP_MultiByteEncoding_UTF8);
            list.add(infoBean15);
        }

        list.add(new PrintLineInfoBean(PrintLineInfoBean.CONTENT_NEW_LINE));
        list.add(new PrintLineInfoBean(PrintLineInfoBean.CONTENT_DOTTED_LINE));
        list.add(new PrintLineInfoBean(PrintLineInfoBean.CONTENT_NEW_LINE));

        PrintLineInfoBean infoBean16 = new PrintLineInfoBean();
        infoBean16.setFontBold(PrintLineInfoBean.FONT_BOLD);
        infoBean16.setAyFontBold(PrintLineInfoBean.FONT_BOLD_NORMAL);
        infoBean16.setMultiByteEncoding(AutoReplyPrint.CP_MultiByteEncoding_GBK);
        infoBean16.setColumnNum(PrintLineInfoBean.THREE_COLUMN);
        infoBean16.setContentLeft("商品");
        infoBean16.setContentCenter("数量");
        infoBean16.setContentRight("小计");
        list.add(infoBean16);

        list.add(new PrintLineInfoBean(PrintLineInfoBean.CONTENT_NEW_LINE));

        PrintLineInfoBean infoBean17 = new PrintLineInfoBean();
        infoBean17.setContentType(PrintLineInfoBean.CONTENT_BLANK_LINE);
        infoBean17.setPrintAndFeedNLine((byte) 30);
        list.add(infoBean17);

        List<FoodBean> beans = data.getFoodList();
        if (null != beans && beans.size() > 0) {
            for (FoodBean bean : beans) {
                PrintLineInfoBean infoBean18 = new PrintLineInfoBean();
                infoBean18.setContentLeft(bean.getName());
                infoBean18.setContentCenter(" x" + bean.getCount());
                infoBean18.setContentRight(bean.getPrice() + "");
                infoBean18.setFontBold(PrintLineInfoBean.FONT_BOLD_NORMAL);
                infoBean18.setAyFontBold(PrintLineInfoBean.FONT_BOLD_NORMAL);
                infoBean18.setColumnNum(PrintLineInfoBean.THREE_COLUMN);
                infoBean18.setMultiByteEncoding(AutoReplyPrint.CP_MultiByteEncoding_GBK);
                list.add(infoBean18);

                list.add(new PrintLineInfoBean(PrintLineInfoBean.CONTENT_NEW_LINE));
            }
        }

        list.add(new PrintLineInfoBean(PrintLineInfoBean.CONTENT_NEW_LINE));

        PrintLineInfoBean infoBea19 = new PrintLineInfoBean();
        infoBea19.setFontBold(PrintLineInfoBean.FONT_BOLD_NORMAL);
        infoBea19.setAyFontBold(PrintLineInfoBean.FONT_BOLD_NORMAL);
        infoBea19.setMultiByteEncoding(AutoReplyPrint.CP_MultiByteEncoding_UTF8);
        infoBea19.setFontAlign(PrintLineInfoBean.ALIGN_RIGHT);
        infoBea19.setContentRight("订单金额：" + data.getTotal());
        list.add(infoBea19);

        list.add(new PrintLineInfoBean(PrintLineInfoBean.CONTENT_NEW_LINE));

        PrintLineInfoBean infoBea20 = new PrintLineInfoBean();
        infoBea20.setFontBold(PrintLineInfoBean.FONT_BOLD_NORMAL);
        infoBea20.setAyFontBold(PrintLineInfoBean.FONT_BOLD_NORMAL);
        infoBea20.setMultiByteEncoding(AutoReplyPrint.CP_MultiByteEncoding_UTF8);
        infoBea20.setFontAlign(PrintLineInfoBean.ALIGN_RIGHT);
        infoBea20.setContentRight("实收金额：" + data.getTotal());
        list.add(infoBea20);

        list.add(new PrintLineInfoBean(PrintLineInfoBean.CONTENT_NEW_LINE));
        list.add(new PrintLineInfoBean(PrintLineInfoBean.CONTENT_DOTTED_LINE));
        list.add(new PrintLineInfoBean(PrintLineInfoBean.CONTENT_NEW_LINE));

        Bitmap bitmap = ReceiptPrintUtil.createQRCodeBitmap("http://weixin.qq.com", 150, 150, "UTF-8", "H", "0", Color.BLACK, Color.WHITE);
        PrintLineInfoBean infoBean21 = new PrintLineInfoBean(PrintLineInfoBean.CONTENT_QRCODE);
        //兼容三款打印机
        infoBean21.setBitmap(bitmap);
        infoBean21.setAyCodeUrl("http://weixin.qq.com");
        infoBean21.setHyBitmapX(1);
        infoBean21.setHyBitmapY(0);
        infoBean21.setAyBitmapX(0);
        infoBean21.setAyBitmapY(72);
        infoBean21.setAyBitmapZ(4);
        list.add(infoBean21);

        PrintLineInfoBean infoBean22 = new PrintLineInfoBean();
        infoBean22.setContentCenter("关注“美团外卖”公众号，获取更多优惠信息");
        infoBean22.setFontBold(PrintLineInfoBean.FONT_BOLD_NORMAL);
        infoBean22.setAyFontBold(PrintLineInfoBean.FONT_BOLD_NORMAL);
        infoBean22.setFontAlign(PrintLineInfoBean.ALIGN_CENTER);
        infoBean22.setMultiByteEncoding(AutoReplyPrint.CP_MultiByteEncoding_UTF8);
        list.add(infoBean22);

        list.add(new PrintLineInfoBean(PrintLineInfoBean.CONTENT_NEW_LINE));

        PrintLineInfoBean infoBean23 = new PrintLineInfoBean();
        infoBean23.setContentCenter("商家电话：" + data.getReceiveMobile());
        infoBean23.setFontBold(PrintLineInfoBean.FONT_BOLD_NORMAL);
        infoBean23.setAyFontBold(PrintLineInfoBean.FONT_BOLD_NORMAL);
        infoBean23.setFontAlign(PrintLineInfoBean.ALIGN_CENTER);
        infoBean23.setMultiByteEncoding(AutoReplyPrint.CP_MultiByteEncoding_UTF8);
        list.add(infoBean23);

        return list;
    }

}
