package de.tud.inf.rn.proxy;

/**
 * Created by nguonly role 7/27/15.
 */

public class TestImpl implements TestIF {
    public String hello(String name) {
        return String.format("Hello %s, this is %s", name, this);
    }
}

