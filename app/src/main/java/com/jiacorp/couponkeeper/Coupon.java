package com.jiacorp.couponkeeper;

import java.io.Serializable;

/**
 * Created by jitse on 11/21/14.
 */
public class Coupon implements Serializable {
    public String title;
    public String expDateString;
    public String filePath;
    public boolean used = false;
    public String id;
    public int rotation;

    public void copyFrom(Coupon c) {
        this.title = c.title;
        this.expDateString = c.expDateString;
        this.filePath = c.filePath;
        this.used = c.used;
        this.id = c.id;
        this.rotation = c.rotation;
    }
}
