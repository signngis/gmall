package com.atguigu.gmall0228.payment.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.atguigu.gmall0228.bean.PaymentInfo;
import com.atguigu.gmall0228.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall0228.service.PaymentService;
import com.atguigu.gmall0228.util.ActiveMQUtil;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    PaymentInfoMapper paymentInfoMapper;
    @Autowired
    ActiveMQUtil activeMQUtil;
    @Autowired
    AlipayClient alipayClient;

    @Override
    public void savePayment(PaymentInfo paymentInfo) {
        paymentInfoMapper.insert(paymentInfo);
    }

    @Override
    public void updatePayment(PaymentInfo paymentInfo) {
        Example example = new Example(PaymentInfo.class);

        example.createCriteria().andEqualTo("outTradeNo",paymentInfo.getOutTradeNo());

        paymentInfoMapper.updateByExampleSelective(paymentInfo,example);
    }

    @Override
    public void sendPaymentResult(String out_trade_no, String trade_status, String trade_no,String queryString) {


        // 修改支付状态
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setAlipayTradeNo(trade_no);
        paymentInfo.setCallbackContent(queryString);
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setPaymentStatus("已支付");
        paymentInfo.setOutTradeNo(out_trade_no);
        updatePayment(paymentInfo);

        Connection connection = activeMQUtil.getConnection();
        try {
            //       获取链接
            connection.start();
            //        创建mq的执行会话
//        第一个值表示是否使用事物,如果悬着true,第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue testquene = session.createQueue("PAYMENT_SUCCESS_QUEUE");
//            创建消息对象
            MessageProducer producer = session.createProducer(testquene);
            ActiveMQMapMessage activeMQMapMessage = new ActiveMQMapMessage();
            activeMQMapMessage.setString("out_trade_no",out_trade_no);
            activeMQMapMessage.setString("trade_status",trade_status);
            activeMQMapMessage.setString("trade_no",trade_no);
//            通过消息对象提交消息内容
            producer.send(activeMQMapMessage);
//            提交会话
            session.commit();
//            关闭连接
            connection.close();
        }catch (JMSException e){
            e.printStackTrace();
        }


    }

    @Override
    public void sendpaymentCheckQueue(String out_trade_no, int countNum) {
//        继续执行延迟任务
        Connection connection = activeMQUtil.getConnection();

        try {
            connection.start();
//            创建mq的执行会话
//            第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue testqueue = session.createQueue("PAYMENT_CHECK_QUEUE");
//            创建消息对象
            MessageProducer producer = session.createProducer(testqueue);
            ActiveMQMapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("out_trade_no",out_trade_no);
            mapMessage.setInt("countNum",countNum);
//            延迟队列的设置,开启延迟队列,1分钟发送一次
            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,60000);

//            通过消息对象提交消息内容
            producer.send(mapMessage);
//            提交会话
            session.commit();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    //检查数据库中的支付状态
    @Override
    public String checkPaymentInfoStatus(String out_trade_no) {
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(out_trade_no);
        PaymentInfo paymentInfo1 = paymentInfoMapper.selectOne(paymentInfo);
        return paymentInfo1.getPaymentStatus();
    }


//  调用支付宝接口,检查支付状态
    @Override
    public Map<String, String> checkPaymentStatus(String out_trade_no) {
        System.out.println("调用支付宝支付接口,检查支付状态");
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent("{\"out_trade_no\":\""+out_trade_no+"\"}");
        AlipayTradeQueryResponse response = null;

        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        String status = "";
        String trade_on = "";
        if(response.isSuccess()){
            System.out.println("调用成功");
            String tradeStatus = response.getTradeStatus();
            if(tradeStatus.equals("TRADE_SUCCESS") || tradeStatus.equals("TRADE_FINISHED")){
                status = "success";
            }
            trade_on = response.getTradeNo();
        }else {
            System.out.println("调用失败");
        }

        Map<String, String> stringStringHashMap = new HashMap<>();

        stringStringHashMap.put("status",status);
        stringStringHashMap.put("trade_on",trade_on);

        return stringStringHashMap;
    }
}
