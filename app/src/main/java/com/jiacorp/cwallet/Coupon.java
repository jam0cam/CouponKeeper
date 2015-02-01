package com.jiacorp.cwallet;

import java.io.File;
import java.io.Serializable;

/**
 * Created by jitse on 11/21/14.
 */
public class Coupon implements Serializable {

    public static final String TOKEN_DELIMITER = "#";
    public static final String SPACE_DELIMITER = "@";
    public static final String DATE_DELIMITER = "%";

    public String title;
    public String expDateString;
    public String filePath;
    public boolean used = false;
    public String id;

    public void copyFrom(Coupon c) {
        this.title = c.title;
        this.expDateString = c.expDateString;
        this.filePath = c.filePath;
        this.used = c.used;
        this.id = c.id;
    }

    public static Coupon fromFile(File file) {
        String path = file.getPath();
        if (!path.contains(".jpg")) {
            return null;
        }

        Coupon c = new Coupon();
        c.filePath = path;

        String name = file.getName();
        name = name.substring(0, name.indexOf(".jpg"));
        String[] result = name.split(TOKEN_DELIMITER);

        if (result.length != 4) {
            return null;
        }

        c.title = result[0];
        String[] date = result[2].split(DATE_DELIMITER);
        c.expDateString = date[0] + "/" + date[1] + "/" + date[2];

        String used = result[3];

        if (used.equals("used")) {
            c.used = true;
        } else {
            c.used = false;
        }

        return c;
    }
}
