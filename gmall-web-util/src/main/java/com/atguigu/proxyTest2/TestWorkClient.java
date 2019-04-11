package com.atguigu.proxyTest2;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

public class TestWorkClient {

    @Test
    public void testWorkProxy() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException{

        IWork teachwork = new TeacherWork();
        WorkProxy workproxy = new WorkProxy(teachwork);
        IWork work = workproxy.createWorkProxy();
        work.work();
    }
}
