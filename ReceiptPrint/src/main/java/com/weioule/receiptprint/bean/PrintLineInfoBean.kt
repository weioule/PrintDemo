package com.weioule.receiptprint.bean

import android.graphics.Bitmap

/**
 * 带打印配置的每行数据信息
 * Author by weioule.
 * Date on 2022/09/10.
 */
class PrintLineInfoBean {
    var fontBold = 0 //加粗模式
    var ayFontBold = 0 //爱印打印机加粗模式
    var fontSize = 0 //字体大小
    var ayFontSize = 0 //爱印打印机字体大小
    var fontAlign = 0 //对齐方式
    var columnNum = 0 //排版列数
    var contentType = 0 //内容类型
    var printAndFeedNLine: Byte = 0 //走纸长度
    var contentLeft: String? = null//左边内容
    var contentRight: String? = null//右边内容
    var contentCenter: String? = null//中间内容
    var bitmap: Bitmap? = null//图片、二维码bitmap（汉印、复坤）
    var ayCodeUrl: String? = null//图片、二维码url  （复坤）
    var hyBitmapX = 0 //图片、二维码bitmap 打印配置(汉印)
    var hyBitmapY = 0 //图片、二维码bitmap 打印配置 (汉印)
    var ayBitmapX = 0 //图片、二维码bitmap 打印配置 (爱印)
    var ayBitmapY = 0 //图片、二维码bitmap 打印配置（爱印)
    var ayBitmapZ = 0 //图片、二维码bitmap 打印配置（爱印)
    var fkBitmapWidth = 0 //图片、二维码bitmap 打印宽度 (复坤)
    var multiByteEncoding = 0 //编码格式:0=gbk  1=utf-8
    var feedLine = 1 //空行数

    constructor() {}
    constructor(contentType: Int) {
        this.contentType = contentType
    }

    companion object {
        //加粗模式
        const val FONT_BOLD = 0 // 字体加粗
        const val FONT_BOLD_NORMAL = 1 // 正常

        //字体大小
        const val FONT_NORMAL = 0 // 正常
        const val FONT_MIDDLE = 1 // 中等
        const val FONT_MIDDLE_ = 3 // 中小
        const val FONT_MIDDLE_2 = 4 // 中大不加粗
        const val FONT_BIG = 2 // 大

        // 对齐方式
        const val ALIGN_LEFT = 0 // 靠左
        const val ALIGN_CENTER = 1 // 居中
        const val ALIGN_RIGHT = 2 // 靠右

        //排版列数
        const val ONE_COLUMN = 0 // 一列
        const val TWO_COLUMN = 1 // 两列
        const val THREE_COLUMN = 2 // 三列

        //内容类型
        const val CONTENT_TXT = 0 // 文字
        const val CONTENT_BITMAP = 1 // 图片
        const val CONTENT_BARCODE = 2 // 条码图片
        const val CONTENT_QRCODE = 3 // 二维码
        const val CONTENT_NEW_LINE = 4 // 换行
        const val CONTENT_DOTTED_LINE = 5 // 虚线
        const val CONTENT_BLANK_LINE = 6 // 空行
    }
}