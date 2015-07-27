package de.tud.inf.rn.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by nguonly role 7/27/15.
 */
public class MyDynamicProxyClass implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //return null;
        return method.invoke(obj, args);
    }

    Object obj;
    public MyDynamicProxyClass(Object obj)
    { this.obj = obj; }

    static public Object newInstance(Object obj, Class[] interfaces)
    {
        return
                java.lang.reflect.Proxy.newProxyInstance(obj.getClass().getClassLoader(),
                        interfaces,
                        new
                                MyDynamicProxyClass(obj));
    }
}
