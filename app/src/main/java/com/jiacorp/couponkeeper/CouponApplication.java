package com.jiacorp.couponkeeper;

import android.app.Application;
import android.graphics.Bitmap;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

/**
 * Created by jitse on 12/12/14.
 */
public class CouponApplication extends Application {

    MyDBHander mDbHandler;
    ImageLoader mImageLoader;

    @Override
    public void onCreate() {
        super.onCreate();

        mDbHandler = new MyDBHander(this, getString(R.string.db_name), null, 1);

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

    public MyDBHander getDbHandler() {
        if (mDbHandler == null) {
            mDbHandler = new MyDBHander(this, getString(R.string.db_name), null, 1);
        }
        return mDbHandler;
    }
}
