package com.atguigu.gmall0228.service;

import com.atguigu.gmall0228.bean.PaymentInfo;

import java.util.Map;

public interface PaymentService {
    void savePayment(PaymentInfo paymentInfo);

    void updatePayment(PaymentInfo paymentInfo);

    void sendPaymentResult(String out_trade_no, String trade_status, String trade_no, String queryString);

    void sendpaymentCheckQueue(String orderId, int i);

    String checkPaymentInfoStatus(String out_trade_no);

    Map<String, String> checkPaymentStatus(String out_trade_no);
}
