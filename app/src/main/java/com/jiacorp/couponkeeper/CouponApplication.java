package com.jiacorp.couponkeeper;

import android.app.Application;

/**
 * Created by jitse on 12/12/14.
 */
public class CouponApplication extends Application {

    MyDBHander mDbHandler;


    @Override
    public void onCreate() {
        super.onCreate();

        mDbHandler = new MyDBHander(this, getString(R.string.db_name), null, 1);
    }

    public MyDBHander getDbHandler() {
        if (mDbHandler == null) {
            mDbHandler = new MyDBHander(this, getString(R.string.db_name), null, 1);
        }
        return mDbHandler;
    }
}
