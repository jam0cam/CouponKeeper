package com.jiacorp.cwallet;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by jitse on 1/7/15.
 */
public class EnhanceActivity extends Activity {

    private static final String TAG = EnhanceActivity.class.getName();
    @InjectView(R.id.img_main)
    ImageView mImageView;

    PhotoViewAttacher mAttacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_coupon);
        ButterKnife.inject(this);

        Uri uri = getIntent().getParcelableExtra(CouponActivity.EXTRA_URI);

        Log.d(TAG, "loading into enhance activity for file: " + uri.getPath());

        Picasso.with(this)
                .load(uri)
                .fit()
                .centerInside();

        mAttacher = new PhotoViewAttacher(mImageView);
        mAttacher.update();
    }
}
