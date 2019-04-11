package com.atguigu.proxyTest;
//  真实对象
public class RealSubject implements Subject{
    @Override
    public void rent() {
        System.out.println("i want to rent my home");
    }

    @Override
    public void hello(String str) {
        System.out.println("hello" + str);
    }
}
