package com.atguigu.proxyTest2;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class WorkProxy  {

    public  IWork work;

    public WorkProxy(IWork work){
        this.work = work;
    }

    public IWork createWorkProxy(){
        InvocationHandler handler = new WorkHandler(work);

       Class<?>[] interfaces =  new Class[]{IWork.class};

       return (IWork) Proxy.newProxyInstance(IWork.class.getClassLoader(),interfaces,handler);
    }



}
