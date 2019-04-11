package com.atguigu.gmall0228.payment.mq;

import com.atguigu.gmall0228.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Map;
//  设置一个消息队列的监听器,当监听到支付状态的消息队列的时候,就拦截,检查判断支付
//状态.如果支付状态是已支付,则判断支付状态,若果支付状态是"success",则修改信息,保存
//到数据库.如果不是success,则设定时间,发送延迟消息队列
@Component
public class PaymentCheckListener {

    @Autowired
    PaymentService paymentService;
    @JmsListener(destination = "PAYMENT_CHECK_QUEUE",containerFactory ="jmsQueueListener" )
    public void consumerPaymentResult(MapMessage mapMessage){

        int countNum = 0;
        String out_trade_no = "";

        try {
            out_trade_no =  mapMessage.getString("out_trade_no");
            countNum = mapMessage.getInt("countNum");
        } catch (JMSException e) {
            e.printStackTrace();
        }

//        检查数据库中的支付状态
        String paymentStatus = paymentService.checkPaymentInfoStatus(out_trade_no);

        if(!paymentStatus.equals("已支付")){
//            调用支付状态查询服务
            Map<String,String> map  = paymentService.checkPaymentStatus(out_trade_no);
            String status = map.get("status");

            if(status.equals("success")){
//                发送支付成功的消息队列
                String trade_no = map.get("trade_no");
                System.out.println("支付状态检查成功,发送支付队列");
                paymentService.sendPaymentResult(out_trade_no,status,trade_no,"");
            }else{
//                继续给支付服务系统发送下一个延迟检查的消息队列
                if(countNum>0){
                    System.err.println("支付状态检查未成功,根据剩余"+(countNum-1)+"检查次数计算下次检查的延迟时间，发送延迟队列");
                    paymentService.sendpaymentCheckQueue(out_trade_no,countNum-1);
                }else {
                    System.out.println("交易失败,关闭该笔交易");
                }
            }
        }
    }
}
