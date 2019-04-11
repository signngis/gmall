package com.atguigu.gmall0228.service;

import com.atguigu.gmall0228.bean.OrderInfo;

public interface OrderService {
    String genTradeCode(String userId);

    boolean checkTradeCode(String userId, String tradeCode);

    void saveOrder(OrderInfo orderInfo);

    OrderInfo getPaymentByOutTradeNo(String orderId);

    void updateOrder(OrderInfo orderInfo);

    void sendOrderResult(String out_trade_no);
}
