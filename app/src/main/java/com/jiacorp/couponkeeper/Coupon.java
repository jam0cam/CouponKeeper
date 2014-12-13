package com.jiacorp.couponkeeper;

import android.graphics.Bitmap;

import java.util.Date;

/**
 * Created by jitse on 11/21/14.
 */
public class Coupon {
    public Bitmap image;
    public String title;
    public String expDateString;
    public Date expDate;
    public String filePath;
    public boolean used = false;
    public String id;

    public Coupon(Bitmap image, String title, String expDateString) {
        this.image = image;
        this.title = title;
        this.expDateString = expDateString;
    }

    public Coupon(String title, String expDate, String path) {
        this.title = title;
        this.expDateString = expDate;
        this.filePath = path;
    }

    public Coupon() {
    }
}
