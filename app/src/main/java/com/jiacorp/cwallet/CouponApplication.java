package com.jiacorp.cwallet;

import android.app.Application;
import android.graphics.Bitmap;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import io.fabric.sdk.android.Fabric;

/**
 * Created by jitse on 12/12/14.
 */
public class CouponApplication extends Application {

    private static final String TAG = CouponApplication.class.getSimpleName();

    MyDBHandler mDbHandler;
    ImageLoader mImageLoader;

    private Tracker mTracker = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        mDbHandler = new MyDBHandler(this, getString(R.string.db_name), null, 1);

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new FadeInBitmapDisplayer(740))
                .build();

        // Create global configuration and initialize ImageLoader with this config
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .build();
        mImageLoader = ImageLoader.getInstance();
        mImageLoader.init(config);


    }

    public ImageLoader getImageLoader() {
        if (mImageLoader == null) {
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                    .build();
            mImageLoader = ImageLoader.getInstance();
            mImageLoader.init(config);
        }

        return mImageLoader;
    }

    public MyDBHandler getDbHandler() {
        if (mDbHandler == null) {
            mDbHandler = new MyDBHandler(this, getString(R.string.db_name), null, 1);
        }
        return mDbHandler;
    }

    public synchronized Tracker getTracker() {
        if ( null == mTracker ) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            mTracker = analytics.newTracker(R.xml.analytics);
        }

        return mTracker;
    }

}
