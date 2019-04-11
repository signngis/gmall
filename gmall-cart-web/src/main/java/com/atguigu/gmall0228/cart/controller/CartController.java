package com.atguigu.gmall0228.cart.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0228.annotation.LoginRequire;
import com.atguigu.gmall0228.bean.CartInfo;
import com.atguigu.gmall0228.bean.SkuInfo;
import com.atguigu.gmall0228.service.CartService;
import com.atguigu.gmall0228.service.SkuService;
import com.atguigu.gmall0228.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Controller
public class CartController {
    @Reference
    SkuService skuService;
    @Reference
    CartService cartService;
//  此方法用来测试自定义注解
/*    @LoginRequire(isNeededSuccess = true)
    @RequestMapping("toTrade")
    public String toTrade(){

        return "tradeTest";
    }*/


    @LoginRequire(isNeededSuccess = false)
    @RequestMapping("checkCart")
    public String checkCart(HttpServletRequest request,HttpServletResponse response ,CartInfo cartInfo, ModelMap map){
        String userId = (String) request.getAttribute("userId");
        List<CartInfo> cartList = new ArrayList<>();
//        判断,用户没登录,则从cookie中取得商品,修改使被选中
        if(StringUtils.isBlank(userId)){
//            获得cookie
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if(StringUtils.isNotBlank(cartListCookie)){
//                把cookie中的数据转化为CartInfo对象的集合
                cartList = JSON.parseArray(cartListCookie, CartInfo.class);
                for (CartInfo info : cartList) {
                    if(info.getSkuId().equals(cartInfo.getSkuId())){
                        info.setIsChecked(cartInfo.getIsChecked());
                    }
                }
//                覆盖cookie
                CookieUtil.setCookie(request, response, "cartListCookie", JSON.toJSONString(cartList), 60 * 60 * 24 * 7, true);
            }else{// 修改cartListCookie不可能为空

            }
        }else{
            //          用户已经登录,直接改变数据库的选中状态
            cartInfo.setUserId(userId);
            cartService.updateCartChecked(cartInfo);
//                同步Redis
            cartService.cartCache(userId);
            cartList = cartService.getCartListCacheByUser(userId);
        }
//        计算总价
        BigDecimal totalPrice = getTotalPrice(cartList);
        map.put("totalPrice",totalPrice);
        map.put("cartList",cartList);
        return "cartListInner";
    }
    @LoginRequire(isNeededSuccess = false)
    @RequestMapping("/cartList")
    public String cartList(HttpServletRequest request,ModelMap map){
//        定义userId
        String userId = (String) request.getAttribute("userId");
        List<CartInfo> cartList = new ArrayList<>();
//        判断用户是否登录
        if(StringUtils.isBlank(userId)){
//            用户为登录,从cookie取出添加的商品
//            查询cookie
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
//          如果有Cookie,从cookie中取出商品
            if(StringUtils.isNotBlank(cartListCookie)){
                cartList = JSON.parseArray(cartListCookie,CartInfo.class);
            }
        }else{
//            如果没有则查询数据库(redis)
            cartList = cartService.getCartListCacheByUser(userId);
        }

        BigDecimal totalPrice =  getTotalPrice(cartList);
        map.put("totalPrice",totalPrice);
        map.put("cartList",cartList);
        return "cartList";
    }

//    如果选中,则计算出商品的价格
    private BigDecimal getTotalPrice(List<CartInfo> cartList) {
        BigDecimal totalPrice = new BigDecimal("0");
//      遍历查看商品的状态,若选中,则算出总价
        for (CartInfo cartInfo : cartList) {
            String isChecked = cartInfo.getIsChecked();
            if(isChecked.equals("1")){
                BigDecimal cartPrice = cartInfo.getCartPrice();
                totalPrice = totalPrice.add(cartPrice);
            }
        }
        return  totalPrice;
    }

//    商品添加购物车的操作:1. 用户已登录 2. 未登录
    @LoginRequire(isNeededSuccess = false)
    @RequestMapping("addToCart")
    public String addToCart(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String,String> map){
//        声明购物车对象
        List<CartInfo> cartInfos = new ArrayList<>();
        String userId = (String) request.getAttribute("userId");
        String skuId = map.get("skuId");
        String num = map.get("num");
//        根据skuId查询出对应的商品信息,封装购物车对象
       CartInfo cartInfo = getCartInfoBySkuId(skuId,num);
//       两个分支
        if(StringUtils.isBlank(userId)){
//            用户未登录操作浏览器的cookie
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if(StringUtils.isBlank(cartListCookie)){
//                cookie中没有数据,直接添加到购物车
                cartInfos.add(cartInfo);
            }else{
                cartInfos = JSON.parseArray(cartListCookie, CartInfo.class);
//                判断是添加还是修改
                boolean b = ifNewCart(cartInfos,cartInfo);
                
                if(b){
//                    新车
                    cartInfos.add(cartInfo);
                }else {
//                    老车,找到对应的商品,修改数量与该商品的总价
                    for (CartInfo info : cartInfos) {
                        if(info.getSkuId().equals(cartInfo.getSkuId())){
                            info.setSkuNum(info.getSkuNum()+Integer.parseInt(num));
                            BigDecimal multiply = info.getSkuPrice().multiply(new BigDecimal(info.getSkuNum()));
                            info.setCartPrice(multiply);
                        }
                    }

                }
            }
//            覆盖浏览器cookie
            CookieUtil.setCookie(request,response,"cartListCookie",JSON.toJSONString(cartInfos), 60 * 60 * 24 * 7, true);
        }else {
//      用户已登录,操作db或redis
//      根据userId和skuId查询当前购物车商品是否曾经添加过
            CartInfo cartDb = cartService.ifCartExist(userId,skuId);
            if(cartDb!=null){
//                更新购物车
                cartInfo.setId(cartDb.getId());
                cartInfo.setSkuNum(Integer.parseInt(num)+cartDb.getSkuNum());
                cartInfo.setCartPrice(cartInfo.getSkuPrice().multiply(new BigDecimal(cartInfo.getSkuNum())));
                cartService.addCart(cartInfo);
            }else{
//                添加新的购物车
                cartInfo.setUserId(userId);
                cartService.addCart(cartInfo);
            }
//            同步缓存
            cartService.cartCache(userId);
        }
        return "redirect:/cartSuccess";
    }

//    判断是否为新车
    private boolean ifNewCart(List<CartInfo> cartInfos, CartInfo cartInfo) {
        boolean b = true;
        for (CartInfo info : cartInfos) {
//            如果新加的商品在购物车中已存在,则为旧车,B为false.
            if(info.getSkuId().equals(cartInfo.getSkuId())){
                b= false;
                break;
            }
        }
        return  b;
    }

//    封装购物车参数
    private CartInfo getCartInfoBySkuId(String skuId,String num) {
        CartInfo cartInfo = new CartInfo();
        SkuInfo skuInfo = skuService.getSkuInfo(skuId);
        // 设置购物车的商品价格
        cartInfo.setSkuPrice(skuInfo.getPrice());
        // 设置购物车的添加数量
        cartInfo.setSkuNum(Integer.parseInt(num));
        // 设置购物车的价格
        BigDecimal multiply = cartInfo.getSkuPrice().multiply(new BigDecimal(num));
        cartInfo.setCartPrice(multiply);
        // 设置购物车其他信息
        cartInfo.setSkuName(skuInfo.getSkuName());
        cartInfo.setIsChecked("1");
        cartInfo.setSkuId(skuId);
        cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());

        return cartInfo;

    }

    @RequestMapping("cartSuccess")
    public String cartSuccess() {

        return "success";
    }
}
