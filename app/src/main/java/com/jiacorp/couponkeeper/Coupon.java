package com.jiacorp.couponkeeper;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by jitse on 11/21/14.
 */
public class Coupon implements Serializable {
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

    public Coupon(String title, String expDate, String path, boolean isUsed) {
        this.title = title;
        this.expDateString = expDate;
        this.filePath = path;
        this.used = isUsed;
    }

    public Coupon() {
    }
}
