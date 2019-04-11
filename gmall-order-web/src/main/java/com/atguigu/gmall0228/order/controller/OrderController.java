package com.atguigu.gmall0228.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0228.annotation.LoginRequire;
import com.atguigu.gmall0228.bean.CartInfo;
import com.atguigu.gmall0228.bean.OrderDetail;
import com.atguigu.gmall0228.bean.OrderInfo;
import com.atguigu.gmall0228.bean.UserAddress;
import com.atguigu.gmall0228.bean.enums.PaymentWay;
import com.atguigu.gmall0228.service.CartService;
import com.atguigu.gmall0228.service.OrderService;
import com.atguigu.gmall0228.service.SkuService;
import com.atguigu.gmall0228.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class OrderController {
    @Reference
    CartService cartService;
    @Reference
    UserService userService;
    @Reference
    OrderService orderService;
    @Reference
    SkuService skuService;

    @LoginRequire(isNeededSuccess = true)
    @RequestMapping("toTrade")
    public String toTrade(HttpServletRequest request, ModelMap map){
        String userId = (String)request.getAttribute("userId");
//       调用购物车服务,根据userId,获取购物车列表
        List<CartInfo> cartList = cartService.getCartListCheckedCacheByUser(userId);

        List<OrderDetail> orderDetails = new ArrayList<>();
//      把购物车信息封装到结算列表对象中,在结算页面需要显示的信息
        for (CartInfo cartInfo : cartList) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getCartPrice());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetails.add(orderDetail);
        }
//        结算页面显示的地址信息
        List<UserAddress> addressListByUser = userService.getAddressListByUserId(userId);//userId未正确获取
//        把封装的信息放入到域中,方便页面的显示
        map.put("orderDetailList",orderDetails);
        map.put("userAddressList",addressListByUser);
        map.put("totalAmount",getTotalPrice(cartList));

//        生成交易码
        String tradeCode =  orderService.genTradeCode(userId);
//        页面放入交易码
        map.put("tradeCode",tradeCode);
        return "trade";
    }
/*
* 提交订单实现:
* 1. 提交订单前,需要验证交易码,确认该订单是否已经提交过
* 2. 若已经提交则,提示错误信息,组织重复提交
* 3. 如果没有提交,则检验从价格和库存,如果价格或库存有变化,则提示客户,阻止提交
* 4. 以上过程全通过,则封装购物车数据信息,封装好后,把存到数据库,点击提交到支付页面,同时清空选中的
* 购物车商品.
* 5. 重定向到支付页面.
*
* */
//    提交订单
    @LoginRequire(isNeededSuccess = true)
    @RequestMapping("submitOrder")
    public String submitOrder(HttpServletRequest request, @RequestParam Map<String,String> paraMap,ModelMap map){
        String userId = (String)request.getAttribute("userId");
        String nickName = (String) request.getAttribute("nickName");
        String tradeCode = paraMap.get("tradeCode");
        String addressId = paraMap.get("addressId");
//        验证交易码
        boolean b = orderService.checkTradeCode(userId,tradeCode);
        if(b){
            List<String> cartIds = new ArrayList<>();
            List<CartInfo> cartList = cartService.getCartListCheckedCacheByUser(userId);
//            封装订单对象
            OrderInfo orderInfo = new OrderInfo();
            List<OrderDetail> orderDetails = new ArrayList<>();

            for (CartInfo cartInfo : cartList) {
                OrderDetail orderDetail = new OrderDetail();
//                验证价格和库存
                boolean ifPrice = skuService.checkSkuPrice(cartInfo);
                if(ifPrice){
//                    封装订单数据
                    orderDetail.setImgUrl(cartInfo.getImgUrl());
                    orderDetail.setSkuNum(cartInfo.getSkuNum());
                    orderDetail.setSkuName(cartInfo.getSkuName());
                    orderDetail.setOrderPrice(cartInfo.getCartPrice());
                    orderDetail.setSkuId(cartInfo.getSkuId());
                    orderDetails.add(orderDetail);
                    cartIds.add(cartInfo.getId());
                }else{
                    map.put("errMsg","订单交易失败,价格或库存变动");
                    return "tradeFail";
                }
            }
//            用户地址的获取
            UserAddress userAddress = userService.getUserAddressByAddressId(addressId);
            orderInfo.setOrderDetailList(orderDetails);
//            日期格式化,封装外部订单号
            SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMddHHmmss");
            String format = sdf.format(new Date());
//            生成设置全局订单号
            String outTradeNo = "ATGUIGU" + format +System.currentTimeMillis();
            orderInfo.setOutTradeNo(outTradeNo);
            orderInfo.setOrderStatus("未付款");
            orderInfo.setProcessStatus("未付款");
            orderInfo.setConsigneeTel(userAddress.getPhoneNum());//userAddress空指针
            orderInfo.setCreateTime(new Date());
            orderInfo.setDeliveryAddress(userAddress.getUserAddress());
//            日期加减,封装过期时间或者预计送达时间
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE,1);
            orderInfo.setExpireTime(c.getTime());
            orderInfo.setOrderComment("硅谷订单,没商品可送");
            orderInfo.setTotalAmount(getTotalPrice(cartList));
            orderInfo.setUserId(userId);
            orderInfo.setPaymentWay(PaymentWay.ONLINE);

//            将订单数据保存到数据库
            orderService.saveOrder(orderInfo);

//            清理购物车
            cartService.cleanCart(cartIds,userId);
//            重定向到支付系统
            return "redirect:http://payment.gmall.com:8087/goToChoosePayWay?orderId=" + outTradeNo +"&totalAmount=" +orderInfo.getTotalAmount().toString()+"&nickName="+nickName;
        }else {
            map.put("errMsg","订单交易失败");
            return "tradeFail";
        }

    }
    private BigDecimal getTotalPrice(List<CartInfo> cartList) {
        BigDecimal totalPrice = new BigDecimal("0");
        for (CartInfo cartInfo : cartList) {
            String isChecked = cartInfo.getIsChecked();
            if(isChecked.endsWith("1")){
                BigDecimal cartPrice = cartInfo.getCartPrice();
                totalPrice = totalPrice.add(cartPrice);
            }
        }
        return totalPrice;
    }

}
