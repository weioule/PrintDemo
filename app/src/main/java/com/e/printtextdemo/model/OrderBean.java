package com.e.printtextdemo.model;


import java.util.List;

import lombok.Data;

/**
 * Created by weioule
 * on 2020/1/1
 */
@Data
public class OrderBean {
    private String receiveMan;//姓名
    private String receiveMobile;//手机号
    private long createTime;//创建时间
    private String orderCode;//订单号
    private String total;//合计
    private String receiveAddress;//地址
    private String expectedReach;//预计到达
    private String remark;//备注
    private List<FoodBean> foodList;  //食物
    private String businessPhone;//商家电话
}
