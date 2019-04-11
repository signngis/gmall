package com.atguigu.proxyTest2;

public class TeacherWork implements IWork{


    @Override
    public void work() {
        System.out.println("teacher teach student");
    }

    @Override
    public void rest(Integer str) {
        System.out.println("teacher rest :" + str +"min");
    }
}
