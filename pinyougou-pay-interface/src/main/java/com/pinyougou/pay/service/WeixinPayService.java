package com.pinyougou.pay.service;

import java.util.Map;

/**
 * 微信支付接口
 *
 * @author Administrator
 */
public interface WeixinPayService {

    /**
     * 生成微信支付二维码
     *
     * @param out_trade_no 商户订单号
     * @param total_fee    订单总金额(单位：分)
     * @return 返回值类型为map，可以利于扩展
     */
    public Map createNative(String out_trade_no, String total_fee);

    /**
     * 查询订单支付状态
     *
     * @param out_trade_no
     * @return
     */
    public Map queryPayStatus(String out_trade_no);


    /**
     * 秒杀订单超时，关闭支付
     *
     * @param out_trade_no
     * @return
     */
    public Map closePay(String out_trade_no);
}