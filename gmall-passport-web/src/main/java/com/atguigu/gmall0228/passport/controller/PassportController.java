package com.atguigu.gmall0228.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0228.bean.CartInfo;
import com.atguigu.gmall0228.bean.UserInfo;
import com.atguigu.gmall0228.service.CartService;
import com.atguigu.gmall0228.service.UserService;
import com.atguigu.gmall0228.util.CookieUtil;
import com.atguigu.gmall0228.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PassportController {

    @Reference
    UserService userService;

    @Reference
    CartService cartService;


//    到login.html页面
    @RequestMapping("goToLogin")
    public String goToLogin(HttpServletRequest request,String originUrl,ModelMap map){

        map.put("originUrl",originUrl);
        return "login";
    }

/*  在验证中心验证是否合法,如果合法则返回success,否则返回false.
    验证中心,先根据key获取封装的信息,经过解码操作获取map对象.判断map对象
    是否为null,如果为null则说明用户没有登录,则验证不通过,返回fail,如果不为null,
    则验证token的过期时间.verify(userId)方法,根据userId,从缓存中取出对象,判断
    缓存中对象是否存在,如果不存在,返回false,如果存在就设置过期时间.
*/
    @RequestMapping("verify")
    @ResponseBody
    public String verify(HttpServletRequest request,String token,String currentIp){
//        验证证书,解码.
        Map userMap = JwtUtil.decode("atguigu0228", token, currentIp);

        if(userMap!=null){
//            验证过期时间
            String userId = userMap.get("userId").toString();
            boolean b = userService.verify(userId);
            if(!b){
                return "fail";
            }
        }else {
            return "fail";
        }
        return "success";
    }

//     获取ip值
    private String getIp(HttpServletRequest request) {
        String ip = "";
//        负载均衡
        ip = request.getHeader("x-forworded-for");

        if(StringUtils.isBlank(ip)){
            ip = request.getRemoteAddr();// 获取原始请求Ip
        }

        if(StringUtils.isBlank(ip)){
            ip = "127.0.0.1";
        }
        return ip;
    }

    // 登录请求.在登录的成功的时候封装用户信息,封装后编码获得token,返回token,方便验证中心检验.
    @RequestMapping("login")
    @ResponseBody
    public String login(HttpServletRequest request, HttpServletResponse response, UserInfo userInfo){
//        调用user服务验证用户和密码
        String token ="";
        UserInfo login = userService.login(userInfo);
        if(login==null){
//            用户名或密码错误
            return "username or password error";
        }else {
//            登录成功
//            9
//            返回token给页面
            String ip = getIp(request);
            HashMap<Object, Object> objectObjectHashMap = new HashMap<>();
//           封装用户信息
            objectObjectHashMap.put("userId",login.getId());
            objectObjectHashMap.put("nickName",login.getNickName());
//            编码
            token = JwtUtil.encode("atguigu0228", objectObjectHashMap, ip);
//             调用购物车合并的业务
            String cartListCookie = CookieUtil.getCookieValue(request,"cartListCookie",true);
            List<CartInfo> cartList = null;
            if(StringUtils.isNotBlank(cartListCookie)){
                cartList = JSON.parseArray(cartListCookie,CartInfo.class);
            }
//          合并购物车信息
            cartService.mergeCart(login.getId(),cartList);
//            删除购物车cookie数据
            CookieUtil.deleteCookie(request,response,"cartListCookie");
            return token;
        }
    }


}
