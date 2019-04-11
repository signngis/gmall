package com.atguigu.proxyTest2;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class WorkHandler implements InvocationHandler {

    private IWork work;


    public WorkHandler(IWork work) {
        this.work = work;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        preWork();

        Object ret = method.invoke(work,args);
        return ret;
    }

    private void preWork() {

        System.out.println("老师上课前要准备讲义");
    }
}
