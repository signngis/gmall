package com.atguigu.proxyTest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Blob;

public class DynamicProxy  implements InvocationHandler {
//    我们代理的真实对象
    private Object subject;

//    给我们要代理的对象赋值
    public  DynamicProxy(Object subject){
        this.subject = subject ;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//        添加自己的操作
        System.out.println("我开始代理了");

        System.out.println("我执行的方法是" + method);

//        开始代理
        method.invoke(subject,args);

//        代理后，我们可以添加自己的操作
        System.out.println("我代理完了");
        return null;
    }
}
