package com.atguigu.gmall0228.order.mq;

import com.atguigu.gmall0228.bean.OrderInfo;
import com.atguigu.gmall0228.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Calendar;

@Component
public class PaymentSuccessListener {

    @Autowired
    OrderService orderService;

    @JmsListener(destination = "PAYMENT_SUCCESS_QUEUE",containerFactory = "jmsQueueListener")
    public void consumerPaymentResult(MapMessage mapMessage){
        try {
            //        获得监听器的内容
            String out_trade_no = mapMessage.getString("out_trade_no");
            String trade_status = mapMessage.getString("trade_status");
            String trade_no = mapMessage.getString("trade_no");

//            根据支付结果更新订单状态
            System.out.println("监听器执行更新订单的服务");
            OrderInfo orderInfo = new OrderInfo();
            orderInfo.setOrderStatus("订单已支付");
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE,3);
            orderInfo.setExpireTime(c.getTime());
            orderInfo.setTrackingNo(trade_no);
            orderInfo.setProcessStatus("订单已支付");
            orderService.updateOrder(orderInfo);
//            发送订单支付消息
            orderService.sendOrderResult(out_trade_no);
        }catch (JMSException e){
            e.printStackTrace();
        }



    }
}
