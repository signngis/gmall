package com.atguigu.gmall0228.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 自定义注解,作用在方法上,属于运行时触发
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequire {
// 此注解的作用是:标有该注解的方法,需要被拦截器拦截验证,默认值为true.
    boolean isNeededSuccess() default true;
}
