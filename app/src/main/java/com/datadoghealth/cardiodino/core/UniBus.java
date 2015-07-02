package com.datadoghealth.cardiodino.core;

/**
 * Created by elizabethrussell on 5/8/15.
 */
public class UniBus {
    private static UIEventBus bus;

    public static UIEventBus get() {
        if (bus == null) bus = new UIEventBus();
        return bus;
    }
}
