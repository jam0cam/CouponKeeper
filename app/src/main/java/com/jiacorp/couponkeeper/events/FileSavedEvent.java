package com.jiacorp.couponkeeper.events;

/**
 * Created by jitse on 1/11/15.
 */
public class FileSavedEvent {
    public String couponId;

    public FileSavedEvent(String couponId) {
        this.couponId = couponId;
    }
}
