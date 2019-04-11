package com.atguigu.gmall0228.order.service.Impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall0228.bean.OrderDetail;
import com.atguigu.gmall0228.bean.OrderInfo;
import com.atguigu.gmall0228.order.mapper.OrderDetailMapper;
import com.atguigu.gmall0228.order.mapper.OrderInfoMapper;
import com.atguigu.gmall0228.service.OrderService;
import com.atguigu.gmall0228.util.ActiveMQUtil;
import com.atguigu.gmall0228.util.RedisUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.List;
import java.util.UUID;
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    OrderInfoMapper orderInfoMapper;
    @Autowired
    OrderDetailMapper orderDetailMapper;
    @Autowired
    ActiveMQUtil activeMQUtil;

    @Override
public String genTradeCode(String userId) {
//        生成提交订单验证码
    String tradeCode = "atguigu"+ UUID.randomUUID().toString();

    Jedis jedis = redisUtil.getJedis();
//        把提交验证码放入到Redis中
        jedis.setex("user:"+userId+":tradeCode",60*10,tradeCode);
        jedis.close();
        return tradeCode;
}

//    检查订单码,是否满足要求,使用完毕后删除
    @Override
    public boolean checkTradeCode(String userId, String tradeCode) {
        boolean b = false;
        Jedis jedis = redisUtil.getJedis();
        String tradeCodeRedis = jedis.get("user:" + userId + ":tradeCode");
        if(StringUtils.isNotBlank(tradeCodeRedis)){
            if(tradeCodeRedis.equals(tradeCode)){
                b = true;
                jedis.del("user:" + userId + ":tradeCode");
            }
        }
        jedis.close();
        return b;
    }

//    保存订单信息到数据库
    @Override
    public void saveOrder(OrderInfo orderInfo) {
        orderInfoMapper.insertSelective(orderInfo);

//        根据订单主键保存订单详情
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insert(orderDetail);
        }
    }

    @Override
    public OrderInfo getPaymentByOutTradeNo(String orderId) {

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOutTradeNo(orderId);
        OrderInfo orderInfo1 = orderInfoMapper.selectOne(orderInfo);
//        List<OrderDetail> orderDetailList = orderInfo1.getOrderDetailList();
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderInfo1.getId());
        List<OrderDetail> select = orderDetailMapper.select(orderDetail);
        orderInfo1.setOrderDetailList(select);
        return orderInfo1;
    }

    @Override
    public void updateOrder(OrderInfo orderInfo) {
        Example example = new Example(OrderInfo.class);
        example.createCriteria().andEqualTo("outTradeNo",orderInfo.getOutTradeNo());
        orderInfoMapper.updateByExampleSelective(orderInfo,example);
    }

    @Override
    public void sendOrderResult(String out_trade_no) {
//        获取mq对象
        Connection connection = activeMQUtil.getConnection();

        try {
//            获取mq链接
            connection.start();
//            创建mq的执行会话
//             第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue testqueue = session.createQueue("ORDER_RESULT_QUEUE");
//            创建消息对象
            MessageProducer producer = session.createProducer(testqueue);
            ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();
            activeMQTextMessage.setText(out_trade_no);

//            通过消息对象提交消息内容
            producer.send(activeMQTextMessage);
//            提交会话
            session.commit();
//            关闭链接
            connection.close();
        }catch (JMSException e){
            e.printStackTrace();
        }
    }
}
