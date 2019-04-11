package com.atguigu.gmall0228.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall0228.annotation.LoginRequire;
import com.atguigu.gmall0228.bean.OrderInfo;
import com.atguigu.gmall0228.bean.PaymentInfo;
import com.atguigu.gmall0228.payment.config.AlipayConfig;
import com.atguigu.gmall0228.service.OrderService;
import com.atguigu.gmall0228.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class paymentController {
    @Autowired
    AlipayClient alipayClient;

    @Reference
    OrderService orderService;

    @Autowired
    PaymentService paymentService;

    /***
     *  支付宝异步回调地址
     */

    @RequestMapping("alipay/callback/notify")
    public String alipayCallbackNotify(){
        return "finish";
    }

//    支付宝同步调用地址
    @LoginRequire(isNeededSuccess = true)
    @RequestMapping("alipay/callback/return")
    public String alipayCallbackReturn(HttpServletRequest request,ModelMap map){
        String userId = (String)request.getAttribute("userId");
        String trade_no = request.getParameter("trade_no");
        String out_trade_no = request.getParameter("out_trade_no");
        String trade_status = request.getParameter("trade_status");
        String queryString = request.getQueryString();

        // 修改支付状态
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setAlipayTradeNo(trade_no);
        paymentInfo.setCallbackContent(queryString);
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setPaymentStatus("已支付");
        paymentInfo.setOutTradeNo(out_trade_no);
        paymentService.updatePayment(paymentInfo);

        // 通知订单系统支付完成
        paymentService.sendPaymentResult(out_trade_no,trade_status,trade_no,queryString);
        // 重定向到订单系统或者返回成功页面
        return "finish";

    }


//
    @LoginRequire(isNeededSuccess = true)
    @RequestMapping("/alipay/submit")
    @ResponseBody
    public String alipaySubmit(HttpServletRequest request,String orderId,ModelMap map){

        String userId = (String) request.getAttribute("userId");
        map.put("orderId",orderId);
//      根据订单id查询订单
        OrderInfo orderInfo = orderService.getPaymentByOutTradeNo(orderId);

//      支付宝接口的参数封装,详细信息参照支付宝接口文档
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);//在公共参数中设置回跳和通知地址
//        alipayRequest.setBizContent("{" +
//                "    \"out_trade_no\":\""+orderId+"\"," +
//                "    \"product_code\":\"FAST_INSTANT_TRADE_PAY\"," +
//                "    \"total_amount\":"+orderInfo.getTotalAmount()+"," +
//                "    \"subject\":\""+orderInfo.getOrderDetailList().get(0).getSkuName()+"\"," +
//                "  }");//填充业务参数


        Map<String, Object> stringObjectHashMap = new HashMap<>();
        stringObjectHashMap.put("out_trade_no",orderId);
        stringObjectHashMap.put("product_code","FAST_INSTANT_TRADE_PAY");
        stringObjectHashMap.put("total_amount",0.01);
        stringObjectHashMap.put("subject",orderInfo.getOrderDetailList().get(0).getSkuName());
        alipayRequest.setBizContent(JSON.toJSONString(stringObjectHashMap));
        String form="";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        System.out.println(form);

        // 保存支付信息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setPaymentStatus("未支付");
        paymentInfo.setOutTradeNo(orderId);
        paymentInfo.setAlipayTradeNo("");
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setSubject(orderInfo.getOrderDetailList().get(0).getSkuName());
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setOrderId(orderInfo.getId());
        paymentService.savePayment(paymentInfo);
//        给支付系统发送一个延迟检查的消息队列
        System.out.println("提交支付,发送延迟队列");
//      发送延迟队列,根据订单号来检查,延迟队列最多发送五次
        paymentService.sendpaymentCheckQueue(orderId,5);
        // 重定向到支付宝支付页面
        return form;
    }

    @LoginRequire(isNeededSuccess = true)
    @RequestMapping("goToChoosePayWay")
    public String goToChoosePayWay(HttpServletRequest request, String orderId, String totalAmount,String nickName, ModelMap map){

        String userId = (String) request.getAttribute("userId");
        map.put("orderId",orderId);
        map.put("nickName",nickName);
        map.put("totalAmount",totalAmount);
        return "index";
    }



}
