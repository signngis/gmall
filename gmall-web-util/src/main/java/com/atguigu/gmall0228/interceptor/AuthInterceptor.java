package com.atguigu.gmall0228.interceptor;

import com.atguigu.gmall0228.annotation.LoginRequire;
import com.atguigu.gmall0228.util.CookieUtil;
import com.atguigu.gmall0228.util.HttpClientUtil;
import com.atguigu.gmall0228.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/*拦截的逻辑: 1. 我们需要拦截所有关于web页面相关的,所以把拦截器放在web-service中
*             2. 拦截条件是标注了自定义的注解的方法(@LoginRequire)
*             3. 没有标注注解的方法不拦截,直接过,例如index页面,item模块,list等,和用户登录无关的
*             4. 还有一些注解值为false的,需要拦截,但是不通过也放行.例如购物车模块,有一部分是不需要登录
*             的.
*             5. 注解值为true的情况,必须拦截,同时还需要验证中心返回的值是"success"才放行.例如结算模块.
* */
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    // 重写拦截器preHandle方法
    // 参数：request、response、Handle
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("mvc的方法在被请求之前，用户权限拦截器");

        // 判断被请求的方法是否需要拦截
        HandlerMethod mh = (HandlerMethod)handler;
//        此行表示标注有@LoginRequire的注解的方法,需要拦截
        LoginRequire methodAnnotation = mh.getMethodAnnotation(LoginRequire.class);

        if(methodAnnotation==null){
            // 直接过
            System.out.println("无需验证，直接过");
            return true;
        }

      /*
      * 分析:
      * 一.登录的时候有四种状态
      *   1. oldToken为空,newToken也为空 表示从来没有登陆过
      *    从来没有登录过,则不携带token,被拦截后到认证中心,会被重定向到登录
      *    页面,登录获取token,认证中心返回false,不通过.
      *
      *   2. oldToken不为空,newToken为空 以前登陆过,旧登录,且token没过期
      *   被拦截后到认证中心,发现有token,且token通过了认证.则认证中心返回true,通过.
      *   这表示以前的登录还有效.
      *   3. oldToken为空,newToken不为空 新登录
      *
      *
      *   4. oldToken不为空,newToken不为空 新登录
      * */
        String token = "";
//        旧token,之前登录的token
        String oldToken = CookieUtil.getCookieValue(request,"userToken",true);
//        刚刚登录,新token
        String newToken = request.getParameter("newToken");
        boolean neededSuccess = methodAnnotation.isNeededSuccess();
//      从未登录
        if(StringUtils.isBlank(newToken)&&StringUtils.isBlank(oldToken)&&neededSuccess){
//            重定向到认证中心的登录窗口
            response.sendRedirect("http://passport.gmall.com:8085/goToLogin?originUrl=" + request.getRequestURL());
            return false;
        }
//        新登录,放cookie验证
        if(StringUtils.isBlank(oldToken)&&StringUtils.isNotBlank(newToken)||StringUtils.isNotBlank(oldToken)&&StringUtils.isNotBlank(newToken)){
            token = newToken;
            CookieUtil.setCookie(request,response,"userToken",newToken,60*30,true);
        }

//        旧登陆
        if(StringUtils.isNotBlank(oldToken)&&StringUtils.isBlank(newToken)){
            token = oldToken;
        }

        String success = "";

//        如果token不为空,则去验证中心验证,验证中心会返回fail或者success
        if(StringUtils.isNotBlank(token)){
            success = HttpClientUtil.doGet("Http://passport.gmall.com:8085/verify?token=" + token +"&currentIp=" +getIp(request));
        }

//        如果验证成功,则保存用户信息
        if("success".equals(success)){
//            将用户信息放入到request
            Map atguigu0228 = JwtUtil.decode("atguigu0228", token, getIp(request));
            request.setAttribute("userId",atguigu0228.get("userId").toString());
            request.setAttribute("nickName",atguigu0228.get("nickName").toString());
        }
//      必须要拦截,且验证不通过(token过期的情况)
        if(neededSuccess && !"success".equals(success)){
//            Cookie中的token无效了,删除cookie
            CookieUtil.deleteCookie(request,response,"userToken");
//            重定向到验证中心的登录页面
            response.sendRedirect("http://passport.gmall.com:8085/goToLogin?originUrl=" + request.getRequestURL());
            return false;
        }

        return true;
    }
    private String getIp(HttpServletRequest request) {

        String ip = "";
        ip = request.getHeader("x-forwarded-for");// 负载均衡

        if (StringUtils.isBlank(ip)) {
            ip = request.getRemoteAddr();// 原始请求ip
        }

        if (StringUtils.isBlank(ip)) {
            ip = "127.0.0.1";
        }

        return ip;
    }

}
